package de.uniluebeck.itm.ubermep.mep.channel.channelsink;

import de.uniluebeck.itm.uberlay.UberlayBootstrap;
import org.jboss.netty.channel.AbstractChannelSink;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 23.08.11
 * Time: 17:58
 * To change this template use File | Settings | File Templates.
 */
public class RequestResponseChannelSink extends AbstractChannelSink {
	final Logger log = LoggerFactory.getLogger(RequestResponseChannelSink.class);
	private final UberlayBootstrap bootstrap;

	public RequestResponseChannelSink(UberlayBootstrap bootstrap){
		this.bootstrap = bootstrap;
	}

	@Override
	public void eventSunk(ChannelPipeline pipeline, ChannelEvent e) throws Exception {
		log.info("eventSunk");
		bootstrap.getApplicationChannel().get().getPipeline().sendDownstream(e);
	}
}
