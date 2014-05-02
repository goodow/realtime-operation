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

/**
 * An <code>UndoManager</code> that provides versions of the undo and redo methods which return more
 * information.
 * 
 * @param <O> The type of operations.
 */
public interface UndoManagerPlus<O> extends UndoManager<O> {
  /**
   * Effects a redo. Returns null if there are no operations to redo.
   * 
   * NOTE: Warning. This interface method may change.
   * 
   * @return a pair containing the operation that will effect a redo and the relevant transformed
   *         non-undoable operation (which may be null if no such operation exists)
   * 
   *         NOTE: Returning null is probably slightly harder to use than returning an operation
   *         that does nothing.
   */
  Pair<O, O> redoPlus();

  /**
   * Effects an undo. Returns null if there are no operations to undo.
   * 
   * NOTE: Warning. This interface method may change.
   * 
   * @return a pair containing the operation that will effect an undo and the relevant transformed
   *         non-undoable operation (which may be null if no such operation exists)
   * 
   *         NOTE: Returning null is probably slightly harder to use than returning an operation
   *         that does nothing.
   */
  Pair<O, O> undoPlus();

}
