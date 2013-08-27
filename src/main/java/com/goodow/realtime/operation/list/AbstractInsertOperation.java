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
package com.goodow.realtime.operation.list;

import com.goodow.realtime.operation.AbstractOperation;

public abstract class AbstractInsertOperation<T> extends AbstractListOperation<T> {
  public static final int TYPE = 5;

  protected AbstractInsertOperation(String id, int startIndex, T values) {
    super(TYPE, id, startIndex, values, -1);
  }

  @Override
  public void apply(ListTarget<T> target) {
    target.insert(startIndex, values);
  }

  @Override
  public int transformIndexReference(int index, boolean rigthSide, boolean canBeDeleted) {
    if (rigthSide ? startIndex <= index : startIndex < index) {
      return index + length;
    }
    return index;
  }

  @Override
  public AbstractInsertOperation<T>[] transformWith(AbstractOperation<ListTarget<T>> operation,
      boolean arrivedAfter) {
    assert operation instanceof AbstractListOperation && isSameId(operation);
    AbstractListOperation<T> op = (AbstractListOperation<T>) operation;
    int transformedStart = op.transformIndexReference(startIndex, arrivedAfter, false);
    return transformedStart == startIndex ? asArray(this)
        : asArray(create(transformedStart, values));
  }

  protected abstract AbstractInsertOperation<T> create(int startIndex, T values);
}