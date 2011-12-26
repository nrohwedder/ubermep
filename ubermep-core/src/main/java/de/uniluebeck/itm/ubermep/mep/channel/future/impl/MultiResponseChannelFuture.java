package de.uniluebeck.itm.ubermep.mep.channel.future.impl;

import de.uniluebeck.itm.ubermep.mep.channel.future.UbermepAbstractMultiResponseChannelFuture;
import org.jboss.netty.channel.Channel;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 02.09.11
 * Time: 21:03
 * To change this template use File | Settings | File Templates.
 */
public class MultiResponseChannelFuture extends UbermepAbstractMultiResponseChannelFuture {

	/**
	 * Creates a new instance.
	 *
	 * @param channel the {@link org.jboss.netty.channel.Channel} associated with this future
	 * @param expectedResponses expected SingleMultiResponse
	 */
	public MultiResponseChannelFuture(Channel channel, int expectedResponses) {
		super(channel, expectedResponses);
	}
}
