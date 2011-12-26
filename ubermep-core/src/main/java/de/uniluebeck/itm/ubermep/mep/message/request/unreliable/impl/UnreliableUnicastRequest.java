package de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.request.SingleRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.UnicastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.UnreliableRequest;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 18.07.11
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public class UnreliableUnicastRequest extends SingleRequest implements UnreliableRequest, UnicastRequest {
	public UnreliableUnicastRequest(UPAddress destUrn, byte[] payload) {
		super(destUrn, payload);
	}

	@Override
	public String toString() {
		return "UnreliableUnicastRequest{" + super.toString() + "}";
	}
}
