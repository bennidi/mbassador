package org.mbassy.common;


import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * This data structure is optimized for non-blocking reads even when write operations occur.
 * Running read iterators will not be affected by add operations since writes always insert at the head of the
 * structure. Remove operations can affect any running iterator such that a removed element that has not yet
 * been reached by the iterator will not appear in that iterator anymore.
 *
 * The structure uses weak references to the elements. Iterators automatically perform cleanups of
 * garbace collect objects during iteration.
 * No dedicated maintenance operations need to be called or run in background.
 *
 *
 * <p/>
 * @author bennidi
 * Date: 2/12/12
 */
public class ConcurrentLinkedBag<T> implements Iterable<T> {


	private WeakHashMap<T, ListEntry<T>> entries = new WeakHashMap<T, ListEntry<T>>(); // maintain a map of entries for O(log n) lookup

	private ListEntry<T> head; // reference to the first element

	public ConcurrentLinkedBag<T> add(T element) {
		if (element == null || entries.containsKey(element)) return this;
		synchronized (this) {
			insert(element);
		}
		return this;
	}

	private void insert(T element) {
		if(head == null){
			head = new ListEntry<T>(element);
		}
		else{
			head = new ListEntry<T>(element, head);
		}
		entries.put(element, head);
	}

	public ConcurrentLinkedBag<T> addAll(Iterable<T> elements) {
		for (T element : elements) {
			if (element == null || entries.containsKey(element)) return this;
			synchronized (this) {
				insert(element);
			}
		}
		return this;
	}

	public ConcurrentLinkedBag<T> remove(T element) {
		if (!entries.containsKey(element)) return this;
		synchronized (this) {
			ListEntry<T> listelement = entries.get(element);
			if(listelement != head){
				listelement.remove();
			}
			else{
				head = head.next();
			}
			entries.remove(element);
		}
		return this;
	}

	public Iterator<T> iterator() {
		return new Iterator<T>() {

			private ListEntry<T> current = head;

			public boolean hasNext() {
                if(current == null) return false;
                T value = current.getValue();
                if(value == null){    // auto-removal of orphan references
                    remove();
                    return hasNext();
                }
                else{
                    return true;
                }
			}

			public T next() {
				if(current == null) return null;
				T value = current.getValue();
				if(value == null){    // auto-removal of orphan references
					remove();
					return next();
				}
				else{
					current = current.next();
					return value;
				}
			}

			public void remove() {
				if(current == null)return;
				synchronized (ConcurrentLinkedBag.this){
				current.remove();
				current = current.next();}
			}
		};
	}


	public class ListEntry<T> {

		private WeakReference<T> value;

		private ListEntry<T> next;

		private ListEntry<T> predecessor;


		private ListEntry(T value) {
			this.value = new WeakReference<T>(value);
		}

		private ListEntry(T value, ListEntry<T> next) {
			this(value);
			this.next = next;
			next.predecessor = this;
		}

		public T getValue() {
			return value.get();
		}

		public void remove(){
			if(predecessor != null){
				predecessor.setNext(next());
			}
			else if(next() != null){
				next.predecessor = null;
			}
		}

		public void setNext(ListEntry<T> element) {
			this.next = element;
			if(element != null)element.predecessor = this;
		}

		public ListEntry<T> next() {
			return next;
		}

		public boolean hasNext() {
			return next() != null;
		}

	}
}
