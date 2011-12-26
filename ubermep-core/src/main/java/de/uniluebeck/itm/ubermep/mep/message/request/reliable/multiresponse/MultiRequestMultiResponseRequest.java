package de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.RequestResponseMessage;
import de.uniluebeck.itm.ubermep.mep.message.request.MultiRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 10.08.11
 * Time: 17:05
 * To change this template use File | Settings | File Templates.
 */
public class MultiRequestMultiResponseRequest extends MultiRequest implements ReliableRequest, RequestResponseMessage, MultiResponseRequest{
	public MultiRequestMultiResponseRequest(Collection<UPAddress> destUrns, byte[] payload) {
		super(destUrns, payload);
	}

	public static class SingleMultiRequestMultiResponseRequest extends SingleRequestMultiResponseRequest{
		public SingleMultiRequestMultiResponseRequest(UPAddress destUrn, byte[] payload) {
			super(destUrn, payload);
		}
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
