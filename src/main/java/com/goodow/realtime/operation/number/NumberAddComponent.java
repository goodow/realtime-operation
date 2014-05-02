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
package com.goodow.realtime.operation.number;

import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.Operation;
import com.goodow.realtime.operation.impl.AbstractComponent;

public class NumberAddComponent extends AbstractComponent<NumberTarget> {

  public static final int TYPE = 8;
  public final double number;

  public NumberAddComponent(String id, double number) {
    super(TYPE, id);
    assert number != 0;
    this.number = number;
  }

  @Override
  public void apply(NumberTarget target) {
    target.add(number);
  }

  @Override
  public NumberAddComponent invert() {
    return new NumberAddComponent(id, -number);
  }

  @Override
  public NumberAddComponent transform(Operation<NumberTarget> other, boolean applied) {
    assert (other instanceof NumberAddComponent || other instanceof NumberResetComponent)
        && isSameId(other);
    return other instanceof NumberAddComponent ? this : null;
  }

  @Override
  protected void toJson(JsonArray json) {
    json.push(number);
  }
}