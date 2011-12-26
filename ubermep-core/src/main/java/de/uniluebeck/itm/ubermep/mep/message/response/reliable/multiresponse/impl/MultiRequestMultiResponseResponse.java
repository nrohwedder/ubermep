package de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.impl;

import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.MultiResponse;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 16.08.11
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
public class MultiRequestMultiResponseResponse extends MultiResponse {

	public MultiRequestMultiResponseResponse(Request request) {
		super(request);
	}

	@Override
	public synchronized boolean receivedAllResponses() {
		if (super.responses.size() != super.total) {
			return false;
		}
		for (UPAddress address : super.responses.keySet()) {
			for (Response response : super.responses.get(address)) {
				if (response instanceof MultiResponse) {
					if (!((MultiResponse) response).receivedAllResponses()) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
