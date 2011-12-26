package de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl;

import com.google.protobuf.ByteString;
import de.uniluebeck.itm.tr.util.TimedCacheListener;
import de.uniluebeck.itm.tr.util.Tuple;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepSingleResponseExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.cache.RequestEntry;
import de.uniluebeck.itm.ubermep.mep.cache.UbermepObjectCache;
import de.uniluebeck.itm.ubermep.mep.channel.future.impl.SingleRequestSingleResponseChannelFuture;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.AbstractServiceHandler;
import de.uniluebeck.itm.ubermep.mep.listener.SingleRequestSingleResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.ErrorResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse.SingleRequestSingleResponseResponse;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import de.uniluebeck.itm.ubermep.mep.protocol.MEPPacketBuilder;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 21.08.11
 * Time: 12:53
 * To change this template use File | Settings | File Templates.
 */
public class SingleRequestSingleResponseServiceHandler extends AbstractServiceHandler {
	private final Logger log = LoggerFactory.getLogger(SingleRequestSingleResponseServiceHandler.class);
	private final UbermepObjectCache<RequestEntry> requestCache;

	public SingleRequestSingleResponseServiceHandler(int timeOut, TimeUnit timeUnit,
													 List<SingleRequestSingleResponseRequestListener> listeners) {
		this.requestCache = new UbermepObjectCache<RequestEntry>(timeOut, timeUnit);
		this.requestCache.setTimedCacheListener(new TimedCacheListener<Integer, RequestEntry>() {
			@Override
			public Tuple<Long, TimeUnit> timeout(Integer key, RequestEntry value) {
				SingleRequestSingleResponseRequest request = (SingleRequestSingleResponseRequest) value.getRequest();
				((SingleRequestSingleResponseChannelFuture) value.getFuture()).addTimeOutResponse(request);
				return null;
			}
		});
		this.singleRequestSingleResponseRequestListeners = listeners;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() instanceof MEP.MEPPacket) {
			MEP.MEPPacket message = (MEP.MEPPacket) e.getMessage();
			if (MEPPacketBuilder.isRequest(message)) {
				handleRequest(ctx, e, message);
			} else if (MEPPacketBuilder.isResponse(message)) {
				handleResponse(message);
			}
		} else {
			super.messageReceived(ctx, e);
		}
	}

	private void handleResponse(MEP.MEPPacket message) {
		if (checkNullRequestEntry(requestCache.get(message.getRequestID()), message)) {
			return;
		}

		RequestEntry requestEntry = requestCache.remove(message.getRequestID());

		//Check for ErrorResponse
		if (message.getExceptionOccurred()) {
			((SingleRequestSingleResponseChannelFuture) requestEntry.getFuture()).setResponse(
					new ErrorResponse(
							requestEntry.getRequest(),
							((SingleRequestSingleResponseRequest) requestEntry.getRequest()).getDestUrn(),
							new UbermepSingleResponseExceptionEvent(new String(message.getPayload().toByteArray()))
					)
			);
		} else {
			((SingleRequestSingleResponseChannelFuture) requestEntry.getFuture()).setResponse(
					new SingleRequestSingleResponseResponse(
							requestEntry.getRequest(), message.getPayload().toByteArray()
					)
			);
		}
	}

	private void handleRequest(ChannelHandlerContext ctx, MessageEvent event, MEP.MEPPacket message) {
		try {
			byte[] responsePayload = handleSingleRequestSingleResponseRequest(
					event.getRemoteAddress().toString(), message.getPayload().toByteArray());
			MEP.MEPPacket.Builder builder = MEP.MEPPacket.newBuilder(message).
					setPayload(ByteString.copyFrom(responsePayload));
			message = builder.setMessageType(MEP.MessageType.SINGLE_RESPONSE).build();
			ctx.getChannel().write(message, event.getRemoteAddress());
		} catch (UbermepExceptionEvent ee) {
			message = MEP.MEPPacket.newBuilder(message).
					setPayload(ByteString.copyFrom(ee.getMessage().getBytes())).
					setMessageType(ee.getMessageType()).
					setExceptionOccurred(true).build();
			ctx.getChannel().write(message, event.getRemoteAddress());
		}
	}

	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		log.info("handleDownstream: {}", e);
		if (isDownstreamMessageEvent(e)
				&& ((DownstreamMessageEvent) e).getMessage() instanceof SingleRequestSingleResponseRequest) {

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
