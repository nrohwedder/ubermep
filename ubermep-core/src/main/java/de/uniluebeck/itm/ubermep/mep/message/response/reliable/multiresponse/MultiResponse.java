package de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse;

import com.google.common.collect.Multimap;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.ReliableResponse;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 12.08.11
 * Time: 16:48
 * To change this template use File | Settings | File Templates.
 */
public abstract class MultiResponse extends Response implements ReliableResponse {
	protected int total = -1;
	protected Multimap<UPAddress, Response> responses;
	//private Set<Response> responses;

	protected MultiResponse(Request request, Multimap<UPAddress, Response> responses) {
		super(request, null);
		this.responses = responses;
		this.total = responses.size();
	}

	public MultiResponse(Request request) {
		super(request, null);
	}

	public synchronized Multimap<UPAddress, Response> getResponses() {
		return responses;
	}

	public synchronized boolean receivedAllResponses() {
		return this.total == this.responses.size();
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) throws TotalAlreadySetException {
		if (total != -1) {
			throw new TotalAlreadySetException("Warning: Could not set total! " +
					"\nTotal messages already set to: " + total + "!");
		}
		this.total = total;
	}

	public void setResponses(Multimap<UPAddress, Response> responses) {
		this.total = responses.size();
		this.responses = responses;
	}

	public Collection<Response> getResponse(UPAddress address) {
		return responses.get(address);
	}

	@Override
	public String toString() {
		return "MultiResponse{" +
				"total=" + total +
				", responses=" + responses +
				'}';
	}

	private class TotalAlreadySetException extends Throwable {
		public TotalAlreadySetException(String message) {
			super(message);
		}
	}
}
