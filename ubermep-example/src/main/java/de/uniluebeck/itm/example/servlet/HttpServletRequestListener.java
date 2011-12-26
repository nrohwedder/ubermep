package de.uniluebeck.itm.example.servlet;

import de.uniluebeck.itm.handlerstack.FilterPipeline;
import de.uniluebeck.itm.handlerstack.FilterPipelineImpl;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepSingleResponseExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.listener.SingleRequestSingleResponseRequestListener;
import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 12.10.11
 * Time: 14:31
 * To change this template use File | Settings | File Templates.
 */
public class HttpServletRequestListener implements SingleRequestSingleResponseRequestListener {
	private class HttpRequestListener implements FilterPipeline.UpstreamOutputListener{
		private HttpRequest request;

		public HttpRequest getRequest() {
			return request;
		}

		@Override
		public void receiveUpstreamOutput(Object o) {
			this.request = (HttpRequest) o;
		}

		@Override
		public void upstreamExceptionCaught(Throwable e) {
			//do nothing
		}
	}

	private class HttpResponseListener implements FilterPipeline.DownstreamOutputListener{
		private ChannelBuffer channelBuffer;

		public ChannelBuffer getChannelBuffer() {
			return channelBuffer;
		}

		@Override
		public void receiveDownstreamOutput(ChannelBuffer message) {
			this.channelBuffer = message;
		}

		@Override
		public void downstreamExceptionCaught(Throwable e) {
			//do nothing
		}
	}

	FilterPipeline pipeline = new FilterPipelineImpl();

	public HttpServletRequestListener() {
		List<Tuple<String, ChannelHandler>> tuples = new ArrayList<Tuple<String, ChannelHandler>>();
		tuples.add(new Tuple<String, ChannelHandler>("decoder", new HttpRequestDecoder()));
		tuples.add(new Tuple<String, ChannelHandler>("encoder", new HttpResponseEncoder()));
		pipeline.setChannelPipeline(tuples);

	}

	@Override
	public byte[] handleSingleRequestSingleResponseRequest(String senderUrn, byte[] requestPayload) throws UbermepExceptionEvent {
		try {
			final ChannelBuffer requestPayloadChannelBuffer = ChannelBuffers.wrappedBuffer(requestPayload);

			HttpRequestListener requestListener = new HttpRequestListener();

			pipeline.addListener(requestListener);
			pipeline.sendUpstream(requestPayloadChannelBuffer);

			//create HttpResponse
			HttpResponse response = handleHttpRequest(requestListener.getRequest());

			HttpResponseListener responseListener = new HttpResponseListener();

			pipeline.addListener(responseListener);
			pipeline.sendDownstream(response);

			ChannelBuffer responseBuffer = responseListener.getChannelBuffer();
			return responseBuffer.toByteBuffer().array();
		} catch (Exception e) {
			throw new UbermepSingleResponseExceptionEvent(e);
		}
	}

	private HttpResponse handleHttpRequest(HttpRequest request) {
		if (request != null) {

			//HttpRequest request = (HttpRequest) o;

			//do call html-page and get response(s)
			//...
			// and then create response
			byte[] responsePayload = ("uri: " + request.getUri() + " successfully called!").getBytes();
			ChannelBuffer contentChannelBuffer = ChannelBuffers.buffer(responsePayload.length);
			contentChannelBuffer.writeBytes(responsePayload);

			HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
			response.setContent(contentChannelBuffer);
			return response;
		}
		return null;
	}

}
