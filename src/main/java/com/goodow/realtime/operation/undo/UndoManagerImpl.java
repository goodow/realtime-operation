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
 * An undo manager implementation.
 * 
 * 
 * @param <O> The type of operations.
 */
public final class UndoManagerImpl<O extends Operation<?>> implements UndoManagerPlus<O> {

  private static final class Checkpointer {
    private final JsonArray partitions = Json.createArray();
    private int lastPartition = 0;

    void checkpoint() {
      if (lastPartition > 0) {
        partitions.push(lastPartition);
        lastPartition = 0;
      }
    }

    void increment() {
      ++lastPartition;
    }

    int releaseCheckpoint() {
      if (lastPartition > 0) {
        int value = lastPartition;
        lastPartition = 0;
        return value;
      }
      if (partitions.length() == 0) {
        return 0;
      }
      int number = (int) partitions.getNumber(partitions.length() - 1);
      partitions.remove(partitions.length() - 1);
      return number;
    }
  }

  private final UndoStack<O> undoStack;
  private final UndoStack<O> redoStack;
  private final Checkpointer checkpointer = new Checkpointer();
  private final Transformer<O> algorithms;

  public UndoManagerImpl(Transformer<O> algorithms) {
    this.algorithms = algorithms;
    undoStack = new UndoStack<O>(algorithms);
    redoStack = new UndoStack<O>(algorithms);
  }

  @Override
  public boolean canRedo() {
    return redoStack.stack.length() != 0;
  }

  @Override
  public boolean canUndo() {
    return undoStack.stack.length() != 0;
  }

  @Override
  public void checkpoint() {
    checkpointer.checkpoint();
  }

  @Override
  public void nonUndoableOp(O op) {
    undoStack.nonUndoableOperation(op);
    redoStack.nonUndoableOperation(op);
  }

  // TODO: This current implementation does more work than necessary.
  @Override
  public O redo() {
    Pair<O, O> redoPlus = redoPlus();
    return redoPlus == null ? null : redoPlus.first;
  }

  @Override
  public Pair<O, O> redoPlus() {
    Pair<O, O> ops = redoStack.pop();
    if (ops != null) {
      checkpointer.checkpoint();
      undoStack.push(ops.first);
      checkpointer.increment();
    }
    return ops;
  }

  // TODO: This current implementation does more work than necessary.
  @Override
  public O undo() {
    Pair<O, O> undoPlus = undoPlus();
    return undoPlus == null ? null : undoPlus.first;
  }

  @Override
  public void undoableOp(O op) {
    undoStack.push(op);
    checkpointer.increment();
    redoStack.clear();
  }

  // TODO: This current implementation does more work than necessary.
  @Override
  public Pair<O, O> undoPlus() {
    int numToUndo = checkpointer.releaseCheckpoint();
    if (numToUndo == 0) {
      return null;
    }
    JsonArray operations = Json.createArray();
    for (int i = 0; i < numToUndo - 1; ++i) {
      operations.push(undoStack.pop().first);
    }
    Pair<O, O> ops = undoStack.pop();
    operations.push(ops.first);
    O op = algorithms.compose(operations);
    redoStack.push(op);
    return new Pair<O, O>(op, ops.second);
  }
}