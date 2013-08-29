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

import com.goodow.realtime.operation.RealtimeOperation;
import com.goodow.realtime.operation.Transformer;
import com.goodow.realtime.operation.TransformerImpl;
import com.goodow.realtime.operation.util.Pair;

import java.util.List;

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
    public List<Object> redo() {
      return redoPlus().first;
    }

    @Override
    public Pair<List<Object>, List<Object>> redoPlus() {
      throw new UnsupportedOperationException("No Redo For You!");
    }

    @Override
    public List<Object> undo() {
      return undoPlus().first;
    }

    @Override
    public void undoableOp(Object op) {
    }

    @Override
    public Pair<List<Object>, List<Object>> undoPlus() {
      throw new UnsupportedOperationException("No Undo For You!");
    }
  };
  private static final UndoManagerImpl.Algorithms<RealtimeOperation> algorithms =
      new UndoManagerImpl.Algorithms<RealtimeOperation>() {
        Transformer<RealtimeOperation> transformer = new TransformerImpl<RealtimeOperation>();

        @Override
        public RealtimeOperation invert(RealtimeOperation operation) {
          return operation.invert();
        }

        @Override
        public void transform(List<RealtimeOperation> results, RealtimeOperation clientOp,
            List<RealtimeOperation> serverOps, int startIndex) {
          transformer.transform(results, clientOp, serverOps, startIndex, true);
        }
      };

  /**
   * Creates a new undo manager.
   * 
   * @return A new undo manager.
   */
  public static UndoManagerPlus<RealtimeOperation> createUndoManager() {
    return new UndoManagerImpl<RealtimeOperation>(algorithms);
  }

  /**
   * Implementation that does almost nothing
   */
  @SuppressWarnings("unchecked")
  public static <T> UndoManagerPlus<T> getNoOp() {
    return (UndoManagerPlus<T>) NOP_IMPL;
  }
}
