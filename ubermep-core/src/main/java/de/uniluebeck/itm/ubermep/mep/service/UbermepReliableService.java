package de.uniluebeck.itm.ubermep.mep.service;

import com.google.common.util.concurrent.ListenableFuture;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.channel.future.UbermepAbstractChannelFuture;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 28.07.11
 * Time: 18:22
 * To change this template use File | Settings | File Templates.
 */
public interface UbermepReliableService {
	public ListenableFuture<Response> send(ReliableRequest request) throws ExecutionException, InterruptedException;
	public <T extends UbermepAbstractChannelFuture> T send(Object object, UPAddress urn, Class<T> channelFutureClass)
			throws ExecutionException, InterruptedException;
}
