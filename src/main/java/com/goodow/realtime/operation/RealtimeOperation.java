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

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RealtimeOperation implements Operation<Object> {

  public final String userId;
  public final String sessionId;
  public final List<? extends Operation<?>> operations;

  public RealtimeOperation(String userId, String sessionId, List<? extends Operation<?>> operations) {
    assert operations != null && !operations.isEmpty() && !operations.contains(null);
    this.userId = userId;
    this.sessionId = sessionId;
    this.operations = Collections.unmodifiableList(operations);
  }

  public RealtimeOperation(String userId, String sessionId, Operation<?>... operations) {
    this(userId, sessionId, Arrays.asList(operations));
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
    int size = operations.size();
    List<Operation<?>> ops = new ArrayList<Operation<?>>(size);
    for (int i = size - 1; i >= 0; i--) {
      Operation<?> invertOp = operations.get(i).invert();
      // assert invertOp != null;
      if (invertOp != null) {
        ops.add(invertOp);
      }
    }
    return ops.isEmpty() ? null : new RealtimeOperation(userId, sessionId, ops);
  }

  @Override
  public JsonArray toJson() {
    JsonArray json = Json.createArray();
    for (Operation<?> op : operations) {
      json.push(op.toJson());
    }
    return json;
  }

  @Override
  public String toString() {
    return toJson().toJsonString();
  }

  @Override
  public Pair<RealtimeOperation[], RealtimeOperation[]> transformWith(
      Operation<Object> serverOperation) {
    assert serverOperation instanceof RealtimeOperation;
    RealtimeOperation serverOp = (RealtimeOperation) serverOperation;
    TransformerImpl<Operation<?>> transformer = new TransformerImpl<Operation<?>>();
    @SuppressWarnings("unchecked")
    Pair<List<Operation<?>>, List<Operation<?>>> pair =
        transformer.transform((List<Operation<?>>) operations,
            (List<Operation<?>>) serverOp.operations);
    RealtimeOperation[] transformedClientOp =
        pair.first.isEmpty() ? null : new RealtimeOperation[] {new RealtimeOperation(userId,
            sessionId, pair.first)};
    RealtimeOperation[] transformedServerOp =
        pair.second.isEmpty() ? null : new RealtimeOperation[] {new RealtimeOperation(
            serverOp.userId, serverOp.sessionId, pair.second)};
    return Pair.of(transformedClientOp, transformedServerOp);
  }
}