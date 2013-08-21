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
package com.goodow.realtime.operation.list.string;

import com.goodow.realtime.operation.list.AbstractInsertOperation;

import elemental.json.JsonArray;

public class StringInsertOperation extends AbstractInsertOperation<String> {
  public static StringInsertOperation parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    String values = StringHelper.INSTANCE.parseValues(serialized.getArray(3));
    return new StringInsertOperation(parseId(serialized), parseStartIndex(serialized), values);
  }

  public StringInsertOperation(String id, int startIndex, String values) {
    super(id, startIndex, values);
  }

  @Override
  public StringDeleteOperation invert() {
    return new StringDeleteOperation(id, startIndex, values);
  }

  @Override
  protected StringInsertOperation create(int startIndex, String values) {
    return new StringInsertOperation(id, startIndex, values);
  }

  @Override
  protected StringHelper getHelper() {
    return StringHelper.INSTANCE;
  }
}