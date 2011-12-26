package de.uniluebeck.itm.ubermep;

import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.RequestListenerService;
import de.uniluebeck.itm.ubermep.service.Service;
import de.uniluebeck.itm.ubermep.service.UbermepService;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 27.10.11erm
 * Time: 07:18
 * To change this template use File | Settings | File Templates.
 */
public interface Peer extends Service, RequestListenerService, UbermepService {
	public UPAddress getLocalUPAddress();

	public InetSocketAddress getLocalSocketAddress();

	public InetSocketAddress getRemoteSocketAddress();

	/**
	 * connects an channel to a Remote-Address of a Peer
	 * @param remoteAddress building a channel to
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void connect(InetSocketAddress remoteAddress) throws ExecutionException, InterruptedException;
}
