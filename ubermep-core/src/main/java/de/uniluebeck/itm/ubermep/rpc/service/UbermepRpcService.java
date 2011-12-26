package de.uniluebeck.itm.ubermep.rpc.service;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.rpc.channel.UbermepRpcChannel;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 05.12.11
 * Time: 08:18
 * To change this template use File | Settings | File Templates.
 */
public interface UbermepRpcService {
	public UbermepRpcChannel getRpcChannel(UPAddress urn) throws ExecutionException, InterruptedException;

	public void registerBlockingService(RpcBlockingService service);

	public void registerService(RpcService service);

}
