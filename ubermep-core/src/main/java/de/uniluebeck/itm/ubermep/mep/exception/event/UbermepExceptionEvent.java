package de.uniluebeck.itm.ubermep.mep.exception.event;

import de.uniluebeck.itm.ubermep.mep.protocol.MEP;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 06.09.11
 * Time: 14:44
 * To change this template use File | Settings | File Templates.
 */
public abstract class UbermepExceptionEvent extends Throwable {
	private final MEP.MessageType messageType;
	public UbermepExceptionEvent(Throwable cause, MEP.MessageType messageType) {
		super(cause);
		this.messageType = messageType;
	}

	public UbermepExceptionEvent(String message, MEP.MessageType messageType){
		super(message);
		this.messageType = messageType;
	}

	public MEP.MessageType getMessageType() {
		return messageType;
	}
}
