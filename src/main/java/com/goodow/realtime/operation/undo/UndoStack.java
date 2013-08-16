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
package com.goodow.realtime.operation.undo;

import com.goodow.realtime.operation.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * An undo stack.
 * 
 * TODO: This can be heavily optimised.
 * 
 * 
 * @param <T> The type of operations.
 */
final class UndoStack<T> {

  private static int MAX_CAPACITY = 100;
  private final UndoManagerImpl.Algorithms<T> algorithms;
  private final List<T> ops = new LinkedList<T>();
  private boolean checkpointer;

  UndoStack(UndoManagerImpl.Algorithms<T> algorithms) {
    this.algorithms = algorithms;
  }

  void checkpoint() {
    checkpointer = true;
  }

  /**
   * Clear the stack.
   */
  void clear() {
    ops.clear();
  }

  boolean isEmpty() {
    return ops.isEmpty();
  }

  /**
   * Intermingles intervening operations that should not be undone.
   * 
   * @param op the operation that should not be undone
   */
  void nonUndoableOperation(T op) {
    assert op != null;
    if (!ops.isEmpty()) {
      ops.add(op);
      keepCapacity(ops);
    }
  }

  /**
   * Pops an operation from the undo stack and returns the operation that effects the undo and the
   * transformed non-undoable operation.
   * 
   * @return the operation that accomplishes the desired undo
   */
  Pair<List<T>, List<T>> pop() {
    List<T> transformedClientOps = new ArrayList<T>();
    int index;
    do {
      index = popOne(transformedClientOps);
    } while (ops.get(index - 1) != null);
    ops.remove(index - 1);
    return Pair.of(ops.subList(index - 1, ops.size()), transformedClientOps);
  }

  /**
   * Pushes an operation onto the undo stack.
   * 
   * @param op the operation to push onto the undo stack
   */
  void push(T op) {
    assert op != null;
    if (ops.isEmpty() && !checkpointer) {
      return;
    }
    if (checkpointer) {
      ops.add(null);
      checkpointer = false;
    }
    ops.add(null);
    ops.add(op);
    keepCapacity(ops);
  }

  private void keepCapacity(List<T> list) {
    int size = list.size();
    if (size <= MAX_CAPACITY) {
      return;
    }
    assert list.get(0) == null && list.get(1) == null;
    list.remove(1);
    list.remove(0);
    size = size - 2;
    int nextCheckpointer = -1;
    boolean previousIsNull = false;
    for (T op : list) {
      if (previousIsNull && op == null && size - nextCheckpointer <= MAX_CAPACITY) {
        break;
      }
      nextCheckpointer++;
      previousIsNull = op == null;
    }
    for (int i = 0; i < nextCheckpointer; i++) {
      list.remove(0);
    }
  }

  private int popOne(List<T> results) {
    int index = ops.size() - 1;
    for (;; index--) {
      if (ops.get(index) == null) {
        break;
      }
    }
    ops.remove(index);
    T op = algorithms.invert(ops.remove(index));
    algorithms.transform(results, op, ops, index);
    return index;
  }
}