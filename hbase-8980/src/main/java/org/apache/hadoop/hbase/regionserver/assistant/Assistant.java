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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * Abstract class for objects that generate assistant data when region has
 * assistant store(s).
 * 
 * Assistant instances are created one per assistant store.
 * 
 * When implementing your own assistants, please implement the methods
 * {@link #assist(HRegion, Mutation)} and {@link #getName()}.
 * 
 * NOTE: For each generated assistant mutation, its KeyValues' value must be the
 * same as the original corresponding mutation's row. Thus, when region split,
 * we could make sure the daughter region has its correct assistant data.
 */
public abstract class Assistant {
	
  public static final Log LOG = LogFactory.getLog(Assistant.class);
  /**
   * Help the given region to get some assistant data as the given mutations.
   * 
   * We will check the generated mutations to make sure the values of generated
   * KeyValues are same as the original mutation's row
   * @param store
   * @param mutations
   * @param assistanStoreName name of assistant store
   * @return a collection of mutations
   * @throws IOException
   */
  public final Collection<Mutation> assist(final HRegion region,
      final Collection<Mutation> mutations, final byte[] assistanStoreName)
      throws IOException {
    List<Mutation> generatedMutations = new ArrayList<Mutation>();
    for (Mutation mutation : mutations) {
      Collection<Mutation> muts = assist(region, mutation, assistanStoreName);
      checkValue(mutation.getRow(), muts);
      
      generatedMutations.addAll(muts);
    }
    return generatedMutations;
  }

  /**
   * Check whether each KeyValue's value in mutations is the same as the given
   * expected value
   * @param expectedValue
   * @param toCheckMutations
   */
  private void checkValue(byte[] expectedValue, Collection<Mutation> toCheckMutations) {
    for (Mutation toCheckMutation : toCheckMutations) {
      for (List<KeyValue> kvList : toCheckMutation.getFamilyMap().values()) {
        for (KeyValue kv : kvList) {
          if (!Bytes.equals(expectedValue, 0, expectedValue.length,
              kv.getBuffer(), kv.getValueOffset(), kv.getValueLength())) {
            throw new IllegalArgumentException("Assistant class " + getName()
                + " is not legal");
          }
        }
      }
    }
  }

  /**
   * Help the given region to get some assistant data as the given mutation
   * 
   * This method would be implemented in kinds of Assistants as use cases
   * 
   * NOTE: The generated mutations' KeyValue must has the same value as given
   * mutation's row
   * @param region
   * @param mutation
   * @param assistanStoreName name of assistant store
   * @return a collection of mutations
   * @throws IOException
   */
  protected abstract Collection<Mutation> assist(final HRegion region,
      final Mutation mutation, final byte[] assistanStoreName)
      throws IOException;

  /**
   * @return the name of this Assistant
   */
  public abstract String getName();

  /**
   * Initialize the instance as the given string
   * @return the own Assistant objec
   */
  public abstract Assistant initialize(String argumentStr);

}
