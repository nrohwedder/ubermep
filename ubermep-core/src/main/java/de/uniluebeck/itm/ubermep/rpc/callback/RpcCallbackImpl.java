package de.uniluebeck.itm.ubermep.rpc.callback;

import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 28.10.11
 * Time: 12:00
 * To change this template use File | Settings | File Templates.
 */
public class RpcCallbackImpl<T extends Message> extends MEPRpcCallback<T> {
	Logger log = LoggerFactory.getLogger(RpcCallbackImpl.class);
	
	@Override
	public synchronized void run(T response) {
		if (response == null) {
			log.warn("Warning: Response is null! Possible Exception-Occurrence! " +
					"Please see controller for further details!");
		}
		super.run(response);
	}
}
