package de.uniluebeck.itm.ubermep.mep.channel.runnable;

import com.google.common.util.concurrent.ListenableFuture;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 05.12.11
 * Time: 21:15
 * To change this template use File | Settings | File Templates.
 */
public class ResponsePercentProgressRunnable extends ResponseProgressRunnable{
	private final Logger log = LoggerFactory.getLogger(ResponsePercentProgressRunnable.class);

	public ResponsePercentProgressRunnable(ListenableFuture<Response> responseFuture) {
		super(responseFuture);
	}

	@Override
	public void progress(String senderUrn, byte[] payload, int current, int total) {
		log.info("Received single MultiResponse from: {}: --- Progress: {} % ---", senderUrn, current * 100 / total);
	}
}
