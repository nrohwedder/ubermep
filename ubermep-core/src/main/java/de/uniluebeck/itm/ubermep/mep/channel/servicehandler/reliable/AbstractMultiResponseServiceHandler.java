package de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable;

import com.google.protobuf.ByteString;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.AbstractServiceHandler;
import de.uniluebeck.itm.ubermep.mep.handle.MultiResponseHandle;
import de.uniluebeck.itm.ubermep.mep.listener.MultiResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import org.jboss.netty.channel.ChannelHandlerContext;

import java.net.SocketAddress;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 27.09.11
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractMultiResponseServiceHandler extends AbstractServiceHandler {

	protected AbstractMultiResponseServiceHandler(List<MultiResponseRequestListener> listeners) {
		this.multiResponseRequestListeners = listeners;
	}

	protected MultiResponseHandle handleMultiResponse(final ChannelHandlerContext ctx,
													  final MEP.MEPPacket requestPacket,
													  final SocketAddress remoteAddress) {
		return new MultiResponseHandle() {
			@Override
			public void handleSingleResponse(byte[] payload, int current, int total){
				log.info("Sending Multi-Response Nr.:{}...", current);
				MEP.MEPPacket.Builder builder = MEP.MEPPacket.newBuilder(requestPacket).
						setPayload(ByteString.copyFrom(payload));
				MEP.MEPPacket message = builder.setMessageType(MEP.MessageType.MULTI_RESPONSE).
						setPayload(ByteString.copyFrom(payload)).
						setCurrentMessageNumber(current).
						setTotalMessageNumber(total).
						build();

				ctx.getChannel().write(message, remoteAddress);
			}
		};
	}

}
