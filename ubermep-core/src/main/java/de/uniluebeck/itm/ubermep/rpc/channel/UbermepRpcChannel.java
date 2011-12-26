package de.uniluebeck.itm.ubermep.rpc.channel;

import com.google.protobuf.*;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import de.uniluebeck.itm.ubermep.rpc.callback.MEPRpcCallback;
import de.uniluebeck.itm.ubermep.rpc.handler.RpcServiceHandler;
import org.jboss.netty.channel.Channel;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 14.09.11
 * Time: 12:40
 * To change this template use File | Settings | File Templates.
 */
public class UbermepRpcChannel implements BlockingRpcChannel, RpcChannel {
	private final RpcServiceHandler rpcServiceHandler;
	private final Channel channel;
	private final UPAddress remoteAddress;

	public UbermepRpcChannel(RpcServiceHandler rpcServiceHandler, Channel channel, UPAddress remoteAddress) {
		this.rpcServiceHandler = rpcServiceHandler;
		this.channel = channel;
		this.remoteAddress = remoteAddress;
	}

	@Override
	public Message callBlockingMethod(Descriptors.MethodDescriptor method, RpcController controller, Message request,
									  Message responsePrototype) throws ServiceException {
		final RpcCallback<Message> done = new MEPRpcCallback<Message>();
		rpcServiceHandler.callRPCAndReturnResponse(
				channel, remoteAddress, MEP.ServiceType.BLOCKING_SERVICE,
				method, controller, request, responsePrototype, done);
		//wait for response to be returned
		Message response = ((MEPRpcCallback) done).getResponse();

		if (controller.failed()) {
			throw new ServiceException(controller.errorText());
		}
		return response;
	}


	@Override
	public void callMethod(Descriptors.MethodDescriptor method, RpcController controller, Message request,
						   Message responsePrototype, RpcCallback<Message> done) {
		rpcServiceHandler.callRPCAndReturnResponse(channel, remoteAddress, MEP.ServiceType.SERVICE,
				method, controller, request, responsePrototype, done);
	}
}
