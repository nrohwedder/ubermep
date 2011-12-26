package de.uniluebeck.itm.ubermep.mep.exception.event;

import de.uniluebeck.itm.ubermep.mep.protocol.MEP;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 06.09.11
 * Time: 16:50
 * To change this template use File | Settings | File Templates.
 */
public class UbermepMultiResponseExceptionEvent extends UbermepExceptionEvent {
	public UbermepMultiResponseExceptionEvent(Throwable cause) {
		super(cause, MEP.MessageType.MULTI_RESPONSE);
	}
}
