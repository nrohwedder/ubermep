package de.uniluebeck.itm.ubermep.mep.message.request;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 26.07.11
 * Time: 10:31
 * To change this template use File | Settings | File Templates.
 */
public abstract class MultiRequest extends Request {
	protected final Collection<UPAddress> destUrns;

	protected MultiRequest(Collection<UPAddress> destUrns, byte[] payload){
		super(payload);
		this.destUrns = destUrns;
	}

	public Collection<UPAddress> getDestUrns() {
		return destUrns;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		MultiRequest that = (MultiRequest) o;

		if (destUrns != null ? !destUrns.equals(that.destUrns) : that.destUrns != null) return false;

		return true;
	}

}
