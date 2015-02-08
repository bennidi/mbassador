package net.engio.mbassy.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.googlecode.concurentlocks.ReadWriteUpdateLock;
import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock;


/**
 * Simple tree structure that is a map that contains a chain of keys to get to a value.
 *
 * @author dorkbox, llc
 *         Date: 2/2/15
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

    public void removeValue() {
        Lock WRITE = this.lock.writeLock();
        WRITE.lock();  // upgrade to the write lock, at this point blocks other readers

        this.value = null;

        WRITE.unlock();
    }

    /**
     * Removes a branch from the tree, and cleans up, if necessary
     */
    public void remove(KEY key) {
        if (key == null) {
            removeLeaf(key);
        }
    }

    /**
     * Removes a branch from the tree, and cleans up, if necessary
     */
    public void remove(KEY key1, KEY key2) {
        if (key1 == null || key2 == null) {
            return;
        }

        Lock UPDATE = this.lock.updateLock();
        UPDATE.lock(); // allows other readers, blocks others from acquiring update or write locks

        ObjectTree<KEY, VALUE> leaf = null;
        if (this.children != null) {
            leaf = this.children.get(key1);

            if (leaf != null) {
                // promote to writelock and try again - Concurrency in Practice,16.4.2, last sentence on page. Careful for stale state
                Lock WRITE = this.lock.writeLock();
                WRITE.lock();  // upgrade to the write lock, at this point blocks other readers

                leaf.removeLeaf(key2);
                this.children.remove(key1);

                if (this.children.isEmpty()) {
                    this.children = null;
                }
                WRITE.unlock();
            }
        }

        UPDATE.unlock();
    }

    /**
     * Removes a branch from the tree, and cleans up, if necessary
     */
    public void remove(KEY key1, KEY key2, KEY key3) {
        if (key1 == null || key2 == null) {
            return;
        }

        Lock UPDATE = this.lock.updateLock();
        UPDATE.lock(); // allows other readers, blocks others from acquiring update or write locks

        ObjectTree<KEY, VALUE> leaf = null;
        if (this.children != null) {
            leaf = this.children.get(key1);

            if (leaf != null) {
                // promote to writelock and try again - Concurrency in Practice,16.4.2, last sentence on page. Careful for stale state
                Lock WRITE = this.lock.writeLock();
                WRITE.lock();  // upgrade to the write lock, at this point blocks other readers

                leaf.remove(key2, key3);
                this.children.remove(key1);

                if (this.children.isEmpty()) {
                    this.children = null;
                }
                WRITE.unlock();
            }
        }

        UPDATE.unlock();
    }


    /**
     * Removes a branch from the tree, and cleans up, if necessary
     */
    @SuppressWarnings("unchecked")
    public void remove(KEY... keys) {
        if (keys == null) {
            return;
        }

        removeLeaf(0, keys);
    }

    /**
     * Removes a branch from the tree, and cleans up, if necessary
     */
    private final void removeLeaf(KEY key) {
        if (key != null) {
            // promote to writelock and try again - Concurrency in Practice,16.4.2, last sentence on page. Careful for stale state
            Lock WRITE = this.lock.writeLock();
            WRITE.lock();  // upgrade to the write lock, at this point blocks other readers

            if (this.children != null) {
                ObjectTree<KEY, VALUE> leaf = this.children.get(key);
                if (leaf != null) {
                    if (leaf.children == null && leaf.value == null) {
                        this.children.remove(key);
                    }

                    if (this.children.isEmpty()) {
                        this.children = null;
                    }
                }
            }
            WRITE.unlock();
        }
    }

    // keys CANNOT be null here!
    private final void removeLeaf(int index, KEY[] keys) {
        Lock UPDATE = this.lock.updateLock();
        UPDATE.lock(); // allows other readers, blocks others from acquiring update or write locks

        if (index == keys.length) {
            // promote to writelock and try again - Concurrency in Practice,16.4.2, last sentence on page. Careful for stale state
            Lock WRITE = this.lock.writeLock();
            WRITE.lock();  // upgrade to the write lock, at this point blocks other readers

            // we have reached the leaf to remove!
            this.value = null;
            this.children = null;

            WRITE.unlock();
        } else if (this.children != null) {
            // promote to writelock and try again - Concurrency in Practice,16.4.2, last sentence on page. Careful for stale state
            Lock WRITE = this.lock.writeLock();
            WRITE.lock();  // upgrade to the write lock, at this point blocks other readers

            if (this.children != null) {
                ObjectTree<KEY, VALUE> leaf = this.children.get(keys[index]);
                if (leaf != null) {
                    leaf.removeLeaf(index+1, keys);
                    if (leaf.children == null && leaf.value == null) {
                        this.children.remove(keys[index]);
                    }

                    if (this.children.isEmpty()) {
                        this.children = null;
                    }
                }
            }
            WRITE.unlock();
        }

        UPDATE.unlock();
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
    public ObjectTree<KEY, VALUE> createLeaf(KEY... keys) {
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

    public VALUE getValue(KEY key1, KEY key2) {
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

    public VALUE getValue(KEY key1, KEY key2, KEY key3) {
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
    public VALUE getValue(KEY... keys) {
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

    public final ObjectTree<KEY, VALUE> getLeaf(KEY key1, KEY key2) {
        ObjectTree<KEY, VALUE> tree = null;
        // get value from our children
        tree = getLeaf(key1);
        if (tree != null) {
            tree = tree.getLeaf(key2);
        }

        if (tree == null) {
            return null;
        }

        return tree;
    }

    public final ObjectTree<KEY, VALUE> getLeaf(KEY key1, KEY key2, KEY key3) {
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

        return tree;
    }

    @SuppressWarnings("unchecked")
    public final ObjectTree<KEY, VALUE> getLeaf(KEY... keys) {
        int size = keys.length;

        if (size == 0) {
            return null;
        }

        ObjectTree<KEY, VALUE> tree = null;
        // get value from our children
        tree = getLeaf(keys[0]);

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

        return tree;
    }
}