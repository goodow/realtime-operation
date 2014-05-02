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
import com.goodow.realtime.operation.impl.AbstractComponent;
import com.goodow.realtime.operation.list.string.StringDeleteComponent;
import com.goodow.realtime.operation.list.string.StringInsertComponent;
import com.goodow.realtime.operation.list.string.StringReplaceComponent;

import junit.framework.TestCase;

public class ReplaceTest extends TestCase {
  private AbstractComponent<?> expected0;
  private AbstractComponent<?> expected1;

  public void testParseFromJson() {
    AbstractReplaceComponent<String> op = new StringReplaceComponent(null, 2, "abc", "123");
    assertEquals(op, StringReplaceComponent.parse(Json.<JsonArray> parse(op.toString())));
    op = new StringReplaceComponent("id", 0, "abcd", "true");
    assertEquals(op, StringReplaceComponent.parse(Json.<JsonArray> parse(op.toString())));
  }

  public void testReplaceVsDelete() {
    // A's deletion spatially before B's replacement
    AbstractReplaceComponent<String> op1 = new StringReplaceComponent(null, 2, "abc", "234");
    AbstractDeleteComponent<String> op2 = new StringDeleteComponent(null, 0, "0");
    Operation<ListTarget<String>> transformedOp = op1.transform(op2, true);
    expected0 = new StringReplaceComponent(null, 1, "abc", "234");
    equals(expected0, transformedOp);
    transformedOp = op1.transform(op2, false);
    equals(expected0, transformedOp);

    // A's deletion spatially adjacent to and before B's replacement
    op2 = new StringDeleteComponent(null, 0, "01");
    transformedOp = op1.transform(op2, true);
    expected0 = new StringReplaceComponent(null, 0, "abc", "234");
    equals(expected0, transformedOp);
    transformedOp = op1.transform(op2, false);
    equals(expected0, transformedOp);

    // A's deletion overlaps B's replacement
    op2 = new StringDeleteComponent(null, 1, "12");
    transformedOp = op1.transform(op2, true);
    expected0 = new StringReplaceComponent(null, 1, "bc", "34");
    equals(expected0, transformedOp);
    transformedOp = op1.transform(op2, false);
    equals(expected0, transformedOp);

    // A's deletion overlaps B's replacement
    op2 = new StringDeleteComponent(null, 3, "3456");
    transformedOp = op1.transform(op2, true);
    expected0 = new StringReplaceComponent(null, 2, "a", "2");
    equals(expected0, transformedOp);
    transformedOp = op1.transform(op2, false);
    equals(expected0, transformedOp);

    // A's deletion a subset of B's replacement
    op2 = new StringDeleteComponent(null, 3, "3");
    transformedOp = op1.transform(op2, true);
    expected0 = new StringReplaceComponent(null, 2, "ac", "24");
    equals(expected0, transformedOp);
    transformedOp = op1.transform(op2, false);
    equals(expected0, transformedOp);

    // A's deletion identical to B's replacement
    op2 = new StringDeleteComponent(null, 2, "234");
    transformedOp = op1.transform(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transform(op2, false);
    assertNull(transformedOp);
  }

  public void testReplaceVsInsert() {
    // A's insertion spatially before B's replacement
    AbstractReplaceComponent<String> op1 = new StringReplaceComponent(null, 3, "abc", "345");
    AbstractInsertComponent<String> op2 = new StringInsertComponent(null, 2, "de");
    Operation[] transformedOp = new Operation[2];
    transformedOp[0] = op1.transform(op2, true);
    expected0 = new StringReplaceComponent(null, 5, "abc", "345");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    equals(expected0, transformedOp[0]);

    // A's insertion spatially at the start of B's replacement
    op2 = new StringInsertComponent(null, 3, "de");
    transformedOp[0] = op1.transform(op2, true);
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    equals(expected0, transformedOp[0]);

    // A's insertion spatially inside B's replacement
    op2 = new StringInsertComponent(null, 4, "de");
    transformedOp = op1.transformComponent(op2, true);
    expected0 = new StringReplaceComponent(null, 3, "a", "3");
    expected1 = new StringReplaceComponent(null, 6, "bc", "45");
    equals(expected0, transformedOp[0]);
    equals(expected1, transformedOp[1]);
    transformedOp = op1.transformComponent(op2, false);
    equals(expected0, transformedOp[0]);
    equals(expected1, transformedOp[1]);

    // A's insertion spatially at the end of B's replacement
    op2 = new StringInsertComponent(null, 6, "de");
    transformedOp[0] = op1.transform(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    assertSame(op1, transformedOp[0]);

    // A's insertion spatially after B's replacement
    op2 = new StringInsertComponent(null, 7, "de");
    transformedOp[0] = op1.transform(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    assertSame(op1, transformedOp[0]);
  }

  public void testReplaceVsReplace() {
    // A's replacement spatially before B's replacement
    AbstractReplaceComponent<String> op1 = new StringReplaceComponent(null, 2, "abc", "234");
    AbstractReplaceComponent<String> op2 = new StringReplaceComponent(null, 6, "a", "6");
    Operation[] transformedOp = new Operation[2];
    transformedOp[0] = op1.transform(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp[0] = op2.transform(op1, true);
    assertSame(op2, transformedOp[0]);
    transformedOp[0] = op2.transform(op1, false);
    assertSame(op2, transformedOp[0]);

    // A's replacement spatially adjacent to and before B's replacement
    op2 = new StringReplaceComponent(null, 5, "ab", "56");
    transformedOp[0] = op1.transform(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp[0] = op2.transform(op1, true);
    assertSame(op2, transformedOp[0]);
    transformedOp[0] = op2.transform(op1, false);
    assertSame(op2, transformedOp[0]);

    // A's replacement overlaps B's replacement
    op2 = new StringReplaceComponent(null, 3, "abcd", "3456");
    transformedOp[0] = op1.transform(op2, false);
    expected0 = new StringReplaceComponent(null, 2, "a34", "234");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, true);
    expected0 = new StringReplaceComponent(null, 2, "a", "2");
    equals(expected0, transformedOp[0]);

    transformedOp[0] = op2.transform(op1, false);
    expected0 = new StringReplaceComponent(null, 3, "34cd", "3456");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op2.transform(op1, true);
    expected0 = new StringReplaceComponent(null, 5, "cd", "56");
    equals(expected0, transformedOp[0]);

    // A's replacement a subset of B's replacement
    op2 = new StringReplaceComponent(null, 1, "abcdef", "123456");
    transformedOp[0] = op1.transform(op2, false);
    expected0 = new StringReplaceComponent(null, 2, "234", "234");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, true);
    assertNull(transformedOp[0]);

    transformedOp[0] = op2.transform(op1, false);
    expected0 = new StringReplaceComponent(null, 1, "a234ef", "123456");
    equals(expected0, transformedOp[0]);
    transformedOp = op2.transformComponent(op1, true);
    expected0 = new StringReplaceComponent(null, 1, "a", "1");
    expected1 = new StringReplaceComponent(null, 5, "ef", "56");
    equals(expected0, transformedOp[0]);
    equals(expected1, transformedOp[1]);

    // A's replacement identical to B's replacement
    op2 = new StringReplaceComponent(null, 2, "abc", "234");
    transformedOp[0] = op1.transform(op2, false);
    expected0 = new StringReplaceComponent(null, 2, "234", "234");
    equals(expected0, transformedOp[0]);
    transformedOp[0] = op1.transform(op2, true);
    assertNull(transformedOp[0]);
  }

  void equals(Operation<?> op0, Operation<?> op1) {
    assertEquals(op0, op1);
    assertEquals(op0.invert(), op1.invert());
  }
}
