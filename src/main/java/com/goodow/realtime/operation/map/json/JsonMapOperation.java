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

import com.goodow.realtime.operation.map.AbstractMapOperation;

import elemental.json.JsonArray;
import elemental.json.JsonType;
import elemental.json.JsonValue;

public class JsonMapOperation extends AbstractMapOperation<JsonValue> {
  public static boolean jsonEquals(JsonValue value0, JsonValue value1) {
    return value0 == null ? value1 == null : (value1 == null ? false : value0.toJson().equals(
        value1.toJson()));
  }

  public static JsonMapOperation parse(JsonArray serialized) {
    int length = serialized.length();
    assert serialized.getNumber(0) == TYPE && (length == 3 || length == 4);
    return new JsonMapOperation(parseId(serialized), serialized.getString(2), null, length == 3
        ? null : serialized.get(3));
  }

  public JsonMapOperation(String id, String key, JsonValue oldValue, JsonValue newValue) {
    super(id, key, oldValue, newValue);
    assert oldValue == null || oldValue.getType() != JsonType.NULL;
    assert newValue == null || newValue.getType() != JsonType.NULL;
  }

  @Override
  public JsonMapOperation invert() {
    return new JsonMapOperation(id, key, newValue, oldValue);
  }

  @Override
  protected JsonMapOperation create(String id, String key, JsonValue oldValue, JsonValue newValue) {
    return new JsonMapOperation(id, key, oldValue, newValue);
  }

  @Override
  protected boolean equals(JsonValue value0, JsonValue value1) {
    return jsonEquals(value0, value1);
  }

  @Override
  protected void toString(StringBuilder sb) {
    super.toString(sb);
    if (newValue != null) {
      sb.append(',').append(newValue.toJson());
    }
  }
}