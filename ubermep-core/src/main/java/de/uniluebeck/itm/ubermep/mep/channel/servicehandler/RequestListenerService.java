package de.uniluebeck.itm.ubermep.mep.channel.servicehandler;

import de.uniluebeck.itm.ubermep.mep.listener.MultiResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.listener.SingleRequestSingleResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.listener.UnicastMulticastRequestListener;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 27.10.11
 * Time: 14:25
 * To change this template use File | Settings | File Templates.
 */
public interface RequestListenerService {
	public void addRequestListener(UnicastMulticastRequestListener requestListener);

	public void addRequestListener(SingleRequestSingleResponseRequestListener requestListener);

	public void addRequestListener(MultiResponseRequestListener requestListener);
}
