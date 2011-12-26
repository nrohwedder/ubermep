package de.uniluebeck.itm.ubermep.mep.channel.future;

import de.uniluebeck.itm.ubermep.mep.exception.TimeOutException;
import de.uniluebeck.itm.ubermep.mep.message.request.SingleRequest;
import org.jboss.netty.channel.Channel;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 12.10.11
 * Time: 17:06
 * To change this template use File | Settings | File Templates.
 */
public abstract class UbermepAbstractSingleResponseChannelFuture<T> extends UbermepAbstractChannelFuture {
	protected T response;
	protected Throwable cause;

	/**
	 * Creates a new instance.
	 *
	 * @param channel the {@link org.jboss.netty.channel.Channel} associated with this future
	 */
	public UbermepAbstractSingleResponseChannelFuture(Channel channel) {
		super(channel);
	}

	public synchronized void setResponse(T response) {
		this.response = response;
		done();
	}

	public synchronized T getResponse() {
		if (!isDone()){
			try {
				wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return response;
	}

	@Override
	public synchronized boolean isSuccess() {
		return isDone() && (cause == null);
	}

	@Override
	public synchronized boolean setFailure(Throwable cause) {
		if (isDone()) {
			return false;
		}
		this.cause = cause;
		done();
		return super.setFailure(cause);
	}

	public synchronized void addTimeOutResponse(SingleRequest request) {
		setFailure(new TimeOutException(request.getDestUrn()));
	}

}
