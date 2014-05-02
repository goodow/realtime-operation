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

import com.goodow.realtime.operation.impl.CollaborativeOperation;
import com.goodow.realtime.operation.impl.CollaborativeTransformer;
import com.goodow.realtime.operation.util.Pair;

/**
 * A factory for creating undo managers for document operations.
 */
public final class UndoManagerFactory {
  private static final UndoManagerPlus<?> NOP_IMPL = new UndoManagerPlus<Object>() {
    @Override
    public boolean canRedo() {
      return false;
    }

    @Override
    public boolean canUndo() {
      return false;
    }

    @Override
    public void checkpoint() {
    }

    @Override
    public void nonUndoableOp(Object op) {
    }

    @Override
    public Object redo() {
      return redoPlus().first;
    }

    @Override
    public Pair<Object, Object> redoPlus() {
      throw new UnsupportedOperationException("No Redo For You!");
    }

    @Override
    public Object undo() {
      return undoPlus().first;
    }

    @Override
    public void undoableOp(Object op) {
    }

    @Override
    public Pair<Object, Object> undoPlus() {
      throw new UnsupportedOperationException("No Undo For You!");
    }
  };

  /**
   * Creates a new undo manager.
   * 
   * @return A new undo manager.
   */
  public static UndoManagerPlus<CollaborativeOperation> createUndoManager() {
    return new UndoManagerImpl<CollaborativeOperation>(new CollaborativeTransformer());
  }

  /**
   * Implementation that does almost nothing
   */
  @SuppressWarnings("unchecked")
  public static <T> UndoManagerPlus<T> getNoOp() {
    return (UndoManagerPlus<T>) NOP_IMPL;
  }
}