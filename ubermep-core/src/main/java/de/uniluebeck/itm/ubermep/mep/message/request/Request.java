package de.uniluebeck.itm.ubermep.mep.message.request;

import de.uniluebeck.itm.ubermep.mep.message.Message;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 18.07.11
 * Time: 11:36
 * To change this template use File | Settings | File Templates.
 */
public abstract class Request extends Message {
	protected long timeOut = 0;
	protected TimeUnit timeOutUnit;

	protected Request(byte[] payload) {
		super(payload);
	}

	public boolean hasTimeOut() {
		return timeOut != 0;
	}
}
