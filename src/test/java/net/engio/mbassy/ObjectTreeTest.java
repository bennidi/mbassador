/*
 * Copyright 2015 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.engio.mbassy;

import net.engio.mbassy.common.AssertSupport;
import net.engio.mbassy.common.ObjectTree;

import org.junit.Test;

public class ObjectTreeTest extends AssertSupport {

    public void test(ObjectTree<Class<?>, String> tree, String string, Class<?> clazz) {
        tree.put(string, clazz);
        assertEquals(string, tree.get(clazz));
    }

    public void test(ObjectTree<Class<?>, String> tree, String string, Class<?> clazz1, Class<?> clazz2) {
        tree.put(string, clazz1, clazz2);
        assertEquals(string, tree.get(clazz1, clazz2));
    }

    public void test(ObjectTree<Class<?>, String> tree, String string, Class<?> clazz1, Class<?> clazz2, Class<?> clazz3) {
        tree.put(string, clazz1, clazz2, clazz3);
        assertEquals(string, tree.get(clazz1, clazz2, clazz3));
    }

    public void test(ObjectTree<Class<?>, String> tree, String string, Class<?>... clazzes) {
        tree.put(string, clazzes);
        assertEquals(string, tree.get(clazzes));
    }

    @Test
    public void testObjectTree() {
        ObjectTree<Class<?>, String> tree = new ObjectTree<Class<?>, String>();

        test(tree, "s", String.class);
        test(tree, "x", String.class);
        test(tree, "o", Object.class);

        test(tree, "ss", String.class, String.class);
        test(tree, "oo", Object.class, Object.class);

        test(tree, "sss", String.class, String.class, String.class);
        test(tree, "xo", Object.class, Object.class);

        test(tree, "ssss", String.class, String.class, String.class, String.class);
        test(tree, "oosif", Object.class, Object.class, String.class, Integer.class, Float.class);
    }
}
