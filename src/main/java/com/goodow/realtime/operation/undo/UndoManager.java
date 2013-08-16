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

import java.util.List;

/**
 * An undo manager.
 * 
 * 
 * @param <T> The type of operations.
 */
public interface UndoManager<T> {

  /**
   * @return True if the model can currently redo.
   */
  boolean canRedo();

  /**
   * @return True if the model can currently undo.
   */
  boolean canUndo();

  /**
   * Places an undo checkpoint.
   */
  void checkpoint();

  /**
   * Places into the undo manager an operation that should not be undone by undos.
   * 
   * @param op the operation that should not be undone by any undos
   */
  void nonUndoableOp(T op);

  /**
   * Effects a redo. Returns null if there are no operations to redo.
   * 
   * @return the operation that will effect a redo
   */
  List<T> redo();

  /**
   * Effects an undo. Returns null if there are no operations to undo.
   * 
   * @return the operation that will effect an undo
   */
  List<T> undo();

  /**
   * Places into the undo manager an operation that should be undone by undos.
   * 
   * @param op the operation that should be undone by an appropriate undo
   */
  void undoableOp(T op);
}
