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
package com.goodow.realtime.operation;

import elemental.json.JsonArray;

public class CreateOperation extends AbstractOperation<Void> {
  public static final int TYPE = 7;
  public static final int MAP = 0;
  public static final int LIST = 1;
  public static final int STRING = 2;
  public static final int INDEX_REFERENCE = 4;

  public static CreateOperation parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 3;
    return new CreateOperation(parseId(serialized), (int) serialized.getNumber(2));
  }

  public final int type;

  public CreateOperation(String id, int type) {
    super(id);
    this.type = type;
  }

  @Override
  public void apply(Void target) {
    throw new IllegalStateException();
  }

  @Override
  public int getType() {
    return TYPE;
  }

  @Override
  public Operation<Void> invert() {
    throw new UnsupportedOperationException();
  }

  @Override
  public CreateOperation[] transformWith(Operation<Void> operation, boolean arrivedAfter) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void toString(StringBuilder sb) {
    sb.append(type);
  }
}