package de.uniluebeck.itm.uberlay;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.channel.*;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.08.11
 * Time: 11:22
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractApplicationChannel extends AbstractChannel implements ApplicationChannel {
	protected AbstractApplicationChannel(Channel parent, ChannelFactory factory, ChannelPipeline pipeline, ChannelSink sink) {
		super(parent, factory, pipeline, sink);
	}

	protected AbstractApplicationChannel(Integer id, Channel parent, ChannelFactory factory, ChannelPipeline pipeline, ChannelSink sink) {
		super(id, parent, factory, pipeline, sink);
	}

	@Override
	public Map<UPAddress, ChannelFuture> write(Object message, Collection<UPAddress> remoteAddresses) {
		Map<UPAddress, ChannelFuture> futureMap = new HashMap<UPAddress, ChannelFuture>();
		for (UPAddress remoteAddress : remoteAddresses) {
			ChannelFuture future = Channels.future(this);
			this.getPipeline().sendDownstream(new DownstreamMessageEvent(this, future, message, remoteAddress));
			futureMap.put(remoteAddress, future.awaitUninterruptibly());
		}
		return futureMap;
	}

	public void write(Object message, SocketAddress remoteAddress, ChannelFuture future) {
		this.getPipeline().sendDownstream(new DownstreamMessageEvent(this, future, message, remoteAddress));
	}

}
