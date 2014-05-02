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

import com.goodow.realtime.operation.Operation;

public abstract class AbstractInsertComponent<T> extends AbstractListComponent<T> {
  public static final int TYPE = 5;

  protected AbstractInsertComponent(String id, int startIndex, T values) {
    super(TYPE, id, startIndex, values, -1);
  }

  @Override
  public void apply(ListTarget<T> target) {
    target.insert(startIndex, values);
  }

  @Override
  public AbstractInsertComponent<T> transform(Operation<ListTarget<T>> other, boolean applied) {
    assert other instanceof AbstractListComponent && isSameId(other);
    AbstractListComponent<T> op = (AbstractListComponent<T>) other;
    int transformedStart = op.transformIndexReference(startIndex, !applied, false);
    return transformedStart == startIndex ? this : create(transformedStart, values);
  }

  @Override
  public int transformIndexReference(int index, boolean rigthSide, boolean canBeDeleted) {
    if (rigthSide ? startIndex <= index : startIndex < index) {
      return index + length;
    }
    return index;
  }

  protected abstract AbstractInsertComponent<T> create(int startIndex, T values);
}