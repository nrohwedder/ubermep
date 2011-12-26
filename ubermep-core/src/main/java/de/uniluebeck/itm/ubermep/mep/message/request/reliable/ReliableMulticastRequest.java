package de.uniluebeck.itm.ubermep.mep.message.request.reliable;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.request.MultiRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.MulticastRequest;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 28.07.11
 * Time: 18:26
 * To change this template use File | Settings | File Templates.
 */
public class ReliableMulticastRequest extends MultiRequest implements ReliableRequest, MulticastRequest {

	public ReliableMulticastRequest(Collection<UPAddress> destUrns, byte[] payload) {
		super(destUrns, payload);
	}

	@Override
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	@Override
	public void setTimeOutUnit(TimeUnit timeOutUnit) {
		this.timeOutUnit = timeOutUnit;
	}

	@Override
	public long getTimeOut() {
		return this.timeOut;
	}

	@Override
	public TimeUnit getTimeOutUnit() {
		return this.timeOutUnit;
	}

}
