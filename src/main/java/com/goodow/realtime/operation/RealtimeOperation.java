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

import java.util.ArrayList;
import java.util.List;

public class RealtimeOperation implements Operation<Object> {

  private final List<? extends AbstractOperation<?>> operations;
  public final String userId;
  public final String sessionId;

  public RealtimeOperation(String userId, String sessionId,
      List<? extends AbstractOperation<?>> operations) {
    assert userId != null && operations != null && !operations.isEmpty();
    this.userId = userId;
    this.sessionId = sessionId;
    this.operations = operations;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void apply(Object target) {
    for (Operation<?> op : operations) {
      ((Operation<Object>) op).apply(target);
    }
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
    RealtimeOperation other = (RealtimeOperation) obj;
    if (operations == null) {
      if (other.operations != null) {
        return false;
      }
    } else if (!operations.equals(other.operations)) {
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((operations == null) ? 0 : operations.hashCode());
    result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
    result = prime * result + ((userId == null) ? 0 : userId.hashCode());
    return result;
  }

  @Override
  public RealtimeOperation invert() {
    List<AbstractOperation<?>> ops = new ArrayList<AbstractOperation<?>>();
    for (int i = operations.size() - 1; i >= 0; i--) {
      AbstractOperation<?> invertOp = operations.get(i).invert();
      assert invertOp != null;
      ops.add(invertOp);
    }
    return new RealtimeOperation(userId, sessionId, ops);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder().append('[');
    boolean isFirst = true;
    for (Operation<?> op : operations) {
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append(',');
      }
      sb.append(op.toString());
    }
    sb.append(']');
    return sb.toString();
  }

  @Override
  public Pair<RealtimeOperation[], RealtimeOperation[]> transformWith(
      Operation<Object> serverOperation) {
    assert serverOperation instanceof RealtimeOperation;
    RealtimeOperation serverOp = (RealtimeOperation) serverOperation;
    TransformerImpl<AbstractOperation<?>> transformer = new TransformerImpl<AbstractOperation<?>>();
    @SuppressWarnings("unchecked")
    Pair<List<AbstractOperation<?>>, List<AbstractOperation<?>>> pair =
        transformer.transform((List<AbstractOperation<?>>) operations,
            (List<AbstractOperation<?>>) serverOp.operations);
    RealtimeOperation[] transformedClientOp =
        pair.first.isEmpty() ? null : new RealtimeOperation[] {new RealtimeOperation(userId,
            sessionId, pair.first)};
    RealtimeOperation[] transformedServerOp =
        pair.second.isEmpty() ? null : new RealtimeOperation[] {new RealtimeOperation(
            serverOp.userId, serverOp.sessionId, pair.second)};
    return Pair.of(transformedClientOp, transformedServerOp);
  }
}