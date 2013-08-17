/*
 * Copyright 2013 Goodow.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.goodow.realtime.operation.map.string;

import com.goodow.realtime.operation.map.AbstractMapOperation;

import elemental.json.JsonArray;

public class StringMapOperation extends AbstractMapOperation<String> {
  public static StringMapOperation parse(JsonArray serialized) {
    int length = serialized.length();
    assert serialized.getNumber(0) == TYPE && (length == 3 || length == 4);
    return new StringMapOperation(parseId(serialized), serialized.getString(2), null, length == 3
        ? null : serialized.getString(3));
  }

  public StringMapOperation(String id, String key, String oldValue, String newValue) {
    super(id, key, oldValue, newValue);
  }

  @Override
  public StringMapOperation invert() {
    return new StringMapOperation(id, key, newValue, oldValue);
  }

  @Override
  protected StringMapOperation create(String id, String key, String oldValue, String newValue) {
    return new StringMapOperation(id, key, oldValue, newValue);
  }

  @Override
  protected void toString(StringBuilder sb) {
    super.toString(sb);
    if (newValue != null) {
      sb.append(",'").append(newValue).append('\'');
    }
  }
}