package de.uniluebeck.itm.ubermep.mep.message.response;

import de.uniluebeck.itm.ubermep.mep.message.Message;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 18.07.11
 * Time: 11:35
 * To change this template use File | Settings | File Templates.
 */
public abstract class Response extends Message {
	protected final Request request;

	protected Response(Request request, byte[] payload) {
		super(payload);
		this.request = request;
	}

	public Request getRequest() {
		return request;
	}

	@Override
	public String toString() {
		return super.toString() + "Response{" +
				"request=" + (request != null ? request : "null") +
				'}';
	}
}
