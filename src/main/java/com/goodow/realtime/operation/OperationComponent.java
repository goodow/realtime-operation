/*
 * Copyright 2014 Goodow.com
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

public abstract class OperationComponent<T> implements Operation<T> {

  @Override
  public Operation<T> transform(Operation<T> other, boolean applied) {
    throw new UnsupportedOperationException();
  }

  public OperationComponent<T>[] transformComponent(OperationComponent<T> other, boolean applied) {
    throw new UnsupportedOperationException();
  }
}
