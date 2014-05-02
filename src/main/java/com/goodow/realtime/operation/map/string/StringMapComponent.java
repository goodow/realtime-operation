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

import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.map.AbstractMapComponent;

public class StringMapComponent extends AbstractMapComponent<String> {
  public static StringMapComponent parse(JsonArray serialized) {
    int length = serialized.length();
    assert serialized.getNumber(0) == TYPE && (length == 3 || length == 4);
    return new StringMapComponent(parseId(serialized), serialized.getString(2), null, length == 3
        ? null : serialized.getString(3));
  }

  public StringMapComponent(String id, String key, String oldValue, String newValue) {
    super(id, key, oldValue, newValue);
  }

  @Override
  public StringMapComponent invert() {
    return new StringMapComponent(id, key, newValue, oldValue);
  }

  @Override
  protected StringMapComponent create(String id, String key, String oldValue, String newValue) {
    return new StringMapComponent(id, key, oldValue, newValue);
  }

  @Override
  protected void toJson(JsonArray json) {
    super.toJson(json);
    if (newValue != null) {
      json.push(newValue);
    }
  }
}