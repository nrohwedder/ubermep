package de.uniluebeck.itm.ubermep.mep.channel.channels.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import de.uniluebeck.itm.uberlay.UberlayBootstrap;
import de.uniluebeck.itm.ubermep.mep.channel.channels.AbstractRequestResponseChannel;
import de.uniluebeck.itm.ubermep.mep.channel.future.impl.SingleRequestSingleResponseChannelFuture;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 19.08.11
 * Time: 20:04
 * To change this template use File | Settings | File Templates.
 */
public class SingleRequestSingleResponseChannel extends AbstractRequestResponseChannel
		<SingleRequestSingleResponseRequest, Response> {

	public SingleRequestSingleResponseChannel(UberlayBootstrap bootstrap, ChannelPipeline pipeline)
			throws ExecutionException, InterruptedException {
		super(bootstrap, pipeline);
	}

	@Override
	public ListenableFuture<Response> write(SingleRequestSingleResponseRequest request)
			throws ExecutionException, InterruptedException {
		ChannelFuture channelFuture = new SingleRequestSingleResponseChannelFuture(this);

		final SettableFuture<Response> returnFuture = SettableFuture.create();
		channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				returnFuture.set(((SingleRequestSingleResponseChannelFuture) future).getResponse());
			}
		});
		createAndSendDownstreamEvent(request, request.getDestUrn(), channelFuture);

		return returnFuture;
	}

}
