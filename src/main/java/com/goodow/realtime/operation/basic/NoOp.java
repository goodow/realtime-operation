/*
 * Copyright 2012 Goodow.com
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
package com.goodow.realtime.operation.basic;

import com.goodow.realtime.operation.Operation;
import com.goodow.realtime.operation.util.Pair;

public class NoOp<T> implements Operation<T> {
  public static final int TYPE = 9;

  public static <T> NoOp<T> get() {
    return new NoOp<T>();
  }

  private NoOp() {
  }

  @Override
  public void apply(T target) {
  }

  @Override
  public Operation<T> composeWith(Operation<T> op) {
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof NoOp;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public int getType() {
    return TYPE;
  }

  @Override
  public Operation<T> invert() {
    return this;
  }

  @Override
  public boolean isNoOp() {
    return true;
  }

  @Override
  public void setId(String id) {
  }

  @Override
  public String toString() {
    return null;
  }

  @Override
  public Pair<NoOp<T>, ? extends Operation<?>> transformWith(Operation<?> clientOp) {
    throw new IllegalStateException();
  }
}