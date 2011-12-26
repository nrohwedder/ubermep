package de.uniluebeck.itm.example.servlet;

import de.uniluebeck.itm.handlerstack.FilterPipeline;
import de.uniluebeck.itm.handlerstack.FilterPipelineImpl;
import de.uniluebeck.itm.tr.util.Tuple;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.channel.future.impl.SingleRequestSingleResponseChannelFuture;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse.SingleRequestSingleResponseResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 11.10.11
 * Time: 15:47
 * To change this template use File | Settings | File Templates.
 */
public class HttpServletChannelDownstreamHandler implements ChannelDownstreamHandler {
	FilterPipeline pipeline = new FilterPipelineImpl();

	public HttpServletChannelDownstreamHandler() {
		List<Tuple<String, ChannelHandler>> tuples = new ArrayList<Tuple<String, ChannelHandler>>();
		tuples.add(new Tuple<String, ChannelHandler>("encoder", new HttpRequestEncoder()));
		tuples.add(new Tuple<String, ChannelHandler>("decoder", new HttpResponseDecoder()));
		pipeline.setChannelPipeline(tuples);
	}

	@Override
	public void handleDownstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {
		if (((DownstreamMessageEvent) e).getMessage() instanceof HttpRequest) {
			final HttpRequest request = (HttpRequest) ((DownstreamMessageEvent) e).getMessage();

			final ChannelFuture future = new SingleRequestSingleResponseChannelFuture(ctx.getChannel());
			final String urn = ((DownstreamMessageEvent) e).getRemoteAddress().toString();

			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					// additional: check for error of future if needed
					Response response = ((SingleRequestSingleResponseChannelFuture) future).getResponse();
					if (response instanceof SingleRequestSingleResponseResponse) {
						ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(response.getPayload());

						pipeline.addListener(createUpstreamOutputListener(e, buffer));
						pipeline.sendUpstream(buffer);
					} else {
						e.getFuture().setFailure(future.getCause());
					}
				}
			});

			pipeline.addListener(createDownstreamOutputListener(ctx, future, urn));
			pipeline.sendDownstream(request);

		} else {
			ctx.sendDownstream(e);
		}
	}

	private FilterPipeline.UpstreamOutputListener createUpstreamOutputListener(final ChannelEvent e, final ChannelBuffer buffer) {
		return new FilterPipeline.UpstreamOutputListener() {
			@Override
			public void receiveUpstreamOutput(Object o) {
				((HttpResponse) o).setContent(buffer);
				((HttpServletChannelFuture) e.getFuture()).setResponse((HttpResponse) o);
			}

			@Override
			public void upstreamExceptionCaught(Throwable e) {
				//To change body of implemented methods use File | Settings | File Templates.
			}
		};
	}

	private FilterPipeline.DownstreamOutputListener createDownstreamOutputListener(final ChannelHandlerContext ctx,
																				   final ChannelFuture future,
																				   final String urn) {
		return new FilterPipeline.DownstreamOutputListener() {

			@Override
			public void receiveDownstreamOutput(ChannelBuffer message) {
				sendDownstreamRequest(ctx, future, urn, message);
			}

			@Override
			public void downstreamExceptionCaught(Throwable e) {
				//To change body of implemented methods use File | Settings | File Templates.
			}
		};
	}

	private void sendDownstreamRequest(ChannelHandlerContext ctx, ChannelFuture future, String urn, ChannelBuffer serializedPayload) {
		UPAddress remoteAddress = new UPAddress(urn);

		byte[] requestPayload = serializedPayload.toByteBuffer().array();
		SingleRequestSingleResponseRequest request = new SingleRequestSingleResponseRequest(remoteAddress, requestPayload);
		DownstreamMessageEvent event = new DownstreamMessageEvent(ctx.getChannel(), future, request, remoteAddress);
		ctx.sendDownstream(event);
	}
}
