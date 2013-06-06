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
package com.goodow.realtime.operation;

import com.goodow.realtime.operation.util.Pair;

public class RealtimeOperation<T> implements Operation<T> {

  public final Operation<T> op;
  public final String userId;
  public final String sessionId;

  public RealtimeOperation(Operation<T> op) {
    this(op, null, null);
  }

  public RealtimeOperation(Operation<T> op, String userId, String sessionId) {
    this.op = op;
    this.userId = userId;
    this.sessionId = sessionId;
  }

  @Override
  public void apply(T target) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RealtimeOperation<T> composeWith(Operation<T> operation) {
    assert operation instanceof RealtimeOperation;
    assert !isNoOp() && !operation.isNoOp();
    RealtimeOperation<T> op = (RealtimeOperation<T>) operation;
    assert getType() == op.getType() && getId().equals(op.getId())
        && sessionId.equals(op.sessionId) && userId.equals(op.userId);
    Operation<T> composition = this.op.composeWith(op.<T> getOp());
    composition.setId(getId());
    return new RealtimeOperation<T>(composition, userId, sessionId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof RealtimeOperation)) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    RealtimeOperation other = (RealtimeOperation) obj;
    if (op == null) {
      if (other.op != null) {
        return false;
      }
    } else if (!op.equals(other.op)) {
      return false;
    }
    if (sessionId == null) {
      if (other.sessionId != null) {
        return false;
      }
    } else if (!sessionId.equals(other.sessionId)) {
      return false;
    }
    if (userId == null) {
      if (other.userId != null) {
        return false;
      }
    } else if (!userId.equals(other.userId)) {
      return false;
    }
    return true;
  }

  @Override
  public String getId() {
    return op.getId();
  }

  @SuppressWarnings({"unchecked", "hiding"})
  public <T> Operation<T> getOp() {
    return (Operation<T>) op;
  }

  @Override
  public int getType() {
    return op.getType();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((op == null) ? 0 : op.hashCode());
    result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
    result = prime * result + ((userId == null) ? 0 : userId.hashCode());
    return result;
  }

  @Override
  public Operation<T> invert() {
    return new RealtimeOperation<T>(op.invert(), userId, sessionId);
  }

  @Override
  public boolean isNoOp() {
    return op.isNoOp();
  }

  @Override
  public void setId(String id) {
    op.setId(id);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[").append(op.getType()).append(",\"").append(getId()).append("\",").append(
        op.toString()).append("]");
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Pair<RealtimeOperation<T>, ? extends RealtimeOperation<?>> transformWith(
      Operation<?> clientOp) {
    assert !isNoOp() && !clientOp.isNoOp();
    assert clientOp instanceof RealtimeOperation;
    RealtimeOperation<T> op = (RealtimeOperation<T>) clientOp;
    if (!getId().equals(op.getId())) {
      return Pair.of(this, op);
    }
    assert getType() == op.getType() && getId().equals(op.getId());
    Pair<? extends Operation<T>, ? extends Operation<?>> pair = this.op.transformWith(op.getOp());
    pair.first.setId(getId());
    pair.second.setId(getId());
    RealtimeOperation<T> transformedServerOp =
        new RealtimeOperation<T>(pair.first, userId, sessionId);
    @SuppressWarnings("rawtypes")
    RealtimeOperation<?> transformedClientOp =
        new RealtimeOperation(pair.second, op.userId, op.sessionId);
    return Pair.of(transformedServerOp, transformedClientOp);
  }

}
