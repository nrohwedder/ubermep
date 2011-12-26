package de.uniluebeck.itm.ubermep.rpc.callback;

import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 16.09.11
 * Time: 12:50
 * To change this template use File | Settings | File Templates.
 */
public class MEPRpcCallback<T extends Message> implements RpcCallback<T> {
	private T response;

	private boolean done = false;

	@Override
	public synchronized void run(T response) {
		this.response = response;
		this.done = true;
		notifyAll();
	}

	public synchronized T getResponse() {
		if (!this.isDone()) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return response;
	}

	public synchronized boolean isDone() {
		return done;
	}
}
