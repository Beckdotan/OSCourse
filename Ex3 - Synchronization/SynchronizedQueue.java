// Dotan Beck - 313602641
/**
 * A synchronized bounded-size queue for multithreaded producer-consumer applications.
 * 
 * @param <T> Type of data items


 */
public class SynchronizedQueue<T> {
	public T[] buffer;
	private int producers;
	private int capacity;
	private int front;
	private int back;
	private int size;
	//static pthread_mutex_t my_lock = PTHREAD_MUTEX_INITIALIZER;
	// TODO: Add more private members here as necessary
	
	/**
	 * Constructor. Allocates a buffer (an array) with the given capacity and
	 * resets pointers and counters.
	 * @param capacity Buffer capacity
	 */
	@SuppressWarnings("unchecked")
	public SynchronizedQueue(int capacity) {
		this.buffer = (T[])(new Object[capacity]);
		this.producers = 0;
		this.capacity = capacity;
		this.front = 0;
		this.back = 0;
	}
	
	/**
	 * Dequeues the first item from the queue and returns it.
	 * If the queue is empty but producers are still registered to this queue, 
	 * this method blocks until some item is available.
	 * If the queue is empty and no more items are planned to be added to this 
	 * queue (because no producers are registered), this method returns null.
	 * 
	 * @return The first item, or null if there are no more items
	 * @see #registerProducer()
	 * @see #unregisterProducer()
	 */
	public T dequeue() {
		T item;

		//first check:
		if ( this.size == 0 && this.producers == 0){
			return null;
		}

		//if there is atlist one item in the queue
		synchronized (this) {

			try {
				if (this.size == 0 && this.producers >= 1) {
					wait();
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			item = buffer[front];
			front = (front + 1) % this.capacity;
			size--;
			this.notifyAll();
		}

		return item;
	}


	/**
	 * Enqueues an item to the end of this queue. If the queue is full, this 
	 * method blocks until some space becomes available.
	 * 
	 * @param item Item to enqueue
	 */
	public void enqueue(T item) {
		synchronized(this) {

			try {
				if (this.size == capacity) {
					wait();
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			buffer[back] = item;
			size++;
			back = (back + 1) % this.capacity;
			this.notifyAll();
		}
	}

	/**
	 * Returns the capacity of this queue
	 * @return queue capacity
	 */
	public int getCapacity() {
		return this.capacity;
	}

	/**
	 * Returns the current size of the queue (number of elements in it)
	 * @return queue size
	 */
	public int getSize() {
		return this.size;
	}
	
	/**
	 * Registers a producer to this queue. This method actually increases the
	 * internal producers counter of this queue by 1. This counter is used to
	 * determine whether the queue is still active and to avoid blocking of
	 * consumer threads that try to dequeue elements from an empty queue, when
	 * no producer is expected to add any more items.
	 * Every producer of this queue must call this method before starting to 
	 * enqueue items, and must also call <see>{@link #unregisterProducer()}</see> when
	 * finishes to enqueue all items.
	 * 
	 * @see #dequeue()
	 * @see #unregisterProducer()
	 */
	public synchronized void registerProducer() {
		// TODO: This should be in a critical section
		//pthread_mutex_lock (&my_lock);
		this.producers++;
		//pthread_mutex_unlock (&my_lock);
	}

	/**
	 * Unregisters a producer from this queue. See <see>{@link #registerProducer()}</see>.
	 * 
	 * @see #dequeue()
	 * @see #registerProducer()
	 */
	public synchronized void unregisterProducer() {
		// TODO: This should be in a critical section
		//pthread_mutex_lock (my_lock);
		this.producers--;
		//pthread_mutex_unlock (my_lock);
	}
}
