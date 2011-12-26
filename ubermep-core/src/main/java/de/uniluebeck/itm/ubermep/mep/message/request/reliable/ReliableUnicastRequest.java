package de.uniluebeck.itm.ubermep.mep.message.request.reliable;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.request.SingleRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.UnicastRequest;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 28.07.11
 * Time: 18:25
 * To change this template use File | Settings | File Templates.
 */
public class ReliableUnicastRequest extends SingleRequest implements ReliableRequest, UnicastRequest {
	public ReliableUnicastRequest(UPAddress destUrn, byte[] payload) {
		super(destUrn, payload);
	}

	@Override
	public String toString() {
		return "ReliableUnicastRequest{" + super.toString() + "}";
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
