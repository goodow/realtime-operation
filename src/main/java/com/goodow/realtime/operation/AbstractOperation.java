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
package com.goodow.realtime.operation;

import com.goodow.realtime.operation.util.Pair;

import elemental.json.JsonArray;
import elemental.json.JsonType;

public abstract class AbstractOperation<T> implements Operation<T> {
  protected static String parseId(JsonArray serialized) {
    return serialized.get(1).getType() == JsonType.NULL ? null : serialized.getString(1);
  }

  public final int type;
  public final String id;

  protected AbstractOperation(int type, String id) {
    this.type = type;
    this.id = id;
  }

  @Override
  public boolean equals(Object obj) {
    return toString().equals(obj.toString());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + toString().hashCode();
    return result;
  }

  @Override
  public abstract AbstractOperation<T> invert();

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder().append('[');
    sb.append(type).append(',');
    if (id == null) {
      sb.append((String) null);
    } else {
      sb.append('"').append(id).append('"');
    }
    sb.append(',');
    toString(sb);
    sb.append(']');
    return sb.toString();
  }

  /**
   * @param operation
   * @param arrivedAfter Whether this operation reaches the server after {@code operation}.
   * @return
   */
  public abstract AbstractOperation<T>[] transformWith(AbstractOperation<T> operation,
      boolean arrivedAfter);

  @Override
  public Pair<AbstractOperation<T>[], AbstractOperation<T>[]> transformWith(
      Operation<T> serverOperation) {
    assert serverOperation instanceof AbstractOperation
        && isSameId((AbstractOperation<T>) serverOperation);
    AbstractOperation<T> serverOp = (AbstractOperation<T>) serverOperation;
    AbstractOperation<T>[] transformedClientOps = this.transformWith(serverOp, true);
    AbstractOperation<T>[] transformedServerOps = serverOp.transformWith(this, false);
    return Pair.of(transformedClientOps, transformedServerOps);
  }

  protected <O> O[] asArray(O... operations) {
    return operations;
  }

  protected boolean isSameId(AbstractOperation<?> operation) {
    String id2 = operation.id;
    return id == null ? id2 == null : id.equals(id2);
  }

  protected abstract void toString(StringBuilder sb);
}