package de.uniluebeck.itm.ubermep.mep.channel.runnable;

import com.google.common.util.concurrent.ListenableFuture;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 12.12.11
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public abstract class ResponseProgressRunnable extends ResponseListenerRunnable implements ProgressListenerRunnable {

	public ResponseProgressRunnable(ListenableFuture<Response> responseFuture) {
		super(responseFuture);
	}
}
