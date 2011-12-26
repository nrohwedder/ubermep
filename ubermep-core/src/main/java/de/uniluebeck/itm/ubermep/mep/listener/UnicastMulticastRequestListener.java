package de.uniluebeck.itm.ubermep.mep.listener;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 27.10.11
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
public interface UnicastMulticastRequestListener{

	/**
	 * Implement for handling unicast / multicast.
	 *
	 * @param senderUrn the URN of the sender of the message received
	 * @param payload the payload of the message received
	 */
	
	boolean handleUnicastMulticastRequest(String senderUrn, byte[] payload);
}
