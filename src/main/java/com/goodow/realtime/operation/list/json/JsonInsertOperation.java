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
package com.goodow.realtime.operation.list.json;

import com.goodow.realtime.operation.list.AbstractInsertOperation;

import elemental.json.JsonArray;
import elemental.json.JsonValue;

public class JsonInsertOperation extends AbstractInsertOperation<JsonValue[]> {
  public static JsonInsertOperation parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    JsonValue[] values = JsonHelper.INSTANCE.parse(serialized.getArray(3));
    return new JsonInsertOperation(parseId(serialized), parseStartIndex(serialized), values);
  }

  public JsonInsertOperation(String id, int startIndex, JsonValue[] values) {
    super(id, startIndex, values);
  }

  @Override
  public JsonDeleteOperation invert() {
    return new JsonDeleteOperation(id, startIndex, values);
  }

  @Override
  protected JsonInsertOperation create(int startIndex, JsonValue[] values) {
    return new JsonInsertOperation(id, startIndex, values);
  }

  @Override
  protected JsonHelper getHelper() {
    return JsonHelper.INSTANCE;
  }
}