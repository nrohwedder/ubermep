package de.uniluebeck.itm.ubermep.service;

import de.uniluebeck.itm.ubermep.mep.service.UbermepReliableService;
import de.uniluebeck.itm.ubermep.mep.service.UbermepUnreliableService;
import de.uniluebeck.itm.ubermep.rpc.service.UbermepRpcService;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 18.07.11
 * Time: 11:34
 * To change this template use File | Settings | File Templates.
 */
public interface UbermepService extends UbermepUnreliableService, UbermepReliableService, UbermepRpcService {
	/**
	 *	call for support of manual added SimpleChannelHandler
	 *
	 * @param channelHandler handler to be registered
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void registerChannelHandler(SimpleChannelHandler channelHandler) throws ExecutionException, InterruptedException;

	/**
	 *	call for support of manual added ChannelUpstreamHandler
	 *
	 * @param upstreamHandler handler to be registered
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void registerUpstreamHandler(ChannelUpstreamHandler upstreamHandler) throws ExecutionException, InterruptedException;

	/**
	 *	call for support of manual added ChannelDownstreamHandler
	 *
	 * @param downstreamHandler handler to be registered
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void registerDownstreamHandler(ChannelDownstreamHandler downstreamHandler) throws ExecutionException, InterruptedException;

}
