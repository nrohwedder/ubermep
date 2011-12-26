package de.uniluebeck.itm.ubermep.mep.channel.callable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.uberlay.ApplicationChannel;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.exception.RequestCouldNotBeDeliveredException;
import de.uniluebeck.itm.ubermep.mep.exception.TimeOutException;
import de.uniluebeck.itm.ubermep.mep.message.request.MultiRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.request.SingleRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableMulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableUnicastRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.ErrorResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.TimeOutResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.impl.ReliableMulticastResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse.ReliableUnicastResponse;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import de.uniluebeck.itm.ubermep.mep.protocol.MEPPacketBuilder;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.09.11
 * Time: 11:34
 * Response-Callable for Unicast/Multicast - Requests
 */
public class ResponseCallable implements Callable<Response> {
	private final Request request;
	private final Channel applicationChannel;
	private final int timeOut;
	private final TimeUnit timeUnit;

	public ResponseCallable(final Request request, final Channel applicationChannel,
							final int timeOut, final TimeUnit timeUnit) {
		this.request = request;
		this.applicationChannel = applicationChannel;
		this.timeOut = timeOut;
		this.timeUnit = timeUnit;
	}

	@Override
	public Response call() throws Exception {
		MEP.MEPPacket mepPacket = MEPPacketBuilder.createMEPPacketFromRequest(request, null);
		ChannelFuture future;
		if (request instanceof SingleRequest) {
			UPAddress remoteAddress = ((SingleRequest) request).getDestUrn();
			future = applicationChannel.write(mepPacket, remoteAddress);
			return createResponseFromFutureOrResponseMap(future, remoteAddress, null);
		} else if (request instanceof MultiRequest) {
			MultiRequest multiRequest = ((MultiRequest) request);
			return createResponseFromFutureOrResponseMap(null, null,
					createMulticastResponsesFromFutureMap(
							((ApplicationChannel) applicationChannel).write(mepPacket, multiRequest.getDestUrns())
					)
			);
		}
		return null;
	}

	private Multimap<UPAddress, Response> createMulticastResponsesFromFutureMap
			(Map<UPAddress, ChannelFuture> channelFutureMap) {
		Multimap<UPAddress, Response> responses = HashMultimap.create();
		for (UPAddress remoteAddress : channelFutureMap.keySet()) {
			responses.put(
					remoteAddress,
					createResponseFromFutureOrResponseMap(channelFutureMap.get(remoteAddress), remoteAddress, null)
			);
		}
		return responses;
	}

	private Response createResponseFromFutureOrResponseMap(ChannelFuture future, UPAddress address,
														   Multimap<UPAddress, Response> responseMap) {
		if (future == null) {
			return new ReliableMulticastResponse(request, responseMap);
		} else {
			if (!future.isDone()) {
				try {
					long timeOutInMillis;
					if (request.hasTimeOut()) {
						ReliableRequest reliableRequest = (ReliableRequest) request;
						timeOutInMillis = reliableRequest.getTimeOutUnit().toMillis(reliableRequest.getTimeOut());
					} else {
						timeOutInMillis = timeUnit.toMillis(timeOut);
					}
					if (!future.await(timeOutInMillis)) {
						future.setFailure(new TimeOutException(address));
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			if (future.isSuccess()) {
				if (request instanceof ReliableUnicastRequest) {
					return new ReliableUnicastResponse(request);
				} else if (request instanceof ReliableMulticastRequest) {
					assert address != null;
					return new ReliableMulticastResponse.SingleReliableMulticastResponse(request, address);
				}
			}

			//future is not success
			return createErrorOrTimeOutResponse(future, address);
		}
	}

	private Response createErrorOrTimeOutResponse(ChannelFuture future, UPAddress address) {
		if (request instanceof SingleRequest && address == null) {
			address = new UPAddress(((SingleRequest) request).getDestUrn());
		}
		Throwable cause = future.getCause();
		if (cause == null) {
			cause = new RequestCouldNotBeDeliveredException("Warning: Could not evaluate failure of transport!");
		}
		if (cause instanceof TimeOutException) {
			return new TimeOutResponse(request, address);
		} else {
			return new ErrorResponse(request, address, cause);
		}
	}

}
