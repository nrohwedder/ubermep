package de.uniluebeck.itm.ubermep.mep.listener;

import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 27.10.11
 * Time: 13:55
 * To change this template use File | Settings | File Templates.
 */
public interface SingleRequestSingleResponseRequestListener{
	/**
	 *
	 * implement for reliable single-request-single-response-request
	 * the first implementation where array of return value is not null is used
	 *
	 * @param senderUrn the URN of the sender of the message received
	 * @param requestPayload the payload of the message received
	 * @return responsePayload the payload of the response message to be send
	 * 					if responsePayload is null, next SingleRequestSingleResponseRequestListener in list will be used
	 * @throws de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent if Exception occurs during process
	 */
	byte[] handleSingleRequestSingleResponseRequest(String senderUrn, byte[] requestPayload) throws UbermepExceptionEvent;

}
