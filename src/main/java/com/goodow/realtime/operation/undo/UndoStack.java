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

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.Operation;
import com.goodow.realtime.operation.Transformer;
import com.goodow.realtime.operation.util.Pair;

/**
 * An undo stack.
 * 
 * TODO: This can be heavily optimised.
 * 
 * @param <O> The type of operations.
 */
final class UndoStack<O extends Operation<?>> {
  private static final class StackEntry<T> {
    final T op;
    JsonArray nonUndoables = Json.createArray();

    StackEntry(T op) {
      this.op = op;
    }
  }

  final JsonArray stack = Json.createArray(); // Stack<StackEntry<T>>
  final Transformer<O> transformer;

  UndoStack(Transformer<O> transformer) {
    this.transformer = transformer;
  }

  /**
   * Clear the stack.
   */
  void clear() {
    stack.clear();
  }

  /**
   * Intermingles intervening operations that should not be undone.
   * 
   * @param op the operation that should not be undone
   */
  void nonUndoableOperation(O op) {
    if (stack.length() > 0) {
      stack.<StackEntry<O>> get(stack.length() - 1).nonUndoables.push(op);
    }
  }

  /**
   * Pops an operation from the undo stack and returns the operation that effects the undo and the
   * transformed non-undoable operation.
   * 
   * @return a pair containeng the operation that accomplishes the desired undo and the transformed
   *         non-undoable operation
   */
  Pair<O, O> pop() {
    if (stack.length() == 0) {
      return null;
    }
    StackEntry<O> entry = stack.remove(stack.length() - 1);
    @SuppressWarnings("unchecked")
    O op = (O) entry.op.invert();
    if (entry.nonUndoables.length() == 0) {
      return new Pair<O, O>(op, null);
    }
    Pair<O, O> pair = transformer.transform(op, transformer.compose(entry.nonUndoables));
    StackEntry<O> nextEntry =
        stack.length() == 0 ? null : stack.<StackEntry<O>> get(stack.length() - 1);
    if (nextEntry != null) {
      nextEntry.nonUndoables.push(pair.second);
    }
    return new Pair<O, O>(pair.first, pair.second);
  }

  /**
   * Pushes an operation onto the undo stack.
   * 
   * @param op the operation to push onto the undo stack
   */
  void push(O op) {
    stack.push(new StackEntry<O>(op));
  }
}