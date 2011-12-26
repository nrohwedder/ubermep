package de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse;

import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse.SingleResponse;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 16.08.11
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
public class SingleMultiResponseResponse extends SingleResponse {
	private final int current;
	protected final int total;
	public SingleMultiResponseResponse(Request request, byte[] payload, int current, int total) {
		super(request, payload);
		this.current = current;
		this.total = total;
	}

	public int getCurrent() {
		return current;
	}

	public int getTotal() {
		return total;
	}
}
