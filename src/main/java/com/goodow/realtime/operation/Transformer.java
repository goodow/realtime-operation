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

import java.util.List;

import elemental.json.JsonValue;

public interface Transformer<T> {
  List<T> compact(List<T> operations);

  // OperationSink<O> createSnapshot(JsonValue serialized);

  T createOperation(String userId, String sessionId, JsonValue serialized);

  Pair<List<T>, List<T>> transform(List<T> clientOps, List<T> serverOps);

  void transform(List<T> transformedResults, T operation, List<T> transformWith, int startIndex,
      boolean arrivedAfter);
}
