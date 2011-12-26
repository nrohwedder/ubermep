package de.uniluebeck.itm.ubermep.mep.message.response;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 05.08.11
 * Time: 13:35
 * To change this template use File | Settings | File Templates.
 */
public class ErrorResponse extends Response{
	private Throwable cause;
	private UPAddress localAddress;
	public ErrorResponse(Request request, UPAddress localAddress,  Throwable cause) {
		super(request, cause.getMessage().getBytes());
		this.cause = cause;
		this.localAddress = localAddress;
	}

	public Throwable getCause() {
		return cause;
	}

	public UPAddress getLocalAddress() {
		return localAddress;
	}

	@Override
	public String toString() {
		return "ErrorResponse{" +
				"cause=" + cause +
				", address=" + localAddress +
				'}';
	}
}
