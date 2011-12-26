package de.uniluebeck.itm.ubermep.mep.service;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.UnreliableRequest;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 28.07.11
 * Time: 18:20
 * To change this template use File | Settings | File Templates.
 */
public interface UbermepUnreliableService {
	public void send(UnreliableRequest request) throws ExecutionException, InterruptedException;

	public void send(Object object, UPAddress urn) throws ExecutionException, InterruptedException;
}
