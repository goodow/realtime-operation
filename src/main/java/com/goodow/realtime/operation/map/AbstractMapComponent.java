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

import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.Operation;
import com.goodow.realtime.operation.impl.AbstractComponent;

public abstract class AbstractMapComponent<T> extends AbstractComponent<MapTarget<T>> {
  public static final int TYPE = 8;

  protected final String key;
  protected final T oldValue;
  protected final T newValue;

  protected AbstractMapComponent(String id, String key, T oldValue, T newValue) {
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
  public AbstractMapComponent<T> transform(Operation<MapTarget<T>> other, boolean applied) {
    assert other instanceof AbstractMapComponent && isSameId(other);
    AbstractMapComponent<T> op = (AbstractMapComponent<T>) other;
    if (!key.equals(op.key)) {
      return this;
    }
    if (applied || equals(newValue, op.newValue)) {
      return null;
    }
    return create(id, key, op.newValue, newValue);
  }

  protected abstract AbstractMapComponent<T> create(String id, String key, T oldValue, T newValue);

  protected boolean equals(T value0, T value1) {
    return value0 == null ? value1 == null : value0.equals(value1);
  }

  @Override
  protected void toJson(JsonArray json) {
    json.push(key);
  }
}