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
package net.engio.mbassy.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.googlecode.concurentlocks.ReadWriteUpdateLock;
import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock;


/**
 * Simple tree structure that is a map that contains a chain of keys to get to a value.
 */
public class ObjectTree<KEY, VALUE> {
    private final ReadWriteUpdateLock lock = new ReentrantReadWriteUpdateLock();

    private Map<KEY, ObjectTree<KEY, VALUE>> children;
    private volatile VALUE value;

    public ObjectTree() {
    }

    public VALUE getValue() {
        Lock readLock = this.lock.readLock();
        readLock.lock(); // allows other readers, blocks others from acquiring update or write locks
        VALUE returnValue = this.value;
        readLock.unlock();

        return returnValue;
    }

    public void putValue(VALUE value) {
        Lock WRITE = this.lock.writeLock();
        WRITE.lock();  // upgrade to the write lock, at this point blocks other readers

        this.value = value;

        WRITE.unlock();
    }


    /**
     * BACKWARDS, because our signature must allow for N keys...
     */
    public void put(VALUE value, KEY key) {
        // have to put value into our children
        createLeaf(key, value, true);
    }

    /**
     * BACKWARDS, because our signature must allow for N keys...
     */
    public void put(VALUE value, KEY key1, KEY key2) {
        // have to put value into our children
        ObjectTree<KEY, VALUE> leaf = createLeaf(key1, value, false);
        if (leaf != null) {
            leaf.createLeaf(key2, value, true);
        }
    }

    /**
     * BACKWARDS, because our signature must allow for N keys...
     */
    public void put(VALUE value, KEY key1, KEY key2, KEY key3) {
        // have to put value into our children
        ObjectTree<KEY, VALUE> leaf = createLeaf(key1, value, false);
        if (leaf != null) {
            leaf = leaf.createLeaf(key2, value, false);
        }
        if (leaf != null) {
            leaf.createLeaf(key3, value, true);
        }
    }

    /**
     * BACKWARDS, because our signature must allow for N keys...
     */
    @SuppressWarnings("unchecked")
    public void put(VALUE value, KEY... keys) {
        if (keys == null) {
            return;
        }

        int length = keys.length;
        int length_1 = length - 1;
        boolean setFirstValue = length == 1;

        // have to put value into our children
        ObjectTree<KEY, VALUE> leaf = createLeaf(keys[0], value, setFirstValue);
        for (int i=1;i<length;i++) {
            if (leaf != null) {
                leaf = leaf.createLeaf(keys[i], value, i == length_1);
            }
        }
    }

    /**
     * BACKWARDS, because our signature must allow for N keys...
     */
    @SuppressWarnings("unchecked")
    public ObjectTree<KEY, VALUE> createLeaves(KEY... keys) {
        if (keys == null) {
            return this;
        }
        int length = keys.length;

        // have to put value into our children
        ObjectTree<KEY, VALUE> leaf = createLeaf(keys[0], null, false);
        for (int i=1;i<length;i++) {
            if (leaf != null) {
                leaf = leaf.createLeaf(keys[i], null, false);
            }
        }

        return leaf;
    }


    public final ObjectTree<KEY, VALUE> createLeaf(KEY key, VALUE value, boolean setValue) {
        if (key == null) {
            return null;
        }

        ObjectTree<KEY, VALUE> objectTree;

        Lock UPDATE = this.lock.updateLock();
        UPDATE.lock(); // allows other readers, blocks others from acquiring update or write locks

        if (this.children == null) {
            // promote to writelock and try again - Concurrency in Practice,16.4.2, last sentence on page. Careful for stale state
            Lock WRITE = this.lock.writeLock();
            WRITE.lock();  // upgrade to the write lock, at this point blocks other readers

            if (this.children == null) {
                this.children = new HashMap<KEY, ObjectTree<KEY, VALUE>>(2);

                // might as well add too
                objectTree = new ObjectTree<KEY, VALUE>();
                if (setValue) {
                    objectTree.value = value;
                }

                this.children.put(key, objectTree);
            } else {
                objectTree = this.children.get(key);

                if (setValue) {
                    objectTree.value = value;
                }
            }

            WRITE.unlock();
        } else {
            objectTree = this.children.get(key);

            // promote to writelock and try again - Concurrency in Practice,16.4.2, last sentence on page. Careful for stale state
            Lock WRITE = this.lock.writeLock();
            // make sure we have a tree for the specified node
            if (objectTree == null) {
                WRITE.lock();  // upgrade to the write lock, at this point blocks other readers

                objectTree = this.children.get(key);
                if (objectTree == null) {
                    objectTree = new ObjectTree<KEY, VALUE>();
                    if (setValue) {
                        objectTree.value = value;
                    }

                    this.children.put(key, objectTree);
                }

                WRITE.unlock(); // downgrade back to update lock
            } else if (setValue) {
                WRITE.lock();  // upgrade to the write lock, at this point blocks other readers

                objectTree.value = value;

                WRITE.unlock(); // downgrade back to update lock
            }
        }
        UPDATE.unlock();

        return objectTree;
    }


    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////


    public VALUE get(KEY key) {
        ObjectTree<KEY, VALUE> objectTree = null;
        // get value from our children
        objectTree = getLeaf(key);

        if (objectTree == null) {
            return null;
        }

        Lock readLock = objectTree.lock.readLock();
        readLock.lock(); // allows other readers, blocks others from acquiring update or write locks
        VALUE returnValue = objectTree.value;
        readLock.unlock();

        return returnValue;
    }

    public VALUE get(KEY key1, KEY key2) {
        ObjectTree<KEY, VALUE> tree = null;
        // get value from our children
        tree = getLeaf(key1);
        if (tree != null) {
            tree = tree.getLeaf(key2);
        }

        if (tree == null) {
            return null;
        }

        Lock readLock = tree.lock.readLock();
        readLock.lock(); // allows other readers, blocks others from acquiring update or write locks
        VALUE returnValue = tree.value;
        readLock.unlock();

        return returnValue;
    }

    public VALUE get(KEY key1, KEY key2, KEY key3) {
        ObjectTree<KEY, VALUE> tree = null;
        // get value from our children
        tree = getLeaf(key1);
        if (tree != null) {
            tree = tree.getLeaf(key2);
        }
        if (tree != null) {
            tree = tree.getLeaf(key3);
        }

        if (tree == null) {
            return null;
        }

        Lock readLock = tree.lock.readLock();
        readLock.lock(); // allows other readers, blocks others from acquiring update or write locks
        VALUE returnValue = tree.value;
        readLock.unlock();

        return returnValue;
    }

    @SuppressWarnings("unchecked")
    public VALUE get(KEY... keys) {
        ObjectTree<KEY, VALUE> tree = null;
        // get value from our children
        tree = getLeaf(keys[0]);

        int size = keys.length;
        for (int i=1;i<size;i++) {
            if (tree != null) {
                tree = tree.getLeaf(keys[i]);
            } else {
                return null;
            }
        }

        if (tree == null) {
            return null;
        }

        Lock readLock = tree.lock.readLock();
        readLock.lock(); // allows other readers, blocks others from acquiring update or write locks
        VALUE returnValue = tree.value;
        readLock.unlock();

        return returnValue;
    }

    public final ObjectTree<KEY, VALUE> getLeaf(KEY key) {
        if (key == null) {
            return null;
        }

        ObjectTree<KEY, VALUE> tree;

        Lock READ = this.lock.readLock();
        READ.lock(); // allows other readers, blocks others from acquiring update or write locks

        if (this.children == null) {
            tree = null;
        } else {
            tree = this.children.get(key);
        }

        READ.unlock();

        return tree;
    }
}