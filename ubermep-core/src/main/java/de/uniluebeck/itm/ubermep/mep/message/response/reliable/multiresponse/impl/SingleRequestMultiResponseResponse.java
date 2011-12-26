package de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.impl;

import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.MultiResponse;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 26.07.11
 * Time: 15:03
 * To change this template use File | Settings | File Templates.
 */
public class SingleRequestMultiResponseResponse extends MultiResponse {
	public SingleRequestMultiResponseResponse(Request request){
		super(request);
	}
}
