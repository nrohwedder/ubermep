package de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.request.MultiRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.MulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.UnreliableRequest;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.07.11
 * Time: 17:52
 * To change this template use File | Settings | File Templates.
 */
public class UnreliableMulticastRequest extends MultiRequest implements UnreliableRequest, MulticastRequest {
	public UnreliableMulticastRequest(Collection<UPAddress> destUrns, byte[] payload) {
		super(destUrns, payload);
	}
}
