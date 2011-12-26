package de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.RequestResponseMessage;
import de.uniluebeck.itm.ubermep.mep.message.request.SingleRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 26.07.11
 * Time: 10:37
 * To change this template use File | Settings | File Templates.
 */
public class SingleRequestMultiResponseRequest extends SingleRequest implements ReliableRequest, RequestResponseMessage, MultiResponseRequest{

	public SingleRequestMultiResponseRequest(UPAddress destUrn, byte[] payload) {
		super(destUrn, payload);
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
