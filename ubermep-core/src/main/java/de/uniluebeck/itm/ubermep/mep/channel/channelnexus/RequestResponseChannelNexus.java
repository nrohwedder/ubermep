package de.uniluebeck.itm.ubermep.mep.channel.channelnexus;

import com.google.common.util.concurrent.ListenableFuture;
import de.uniluebeck.itm.ubermep.mep.channel.channels.AbstractRequestResponseChannel;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.11.11
 * Time: 21:06
 * To change this template use File | Settings | File Templates.
 */
public class RequestResponseChannelNexus <C extends AbstractRequestResponseChannel>
		implements ChannelUpstreamHandler, ChannelDownstreamHandler{
	private final C channel;

	public RequestResponseChannelNexus(C channel) {
		this.channel = channel;
	}

	public C getChannel() {
		return channel;
	}

	@SuppressWarnings("unchecked")
	public ListenableFuture<Response> write(Request request) throws ExecutionException, InterruptedException{
		return channel.write(request);
	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		channel.getPipeline().sendUpstream(e);
	}

	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		channel.getPipeline().sendDownstream(e);
	}
}
