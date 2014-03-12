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
package org.apache.hadoop.hbase.regionserver;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.regionserver.assistant.Assistant;

/**
 * An assistant store is used to store data with another organization which is
 * defined as configuration. It means each region could have several
 * organizations of data using assistant stores. We could use it to speed up
 * some read(scan) with filter.
 * 
 * If using the assistant store in the region, each original KeyValue would has
 * zero or some corresponding KeyValue(s) in assistant store, the corresponding
 * KeyValue's row is redefined, but its value is the same as original KeyValue's
 * row.
 * 
 * For example, if a region includes the following KeyValues:
 * r1/c1:q1/v1
 * r2/c1:q1/v2
 * r3/c1:q1/v1
 * r4/c1:q1/v3
 * r5/c1:q1/v1
 * 
 * we could use an assistant store for it, include the following kvs:
 * v1/c1:q1/r1
 * v1/c1:q1/r3
 * v1/c1:q1/r5
 * v2/c1:q1/r2
 * v3/c1:q1/r4
 * 
 * 
 * when scanning with the ColumnValueFilter, the assistant store could speed
 * up this read.  
 */
public class AssistantStore extends Store {
  static final Log LOG = LogFactory.getLog(AssistantStore.class);

  private final Assistant assistant;

  /**
   * Constructor
   * @param basedir basedir qualified path under which the region
   *          directory;generally the table subdirectory lives;
   * @param region
   * @param family HColumnDescriptor for this column
   * @param fs file system object
   * @param conf configuration object. Can be null.
   * @param assistantConf
   * @throws IOException
   */
  protected AssistantStore(Path basedir, HRegion region,
      HColumnDescriptor family, FileSystem fs, Configuration conf,
      String assistantConf) throws IOException {
    super(basedir, region, family, fs, conf);
    Matcher matcher = HConstants.ASSISTANT_CONF_VALUE_PATTERN
        .matcher(assistantConf);
    if (matcher.matches()) {
      this.assistant = getAssistantByName(matcher.group(1), matcher.group(2));
    } else {
      throw new RuntimeException("Assistant configuration  " + assistantConf
          + " does not match pattern");
    }

  }

  private Assistant getAssistantByName(String assistantClassName, String argStr) {
    Class<? extends Assistant> assistantClass = null;
    try {
      assistantClass = getClass(assistantClassName, Assistant.class);
      Constructor<? extends Assistant> c = assistantClass.getConstructor();
      return c.newInstance().initialize(argStr);
    } catch (Exception e) {
      String msg = "Failed construction of assistant for" + assistantClassName
          + " with argument = " + argStr;
      LOG.warn(msg, e);
      throw new RuntimeException(msg, e);
    }
  }

  private <U> Class<? extends U> getClass(String name, Class<U> xface) {
    try {
      Class<?> theClass = Class.forName(name);
      if (theClass != null && !xface.isAssignableFrom(theClass))
        throw new RuntimeException(theClass + " not " + xface.getName());
      else if (theClass != null)
        return theClass.asSubclass(xface);
      else
        return null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public byte[] getSplitPoint() {
    // Returns null to indicate this store can't decide the region's split point
    return null;
  }

  @Override
  public boolean isAssistant() {
    return true;
  }

  /**
   * Generate the assistant data with the given mutations
   * @param mutations
   * @return a collection of generated mutations
   * @throws IOException
   */
  @Override
  public Collection<Mutation> generateAssistantData(
      Collection<Mutation> mutations) throws IOException {
    Collection<Mutation> generatedMutations = assistant.assist(
        this.getHRegion(), mutations, this.getFamily().getName());
    // LOG.debug("generatedMutations:" + generatedMutations);
    return generatedMutations;
  }

}
