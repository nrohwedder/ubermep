package de.uniluebeck.itm.ubermep.mep.channel.pipelinefactory;

import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.MultiRequestMultiResponseServiceHandler;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.SingleRequestMultiResponseServiceHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.12.11
 * Time: 12:57
 * To change this template use File | Settings | File Templates.
 */
public class MultiResponseChannelPipelineFactory implements ChannelPipelineFactory {
	private SingleRequestMultiResponseServiceHandler singleRequestMultiResponseServiceHandler;
	private MultiRequestMultiResponseServiceHandler multiRequestMultiResponseServiceHandler;

	public MultiResponseChannelPipelineFactory(SingleRequestMultiResponseServiceHandler
													   singleRequestMultiResponseServiceHandler,
											   MultiRequestMultiResponseServiceHandler
													   multiRequestMultiResponseServiceHandler) {
		this.singleRequestMultiResponseServiceHandler = singleRequestMultiResponseServiceHandler;
		this.multiRequestMultiResponseServiceHandler = multiRequestMultiResponseServiceHandler;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		return pipeline(singleRequestMultiResponseServiceHandler, multiRequestMultiResponseServiceHandler);
	}
}
