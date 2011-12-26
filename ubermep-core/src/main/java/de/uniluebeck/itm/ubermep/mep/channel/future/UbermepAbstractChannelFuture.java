package de.uniluebeck.itm.ubermep.mep.channel.future;

import de.uniluebeck.itm.ubermep.mep.listener.MultiResponseChannelFutureProgressListener;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 12.10.11
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
public abstract class UbermepAbstractChannelFuture extends DefaultChannelFuture {
	private List<ChannelFutureListener> listeners;
	protected final Logger logger = LoggerFactory.getLogger(UbermepAbstractChannelFuture.class);
	private boolean done = false;

	/**
	 * Creates a new instance.
	 *
	 * @param channel the {@link org.jboss.netty.channel.Channel} associated with this future
	 */
	public UbermepAbstractChannelFuture(Channel channel) {
		super(channel, true);
	}

	public synchronized boolean isDone() {
		return done;
	}

	protected synchronized void done() {
		this.done = true;
		notifyListeners();
		notifyAll();
	}

	@Override
	public synchronized void addListener(ChannelFutureListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener");
		}

		if (isDone()) {
			notifyListener(listener);
		} else {
			if (listeners == null) {
				listeners = new ArrayList<ChannelFutureListener>(1);
			}
			listeners.add(listener);
		}
	}

	public synchronized void removeListener(ChannelFutureListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener");
		}

		if (!isDone()) {
			if (listeners != null) {
				listeners.remove(listener);
			}
		}
	}


	private void notifyListener(ChannelFutureListener l) {
		try {
			l.operationComplete(this);
		} catch (Throwable t) {
			logger.warn("An exception was thrown by " + ChannelFutureListener.class.getSimpleName() + ".", t);
		}
	}

	protected synchronized void notifyListeners() {
		if (listeners != null) {
			for (ChannelFutureListener l : listeners) {
				notifyListener(l);
			}
			listeners = null;
		}
	}

	protected void progress(String senderUrn, byte[] payload, int current, int total) {
		if (listeners != null) {
			for (ChannelFutureListener l : listeners) {
				if (l instanceof MultiResponseChannelFutureProgressListener) {
					((MultiResponseChannelFutureProgressListener) l).progress(senderUrn, payload, current, total);
				}
			}
		}

	}
}
