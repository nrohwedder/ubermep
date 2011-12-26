package de.uniluebeck.itm.ubermep.mep.listener;

import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.handle.MultiResponseHandle;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 27.10.11
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
public interface MultiResponseRequestListener{
	/**
	 * implement for reliable single-request-multi-response-request and multi-request-multi-response-request
	 * the first implementation where return value is true is used
	 * @param responseHandle to handle multiResponse
	 * @param senderUrn the URN of the sender of the message received
	 * @param requestPayload the payload of the message received
	 * @return if return value is false, next MultiResponseRequestListener in list will be used
	 * @throws de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent if Exception occurs during process
	 */
	boolean handleMultiResponseRequest(MultiResponseHandle responseHandle, String senderUrn, byte[] requestPayload)
			throws UbermepExceptionEvent;
}
