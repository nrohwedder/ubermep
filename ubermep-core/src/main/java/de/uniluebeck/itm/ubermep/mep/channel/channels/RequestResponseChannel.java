package de.uniluebeck.itm.ubermep.mep.channel.channels;

import com.google.common.util.concurrent.ListenableFuture;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import org.jboss.netty.channel.ChannelPipeline;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 23.08.11
 * Time: 15:16
 * To change this template use File | Settings | File Templates.
 */
public interface RequestResponseChannel<U extends Request, V extends Response> {

	ListenableFuture<V> write(U request) throws ExecutionException, InterruptedException;

	ChannelPipeline getPipeline();
}
