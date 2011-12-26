package de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl;

import com.google.protobuf.ByteString;
import de.uniluebeck.itm.tr.util.TimedCacheListener;
import de.uniluebeck.itm.tr.util.Tuple;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.cache.RequestEntry;
import de.uniluebeck.itm.ubermep.mep.cache.UbermepObjectCache;
import de.uniluebeck.itm.ubermep.mep.channel.future.impl.MultiResponseChannelFuture;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.AbstractMultiResponseServiceHandler;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.handle.MultiResponseHandle;
import de.uniluebeck.itm.ubermep.mep.listener.MultiResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.SingleRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.ErrorResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.SingleMultiResponseResponse;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import de.uniluebeck.itm.ubermep.mep.protocol.MEPPacketBuilder;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 02.09.11
 * Time: 20:54
 * To change this template use File | Settings | File Templates.
 */
public class SingleRequestMultiResponseServiceHandler extends AbstractMultiResponseServiceHandler {
	private final UbermepObjectCache<RequestEntry> requestCache;

	public SingleRequestMultiResponseServiceHandler(int timeOut, TimeUnit timeUnit,
													List<MultiResponseRequestListener> listeners) {
		super(listeners);
		this.requestCache = new UbermepObjectCache<RequestEntry>(timeOut, timeUnit);
		this.requestCache.setTimedCacheListener(new TimedCacheListener<Integer, RequestEntry>() {
			@Override
			public Tuple<Long, TimeUnit> timeout(Integer key, RequestEntry value) {
				SingleRequestMultiResponseRequest request = (SingleRequestMultiResponseRequest) value.getRequest();
				((MultiResponseChannelFuture) value.getFuture()).addTimeOutResponse(request);
				return null;
			}
		});
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() instanceof MEP.MEPPacket) {
			MEP.MEPPacket message = (MEP.MEPPacket) e.getMessage();
			if (MEPPacketBuilder.isRequest(message)) {
				handleRequest(ctx, e, message);
			} else if (MEPPacketBuilder.isResponse(message)) {
				handleResponse(e, message);
			}
		} else {
			super.messageReceived(ctx, e);
		}
	}

	private void handleRequest(ChannelHandlerContext ctx, MessageEvent event, MEP.MEPPacket message) throws ExecutionException, InterruptedException {
		try {
			MultiResponseHandle responseHandle = handleMultiResponse(ctx, message, event.getRemoteAddress());
			handleMultiResponseRequest(
					responseHandle, event.getRemoteAddress().toString(), message.getPayload().toByteArray());
		} catch (UbermepExceptionEvent ee) {
			message = MEP.MEPPacket.newBuilder(message).
					setPayload(ByteString.copyFrom(ee.getMessage().getBytes())).
					setMessageType(ee.getMessageType()).
					setExceptionOccurred(true).build();
			ctx.getChannel().write(message, event.getRemoteAddress());
		}
	}

	private void handleResponse(MessageEvent event, MEP.MEPPacket message) {
		int requestID;
		requestID = message.getRequestID();
		progress(requestCache.get(requestID), message, event.getRemoteAddress());
		addResponse(requestCache.get(requestID), message, event.getRemoteAddress());
	}

	private void progress(RequestEntry requestEntry, MEP.MEPPacket message, SocketAddress remoteAddress) {
		((MultiResponseChannelFuture) requestEntry.getFuture()).progress(
				remoteAddress.toString(),
				message.getPayload().toByteArray(),
				message.getCurrentMessageNumber(),
				message.getTotalMessageNumber());
	}

	private void addResponse(RequestEntry requestEntry, MEP.MEPPacket message, SocketAddress remoteAddress) {
		if (checkNullRequestEntry(requestEntry, message)) {
			return;
		} else if (requestEntry.getFuture().isDone()) {
			return;
		}

		Response response;
		if (message.getExceptionOccurred()) {
			response = new ErrorResponse(
					requestEntry.getRequest(),
					new UPAddress(remoteAddress),
					new Throwable(new String(message.getPayload().toByteArray())));
		} else {
			response = new SingleMultiResponseResponse(
					requestEntry.getRequest(), message.getPayload().toByteArray(),
					message.getCurrentMessageNumber(), message.getTotalMessageNumber()
			);
		}
		((MultiResponseChannelFuture) requestEntry.getFuture()).
				addResponse(
						new UPAddress(remoteAddress),
						response
				);
		checkSucceededChannelFuture(requestEntry, message);
	}

	private void checkSucceededChannelFuture(RequestEntry requestEntry, MEP.MEPPacket message) {
		if (checkNullRequestEntry(requestEntry, message)) {
			return;
		}
		if (requestEntry.getFuture().isSuccess()) {
			requestCache.remove(message.getRequestID());
		}
	}

	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		log.info("handleDownstream: {}", e);
		if (isDownstreamMessageEvent(e) &&
				((DownstreamMessageEvent) e).getMessage() instanceof SingleRequestMultiResponseRequest) {

			Request request = (Request) ((DownstreamMessageEvent) e).getMessage();
			RequestEntry requestEntry = new RequestEntry(request, e.getFuture());
			Integer requestID;
			if (request.hasTimeOut()) {
				ReliableRequest reliableRequest = ((ReliableRequest) request);
				requestID = requestCache.add(requestEntry, reliableRequest.getTimeOut(), reliableRequest.getTimeOutUnit());
			} else {
				requestID = requestCache.add(requestEntry);
			}

			DownstreamMessageEvent event = new DownstreamMessageEvent(
					e.getChannel(),
					e.getFuture(),
					MEPPacketBuilder.createMEPPacketFromMessage(request, requestID),
					((DownstreamMessageEvent) e).getRemoteAddress());

			ctx.sendDownstream(event);
		} else {
			super.handleDownstream(ctx, e);
		}
	}
}
