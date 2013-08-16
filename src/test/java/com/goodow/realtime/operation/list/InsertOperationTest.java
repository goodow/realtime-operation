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
package com.goodow.realtime.operation.list;

import com.goodow.realtime.operation.AbstractOperation;
import com.goodow.realtime.operation.list.string.StringDeleteOperation;
import com.goodow.realtime.operation.list.string.StringInsertOperation;

import junit.framework.TestCase;

import elemental.json.Json;
import elemental.json.JsonArray;

public class InsertOperationTest extends TestCase {
  private AbstractInsertOperation<String> expected0;

  /**
   * Performs tests for transforming text insertions against text deletions.
   */
  public void testInsertVsDelete() {
    // A's insertion spatially before B's deletion
    AbstractInsertOperation<String> op1 = new StringInsertOperation(null, 2, "abc");
    AbstractDeleteOperation<String> op2 = new StringDeleteOperation(null, 3, "def");
    AbstractInsertOperation<String> transformedOp = op1.transformWith(op2, true)[0];
    assertSame(op1, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    assertSame(op1, transformedOp);

    // A's insertion spatially at the start of B's deletion
    op2 = new StringDeleteOperation(null, 2, "def");
    transformedOp = op1.transformWith(op2, true)[0];
    assertSame(op1, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    assertSame(op1, transformedOp);

    // A's insertion spatially inside B's deletion
    op2 = new StringDeleteOperation(null, 1, "def");
    transformedOp = op1.transformWith(op2, true)[0];
    expected0 = new StringInsertOperation(null, 1, "abc");
    equals(expected0, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    equals(expected0, transformedOp);

    // A's insertion spatially at the end of B's deletion
    op2 = new StringDeleteOperation(null, 0, "de");
    transformedOp = op1.transformWith(op2, true)[0];
    expected0 = new StringInsertOperation(null, 0, "abc");
    equals(expected0, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    equals(expected0, transformedOp);

    // A's insertion spatially after B's deletion
    op2 = new StringDeleteOperation(null, 0, "d");
    transformedOp = op1.transformWith(op2, true)[0];
    expected0 = new StringInsertOperation(null, 1, "abc");
    equals(expected0, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    equals(expected0, transformedOp);
  }

  /**
   * Performs tests for transforming text insertions against text insertions.
   */
  public void testInsertVsInsert() {
    // op1's insertion spatially before op2's insertion
    AbstractInsertOperation<String> op1 = new StringInsertOperation(null, 1, "abc");
    AbstractInsertOperation<String> op2 = new StringInsertOperation(null, 2, "def");
    AbstractInsertOperation<String> transformedOp = op1.transformWith(op2, true)[0];
    assertSame(op1, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    assertSame(op1, transformedOp);

    expected0 = new StringInsertOperation(null, 2 + 3, "def");
    transformedOp = op2.transformWith(op1, true)[0];
    equals(expected0, transformedOp);
    transformedOp = op2.transformWith(op1, false)[0];
    equals(expected0, transformedOp);

    // op1's insertion spatially at the same location as op2's insertion
    op2 = new StringInsertOperation(null, 1, "def");
    transformedOp = op1.transformWith(op2, true)[0];
    expected0 = new StringInsertOperation(null, 1 + 3, "abc");
    equals(expected0, transformedOp);

    transformedOp = op1.transformWith(op2, false)[0];
    assertSame(op1, transformedOp);
  }

  public void testParseFromJson() {
    AbstractInsertOperation<String> op = new StringInsertOperation(null, 2, "123");
    equals(op, StringInsertOperation.parse((JsonArray) Json.instance().parse(op.toString())));
    op = new StringInsertOperation("id", 0, "true");
    equals(op, StringInsertOperation.parse((JsonArray) Json.instance().parse(op.toString())));
  }

  public void testTransformIndexReference() {
    AbstractInsertOperation<String> op = new StringInsertOperation(null, 1, "abc");
    // cursor before AbstractInsertOperation's startIndex
    int transformIndex = op.transformIndexReference(0, true, true);
    assertEquals(0, transformIndex);
    transformIndex = op.transformIndexReference(0, false, true);
    assertEquals(0, transformIndex);

    // cursor at the same location as AbstractInsertOperation's startIndex
    transformIndex = op.transformIndexReference(1, true, true);
    assertEquals(1 + 3, transformIndex);
    transformIndex = op.transformIndexReference(1, false, true);
    assertEquals(1, transformIndex);

    // cursor after AbstractInsertOperation's startIndex
    transformIndex = op.transformIndexReference(2, true, true);
    assertEquals(2 + 3, transformIndex);
    transformIndex = op.transformIndexReference(2, false, true);
    assertEquals(2 + 3, transformIndex);
  }

  void equals(AbstractOperation<?> op0, AbstractOperation<?> op1) {
    assertEquals(op0, op1);
    assertEquals(op0.invert(), op1.invert());
  }
}