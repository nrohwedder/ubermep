package de.uniluebeck.itm.ubermep.mep.channel.future;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.uniluebeck.itm.uberlay.NoRouteToPeerException;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.exception.TimeOutException;
import de.uniluebeck.itm.ubermep.mep.message.request.SingleRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.ErrorResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.TimeOutResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.SingleMultiResponseResponse;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 12.10.11
 * Time: 17:13
 * To change this template use File | Settings | File Templates.
 */
public abstract class UbermepAbstractMultiResponseChannelFuture extends UbermepAbstractChannelFuture {
	protected Multimap<UPAddress, Response> responseMap = HashMultimap.create();

	//amount of single MultiResponses for each UPAddress
	protected final int expectedResponses;
	protected final Map<UPAddress, Throwable> causeMap = new HashMap<UPAddress, Throwable>();
	protected final Logger logger = LoggerFactory.getLogger(UbermepAbstractMultiResponseChannelFuture.class);

	/**
	 * Creates a new instance.
	 *
	 * @param channel		   the {@link org.jboss.netty.channel.Channel} associated with this future
	 * @param expectedResponses expected SingleMultiResponses
	 */
	public UbermepAbstractMultiResponseChannelFuture(Channel channel, int expectedResponses) {
		super(channel);
		this.expectedResponses = expectedResponses;
	}

	public synchronized void addResponse(UPAddress address, Response response) {
		this.responseMap.put(address, response);
		if (receivedAllResponses()) {
			done();
		}
	}

	public Multimap<UPAddress, Response> getResponses() {
		if (!isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return responseMap;
	}

	@Override
	public synchronized boolean isSuccess() {
		return isDone() && (causeMap.size() == 0);
	}

	@Override
	public synchronized boolean setFailure(Throwable cause) {
		UPAddress address;
		if (cause instanceof NoRouteToPeerException) {
			address = ((NoRouteToPeerException) cause).getPeerAddress();
			addResponse(address, new ErrorResponse(null, address, cause));
		} else if (cause instanceof TimeOutException) {
			address = ((TimeOutException) cause).getPeerAddress();
			addResponse(address, new TimeOutResponse(null, address));
		} else {
			throw new RuntimeException("Could not add failure response to responseMap!");
		}
		causeMap.put(address, cause);

		if (isDone()) {
			return super.setFailure(cause);
		}
		return true;
	}

	public Map<UPAddress, Throwable> getCauseMap() {
		return causeMap;
	}

	@Override
	public synchronized boolean cancel() {
		done();
		return super.cancel();
	}

	private synchronized boolean receivedAllResponses() {
		if (responseMap.size() == 0 || responseMap.keySet().size() != expectedResponses) {
			return false;
		}
		try {
			for (UPAddress address : responseMap.keySet()) {
				if (!receivedAllResponses(responseMap.get(address))) {
					return false;
				}
			}
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	private boolean receivedAllResponses(Collection<Response> responses) {
		for (Response response : responses) {
			if (response instanceof SingleMultiResponseResponse) {
				if (((SingleMultiResponseResponse) response).getTotal() == responses.size()) {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	public synchronized void addTimeOutResponse(SingleRequest request) {
		setFailure(new TimeOutException(request.getDestUrn()));
	}

	public synchronized void progress(String senderUrn, byte[] payload, int current, int total){
		super.progress(senderUrn, payload, current, total);
	}


}
