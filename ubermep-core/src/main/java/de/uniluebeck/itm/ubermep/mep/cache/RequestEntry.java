package de.uniluebeck.itm.ubermep.mep.cache;

import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import org.jboss.netty.channel.ChannelFuture;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 02.09.11
 * Time: 20:56
 * To change this template use File | Settings | File Templates.
 */
public class RequestEntry {
	private final Request request;
	private final ChannelFuture future;

	public RequestEntry(Request request, ChannelFuture future) {
		this.request = request;
		this.future = future;
	}

	public Request getRequest() {
		return request;
	}

	public ChannelFuture getFuture() {
		return future;
	}
}
