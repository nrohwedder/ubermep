package de.uniluebeck.itm.uberlay;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;

public interface ApplicationChannel extends Channel {

	public Map<UPAddress, ChannelFuture> write(Object message, Collection<UPAddress> remoteAddresses);

	public void write(Object message, SocketAddress remoteAddress, ChannelFuture future);
}
