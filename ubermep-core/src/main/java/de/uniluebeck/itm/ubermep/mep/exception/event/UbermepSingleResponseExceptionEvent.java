package de.uniluebeck.itm.ubermep.mep.exception.event;

import de.uniluebeck.itm.ubermep.mep.protocol.MEP;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 06.09.11
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public class UbermepSingleResponseExceptionEvent extends UbermepExceptionEvent {
	public UbermepSingleResponseExceptionEvent(Throwable cause) {
		super(cause, MEP.MessageType.SINGLE_RESPONSE);
	}

	public UbermepSingleResponseExceptionEvent(String message) {
		super(message, MEP.MessageType.SINGLE_RESPONSE);
	}
}
