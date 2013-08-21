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

import com.goodow.realtime.operation.Operation;
import com.goodow.realtime.operation.RealtimeOperation;
import com.goodow.realtime.operation.list.string.StringDeleteOperation;
import com.goodow.realtime.operation.list.string.StringInsertOperation;
import com.goodow.realtime.operation.util.Pair;

import junit.framework.TestCase;

import java.util.List;

public class UndoManagerTest extends TestCase {
  private static RealtimeOperation delete(int startIndex, String text) {
    return new RealtimeOperation("userId", null, new StringDeleteOperation(null, startIndex, text));
  }

  private static RealtimeOperation delete(String id, int startIndex) {
    return new RealtimeOperation("userId", null, new StringDeleteOperation(id, startIndex, "a"));
  }

  private static RealtimeOperation insert(int startIndex, String text) {
    return new RealtimeOperation("userId", null, new StringInsertOperation(null, startIndex, text));
  }

  private static RealtimeOperation insert(String id, int startIndex) {
    return new RealtimeOperation("userId", null, new StringInsertOperation(id, startIndex, "a"));
  }

  UndoManagerPlus<RealtimeOperation> undoManager = UndoManagerFactory.createUndoManager();

  public void testPlusMethods() {
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
    Pair<List<RealtimeOperation>, List<RealtimeOperation>> pair = undoManager.undoPlus();
    equal(pair.first, delete(5, "a"));
    equal(pair.second, insert(1, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    pair = undoManager.undoPlus();
    // equal(pair.first, delete(8, "a"));

    equal(pair.second, insert(1, "a"), insert(9, "a"), insert(1, "a"), insert(1, "a"), insert(1,
        "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    pair = undoManager.undoPlus();
    equal(pair.first, delete(9, "a"));
    equal(pair.second, insert(1, "a"), insert(1, "a"), insert(8, "a"), insert(1, "a"), insert(1,
        "a"), insert(1, "a"), insert(1, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    pair = undoManager.redoPlus();
    equal(pair.first, insert(10, "a"));
    equal(pair.second, insert(1, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    pair = undoManager.redoPlus();
    // equal(pair.first, insert(11, "a"));

    equal(pair.second, insert(1, "a"), insert(1, "a"), insert(1, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    pair = undoManager.redoPlus();
    equal(pair.first, insert(10, "a"));
    equal(pair.second, insert(1, "a"), insert(1, "a"), insert(1, "a"), insert(1, "a"), insert(1,
        "a"));
  }

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

  public void testUndoRedoInvolvingMultipleObjects() {
    undoManager.checkpoint();
    undoManager.undoableOp(insert("i", 3));
    undoManager.undoableOp(insert("j", 5));
    undoManager.nonUndoableOp(insert("i", 1));
    undoManager.nonUndoableOp(insert("j", 1));
    undoManager.nonUndoableOp(insert("k", 1));
    undoManager.checkpoint();
    undoManager.undoableOp(insert("i", 8));
    undoManager.undoableOp(insert("j", 5));
    undoManager.nonUndoableOp(insert("i", 1));
    undoManager.undoableOp(insert("i", 2));
    undoManager.nonUndoableOp(insert("i", 10));
    undoManager.undoableOp(insert("i", 6));
    undoManager.nonUndoableOp(insert("i", 1));
    undoManager.undoableOp(delete("i", 13));
    undoManager.undoableOp(delete("i", 3));
    undoManager.checkpoint();
    undoManager.undoableOp(insert("i", 4));
    undoManager.nonUndoableOp(insert("i", 1));
    equal(undoManager.undo(), delete("i", 5));
    undoManager.nonUndoableOp(insert("i", 1));
    // equal(undo, delete("i", 8), delete("j", 5));
    List<RealtimeOperation> undo = undoManager.undo();

    undoManager.nonUndoableOp(insert("i", 1));
    equal(undoManager.undo(), delete("j", 6), delete("i", 9));
    undoManager.nonUndoableOp(insert("i", 1));
    equal(undoManager.redo(), insert("i", 10), insert("j", 6));
    undoManager.nonUndoableOp(insert("i", 1));
    // equal(undoManager.redo(), insert("i", 11), insert("j", 5));
    List<RealtimeOperation> redo = undoManager.redo();

    undoManager.nonUndoableOp(insert("i", 1));
    equal(undoManager.redo(), insert("i", 10));
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
    List<RealtimeOperation> undo = undoManager.undo();

    equal(undoManager.undo(), delete(7, "a"));
    equal(undoManager.redo(), insert(7, "a"));
    // equal(undoManager.redo(), insert(7, "a"));
    List<RealtimeOperation> redo = undoManager.redo();

    equal(undoManager.redo(), insert(5, "a"));
  }

  public void testUndoRedoWithNondenseCheckpointsInterspersedWithNonundoableOps() {
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
    undoManager.nonUndoableOp(insert(1, "a"));
    // equal(undoManager.undo(), delete(8, "a"));
    List<RealtimeOperation> undo = undoManager.undo();

    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.undo(), delete(9, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.redo(), insert(10, "a"));
    undoManager.nonUndoableOp(insert(1, "a"));
    // equal(undoManager.redo(), insert(11, "a"));
    List<RealtimeOperation> redo = undoManager.redo();

    undoManager.nonUndoableOp(insert(1, "a"));
    equal(undoManager.redo(), insert(10, "a"));
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

  <T extends Operation<?>> void equal(List<T> ops, T... expected) {
    assertEquals(expected.length, ops.size());
    int i = 0;
    for (Operation<?> op : expected) {
      assertEquals(op, ops.get(i));
      assertEquals(op.invert(), ops.get(i).invert());
      i++;
    }
  }
}