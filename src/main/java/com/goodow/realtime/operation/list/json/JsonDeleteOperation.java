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

import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonElement;
import com.goodow.realtime.operation.list.AbstractDeleteOperation;

public class JsonDeleteOperation extends AbstractDeleteOperation<JsonElement[]> {
  public static JsonDeleteOperation parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    return new JsonDeleteOperation(parseId(serialized), parseStartIndex(serialized),
        (int) serialized.getNumber(3));
  }

  public JsonDeleteOperation(String id, int startIndex, int length) {
    super(id, startIndex, length);
  }

  public JsonDeleteOperation(String id, int startIndex, JsonElement[] values) {
    super(id, startIndex, values);
  }

  @Override
  public JsonInsertOperation invert() {
    assert values != null;
    return new JsonInsertOperation(id, startIndex, values);
  }

  @Override
  protected JsonDeleteOperation create(int startIndex, int length) {
    return new JsonDeleteOperation(id, startIndex, length);
  }

  @Override
  protected JsonDeleteOperation create(int startIndex, JsonElement[] values) {
    return new JsonDeleteOperation(id, startIndex, values);
  }

  @Override
  protected JsonHelper getHelper() {
    return JsonHelper.INSTANCE;
  }
}