package de.uniluebeck.itm.ubermep;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;

import java.net.InetSocketAddress;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.07.11
 * Time: 10:34
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractPeer implements Peer {
	protected final UPAddress localUPAddress;
	protected final InetSocketAddress localSocketAddress;
	protected final InetSocketAddress remoteSocketAddress;

	public AbstractPeer(UPAddress urn, InetSocketAddress localSocketAddress, InetSocketAddress remoteSocketAddress) {
		this.localUPAddress = urn;
		this.localSocketAddress = localSocketAddress;
		this.remoteSocketAddress = remoteSocketAddress;
	}

	public UPAddress getLocalUPAddress() {
		return localUPAddress;
	}

	public InetSocketAddress getLocalSocketAddress() {
		return localSocketAddress;
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return remoteSocketAddress;
	}

	public static InetSocketAddress buildSocketAddress(final String host, final String portString) {
		final int port = Integer.parseInt(portString);
		return new InetSocketAddress(host, port);
	}
}