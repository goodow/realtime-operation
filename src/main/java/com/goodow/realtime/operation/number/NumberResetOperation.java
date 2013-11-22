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
import com.goodow.realtime.operation.AbstractOperation;

public class NumberResetOperation extends AbstractOperation<NumberTarget> {

  public static final int TYPE = 9;
  private final double oldNumber;
  private final double newNumber;

  public NumberResetOperation(String id, double oldNumber, double newNumber) {
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
  public NumberResetOperation invert() {
    assert !Double.isNaN(oldNumber);
    return new NumberResetOperation(id, newNumber, oldNumber);
  }

  @Override
  public NumberResetOperation[] transformWith(AbstractOperation<NumberTarget> operation,
      boolean arrivedAfter) {
    assert (operation instanceof NumberResetOperation || operation instanceof NumberAddOperation)
        && isSameId(operation);
    if (arrivedAfter && operation instanceof NumberResetOperation) {
      double transformedOldNumber = ((NumberResetOperation) operation).newNumber;
      return transformedOldNumber == newNumber ? null : asArray(new NumberResetOperation(id,
          transformedOldNumber, newNumber));
    } else {
      return null;
    }
  }

  @Override
  protected void toJson(JsonArray json) {
    json.push(newNumber);
  }
}