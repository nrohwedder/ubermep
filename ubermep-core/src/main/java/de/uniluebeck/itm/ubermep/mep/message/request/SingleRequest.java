package de.uniluebeck.itm.ubermep.mep.message.request;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 26.07.11
 * Time: 10:25
 * To change this template use File | Settings | File Templates.
 */
public abstract class SingleRequest extends Request {
	protected final UPAddress destUrn;

	protected SingleRequest(UPAddress destUrn, byte[] payload){
		super(payload);
		this.destUrn = destUrn;
	}

	public UPAddress getDestUrn() {
		return destUrn;
	}

	@Override
	public String toString() {
		return "SingleRequest{" +
				"destUrn='" + destUrn + '\'' +
				'}' + super.toString();
	}
}
