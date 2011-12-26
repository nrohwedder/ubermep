package de.uniluebeck.itm.ubermep.rpc.handler;

import com.google.protobuf.*;
import de.uniluebeck.itm.tr.util.TimedCacheListener;
import de.uniluebeck.itm.tr.util.Tuple;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.cache.UbermepObjectCache;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import de.uniluebeck.itm.ubermep.mep.protocol.MEPPacketBuilder;
import de.uniluebeck.itm.ubermep.rpc.callback.MEPRpcCallback;
import de.uniluebeck.itm.ubermep.rpc.callback.RpcCallbackImpl;
import de.uniluebeck.itm.ubermep.rpc.controller.RpcControllerImpl;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 13.09.11
 * Time: 19:27
 * To change this template use File | Settings | File Templates.
 */
public class RpcServiceHandler extends SimpleChannelUpstreamHandler {

	private class RpcEntry {
		final RpcController controller;
		final RpcCallback<Message> callback;
		final Message responsePrototype;

		private RpcEntry(RpcController controller, RpcCallback<Message> callback, Message responsePrototype) {
			this.controller = controller;
			this.callback = callback;
			this.responsePrototype = responsePrototype;
		}
	}

	private final Logger logger = LoggerFactory.getLogger(RpcServiceHandler.class);
	private Map<String, BlockingService> registeredBlockingServices = new HashMap<String, BlockingService>();
	private Map<String, Service> registeredServices = new HashMap<String, Service>();
	private final UbermepObjectCache<RpcEntry> objectCache;

	public RpcServiceHandler(int timeOut, TimeUnit timeUnit) {
		objectCache = new UbermepObjectCache<RpcEntry>(timeOut, timeUnit);
		objectCache.setTimedCacheListener(new TimedCacheListener<Integer, RpcEntry>() {
			@Override
			public Tuple<Long, TimeUnit> timeout(Integer key, RpcEntry value) {
				if (value.controller != null) {
					String message = "TimeOut occured! Rpc-Request removed";
					if (value.responsePrototype != null){
						message += " for Type: " + value.responsePrototype.getClass().getSimpleName();
					}
					value.controller.setFailed(message + "!");
				}
				if (value.callback != null) {
					value.callback.run(null);
				}
				return null;
			}
		});
	}

	public void registerService(BlockingService service) {
		registeredBlockingServices.put(service.getDescriptorForType().getFullName(), service);
	}

	public void registerService(Service service) {
		registeredServices.put(service.getDescriptorForType().getFullName(), service);
	}

	private BlockingService getBlockingService(String descripterForType) {
		return registeredBlockingServices.get(descripterForType);
	}

	private Service getService(String descripterForType) {
		return registeredServices.get(descripterForType);
	}


	//-- Client and Server Method
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() instanceof MEP.MEPPacket) {
			MEP.MEPPacket mepPacket = (MEP.MEPPacket) e.getMessage();

			//check if response received on client
			if (MEPPacketBuilder.isRpcResponse(mepPacket)) {
				setResponse(mepPacket);
			} else {
				// else request received on server
				MEP.RPCMessage rpcRequest = mepPacket.getRpcMessage();
				Service service = getService(rpcRequest.getServiceName());
				BlockingService blockingService = getBlockingService(rpcRequest.getServiceName());

				//check ServiceType
				switch (rpcRequest.getServiceType()) {
					case SERVICE:
						//check if service is registered if not: exception-message returned
						if (service == null) {
							ByteString payload = createServiceNotRegisteredExceptionMessage(
									rpcRequest.getMethodName(), rpcRequest.getServiceName());
							send(e, mepPacket, rpcRequest, payload, true);
						} else {
							//call non-blocking method
							this.callAndReceiveMethod(e, mepPacket, rpcRequest, service);
						}
						return;
					case BLOCKING_SERVICE:
						//check if service is registered
						if (blockingService == null) {
							ByteString payload = createServiceNotRegisteredExceptionMessage(
									rpcRequest.getMethodName(), rpcRequest.getServiceName());
							send(e, mepPacket, rpcRequest, payload, true);
						} else {
							//call blocking method
							this.callAndReceiveBlockingMethod(e, mepPacket, rpcRequest, blockingService);
						}
						return;
				}
			}
		} else {
			super.messageReceived(ctx, e);
		}
	}

	private ByteString createServiceNotRegisteredExceptionMessage(String methodName, String serviceName) {
		return ByteString.copyFrom(("Warning: Could not call Method: " + methodName + " on Service: " +
				serviceName + "! Service not registered! ").getBytes());
	}

	//----> Start Server-Methods

	//---> Start Service-Helper Methods
	private Message createOriginalRequestFromPayload(Message messagePrototype, MEP.MEPPacket mepPacket) throws InvalidProtocolBufferException {
		return messagePrototype.newBuilderForType().mergeFrom(mepPacket.getPayload()).build();
	}

	private void send(MessageEvent e, MEP.MEPPacket mepPacket, MEP.RPCMessage rpcRequest, ByteString payload, boolean exceptionOccured) throws InvalidProtocolBufferException {
		Message rpcResponse = MEPPacketBuilder.createReliableRpcResponse(
				mepPacket.getRequestID(),
				createRpcMessage(rpcRequest),
				payload, exceptionOccured);
		logger.info("sending response: {}", rpcResponse);
		e.getChannel().write(rpcResponse, e.getRemoteAddress());
	}

	private MEP.RPCMessage createRpcMessage(MEP.RPCMessage request) {
		return MEPPacketBuilder.createRpcMessage(
				request.getServiceName(), request.getMethodName(), request.getServiceType());
	}

	//<--- End Service-Helper Methods

	//---> Start Non-Blocking-Service-Helper- Methods
	private void callAndReceiveMethod(MessageEvent e, MEP.MEPPacket mepPacket, MEP.RPCMessage rpcRequest, Service service) throws InvalidProtocolBufferException {
		logger.info("calling Method! {}", mepPacket);
		Descriptors.MethodDescriptor methodDescriptor = getMethodDescriptor(service, rpcRequest.getMethodName());
		Message messagePrototype = service.getRequestPrototype(methodDescriptor);
		Message request = createOriginalRequestFromPayload(messagePrototype, mepPacket);
		try {
			final RpcCallbackImpl<Message> callback = new RpcCallbackImpl<Message>();
			callMethodAndCreateResponse(service, methodDescriptor, request, callback);

			Message response = callback.getResponse();

			send(e, mepPacket, rpcRequest, response.toByteString(), false);
		} catch (ServiceException exc) {
			send(e, mepPacket, rpcRequest, ByteString.copyFrom(exc.getMessage().getBytes()), true);
		}
	}

	private Descriptors.MethodDescriptor getMethodDescriptor(Service service, String methodName) {
		Descriptors.ServiceDescriptor descriptor = service.getDescriptorForType();
		return descriptor.findMethodByName(methodName);
	}

	private void callMethodAndCreateResponse(Service service, Descriptors.MethodDescriptor methodDescriptor, Message message, MEPRpcCallback<Message> done) throws ServiceException {
		service.callMethod(
				methodDescriptor,
				new RpcControllerImpl(),
				message,
				done
		);
	}
	//<---- End Non-Blocking-Service-Helper-Methods

	//---> Start Blocking-Service-Helper-Methods
	private void callAndReceiveBlockingMethod(MessageEvent e, MEP.MEPPacket mepPacket, MEP.RPCMessage rpcRequest, BlockingService service) throws InvalidProtocolBufferException {
		logger.info("calling Blocking Method! {}", mepPacket);
		Descriptors.MethodDescriptor methodDescriptor = getMethodDescriptor(service, rpcRequest.getMethodName());
		Message messagePrototype = service.getRequestPrototype(methodDescriptor);
		Message request = createOriginalRequestFromPayload(messagePrototype, mepPacket);
		try {
			Message response = createResponse(service, methodDescriptor, request);
			send(e, mepPacket, rpcRequest, response.toByteString(), false);
		} catch (ServiceException exc) {
			send(e, mepPacket, rpcRequest, ByteString.copyFrom(exc.getMessage().getBytes()), true);
		}

	}

	private Descriptors.MethodDescriptor getMethodDescriptor(BlockingService service, String methodName) {
		Descriptors.ServiceDescriptor descriptor = service.getDescriptorForType();
		return descriptor.findMethodByName(methodName);
	}

	private Message createResponse(BlockingService service, Descriptors.MethodDescriptor methodDescriptor, Message message) throws ServiceException {
		return service.callBlockingMethod(
				methodDescriptor,
				new RpcControllerImpl(),
				message
		);
	}

	//<---- End Blocking-Service-Helper- Methods

	//<--- End Server-Methods

	//----> Start Client-methods
	public void callRPCAndReturnResponse(Channel channel, UPAddress destUrn, MEP.ServiceType serviceType,
										 Descriptors.MethodDescriptor method, RpcController controller,
										 Message request, Message responsePrototype, RpcCallback<Message> done) {
		RpcEntry entry = new RpcEntry(controller, done, responsePrototype);
		Integer id = objectCache.add(entry);

		String fullServiceName = method.getService().getFullName();
		MEP.RPCMessage message = MEPPacketBuilder.createRpcMessage(fullServiceName, method.getName(), serviceType);
		MEP.MEPPacket mepPacket = MEPPacketBuilder.createReliableRpcRequest(id, message, request.toByteString(), false);

		ChannelFuture future = write(mepPacket, channel, destUrn);

		if (!future.isSuccess()) {
			entry.controller.setFailed(future.getCause().getMessage());
		}
	}

	private ChannelFuture write(MEP.MEPPacket packet, Channel channel, UPAddress remoteAddress) {
		return channel.write(packet, remoteAddress).awaitUninterruptibly();
	}

	private void setResponse(MEP.MEPPacket response) {
		RpcEntry entry = objectCache.remove(response.getRequestID());
		Message message = null;
		boolean setErrorMessage = true;
		if (entry == null) {
			logger.warn("Could not find RpcEntry for response: " + response);
			return;
		}
		if (entry.callback == null) {
			logger.warn("Could not find RpcCallback for response: " + response);
			return;
		}
		if (entry.controller == null) {
			logger.warn("Could not find RpcController for response: " + response + "! " +
					"There will be no error-messages set!");
			setErrorMessage = false;
		}

		if (response.getExceptionOccurred() && setErrorMessage) {
			entry.controller.setFailed(new String(response.getPayload().toByteArray()));
		} else {
			try {
				message = entry.responsePrototype.newBuilderForType().mergeFrom(response.getPayload().toByteArray())
						.build();
			} catch (InvalidProtocolBufferException e) {
				if (setErrorMessage) {
					entry.controller.setFailed(e.getMessage());
				}
			}
		}
		entry.callback.run(message);
	}


	//<--- End Client-methods
}
