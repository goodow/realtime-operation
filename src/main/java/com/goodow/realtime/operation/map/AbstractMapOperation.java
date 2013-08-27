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
package com.goodow.realtime.operation.map;

import com.goodow.realtime.operation.AbstractOperation;

public abstract class AbstractMapOperation<T> extends AbstractOperation<MapTarget<T>> {
  public static final int TYPE = 8;

  protected final String key;
  protected final T oldValue;
  protected final T newValue;

  protected AbstractMapOperation(String id, String key, T oldValue, T newValue) {
    super(TYPE, id);
    assert key != null : "Null key";
    assert (oldValue == null && newValue == null) || !equals(oldValue, newValue);
    this.key = key;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  public void apply(MapTarget<T> target) {
    target.set(key, newValue);
  }

  @Override
  public AbstractMapOperation<T>[] transformWith(AbstractOperation<MapTarget<T>> operation,
      boolean arrivedAfter) {
    assert operation instanceof AbstractMapOperation && isSameId(operation);
    AbstractMapOperation<T> op = (AbstractMapOperation<T>) operation;
    if (!key.equals(op.key)) {
      return asArray(this);
    }
    if (!arrivedAfter || equals(newValue, op.newValue)) {
      return null;
    }
    return asArray(create(id, key, op.newValue, newValue));
  }

  protected abstract AbstractMapOperation<T> create(String id, String key, T oldValue, T newValue);

  protected boolean equals(T value0, T value1) {
    return value0 == null ? value1 == null : value0.equals(value1);
  }

  @Override
  protected void toString(StringBuilder sb) {
    sb.append('"').append(key).append('"');
  }
}