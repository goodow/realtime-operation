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

import java.util.List;

/**
 * An undo manager implementation.
 * 
 * 
 * @param <T> The type of operations.
 */
public final class UndoManagerImpl<T> implements UndoManagerPlus<T> {

  /**
   * Algorithms required by the undo manager.
   * 
   * @param <T> The type of operations.
   */
  public interface Algorithms<T> {

    /**
     * Inverts the given operation.
     * 
     * @param operation The operation to invert.
     * @return The inverse of the given operation.
     */
    T invert(T operation);

    /**
     * Transforms the given operations.
     * 
     * @param clientOp The first concurrent operation.
     * @param serverOps The second concurrent operation.
     */
    void transform(List<T> results, T clientOp, List<T> serverOps, int startIndex);
  }

  private final UndoStack<T> undoStack;
  private final UndoStack<T> redoStack;

  public UndoManagerImpl(Algorithms<T> algorithms) {
    undoStack = new UndoStack<T>(algorithms);
    redoStack = new UndoStack<T>(algorithms);
  }

  @Override
  public boolean canRedo() {
    return !redoStack.isEmpty();
  }

  @Override
  public boolean canUndo() {
    return !undoStack.isEmpty();
  }

  @Override
  public void checkpoint() {
    undoStack.checkpoint();
  }

  @Override
  public void nonUndoableOp(T op) {
    undoStack.nonUndoableOperation(op);
    redoStack.nonUndoableOperation(op);
  }

  // TODO: This current implementation does more work than necessary.
  @Override
  public List<T> redo() {
    Pair<List<T>, List<T>> redoPlus = redoPlus();
    return redoPlus == null ? null : redoPlus.first;
  }

  @Override
  public Pair<List<T>, List<T>> redoPlus() {
    if (!canRedo()) {
      return null;
    }
    Pair<List<T>, List<T>> pair = redoStack.pop();
    undoStack.checkpoint();
    for (T op : pair.first) {
      undoStack.push(op);
    }
    return pair;
  }

  // TODO: This current implementation does more work than necessary.
  @Override
  public List<T> undo() {
    Pair<List<T>, List<T>> undoPlus = undoPlus();
    return undoPlus == null ? null : undoPlus.first;
  }

  @Override
  public void undoableOp(T op) {
    undoStack.push(op);
    redoStack.clear();
  }

  // TODO: This current implementation does more work than necessary.
  @Override
  public Pair<List<T>, List<T>> undoPlus() {
    if (!canUndo()) {
      return null;
    }
    Pair<List<T>, List<T>> pair = undoStack.pop();
    redoStack.checkpoint();
    for (T op : pair.first) {
      redoStack.push(op);
    }
    return pair;
  }
}