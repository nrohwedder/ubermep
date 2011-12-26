package de.uniluebeck.itm.ubermep.mep.protocol;

import com.google.protobuf.ByteString;
import de.uniluebeck.itm.ubermep.mep.message.Message;
import de.uniluebeck.itm.ubermep.mep.message.ReliableMessage;
import de.uniluebeck.itm.ubermep.mep.message.UnreliableMessage;
import de.uniluebeck.itm.ubermep.mep.message.request.MulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.request.UnicastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.MultiRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.SingleRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.impl.MultiRequestMultiResponseResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.impl.SingleRequestMultiResponseResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse.SingleRequestSingleResponseResponse;

import static de.uniluebeck.itm.ubermep.mep.protocol.MEP.MessageType.*;
import static de.uniluebeck.itm.ubermep.mep.protocol.MEP.MessageType.RPC_REQUEST;
import static de.uniluebeck.itm.ubermep.mep.protocol.MEP.MessageType.RPC_RESPONSE;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.08.11
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public class MEPPacketBuilder {

	public static MEP.MEPPacket createMEPPacketFromMessage(Message message, Integer requestID) {
		if (message instanceof Request) {
			return createMEPPacketFromRequest((Request) message, requestID);
		} else if (message instanceof Response) {
			return createMEPPacketFromResponse((Response) message);
		}
		return null;
	}

	public static MEP.MEPPacket createMEPPacketFromRequest(Request request, Integer requestID) {
		if (request instanceof UnicastRequest) {
			return createUnicastPacket(request);
		} else if (request instanceof MulticastRequest) {
			return createMulticastPacket(request);
		} else if (request instanceof SingleRequestSingleResponseRequest) {
			return createSingleRequestSingleResponseRequestPacket(request, requestID);
		} else if (request instanceof SingleRequestMultiResponseRequest) {
			return createMultiResponseRequestPacket(request, requestID);
		} else if (request instanceof MultiRequestMultiResponseRequest) {
			return createMultiResponseRequestPacket(request, requestID);
		}
		return null;
	}

	public static MEP.MEPPacket createMEPPacketFromResponse(Response response) {
		if (response instanceof SingleRequestSingleResponseResponse) {
			return createSingleRequestSingleResponseResponsePacket(response);
		} else if (response instanceof SingleRequestMultiResponseResponse) {
			return createMultiResponseResponsePacket(response);
		} else if (response instanceof MultiRequestMultiResponseResponse) {
			return createMultiResponseResponsePacket(response);
		}
		return null;
	}


	private static MEP.MEPPacket.Builder createDefaultMessageBuilder(Message message) {
		MEP.MEPPacket.Builder builder = MEP.MEPPacket.newBuilder().
				setPayload(ByteString.copyFrom(message.getPayload()));
		if (message instanceof UnreliableMessage) {
			return builder.setReliable(false);
		} else if (message instanceof ReliableMessage) {
			return builder.setReliable(true);
		}
		return null;
	}

	private static MEP.MEPPacket.Builder createDefaultRequestBuilder(Message message, Integer requestID) {
		return createDefaultMessageBuilder(message).setRequestID(requestID);
	}

	private static MEP.MEPPacket createUnicastPacket(Request request) {
		return createDefaultMessageBuilder(request).setMessageType(MEP.MessageType.UNICAST).build();
	}

	private static MEP.MEPPacket createMulticastPacket(Request request) {
		return createDefaultMessageBuilder(request).setMessageType(MEP.MessageType.MULTICAST).build();
	}

	private static MEP.MEPPacket createSingleRequestSingleResponseRequestPacket(Request request, Integer requestID) {
		return createDefaultRequestBuilder(request, requestID).setMessageType(SINGLE_RESPONSE_REQUEST).build();
	}

	private static MEP.MEPPacket createSingleRequestSingleResponseResponsePacket(Response response) {
		return createDefaultMessageBuilder(response).setMessageType(SINGLE_RESPONSE).build();
	}

	private static MEP.MEPPacket createMultiResponseRequestPacket(Request request, Integer requestID) {
		return createDefaultRequestBuilder(request, requestID).setMessageType(MULTI_RESPONSE_REQUEST).build();
	}

	private static MEP.MEPPacket createMultiResponseResponsePacket(Response response) {
		return createDefaultMessageBuilder(response).setMessageType(MULTI_RESPONSE).build();
	}

	public static MEP.RPCMessage createRpcMessage(String serviceName, String methodName, MEP.ServiceType serviceType) {
		return MEP.RPCMessage.newBuilder()
				.setServiceName(serviceName)
				.setMethodName(methodName)
				.setServiceType(serviceType)
				.build();
	}

	public static MEP.MEPPacket createReliableRpcRequest(Integer id, MEP.RPCMessage message, ByteString payload,
														 boolean exceptionOccured) {
		return createReliableRpcRequestResponse(id, message, payload, RPC_REQUEST, exceptionOccured);
	}

	public static MEP.MEPPacket createReliableRpcResponse(Integer id, MEP.RPCMessage message, ByteString payload,
														  boolean exceptionOccured) {
		return createReliableRpcRequestResponse(id, message, payload, RPC_RESPONSE, exceptionOccured);
	}

	private static MEP.MEPPacket createReliableRpcRequestResponse(Integer id, MEP.RPCMessage message,
																  ByteString payload, MEP.MessageType messageType,
																  boolean exceptionOccured) {
		return MEP.MEPPacket.newBuilder()
				.setRequestID(id)
				.setPayload(payload)
				.setReliable(true)
				.setRpcMessage(message)
				.setMessageType(messageType)
				.setExceptionOccurred(exceptionOccured).build();
	}

	public static boolean isRequest(MEP.MEPPacket message) {
		switch (message.getMessageType()) {
			case SINGLE_RESPONSE_REQUEST:
				return true;
			case MULTI_RESPONSE_REQUEST:
				return true;
		}
		return false;
	}

	public static boolean isResponse(MEP.MEPPacket message) {
		switch (message.getMessageType()) {
			case SINGLE_RESPONSE:
				return true;
			case MULTI_RESPONSE:
				return true;
		}
		return false;
	}

	public static boolean isRequestResponseMessage(MEP.MEPPacket message) {
		return (isRequest(message) || isResponse(message));
	}

	public static boolean isRpcRequestResponse(MEP.MEPPacket message) {
		switch (message.getMessageType()) {
			case RPC_REQUEST:
				return true;
			case RPC_RESPONSE:
				return true;
		}
		return false;
	}

	public static boolean isRpcRequest(MEP.MEPPacket message) {
		switch (message.getMessageType()) {
			case RPC_REQUEST:
				return true;
		}
		return false;
	}

	public static boolean isRpcResponse(MEP.MEPPacket message) {
		switch (message.getMessageType()) {
			case RPC_RESPONSE:
				return true;
		}
		return false;
	}

	public static boolean isReliable(MEP.MEPPacket message) {
		return message.getReliable();
	}

	public static boolean isReliableUnicastOrMulticast(MEP.MEPPacket message) {
		if (!message.getReliable()) {
			return false;
		}
		switch (message.getMessageType()) {
			case UNICAST:
				return true;
			case MULTICAST:
				return true;
		}
		return false;
	}
}
