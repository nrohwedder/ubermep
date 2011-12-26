package de.uniluebeck.itm.ubermep.mep.channel.runnable;

import com.google.common.util.concurrent.ListenableFuture;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 12.12.11
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public class ResponseListenerRunnable implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(ResponseListenerRunnable.class);

	private Response response;
	private ListenableFuture<Response> responseFuture;

	public ResponseListenerRunnable(ListenableFuture<Response> responseFuture) {
		this.responseFuture = responseFuture;
	}

	@Override
	public synchronized void run() {
		try {
			response = this.responseFuture.get();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		} catch (ExecutionException e) {
			logger.error(e.getMessage());
		}
	}

	public synchronized Response getResponse() {
		return response;
	}
}
