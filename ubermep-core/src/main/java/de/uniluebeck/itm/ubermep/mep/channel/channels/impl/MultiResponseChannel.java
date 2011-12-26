package de.uniluebeck.itm.ubermep.mep.channel.channels.impl;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import de.uniluebeck.itm.uberlay.UberlayBootstrap;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.channel.channels.AbstractRequestResponseChannel;
import de.uniluebeck.itm.ubermep.mep.channel.future.impl.MultiResponseChannelFuture;
import de.uniluebeck.itm.ubermep.mep.channel.future.impl.SettableProgressFuture;
import de.uniluebeck.itm.ubermep.mep.listener.MultiResponseChannelFutureProgressListener;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.MultiRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.SingleRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.MultiResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.impl.MultiRequestMultiResponseResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.impl.SingleRequestMultiResponseResponse;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 02.09.11
 * Time: 20:17
 * To change this template use File | Settings | File Templates.
 */
public class MultiResponseChannel extends AbstractRequestResponseChannel<Request, Response> {

	public MultiResponseChannel(UberlayBootstrap bootstrap, ChannelPipeline pipeline)
			throws ExecutionException, InterruptedException {
		super(bootstrap, pipeline);
	}

	@Override
	public ListenableFuture<Response> write(Request request) throws ExecutionException, InterruptedException {
		Collection<UPAddress> remoteAddresses;
		final Response response;
		if (request instanceof SingleRequestMultiResponseRequest) {
			remoteAddresses = Sets.newHashSet(((SingleRequestMultiResponseRequest) request).getDestUrn());
			response = new SingleRequestMultiResponseResponse(request);
		} else if (request instanceof MultiRequestMultiResponseRequest) {
			remoteAddresses = ((MultiRequestMultiResponseRequest) request).getDestUrns();
			response = new MultiRequestMultiResponseResponse(request);
		} else {
			String message = "Could not find responsible Request-Type! Abort sending message!";
			log.error(message);
			throw new ExecutionException(new RuntimeException(message));
		}
		final SettableProgressFuture<Response> returnFuture = SettableProgressFuture.create();
		ChannelFuture channelFuture = new MultiResponseChannelFuture(this, remoteAddresses.size());
		channelFuture.addListener(new MultiResponseChannelFutureProgressListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				((MultiResponse) response).setResponses(
						((MultiResponseChannelFuture) future).getResponses());
				returnFuture.set(response);
			}

			@Override
			public void progress(String senderUrn, byte[] payload, int current, int total) {
				returnFuture.progress(senderUrn, payload, current, total);
			}
		});
		createAndSendDownstreamEvent(request, remoteAddresses, channelFuture);
		return returnFuture;
	}
}
