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
import com.goodow.realtime.operation.Operation;
import com.goodow.realtime.operation.list.string.StringInsertComponent;
import com.goodow.realtime.operation.list.string.StringReplaceComponent;

import junit.framework.TestCase;

public class DeleteTest extends TestCase {
  private AbstractDeleteComponent<String> expected0;
  private AbstractDeleteComponent<String> expected1;

  /**
   * Performs tests for transforming text deletions against text deletions.
   */
  public void testDeleteVsDelete() {
    // A's deletion spatially before B's deletion
    AbstractDeleteComponent<String> op1 = new SimpleDeleteComponent<String>(null, 2, "234");
    AbstractDeleteComponent<String> op2 = new SimpleDeleteComponent<String>(null, 6, "6");
    Operation<ListTarget<String>> transformedOp = op1.transform(op2, true);
    assertSame(op1, transformedOp);
    transformedOp = op1.transform(op2, false);
    assertSame(op1, transformedOp);

    transformedOp = op2.transform(op1, true);
    expected0 = new SimpleDeleteComponent<String>(null, 3, "6");
    equals(expected0, transformedOp);
    transformedOp = op2.transform(op1, false);
    equals(expected0, transformedOp);

    // A's deletion spatially adjacent to and before B's deletion
    op2 = new SimpleDeleteComponent<String>(null, 5, "56");
    transformedOp = op1.transform(op2, true);
    assertSame(op1, transformedOp);
    transformedOp = op1.transform(op2, false);
    assertSame(op1, transformedOp);

    transformedOp = op2.transform(op1, true);
    expected0 = new SimpleDeleteComponent<String>(null, 2, "56");
    equals(expected0, transformedOp);
    transformedOp = op2.transform(op1, false);
    equals(expected0, transformedOp);

    // A's deletion overlaps B's deletion
    op2 = new SimpleDeleteComponent<String>(null, 3, "345");
    transformedOp = op1.transform(op2, true);
    expected0 = new SimpleDeleteComponent<String>(null, 2, "2");
    equals(expected0, transformedOp);
    transformedOp = op1.transform(op2, false);
    equals(expected0, transformedOp);

    transformedOp = op2.transform(op1, true);
    expected0 = new SimpleDeleteComponent<String>(null, 2, "5");
    equals(expected0, transformedOp);
    transformedOp = op2.transform(op1, false);
    equals(expected0, transformedOp);

    // A's deletion a subset of B's deletion
    op2 = new SimpleDeleteComponent<String>(null, 1, "123456");
    transformedOp = op1.transform(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transform(op2, false);
    assertNull(transformedOp);

    transformedOp = op2.transform(op1, true);
    expected0 = new SimpleDeleteComponent<String>(null, 1, "156");
    equals(expected0, transformedOp);
    transformedOp = op2.transform(op1, false);
    equals(expected0, transformedOp);

    // A's deletion identical to B's deletion
    op2 = new SimpleDeleteComponent<String>(null, 2, "234");
    transformedOp = op1.transform(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transform(op2, false);
    assertNull(transformedOp);
  }

  /**
   * Performs tests for transforming text insertions against text deletions.
   */
  public void testDeleteVsInsert() {
    // A's insertion spatially before B's deletion
    AbstractDeleteComponent<String> op1 = new SimpleDeleteComponent<String>(null, 3, "abc");
    AbstractInsertComponent<String> op2 = new StringInsertComponent(null, 2, "de");
    Operation[] transformedOp = new Operation[2];
    transformedOp[0] = op1.transform(op2, true);
    expected0 = new SimpleDeleteComponent<String>(null, 5, "abc");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    equals(expected0, transformedOp[0]);

    // A's insertion spatially at the start of B's deletion
    op2 = new StringInsertComponent(null, 3, "de");
    transformedOp[0] = op1.transform(op2, true);
    expected0 = new SimpleDeleteComponent<String>(null, 5, "abc");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    equals(expected0, transformedOp[0]);

    // A's insertion spatially inside B's deletion
    op2 = new StringInsertComponent(null, 4, "de");
    transformedOp = op1.transformComponent(op2, true);
    expected0 = new SimpleDeleteComponent<String>(null, 3, "a");
    expected1 = new SimpleDeleteComponent<String>(null, 5, "bc");
    equals(expected0, transformedOp[0]);
    equals(expected1, transformedOp[1]);
    transformedOp = op1.transformComponent(op2, false);
    equals(expected0, transformedOp[0]);
    equals(expected1, transformedOp[1]);

    // A's insertion spatially at the end of B's deletion
    op2 = new StringInsertComponent(null, 6, "de");
    transformedOp[0] = op1.transform(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    assertSame(op1, transformedOp[0]);

    // A's insertion spatially after B's deletion
    op2 = new StringInsertComponent(null, 7, "de");
    transformedOp[0] = op1.transform(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    assertSame(op1, transformedOp[0]);
  }

  public void testDeleteVsReplace() {
    // A's replacement spatially before B's deletion
    AbstractDeleteComponent<String> op1 = new SimpleDeleteComponent<String>(null, 2, "234");
    StringReplaceComponent op2 = new StringReplaceComponent(null, 0, "0", "a");
    Operation[] transformedOp = new Operation[2];
    transformedOp[0] = op1.transform(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    assertSame(op1, transformedOp[0]);

    // A's replacement spatially adjacent to and before B's deletion
    op2 = new StringReplaceComponent(null, 0, "01", "ab");
    transformedOp[0] = op1.transform(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    assertSame(op1, transformedOp[0]);

    // A's replacement overlaps B's deletion
    // ....[...]....
    // ...[..]....
    op2 = new StringReplaceComponent(null, 0, "012", "abc");
    transformedOp[0] = op1.transform(op2, true);
    expected0 = new SimpleDeleteComponent<String>(null, 2, "c34");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    equals(expected0, transformedOp[0]);

    // A's replacement overlaps B's deletion
    // ....[...]....
    // ......[...]..
    op2 = new StringReplaceComponent(null, 3, "345", "abc");
    transformedOp[0] = op1.transform(op2, true);
    expected0 = new SimpleDeleteComponent<String>(null, 2, "2ab");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    equals(expected0, transformedOp[0]);

    // A's replacement a subset of B's deletion
    op2 = new StringReplaceComponent(null, 3, "3", "a");
    transformedOp[0] = op1.transform(op2, true);
    expected0 = new SimpleDeleteComponent<String>(null, 2, "2a4");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    equals(expected0, transformedOp[0]);

    // A's replacement a superset of B's deletion
    op2 = new StringReplaceComponent(null, 1, "123456", "abcdef");
    transformedOp[0] = op1.transform(op2, true);
    expected0 = new SimpleDeleteComponent<String>(null, 2, "bcd");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    equals(expected0, transformedOp[0]);

    // A's replacement identical to B's deletion
    op2 = new StringReplaceComponent(null, 2, "234", "abc");
    transformedOp[0] = op1.transform(op2, true);
    expected0 = new SimpleDeleteComponent<String>(null, 2, "abc");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    equals(expected0, transformedOp[0]);
  }

  public void testParseFromJson() {
    AbstractDeleteComponent<String> op = new SimpleDeleteComponent<String>(null, 2, "123");
    assertEquals(op, SimpleDeleteComponent.parse(Json.<JsonArray> parse(op.toString())));
    op = new SimpleDeleteComponent<String>("id", 0, "true");
    assertEquals(op, SimpleDeleteComponent.parse(Json.<JsonArray> parse(op.toString())));
  }

  public void testTransformIndexReference() {
    AbstractDeleteComponent<String> op = new SimpleDeleteComponent<String>(null, 1, "abc");
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

  void equals(Operation<?> op0, Operation<?> op1) {
    assertEquals(op0, op1);
    assertEquals(op0.invert(), op1.invert());
  }
}
