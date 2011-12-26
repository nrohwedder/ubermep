package de.uniluebeck.itm.ubermep.mep.exception;

import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;
import org.jboss.netty.channel.ChannelFuture;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 22.11.11
 * Time: 19:28
 * To change this template use File | Settings | File Templates.
 */
public class WrongUbermepAbstractChannelFutureException extends Throwable {
	public WrongUbermepAbstractChannelFutureException(ChannelFuture future, ReliableRequest request) {
		super(" Wrong ChannelFuture: '" + future.getClass().getSimpleName() + "' " +
				" for Request: " + request.getClass().getSimpleName() + "! ");
	}
}
