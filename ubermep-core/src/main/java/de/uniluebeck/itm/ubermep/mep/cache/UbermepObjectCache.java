package de.uniluebeck.itm.ubermep.mep.cache;

import de.uniluebeck.itm.tr.util.TimedCache;
import de.uniluebeck.itm.tr.util.TimedCacheListener;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 19.07.11
 * Time: 13:51
 * To change this template use File | Settings | File Templates.
 */
public class UbermepObjectCache<T extends Object> {
	private int counter;
	protected final Lock lock;
	private TimedCache<Integer, T> timedCache;

	public UbermepObjectCache(int timeOut, TimeUnit timeOutUnit) {
		this.lock = new ReentrantLock();
		this.counter = 0;
		this.timedCache = new TimedCache<Integer, T>(timeOut, timeOutUnit);
	}

	public void setTimedCacheListener(TimedCacheListener<Integer, T> timedCacheListener) {
		this.timedCache.setListener(timedCacheListener);
	}

	public T get(Integer key) {
		return this.timedCache.get(key);
	}

	public T get(T o) {
		if (!timedCache.containsValue(o)) {
			return null;
		}
		for (T value : this.values()) {
			if (value.equals(o)) {
				return o;
			}
		}
		return null;
	}

	public Integer add(T o) {
		lock.lock();
		try {
			this.timedCache.put(counter, o);
			return counter;
		} finally {
			incCounter();
			lock.unlock();
		}
	}

	public Integer add(T o, long timeOut, TimeUnit timeUnit){
		lock.lock();
		try {
			this.timedCache.put(counter, o, timeOut, timeUnit);
			return counter;
		} finally {
			incCounter();
			lock.unlock();
		}
	}

	public T remove(Integer key) {
		lock.lock();
		try {
			return timedCache.remove(key);
		} finally {
			incCounter();
			lock.unlock();
		}
	}

	public T remove(T o) {
		lock.lock();
		try {
			if (get(o) == null) {
				return null;
			}
			for (Integer key : keySet()) {
				if (get(key).equals(o)) {
					return remove(key);
				}
			}
			return null;
		} finally {
			lock.unlock();
		}
	}

	public boolean contains(T o) {
		for (Integer key : keySet()) {
			if (get(key).equals(o)) {
				return true;
			}
		}
		return false;
	}

	private void incCounter() {
		lock.lock();
		try {
			counter = 0;
			while (timedCache.containsKey(counter)) {
				counter += 1;
				//only reset counter if max_value is reached
				if (counter >= Integer.MAX_VALUE) {
					throw new RuntimeException("Could not increment Object-Cache-Counter!! " +
							"\n Integer.MAX_VALUE reached!!");
				}
			}
		} finally {
			lock.unlock();
		}
	}


	protected Set<Integer> keySet() {
		return this.timedCache.keySet();
	}

	public Collection<T> values() {
		return timedCache.values();
	}
}
