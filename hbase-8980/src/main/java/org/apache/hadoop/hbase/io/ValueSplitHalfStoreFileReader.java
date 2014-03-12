/**
 * Copyright The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.apache.hadoop.hbase.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.io.hfile.HFileScanner;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * This class is similar to
 * {@link org.apache.hadoop.hbase.io.HalfStoreFileReader}, the difference is
 * that the half store file is decided by the value rather than key.
 * 
 * For example, suppose the store file includes the following KeyValues:
 * r1/c1:q1/v1
 * r2/c1:q1/v3
 * r3/c1:q1/v1
 * r4/c1:q1/v1
 * r5/c1:q1/v3
 * 
 * With the split key 'v2', 
 * the 'bottom' half file includes the following KeyValues:
 * r1/c1:q1/v1
 * r3/c1:q1/v1
 * r4/c1:q1/v1
 * 
 * the 'top' half file includes the following KeyValues:
 * r2/c1:q1/v3
 * r5/c1:q1/v3
 * 
 */
public class ValueSplitHalfStoreFileReader extends HalfStoreFileReader {
  private final Log LOG = LogFactory.getLog(ValueSplitHalfStoreFileReader.class);
  
  protected final byte[] splitvalue;
  private byte[] lastKey = null;
  private boolean lastKeySeeked = false;

  /**
   * 
   * @param fs
   * @param p
   * @param cacheConf
   * @param r
   * @param preferredEncodingInCache
   * @throws IOException
   */
  public ValueSplitHalfStoreFileReader(final FileSystem fs, final Path p,
      final CacheConfig cacheConf, final Reference r,
      DataBlockEncoding preferredEncodingInCache) throws IOException {
    super(fs, p, cacheConf, r, preferredEncodingInCache);
    splitvalue = KeyValue.createKeyValueFromKey(splitkey).getRow();
  }

  /**
   * Creates a assistant half file reader for a hfile referred to by an
   * hfilelink.
   * @param fs
   * @param p
   * @param link
   * @param cacheConf
   * @param r
   * @param preferredEncodingInCache
   * @throws IOException
   */
  public ValueSplitHalfStoreFileReader(FileSystem fs, Path p, HFileLink link,
      CacheConfig cacheConf, Reference r,
      DataBlockEncoding preferredEncodingInCache) throws IOException {
    super(fs, p, link, cacheConf, r, preferredEncodingInCache);
    splitvalue = KeyValue.createKeyValueFromKey(splitkey).getRow();
  }

  @Override
  public byte[] getLastKey() {
    if (!lastKeySeeked) {
      KeyValue lastKV = KeyValue.createLastOnRow(getHFileReader()
          .getLastRowKey());
      // Get a scanner that caches the block and that uses pread.
      HFileScanner scanner = getScanner(true, true);
      try {
        if (scanner.seekBefore(lastKV.getBuffer(), lastKV.getKeyOffset(),
            lastKV.getKeyLength())) {
          this.lastKey = Bytes.toBytes(scanner.getKey());
        }
      } catch (IOException e) {
        LOG.warn("Failed seekBefore " + Bytes.toStringBinary(lastKV.getKey()),
            e);
      }
    }
    return this.lastKey;
  }

  @Override
  public HFileScanner getScanner(final boolean cacheBlocks,
      final boolean pread, final boolean isCompaction) {
    final HFileScanner s = getHFileReader().getScanner(cacheBlocks, pread,
        isCompaction);
    return new HFileScanner() {
      final HFileScanner delegate = s;

      @Override
      public ByteBuffer getKey() {
        return delegate.getKey();
      }

      @Override
      public String getKeyString() {
        return delegate.getKeyString();
      }

      @Override
      public ByteBuffer getValue() {
        return delegate.getValue();
      }

      @Override
      public String getValueString() {
        return delegate.getValueString();
      }

      @Override
      public KeyValue getKeyValue() {
        return delegate.getKeyValue();
      }

      @Override
      public boolean next() throws IOException {
        while (delegate.next()) {
          if (isCurrentKVValid()) {
            return true;
          }
        }
        return false;
      }

      @Override
      public boolean seekBefore(byte[] key) throws IOException {
        return seekBefore(key, 0, key.length);
      }

      @Override
      public boolean seekBefore(byte[] key, int offset, int length)
          throws IOException {
        byte[] seekKey = key;
        int seekKeyOffset = offset;
        int seekKeyLength = length;
        while (delegate.seekBefore(seekKey, seekKeyOffset, seekKeyLength)) {
          if (isCurrentKVValid()) {
            return true;
          }
          ByteBuffer curKey = getKey();
          if (curKey == null) return false;
          seekKey = curKey.array();
          seekKeyOffset = curKey.arrayOffset();
          seekKeyLength = curKey.limit();
        }
        return false;
      }

      private boolean isCurrentKVValid() {
        ByteBuffer value = getValue();
        if (!top) {
          // Current value < split key, it belongs to bottom, return true
          if (Bytes.compareTo(value.array(), value.arrayOffset(),
              value.limit(), splitvalue, 0, splitvalue.length) < 0) {
            return true;
          }
        } else {
          if (Bytes.compareTo(value.array(), value.arrayOffset(),
              value.limit(), splitvalue, 0, splitvalue.length) >= 0) {
            return true;
          }
        }
        return false;
      }

      @Override
      public boolean seekTo() throws IOException {
        boolean b = delegate.seekTo();
        if (!b) {
          return b;
        }

        if (isCurrentKVValid()) {
          return true;
        }

        return next();
      }

      @Override
      public int seekTo(byte[] key) throws IOException {
        return seekTo(key, 0, key.length);
      }

      public int seekTo(byte[] key, int offset, int length) throws IOException {
        int b = delegate.seekTo(key, offset, length);
        if (b < 0) {
          return b;
        } else {
          if (isCurrentKVValid()) {
            return b;
          } else {
            boolean existBefore = seekBefore(key, offset, length);
            if (existBefore) {
              return 1;
            }
            return -1;
          }
        }
      }

      @Override
      public int reseekTo(byte[] key) throws IOException {
        return reseekTo(key, 0, key.length);
      }

      @Override
      public int reseekTo(byte[] key, int offset, int length)
          throws IOException {
        int b = delegate.reseekTo(key, offset, length);
        if (b < 0) {
          return b;
        } else {
          if (isCurrentKVValid()) {
            return b;
          } else {
            boolean existBefore = seekBefore(key, offset, length);
            if (existBefore) {
              return 1;
            }
            return -1;
          }
        }
      }

      public org.apache.hadoop.hbase.io.hfile.HFile.Reader getReader() {
        return this.delegate.getReader();
      }

      public boolean isSeeked() {
        return this.delegate.isSeeked();
      }
    };
  }

}
