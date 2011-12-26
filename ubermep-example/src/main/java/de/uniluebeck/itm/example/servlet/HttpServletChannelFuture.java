package de.uniluebeck.itm.example.servlet;

import de.uniluebeck.itm.ubermep.mep.channel.future.UbermepAbstractSingleResponseChannelFuture;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 12.10.11
 * Time: 12:48
 * To change this template use File | Settings | File Templates.
 */
public class HttpServletChannelFuture extends UbermepAbstractSingleResponseChannelFuture<HttpResponse> {
	/**
	 * Creates a new instance.
	 *
	 * @param channel	 the {@link org.jboss.netty.channel.Channel} associated with this future
	 */
	public HttpServletChannelFuture(Channel channel) {
		super(channel);
	}

	@Override
	public synchronized boolean setFailure(Throwable cause) {
		//Additionally adapt with HttpServletErrorResponse e.g.: setResponse(new HttpServletErrorResponse());
		return super.setFailure(cause);
	}
}
