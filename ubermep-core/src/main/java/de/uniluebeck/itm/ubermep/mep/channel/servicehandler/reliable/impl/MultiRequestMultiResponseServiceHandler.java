package de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.AbstractMultiResponseServiceHandler;
import de.uniluebeck.itm.ubermep.mep.listener.MultiResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.MultiRequestMultiResponseRequest;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DownstreamMessageEvent;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.09.11
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public class MultiRequestMultiResponseServiceHandler extends AbstractMultiResponseServiceHandler {
	public MultiRequestMultiResponseServiceHandler(List<MultiResponseRequestListener> listeners) {
		super(listeners);
	}

	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		log.info("handleDownstream: {}", e);
		if (isDownstreamMessageEvent (e)
				&& ((DownstreamMessageEvent) e).getMessage() instanceof MultiRequestMultiResponseRequest) {

			DownstreamMessageEvent downstreamMessageEvent = (DownstreamMessageEvent) e;
			Request request = (Request) downstreamMessageEvent.getMessage();
			MultiRequestMultiResponseRequest.SingleMultiRequestMultiResponseRequest singleMultiResponseRequest =
					new MultiRequestMultiResponseRequest.SingleMultiRequestMultiResponseRequest(
							new UPAddress(downstreamMessageEvent.getRemoteAddress()), request.getPayload()
					);
			DownstreamMessageEvent event = new DownstreamMessageEvent(
					e.getChannel(),
					e.getFuture(),
					singleMultiResponseRequest,
					((DownstreamMessageEvent) e).getRemoteAddress()
			);
			super.handleDownstream(ctx, event);
		} else {
			super.handleDownstream(ctx, e);
		}
	}
}
