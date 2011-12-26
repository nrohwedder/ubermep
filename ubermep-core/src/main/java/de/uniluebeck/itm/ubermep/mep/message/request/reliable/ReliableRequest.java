package de.uniluebeck.itm.ubermep.mep.message.request.reliable;

import de.uniluebeck.itm.ubermep.mep.message.ReliableMessage;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.07.11
 * Time: 17:13
 * To change this template use File | Settings | File Templates.
 */
public interface ReliableRequest extends ReliableMessage {
	public void setTimeOut(int timeOut);
	public void setTimeOutUnit(TimeUnit timeOutUnit);
	public boolean hasTimeOut();
	public long getTimeOut();
	public TimeUnit getTimeOutUnit();
}
