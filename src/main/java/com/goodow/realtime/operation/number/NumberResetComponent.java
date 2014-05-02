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

public class NumberResetComponent extends AbstractComponent<NumberTarget> {

  public static final int TYPE = 9;
  private final double oldNumber;
  private final double newNumber;

  public NumberResetComponent(String id, double oldNumber, double newNumber) {
    super(TYPE, id);
    assert !Double.isNaN(newNumber) && oldNumber != newNumber;
    this.oldNumber = oldNumber;
    this.newNumber = newNumber;
  }

  @Override
  public void apply(NumberTarget target) {
    target.reset(newNumber);
  }

  @Override
  public NumberResetComponent invert() {
    assert !Double.isNaN(oldNumber);
    return new NumberResetComponent(id, newNumber, oldNumber);
  }

  @Override
  public NumberResetComponent transform(Operation<NumberTarget> other, boolean applied) {
    assert (other instanceof NumberResetComponent || other instanceof NumberAddComponent)
        && isSameId(other);
    if (other instanceof NumberResetComponent) {
      NumberResetComponent op = ((NumberResetComponent) other);
      return applied || op.newNumber == newNumber ? null : new NumberResetComponent(id,
          op.newNumber, newNumber);
    } else {
      return new NumberResetComponent(id, oldNumber + ((NumberAddComponent) other).number,
          newNumber);
    }
  }

  @Override
  protected void toJson(JsonArray json) {
    json.push(newNumber);
  }
}