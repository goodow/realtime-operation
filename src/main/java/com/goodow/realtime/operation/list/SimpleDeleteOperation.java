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

import com.goodow.realtime.operation.list.json.JsonHelper;
import com.goodow.realtime.operation.list.string.StringHelper;

import elemental.json.JsonArray;

public class SimpleDeleteOperation<T> extends AbstractDeleteOperation<T> implements ListHelper<T> {
  public static <T> SimpleDeleteOperation<T> parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    return new SimpleDeleteOperation<T>(parseId(serialized), parseStartIndex(serialized),
        (int) serialized.getNumber(3));
  }

  private ListHelper<T> delegate;

  public SimpleDeleteOperation(String id, int startIndex, int length) {
    super(id, startIndex, length);
  }

  public SimpleDeleteOperation(String id, int startIndex, T values) {
    super(id, startIndex, values);
  }

  @Override
  public SimpleDeleteOperation<T> invert() {
    assert values != null;
    return new SimpleDeleteOperation<T>(id, startIndex, values);
  }

  @Override
  public int length(T values) {
    return getDelegate(values).length(values);
  }

  @Override
  public T parseValues(JsonArray serialized) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T replaceWith(T values, int startIndex, int length, T replacement) {
    return getDelegate(values).replaceWith(values, startIndex, length, replacement);
  }

  @Override
  public StringBuilder serialize(T values) {
    return getDelegate(values).serialize(values);
  }

  @Override
  public T subset(T values, int startIndex, int length) {
    return getDelegate(values).subset(values, startIndex, length);
  }

  @Override
  public T subset(T values, int startIndex0, int length0, int startIndex1, int length1) {
    return getDelegate(values).subset(values, startIndex0, length0, startIndex1, length1);
  }

  @Override
  public T subset(T values0, int startIndex0, int length0, T values1, int startIndex1, int length1) {
    return getDelegate(values0)
        .subset(values0, startIndex0, length0, values1, startIndex1, length1);
  }

  @Override
  protected SimpleDeleteOperation<T> create(int startIndex, int length) {
    return new SimpleDeleteOperation<T>(id, startIndex, length);
  }

  @Override
  protected SimpleDeleteOperation<T> create(int startIndex, T values) {
    return new SimpleDeleteOperation<T>(id, startIndex, values);
  }

  @Override
  protected ListHelper<T> getHelper() {
    return this;
  }

  @SuppressWarnings("unchecked")
  private ListHelper<T> getDelegate(T values) {
    if (delegate == null) {
      delegate = (ListHelper<T>) (values instanceof String ? new StringHelper() : new JsonHelper());
    }
    return delegate;
  }
}