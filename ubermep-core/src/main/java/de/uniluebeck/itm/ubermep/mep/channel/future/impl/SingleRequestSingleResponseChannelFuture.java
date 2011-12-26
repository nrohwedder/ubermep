package de.uniluebeck.itm.ubermep.mep.channel.future.impl;

import de.uniluebeck.itm.ubermep.mep.exception.TimeOutException;
import de.uniluebeck.itm.ubermep.mep.channel.future.UbermepAbstractSingleResponseChannelFuture;
import de.uniluebeck.itm.ubermep.mep.message.response.ErrorResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.uberlay.NoRouteToPeerException;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.response.TimeOutResponse;
import org.jboss.netty.channel.Channel;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 02.09.11
 * Time: 20:23
 * To change this template use File | Settings | File Templates.
 */
public class SingleRequestSingleResponseChannelFuture extends UbermepAbstractSingleResponseChannelFuture<Response> {

	/**
	 * Creates a new instance.
	 *
	 * @param channel the {@link org.jboss.netty.channel.Channel} associated with this future
	 */
	public SingleRequestSingleResponseChannelFuture(Channel channel) {
		super(channel);
	}

	@Override
	public synchronized boolean setFailure(Throwable cause) {
		UPAddress address;
		if (cause instanceof NoRouteToPeerException) {
			address = ((NoRouteToPeerException) cause).getPeerAddress();
			setResponse(new ErrorResponse(null, address, cause));
		} else if (cause instanceof TimeOutException) {
			address = ((TimeOutException) cause).getPeerAddress();
			setResponse(new TimeOutResponse(null, address));
		} else {
			throw new RuntimeException("Could not add failure response to responseMap!");
		}
		return super.setFailure(cause);
	}

}
