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
package org.apache.hadoop.hbase.regionserver.assistant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * This assistant will swap the row and value, it could be consider as a simple
 * basic index assistant
 */
public class RowValueSwapAssistant extends Assistant {
  private static final Pattern ARGUMENT_PATTERN = Pattern
      .compile("^FAMILY=(.*),QUALIFIER=(.*)$");
  // What family and qualifier we want to swap row, value
  private byte[] family;
  private byte[] qualifier;

  /** default constructor */
  public RowValueSwapAssistant() {
  }

  @Override
  protected Collection<Mutation> assist(final HRegion region,
      final Mutation mutation, final byte[] assistanStoreName)
      throws IOException {
    List<Mutation> generatedMutations = new ArrayList<Mutation>();
    Map<byte[], List<KeyValue>> familyMap = mutation.getFamilyMap();
    if (mutation instanceof Put) {
      for (Map.Entry<byte[], List<KeyValue>> entry : familyMap.entrySet()) {
        for (KeyValue kv : entry.getValue()) {
          if (ignoreKV(kv)) {
        	  System.out.println("rowkey:" + Bytes.toString(kv.getRow())
						+ ",family:" + Bytes.toString(kv.getFamily())
						+ ",quaitor:" + Bytes.toString(kv.getQualifier())+",value:"+Bytes.toString(kv.getValue())+" is ignore row");
        	  continue;
          }
          System.out.println("rowkey:" + Bytes.toString(kv.getRow())
					+ ",family:" + Bytes.toString(kv.getFamily())
					+ ",quaitor:" + Bytes.toString(kv.getQualifier())+",value:"+Bytes.toString(kv.getValue())+" is insert to assistant store"+Bytes.toString(assistanStoreName));	 
          Put p = new Put(Bytes.add(kv.getValue(), kv.getRow()));
          p.add(assistanStoreName, kv.getQualifier(), kv.getRow());
          generatedMutations.add(p);
        }
      }
    } else if (mutation instanceof Delete) {
      if(familyMap.isEmpty()){
        for (byte[] tableFamily : region.getTableDesc().getFamiliesKeys()) {
          ((Delete) mutation).deleteFamily(tableFamily, mutation.getTimeStamp());
        }
      }
      for (Map.Entry<byte[], List<KeyValue>> entry : familyMap.entrySet()) {
        Iterator<KeyValue> kvIterator = entry.getValue().iterator();
        while (kvIterator.hasNext()) {
          KeyValue kv = kvIterator.next();
          if(!kv.isDelete()) continue;
          // Create the Get for the corresponding delete
          Get get = new Get(kv.getRow());
          boolean isLatestTimestamp = kv.isLatestTimestamp();
          if (kv.getType() == KeyValue.Type.Delete.getCode()) {
            if (ignoreKV(kv)) continue;
            get.addColumn(kv.getFamily(), kv.getQualifier());
            if (!isLatestTimestamp) {
              get.setTimeStamp(kv.getTimestamp());
            }
          } else if (kv.getType() == KeyValue.Type.DeleteColumn.getCode()) {
            if (ignoreKV(kv)) continue;
            get.addColumn(kv.getFamily(), kv.getQualifier()).setMaxVersions();
            if (!isLatestTimestamp) {
              get.setTimeRange(0, kv.getTimestamp() + 1);
            }
          } else if (kv.getType() == KeyValue.Type.DeleteFamily.getCode()) {
            if (ignoreFamily(kv.getBuffer(), kv.getFamilyOffset(),
                kv.getFamilyLength())) {
              continue;
            }
            get.addFamily(kv.getFamily()).setMaxVersions();
            if (!isLatestTimestamp) {
              get.setTimeRange(0, kv.getTimestamp() + 1);
            }
          }

          Result result = region.get(get, null);
          if (result.isEmpty()) {
            kvIterator.remove();
            continue;
          }

          // Create the delete on assistant store according to the result

          boolean updateTs = true;
          for (KeyValue existedKV : result.raw()) {
            long tsOfExisted = existedKV.getTimestamp();
            // specify the time stamp for delete to prevent new putting
            byte[] tsBytes = Bytes.toBytes(tsOfExisted);
            if (updateTs) {
              kv.updateLatestStamp(tsBytes);
              updateTs = false;
            }
            if (ignoreQualifier(existedKV.getBuffer(),
                existedKV.getQualifierOffset(), existedKV.getQualifierLength())) {
              continue;
            }

            byte[] assistantRow = Bytes.add(existedKV.getValue(),
                existedKV.getRow());
            Delete assistantDelete = new Delete(assistantRow);
            assistantDelete.addDeleteMarker(new KeyValue(assistantRow,
                assistanStoreName, existedKV.getQualifier(), tsOfExisted,
                KeyValue.Type.Delete, existedKV.getRow()));
            generatedMutations.add(assistantDelete);
          }
        }
      }
    }
    return generatedMutations;
  }

  @Override
  public String getName() {
    return this.getClass().getName();
  }

  /**
   * Check whether the given KeyValue's family and qualifier is wanted
   * @param kv
   * @return true if ignore this kv.
   */
  private boolean ignoreKV(KeyValue kv) {
    if (ignoreFamily(kv.getBuffer(), kv.getFamilyOffset(),
        kv.getFamilyLength())) {
      return true;
    }
    if (ignoreQualifier(kv.getBuffer(), kv.getQualifierOffset(),
        kv.getQualifierLength())) {
      return true;
    }
    return false;
  }

  /**
   * Check whether the given family is wanted
   * @param checkFamily
   * @return true if ignore the given family.
   */
  private boolean ignoreFamily(byte[] checkFamily, int offset, int len) {
    if (this.family != null
        && !Bytes.equals(this.family, 0, this.family.length, checkFamily,
            offset, len)) {
      return true;
    }
    return false;
  }

  /**
   * Check whether the given family is wanted
   * @param checkFamily
   * @return true if ignore the given family.
   */
  private boolean ignoreQualifier(byte[] checkQualifier, int offset, int len) {
    if (this.qualifier != null && !Bytes.equals(this.qualifier, 0, this.qualifier.length,
            checkQualifier, offset, len)) {
      return true;
    }
    return false;
  }

  @Override
  public Assistant initialize(String argumentStr) {
    Matcher matcher = ARGUMENT_PATTERN.matcher(argumentStr);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Argument:" + argumentStr
          + " is not suited for " + getName());
    }
    String familyStr = matcher.group(1);
    String qualStr = matcher.group(2);
    this.family = familyStr.isEmpty() ? null : Bytes.toBytesBinary(familyStr);
    this.qualifier = qualStr.isEmpty() ? null : Bytes.toBytesBinary(qualStr);
    return this;
  }

  /**
   * Get the configuration string of RowValueSwapAssistant when setting
   * assistant in HColumnDescriptor
   * @param family
   * @param qualifier
   * @return the configuration string
   */
  public static String getAssistantConfString(String family, String qualifier) {
    return RowValueSwapAssistant.class.getName() + "|" + "FAMILY="
        + ((family == null || family.isEmpty()) ? "" : family) + ",QUALIFIER="
        + ((qualifier == null || qualifier.isEmpty()) ? "" : qualifier);
  }

}
