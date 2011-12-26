package de.uniluebeck.itm.ubermep.mep.exception;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 13.10.11
 * Time: 16:33
 * To change this template use File | Settings | File Templates.
 */
public class TimeOutException extends Exception{
	UPAddress peerAddress;

	public TimeOutException(UPAddress peerAddress) {
		this.peerAddress = peerAddress;
	}

	public UPAddress getPeerAddress() {
		return peerAddress;
	}

	@Override
	public String getMessage() {
		return "TimeOutException: TimeOut occurred sending message to peer: '" + peerAddress + "'!";
	}

}
