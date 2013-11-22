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

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.AbstractOperation;
import com.goodow.realtime.operation.list.string.StringInsertOperation;
import com.goodow.realtime.operation.list.string.StringReplaceOperation;

import junit.framework.TestCase;

public class DeleteOperationTest extends TestCase {
  private AbstractDeleteOperation<String> expected0;
  private AbstractDeleteOperation<String> expected1;

  /**
   * Performs tests for transforming text deletions against text deletions.
   */
  public void testDeleteVsDelete() {
    // A's deletion spatially before B's deletion
    AbstractDeleteOperation<String> op1 = new SimpleDeleteOperation<String>(null, 2, "234");
    AbstractDeleteOperation<String> op2 = new SimpleDeleteOperation<String>(null, 6, "6");
    AbstractDeleteOperation<String>[] transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    expected0 = new SimpleDeleteOperation<String>(null, 3, "6");
    equals(expected0, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    equals(expected0, transformedOp[0]);

    // A's deletion spatially adjacent to and before B's deletion
    op2 = new SimpleDeleteOperation<String>(null, 5, "56");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    expected0 = new SimpleDeleteOperation<String>(null, 2, "56");
    equals(expected0, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    equals(expected0, transformedOp[0]);

    // A's deletion overlaps B's deletion
    op2 = new SimpleDeleteOperation<String>(null, 3, "345");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new SimpleDeleteOperation<String>(null, 2, "2");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    expected0 = new SimpleDeleteOperation<String>(null, 2, "5");
    equals(expected0, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    equals(expected0, transformedOp[0]);

    // A's deletion a subset of B's deletion
    op2 = new SimpleDeleteOperation<String>(null, 1, "123456");
    transformedOp = op1.transformWith(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);

    transformedOp = op2.transformWith(op1, true);
    expected0 = new SimpleDeleteOperation<String>(null, 1, "156");
    equals(expected0, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    equals(expected0, transformedOp[0]);

    // A's deletion identical to B's deletion
    op2 = new SimpleDeleteOperation<String>(null, 2, "234");
    transformedOp = op1.transformWith(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);
  }

  /**
   * Performs tests for transforming text insertions against text deletions.
   */
  public void testDeleteVsInsert() {
    // A's insertion spatially before B's deletion
    AbstractDeleteOperation<String> op1 = new SimpleDeleteOperation<String>(null, 3, "abc");
    AbstractInsertOperation<String> op2 = new StringInsertOperation(null, 2, "de");
    AbstractDeleteOperation<String>[] transformedOp = op1.transformWith(op2, true);
    expected0 = new SimpleDeleteOperation<String>(null, 5, "abc");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's insertion spatially at the start of B's deletion
    op2 = new StringInsertOperation(null, 3, "de");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new SimpleDeleteOperation<String>(null, 5, "abc");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's insertion spatially inside B's deletion
    op2 = new StringInsertOperation(null, 4, "de");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new SimpleDeleteOperation<String>(null, 3, "a");
    expected1 = new SimpleDeleteOperation<String>(null, 5, "bc");
    equals(expected0, transformedOp[0]);
    equals(expected1, transformedOp[1]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);
    equals(expected1, transformedOp[1]);

    // A's insertion spatially at the end of B's deletion
    op2 = new StringInsertOperation(null, 6, "de");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    // A's insertion spatially after B's deletion
    op2 = new StringInsertOperation(null, 7, "de");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);
  }

  public void testDeleteVsReplace() {
    // A's replacement spatially before B's deletion
    AbstractDeleteOperation<String> op1 = new SimpleDeleteOperation<String>(null, 2, "234");
    StringReplaceOperation op2 = new StringReplaceOperation(null, 0, "0", "a");
    AbstractDeleteOperation<String>[] transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    // A's replacement spatially adjacent to and before B's deletion
    op2 = new StringReplaceOperation(null, 0, "01", "ab");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    // A's replacement overlaps B's deletion
    // ....[...]....
    // ...[..]....
    op2 = new StringReplaceOperation(null, 0, "012", "abc");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new SimpleDeleteOperation<String>(null, 2, "c34");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's replacement overlaps B's deletion
    // ....[...]....
    // ......[...]..
    op2 = new StringReplaceOperation(null, 3, "345", "abc");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new SimpleDeleteOperation<String>(null, 2, "2ab");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's replacement a subset of B's deletion
    op2 = new StringReplaceOperation(null, 3, "3", "a");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new SimpleDeleteOperation<String>(null, 2, "2a4");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's replacement a superset of B's deletion
    op2 = new StringReplaceOperation(null, 1, "123456", "abcdef");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new SimpleDeleteOperation<String>(null, 2, "bcd");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's replacement identical to B's deletion
    op2 = new StringReplaceOperation(null, 2, "234", "abc");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new SimpleDeleteOperation<String>(null, 2, "abc");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);
  }

  public void testParseFromJson() {
    AbstractDeleteOperation<String> op = new SimpleDeleteOperation<String>(null, 2, "123");
    assertEquals(op, SimpleDeleteOperation.parse(Json.<JsonArray> parse(op.toString())));
    op = new SimpleDeleteOperation<String>("id", 0, "true");
    assertEquals(op, SimpleDeleteOperation.parse(Json.<JsonArray> parse(op.toString())));
  }

  public void testTransformIndexReference() {
    AbstractDeleteOperation<String> op = new SimpleDeleteOperation<String>(null, 1, "abc");
    int transformIndex = op.transformIndexReference(0, true, true);
    assertEquals(0, transformIndex);
    transformIndex = op.transformIndexReference(0, true, false);
    assertEquals(0, transformIndex);

    transformIndex = op.transformIndexReference(1, true, true);
    assertEquals(-1, transformIndex);
    transformIndex = op.transformIndexReference(1, true, false);
    assertEquals(1, transformIndex);

    transformIndex = op.transformIndexReference(3, true, true);
    assertEquals(-1, transformIndex);
    transformIndex = op.transformIndexReference(3, true, false);
    assertEquals(1, transformIndex);

    transformIndex = op.transformIndexReference(4, true, true);
    assertEquals(1, transformIndex);
    transformIndex = op.transformIndexReference(4, true, false);
    assertEquals(1, transformIndex);
  }

  void equals(AbstractOperation<?> op0, AbstractOperation<?> op1) {
    assertEquals(op0, op1);
    assertEquals(op0.invert(), op1.invert());
  }
}
