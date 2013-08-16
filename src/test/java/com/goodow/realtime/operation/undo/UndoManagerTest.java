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

import com.goodow.realtime.operation.AbstractOperation;
import com.goodow.realtime.operation.list.AbstractDeleteOperation;
import com.goodow.realtime.operation.list.AbstractInsertOperation;
import com.goodow.realtime.operation.list.string.StringDeleteOperation;
import com.goodow.realtime.operation.list.string.StringInsertOperation;

import junit.framework.TestCase;

import java.util.List;

public class UndoManagerTest extends TestCase {
  private static AbstractDeleteOperation<String> delete(int location, String text) {
    return new StringDeleteOperation(null, location, text);
  }

  private static AbstractInsertOperation<String> insert(int location, String text) {
    return new StringInsertOperation(null, location, text);
  }

  UndoManagerPlus<AbstractOperation<?>> undoManager = UndoManagerFactory.createUndoManager();

  public void testUndoRedo() {
    undoManager.checkpoint();
    undoManager.undoableOp(insert(3, "a"));
    undoManager.checkpoint();
    undoManager.undoableOp(insert(5, "a"));
    undoManager.checkpoint();
    undoManager.undoableOp(insert(4, "a"));
    equal(undoManager.undo(), delete(4, "a"));
    equal(undoManager.undo(), delete(5, "a"));
    equal(undoManager.undo(), delete(3, "a"));
    equal(undoManager.redo(), insert(3, "a"));
    equal(undoManager.redo(), insert(5, "a"));
    equal(undoManager.redo(), insert(4, "a"));
    equal(undoManager.undo(), delete(4, "a"));
    equal(undoManager.undo(), delete(5, "a"));
    equal(undoManager.undo(), delete(3, "a"));
  }

  public void testUndoRedoInterspersedWithNonundoableOps() {
    undoManager.checkpoint();
    undoManager.undoableOp(insert(3, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    undoManager.checkpoint();
    undoManager.undoableOp(insert(5, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    undoManager.checkpoint();
    undoManager.undoableOp(insert(4, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.undo(), delete(5, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.undo(), delete(8, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.undo(), delete(8, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.redo(), insert(9, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.redo(), insert(11, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.redo(), insert(10, "a"));
  }

  public void testUndoRedoWithConsecutiveNonundoableOps() {
    undoManager.checkpoint();
    undoManager.undoableOp(insert(3, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    undoManager.nonUndoableOp(insert(2, "a"));
    undoManager.checkpoint();
    undoManager.undoableOp(insert(6, "a"));
    undoManager.nonUndoableOp(delete(10, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    undoManager.nonUndoableOp(insert(10, "a"));
    undoManager.checkpoint();
    undoManager.undoableOp(insert(5, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.undo(), delete(6, "a"));
    equal(undoManager.undo(), delete(8, "a"));
    equal(undoManager.undo(), delete(7, "a"));
    equal(undoManager.redo(), insert(7, "a"));
    equal(undoManager.redo(), insert(8, "a"));
    equal(undoManager.redo(), insert(6, "a"));
  }

  public void testUndoRedoWithNondenseCheckpointing() {
    undoManager.checkpoint();
    undoManager.undoableOp(insert(3, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    undoManager.checkpoint();
    undoManager.undoableOp(insert(8, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    undoManager.undoableOp(insert(2, "a"));
    undoManager.nonUndoableOp(insert(10, "a"));
    undoManager.undoableOp(insert(6, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    undoManager.undoableOp(delete(13, "a"));
    undoManager.undoableOp(delete(3, "a"));
    undoManager.checkpoint();
    undoManager.undoableOp(insert(4, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.undo(), delete(5, "a"));
    // equal(undoManager.undo(), delete(7, "a"));
    // equal(undoManager.undo(), delete(7, "a"));
    // equal(undoManager.redo(), insert(7, "a"));
    // equal(undoManager.redo(), insert(7, "a"));
    // equal(undoManager.redo(), insert(5, "a"));
  }

  public void testUndoRedoWithNonundoableOps() {
    undoManager.checkpoint();
    undoManager.undoableOp(insert(3, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    undoManager.checkpoint();
    undoManager.undoableOp(insert(5, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    undoManager.checkpoint();
    undoManager.undoableOp(insert(4, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.undo(), delete(5, "a"));
    equal(undoManager.undo(), delete(7, "a"));
    equal(undoManager.undo(), delete(6, "a"));
    equal(undoManager.redo(), insert(6, "a"));
    equal(undoManager.redo(), insert(7, "a"));
    equal(undoManager.redo(), insert(5, "a"));
  }

  void equal(List<AbstractOperation<?>> ops, AbstractOperation<?>... expected) {
    assertEquals(expected.length, ops.size());
    int i = 0;
    for (AbstractOperation<?> op : expected) {
      assertEquals(op, ops.get(i));
      assertEquals(op.invert(), ops.get(i).invert());
      i++;
    }
  }
}