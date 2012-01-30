package de.uniluebeck.itm.ubermep.mep.channel.future.impl;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractFuture;
import de.uniluebeck.itm.ubermep.mep.channel.runnable.ProgressListenerRunnable;

import javax.annotation.Nullable;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 05.12.11
 * Time: 18:26
 * To change this template use File | Settings | File Templates.
 */
public final class SettableProgressFuture<V> extends AbstractFuture<V> {
	private final ProgressList progressList = new ProgressList();

	public static <V> SettableProgressFuture<V> create() {
		return new SettableProgressFuture<V>();
	}

	private SettableProgressFuture() {
	}

	public void progress(String senderUrn, byte[] payload, int current, int total) {
		progressList.progress(senderUrn, payload, current, total);
	}

	@Override
	public void addListener(Runnable listener, Executor exec) {
		if (listener instanceof ProgressListenerRunnable) {
			progressList.add((ProgressListenerRunnable) listener);
		}

		super.addListener(listener, exec);
	}

	@Override
	public boolean set(@Nullable V value) {
		progressList.done();
		return super.set(value);
	}

	@Override
	public boolean setException(Throwable throwable) {
		return super.setException(throwable);
	}

	private class ProgressList {
		private final Queue<ProgressListenerRunnable> runnables = Lists.newLinkedList();

		public void add(ProgressListenerRunnable listener) {
			synchronized (runnables) {
				runnables.add(listener);
			}
		}

		public void progress(String senderUrn, byte[] payload, int current, int total) {
			synchronized (runnables) {
				for (ProgressListenerRunnable runnable : runnables) {
					runnable.singleResponseReceived(senderUrn, payload, current, total);
				}
			}
		}

		public void done() {
			synchronized (runnables) {
				while (!runnables.isEmpty()) {
					runnables.poll();
				}
			}
		}
	}
}
