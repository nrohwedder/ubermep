package de.uniluebeck.itm.ubermep.mep.channel.pipelinefactory;

import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.SingleRequestSingleResponseServiceHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 04.12.11
 * Time: 12:44
 * To change this template use File | Settings | File Templates.
 */
public class SingleRequestSingleResponseChannelPipelineFactory implements ChannelPipelineFactory{
	private SingleRequestSingleResponseServiceHandler singleRequestSingleResponseServiceHandler;

	public SingleRequestSingleResponseChannelPipelineFactory(SingleRequestSingleResponseServiceHandler
																	 singleRequestSingleResponseServiceHandler) {
		this.singleRequestSingleResponseServiceHandler = singleRequestSingleResponseServiceHandler;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		return pipeline(singleRequestSingleResponseServiceHandler);
	}
}
