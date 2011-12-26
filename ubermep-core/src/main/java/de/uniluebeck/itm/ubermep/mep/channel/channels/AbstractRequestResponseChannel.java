package de.uniluebeck.itm.ubermep.mep.channel.channels;

import de.uniluebeck.itm.uberlay.AbstractApplicationChannel;
import de.uniluebeck.itm.uberlay.UberlayBootstrap;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.channel.channelsink.RequestResponseChannelSink;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 21.08.11
 * Time: 12:46
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractRequestResponseChannel<U extends Request, V extends Response>
		extends AbstractApplicationChannel
		implements RequestResponseChannel<U, V> {
	protected final Logger log = LoggerFactory.getLogger(AbstractRequestResponseChannel.class);
	private final UberlayBootstrap bootstrap;

	public AbstractRequestResponseChannel(UberlayBootstrap bootstrap, ChannelPipeline pipeline) throws ExecutionException, InterruptedException {
		super(bootstrap.getApplicationChannel().get(), null, pipeline, new RequestResponseChannelSink(bootstrap));
		this.bootstrap = bootstrap;
	}

	public ChannelPipeline getPipeline() {
		return super.getPipeline();
	}

	public UberlayBootstrap getBootstrap() {
		return bootstrap;
	}

	@Override
	public ChannelConfig getConfig() {
		return getParent().getConfig();
	}

	@Override
	public boolean isBound() {
		return getParent().isBound();
	}

	@Override
	public boolean isConnected() {
		return getParent().isConnected();
	}

	@Override
	public SocketAddress getLocalAddress() {
		return getParent().getLocalAddress();
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return getParent().getRemoteAddress();
	}


	protected void createAndSendDownstreamEvent(Object message, UPAddress remoteAddress, ChannelFuture channelFuture) {
		DownstreamMessageEvent event = new DownstreamMessageEvent(this, channelFuture, message, remoteAddress);
		this.getPipeline().sendDownstream(event);
	}

	protected void createAndSendDownstreamEvent(Object message, Collection<UPAddress> remoteAddresses, ChannelFuture channelFuture){
		for (UPAddress address : remoteAddresses){
			createAndSendDownstreamEvent(message, address, channelFuture);
		}
	}

}
