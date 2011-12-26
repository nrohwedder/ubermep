package de.uniluebeck.itm.ubermep.mep.channel.servicehandler.unreliable;

import de.uniluebeck.itm.uberlay.ApplicationChannel;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.AbstractServiceHandler;
import de.uniluebeck.itm.ubermep.mep.listener.UnicastMulticastRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.UnreliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl.UnreliableMulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl.UnreliableUnicastRequest;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import de.uniluebeck.itm.ubermep.mep.protocol.MEPPacketBuilder;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.07.11
 * Time: 17:09
 * To change this template use File | Settings | File Templates.
 */
public class UnreliableServiceHandler extends AbstractServiceHandler {
	private final Logger log = LoggerFactory.getLogger(UnreliableServiceHandler.class);

	public UnreliableServiceHandler(List<UnicastMulticastRequestListener> unicastMulticastRequestListeners) {
		this.unicastMulticastRequestListeners = unicastMulticastRequestListeners;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() instanceof MEP.MEPPacket && isUnreliable((MEP.MEPPacket) e.getMessage())) {
			MEP.MEPPacket mepPacket = (MEP.MEPPacket) e.getMessage();
			log.info("Received Unreliable MEPPacket: \n{}", mepPacket);
			String urn = e.getRemoteAddress().toString();
			byte[] payload = mepPacket.getPayload().toByteArray();

			handleUnicastMulticastRequest(urn, payload);
		} else {
			super.messageReceived(ctx, e);
		}
	}

	private boolean isUnreliable(MEP.MEPPacket message) {
		return !message.getReliable();
	}

	public synchronized void send(UnreliableRequest request, Channel channel) throws ExecutionException, InterruptedException {
		MEP.MEPPacket mepPacket = MEPPacketBuilder.createMEPPacketFromRequest((Request) request, null);
		if (request instanceof UnreliableUnicastRequest) {
			channel.write(mepPacket, ((UnreliableUnicastRequest) request).getDestUrn());
		} else if (request instanceof UnreliableMulticastRequest) {
			((ApplicationChannel) channel).write(mepPacket, ((UnreliableMulticastRequest) request).getDestUrns());
		}
	}

	public synchronized void send(Object object, Channel channel, UPAddress urn) throws ExecutionException, InterruptedException {
		ChannelFuture future = Channels.future(channel);
		((ApplicationChannel) channel).write(object, urn, future);
	}

}
