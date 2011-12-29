package de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import de.uniluebeck.itm.uberlay.ApplicationChannel;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.channel.callable.ResponseCallable;
import de.uniluebeck.itm.ubermep.mep.channel.channelnexus.RequestResponseChannelNexus;
import de.uniluebeck.itm.ubermep.mep.channel.channels.impl.MultiResponseChannel;
import de.uniluebeck.itm.ubermep.mep.channel.channels.impl.SingleRequestSingleResponseChannel;
import de.uniluebeck.itm.ubermep.mep.channel.future.UbermepAbstractChannelFuture;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.AbstractServiceHandler;
import de.uniluebeck.itm.ubermep.mep.exception.RequestCouldNotBeDeliveredException;
import de.uniluebeck.itm.ubermep.mep.listener.UnicastMulticastRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableMulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableUnicastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.MultiRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.SingleRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.impl.ReliableMulticastResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse.ReliableUnicastResponse;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import de.uniluebeck.itm.ubermep.mep.protocol.MEPPacketBuilder;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.08.11
 * Time: 12:46
 * To change this template use File | Settings | File Templates.
 */
public class ReliableServiceHandler extends AbstractServiceHandler {
	private final Logger log = LoggerFactory.getLogger(ReliableServiceHandler.class);
	private RequestResponseChannelNexus<SingleRequestSingleResponseChannel> singleRequestSingleResponseChannelNexus;
	private RequestResponseChannelNexus<MultiResponseChannel> multiResponseChannelNexus;
	private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
			Executors.newScheduledThreadPool(10));
	private final int timeOut;
	private final TimeUnit timeUnit;

	public ReliableServiceHandler(int timeOut, TimeUnit timeUnit,
								  List<UnicastMulticastRequestListener> unicastMulticastRequestListeners) {
		this.timeOut = timeOut;
		this.timeUnit = timeUnit;
		this.unicastMulticastRequestListeners = unicastMulticastRequestListeners;
	}

	public void setSingleRequestSingleResponseChannelNexus(
			RequestResponseChannelNexus<SingleRequestSingleResponseChannel> upstreamHandler) {
		this.singleRequestSingleResponseChannelNexus = upstreamHandler;
	}

	public void setMultiResponseChannelNexus(RequestResponseChannelNexus<MultiResponseChannel> multiResponseChannelNexus) {
		this.multiResponseChannelNexus = multiResponseChannelNexus;
	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (isUpstreamMessageEvent(e)
				&& ((UpstreamMessageEvent) e).getMessage() instanceof MEP.MEPPacket
				&& MEPPacketBuilder.isReliable((MEP.MEPPacket) ((UpstreamMessageEvent) e).getMessage())) {
			UpstreamMessageEvent event = (UpstreamMessageEvent) e;
			MEP.MEPPacket message = (MEP.MEPPacket) event.getMessage();
			log.info("Received Reliable MEPPacket: \n{}", message);
			if (MEPPacketBuilder.isRequestResponseMessage(message)) {
				//Message is Request-Response-Message
				handleRequestResponseUpstream(ctx, e, message.getMessageType());
			} else {
				//Message is Reliable Unicast, Multicast or RPC
				super.handleUpstream(ctx, e);
			}
		} else {
			super.handleUpstream(ctx, e);
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() instanceof MEP.MEPPacket
				&& MEPPacketBuilder.isReliableUnicastOrMulticast((MEP.MEPPacket) e.getMessage())) {
			//Message is Reliable Unicast or Multicast
			MEP.MEPPacket message = (MEP.MEPPacket) e.getMessage();
			String urn = e.getRemoteAddress().toString();
			byte[] payload = message.getPayload().toByteArray();

			handleUnicastMulticastRequest(urn, payload);

		} else {
			super.messageReceived(ctx, e);
		}
	}

	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (isDownstreamMessageEvent(e)
				&& (((DownstreamMessageEvent) e).getMessage() instanceof ReliableRequest)) {

			ReliableRequest request = (ReliableRequest) ((DownstreamMessageEvent) e).getMessage();
			if (request instanceof SingleRequestSingleResponseRequest) {
				singleRequestSingleResponseChannelNexus.handleDownstream(ctx, e);
			} else if (request instanceof SingleRequestMultiResponseRequest
					|| request instanceof MultiRequestMultiResponseRequest) {
				multiResponseChannelNexus.handleDownstream(ctx, e);
			} else {
				handleDownstreamUnicastMulticast(request, e);
			}
		} else {
			super.handleDownstream(ctx, e);
		}
	}

	private void handleDownstreamUnicastMulticast(ReliableRequest request, ChannelEvent e) throws ExecutionException, InterruptedException {
		Future<Response> future = send(request, e.getChannel());
		while (!future.isDone()) {
		}
		Response response = future.get();
		if ((request instanceof ReliableUnicastRequest) && (response instanceof ReliableUnicastResponse)) {
			e.getFuture().setSuccess();
		} else if ((request instanceof ReliableMulticastRequest) && (response instanceof ReliableMulticastResponse)) {
			e.getFuture().setSuccess();
		} else {
			e.getFuture().setFailure(new RequestCouldNotBeDeliveredException(new String(response.getPayload())));
		}
	}

	private void handleRequestResponseUpstream(ChannelHandlerContext ctx, ChannelEvent e, MEP.MessageType messageType) throws Exception {
		switch (messageType) {
			case SINGLE_RESPONSE_REQUEST:
				singleRequestSingleResponseChannelNexus.handleUpstream(ctx, e);
				return;
			case SINGLE_RESPONSE:
				singleRequestSingleResponseChannelNexus.handleUpstream(ctx, e);
				return;
			case MULTI_RESPONSE_REQUEST:
				multiResponseChannelNexus.handleUpstream(ctx, e);
				return;
			case MULTI_RESPONSE:
				multiResponseChannelNexus.handleUpstream(ctx, e);
				return;
		}
	}

	public ListenableFuture<Response> send(ReliableRequest request, Channel channel) throws ExecutionException, InterruptedException {
		if (request instanceof SingleRequestSingleResponseRequest) {
			return send((SingleRequestSingleResponseRequest) request);
		} else if (request instanceof SingleRequestMultiResponseRequest) {
			return send((SingleRequestMultiResponseRequest) request);
		} else if (request instanceof MultiRequestMultiResponseRequest) {
			return send((MultiRequestMultiResponseRequest) request);
		} else {
			return executorService.submit(new ResponseCallable((Request) request, channel, timeOut, timeUnit));
		}
	}

	public ListenableFuture<Response> send(SingleRequestSingleResponseRequest request) throws ExecutionException, InterruptedException {
		checkNullSingleRequestSingleResponseChannelNexus();
		return singleRequestSingleResponseChannelNexus.write(request);
	}


	public ListenableFuture<Response> send(SingleRequestMultiResponseRequest request) throws ExecutionException, InterruptedException {
		checkNullMultiResponseChannelNexus();
		return multiResponseChannelNexus.write(request);
	}

	public ListenableFuture<Response> send(MultiRequestMultiResponseRequest request) throws ExecutionException, InterruptedException {
		checkNullMultiResponseChannelNexus();
		return multiResponseChannelNexus.write(request);
	}

	public <T extends UbermepAbstractChannelFuture> T send(Object object, Channel channel, UPAddress urn,
														   Class<T> channelFutureClass) throws ExecutionException, InterruptedException {
		T future = null;
		try {
			Constructor<T> c = channelFutureClass.getConstructor(Channel.class);
			future = c.newInstance(channel);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		((ApplicationChannel) channel).write(object, urn, future);
		return future;

	}

	private void checkNullMultiResponseChannelNexus() throws ExecutionException {
		if (multiResponseChannelNexus == null) {
			String message = "Caution: MultiResponseChannel is null!! " +
					"Sending of SingleRequestMultiResponse-Message aborted!";
			throw new ExecutionException(message, new Throwable(message));
		}
	}

	private void checkNullSingleRequestSingleResponseChannelNexus() throws ExecutionException {
		if (singleRequestSingleResponseChannelNexus == null) {
			String message = "Caution: SingleRequestSingleResponseChannel is null!! " +
					"Sending of SingleRequestSingleResponse-Message aborted!";
			throw new ExecutionException(message, new Throwable(message));
		}
	}
}
