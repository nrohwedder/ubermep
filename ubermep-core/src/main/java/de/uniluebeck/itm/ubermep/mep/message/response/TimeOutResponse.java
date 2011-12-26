package de.uniluebeck.itm.ubermep.mep.message.response;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 22.07.11
 * Time: 15:16
 * To change this template use File | Settings | File Templates.
 */
public class TimeOutResponse extends Response {
	private UPAddress localAddress;

	public TimeOutResponse(Request request, UPAddress localAddress) {
		super(request, null);
		this.localAddress = localAddress;
	}

	public UPAddress getLocalAddress() {
		return localAddress;
	}

	@Override
	public String toString() {
		return  "\nType: " + this.getClass().getSimpleName() +
				" - From: " + localAddress;
	}
}
