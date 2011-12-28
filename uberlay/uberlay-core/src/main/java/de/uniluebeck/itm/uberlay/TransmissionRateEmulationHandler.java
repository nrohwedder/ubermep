package de.uniluebeck.itm.uberlay;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 27.12.11
 * Time: 14:23
 * To change this template use File | Settings | File Templates.
 */
public class TransmissionRateEmulationHandler extends SimpleChannelHandler {

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof MessageEvent) {
			ChannelBuffer message = (ChannelBuffer) ((MessageEvent) e).getMessage();
			long size = message.readableBytes() * 8;
			if (size < 16000) {
				double d_ns = 62.5;
				Long l = Math.round(size * d_ns);
				Thread.sleep(0, l.intValue());
			} else {
				double d_ms = 0.0000625;
				Thread.sleep(Math.round(size * d_ms));
			}

		}
		super.handleUpstream(ctx, e);
	}
}
