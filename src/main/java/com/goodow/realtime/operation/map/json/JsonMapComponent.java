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
package com.goodow.realtime.operation.map.json;

import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonElement;
import com.goodow.realtime.operation.map.AbstractMapComponent;

public class JsonMapComponent extends AbstractMapComponent<JsonElement> {
  public static boolean jsonEquals(JsonElement value0, JsonElement value1) {
    return value0 == null ? value1 == null : (value1 == null ? false : value0.toJsonString()
        .equals(value1.toJsonString()));
  }

  public static JsonMapComponent parse(JsonArray serialized) {
    int length = serialized.length();
    assert serialized.getNumber(0) == TYPE && (length == 3 || length == 4);
    return new JsonMapComponent(parseId(serialized), serialized.getString(2), null, length == 3
        ? null : serialized.<JsonElement> get(3));
  }

  public JsonMapComponent(String id, String key, JsonElement oldValue, JsonElement newValue) {
    super(id, key, oldValue, newValue);
  }

  @Override
  public JsonMapComponent invert() {
    return new JsonMapComponent(id, key, newValue, oldValue);
  }

  @Override
  protected JsonMapComponent create(String id, String key, JsonElement oldValue,
      JsonElement newValue) {
    return new JsonMapComponent(id, key, oldValue, newValue);
  }

  @Override
  protected boolean equals(JsonElement value0, JsonElement value1) {
    return jsonEquals(value0, value1);
  }

  @Override
  protected void toJson(JsonArray json) {
    super.toJson(json);
    if (newValue != null) {
      json.push(newValue);
    }
  }
}