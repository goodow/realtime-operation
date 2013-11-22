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

import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.list.AbstractDeleteOperation;

public class StringDeleteOperation extends AbstractDeleteOperation<String> {
  public static StringDeleteOperation parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    return new StringDeleteOperation(parseId(serialized), parseStartIndex(serialized),
        (int) serialized.getNumber(3));
  }

  public StringDeleteOperation(String id, int startIndex, int length) {
    super(id, startIndex, length);
  }

  public StringDeleteOperation(String id, int startIndex, String values) {
    super(id, startIndex, values);
  }

  @Override
  public StringInsertOperation invert() {
    assert values != null;
    return new StringInsertOperation(id, startIndex, values);
  }

  @Override
  protected StringDeleteOperation create(int startIndex, int length) {
    return new StringDeleteOperation(id, startIndex, length);
  }

  @Override
  protected StringDeleteOperation create(int startIndex, String values) {
    return new StringDeleteOperation(id, startIndex, values);
  }

  @Override
  protected StringHelper getHelper() {
    return StringHelper.INSTANCE;
  }
}