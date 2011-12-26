package de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse;

import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.ReliableResponse;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.07.11
 * Time: 11:43
 * To change this template use File | Settings | File Templates.
 */
public abstract class SingleResponse extends Response implements ReliableResponse {
	public SingleResponse(Request request, byte[] payload) {
		super(request, payload);
	}

	public Request getRequest() {
		return request;
	}

	public byte[] getPayload() {
		return payload;
	}
}
