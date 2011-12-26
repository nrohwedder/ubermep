package de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse;

import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.ReliableResponse;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 05.08.11
 * Time: 13:03
 * To change this template use File | Settings | File Templates.
 */
public class ReliableUnicastResponse extends Response implements ReliableResponse {
	public ReliableUnicastResponse(Request request) {
		super(request, null);
	}

	@Override
	public String toString() {
		return "ReliableUnicastResponse{request=" + request +"}";
	}
}
