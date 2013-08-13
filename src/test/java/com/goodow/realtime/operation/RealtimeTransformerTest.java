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
package com.goodow.realtime.operation;

import com.goodow.realtime.operation.list.DeleteOperation;
import com.goodow.realtime.operation.list.InsertOperation;
import com.goodow.realtime.operation.list.ReplaceOperation;
import com.goodow.realtime.operation.util.Pair;

import junit.framework.TestCase;

import elemental.util.ArrayOf;
import elemental.util.Collections;

public class RealtimeTransformerTest extends TestCase {
  RealtimeTransformer transformer = new RealtimeTransformer();

  public void testTransformClientOpToMultipleOps() {
    InsertOperation<String> serverOp0 = new InsertOperation<String>(null, 3, "ab");
    ReplaceOperation<String> serverOp1 = new ReplaceOperation<String>(null, 3, "abc", "def");
    ReplaceOperation<String> clientOp0 = new ReplaceOperation<String>(null, 2, "abc", "234");
    DeleteOperation<String> clientOp1 = new DeleteOperation<String>(null, 2, "234");
    ArrayOf<AbstractOperation<?>> serverOps = Collections.arrayOf();
    ArrayOf<AbstractOperation<?>> clientOps = Collections.arrayOf();
    serverOps.push(serverOp0);
    serverOps.push(serverOp1);
    clientOps.push(clientOp0);
    clientOps.push(clientOp1);
    Pair<ArrayOf<AbstractOperation<?>>, ArrayOf<AbstractOperation<?>>> pair =
        transformer.transform(serverOps, clientOps);
    AbstractOperation<?> expected;

    assertEquals(4, pair.second.length());
    expected = new ReplaceOperation<String>(null, 2, "a", "2");
    assertEquals(expected, pair.second.get(0));
    expected = new ReplaceOperation<String>(null, 5, "ab", "34");
    assertEquals(expected, pair.second.get(1));
    expected = new DeleteOperation<String>(null, 2, "2");
    assertEquals(expected, pair.second.get(2));
    expected = new DeleteOperation<String>(null, 4, "34");
    assertEquals(expected, pair.second.get(3));

    assertEquals(2, pair.first.length());
    expected = new InsertOperation<String>(null, 2, "ab");
    assertEquals(expected, pair.first.get(0));
    expected = new ReplaceOperation<String>(null, 2, "ab", "de");
    assertEquals(expected, pair.first.get(1));
  }

  public void testTransformServerOpToMultipleOps() {
    ReplaceOperation<String> serverOp0 = new ReplaceOperation<String>(null, 2, "abc", "234");
    DeleteOperation<String> serverOp1 = new DeleteOperation<String>(null, 2, "234");
    InsertOperation<String> clientOp0 = new InsertOperation<String>(null, 3, "ab");
    ReplaceOperation<String> clientOp1 = new ReplaceOperation<String>(null, 3, "abc", "def");
    ArrayOf<AbstractOperation<?>> serverOps = Collections.arrayOf();
    ArrayOf<AbstractOperation<?>> clientOps = Collections.arrayOf();
    serverOps.push(serverOp0);
    serverOps.push(serverOp1);
    clientOps.push(clientOp0);
    clientOps.push(clientOp1);
    Pair<ArrayOf<AbstractOperation<?>>, ArrayOf<AbstractOperation<?>>> pair =
        transformer.transform(serverOps, clientOps);
    AbstractOperation<?> expected;

    assertEquals(2, pair.second.length());
    expected = new InsertOperation<String>(null, 2, "ab");
    assertEquals(expected, pair.second.get(0));
    expected = new ReplaceOperation<String>(null, 2, "ab", "de");
    assertEquals(expected, pair.second.get(1));

    assertEquals(4, pair.first.length());
    expected = new ReplaceOperation<String>(null, 2, "a", "2");
    assertEquals(expected, pair.first.get(0));
    expected = new ReplaceOperation<String>(null, 6, "a", "4");
    assertEquals(expected, pair.first.get(1));
    expected = new DeleteOperation<String>(null, 2, "2");
    assertEquals(expected, pair.first.get(2));
    expected = new DeleteOperation<String>(null, 4, "34");
    assertEquals(expected, pair.first.get(3));

  }

  public void testTransformServerOpToNoOp() {

  }

}
