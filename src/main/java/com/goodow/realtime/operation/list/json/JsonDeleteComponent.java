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
import com.goodow.realtime.operation.list.AbstractDeleteComponent;

public class JsonDeleteComponent extends AbstractDeleteComponent<JsonArray> {
  public static JsonDeleteComponent parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    return new JsonDeleteComponent(parseId(serialized), parseStartIndex(serialized),
        (int) serialized.getNumber(3));
  }

  public JsonDeleteComponent(String id, int startIndex, int length) {
    super(id, startIndex, length);
  }

  public JsonDeleteComponent(String id, int startIndex, JsonArray values) {
    super(id, startIndex, values);
  }

  @Override
  public JsonInsertComponent invert() {
    assert values != null;
    return new JsonInsertComponent(id, startIndex, values);
  }

  @Override
  protected JsonDeleteComponent create(int startIndex, int length) {
    return new JsonDeleteComponent(id, startIndex, length);
  }

  @Override
  protected JsonDeleteComponent create(int startIndex, JsonArray values) {
    return new JsonDeleteComponent(id, startIndex, values);
  }

  @Override
  protected JsonHelper getHelper() {
    return JsonHelper.INSTANCE;
  }
}