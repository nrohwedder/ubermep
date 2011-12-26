package de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse;

import de.uniluebeck.itm.ubermep.mep.message.request.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 26.07.11
 * Time: 10:14
 * To change this template use File | Settings | File Templates.
 */
public class SingleRequestSingleResponseResponse extends SingleResponse {
	public SingleRequestSingleResponseResponse(Request request, byte[] payload) {
		super(request, payload);
	}
}
