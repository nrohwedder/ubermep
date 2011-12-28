package de.uniluebeck.itm.ubermep.mep.channel.pipelinefactory;

import de.uniluebeck.itm.uberlay.TransmissionRateEmulationHandler;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.ReliableServiceHandler;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.unreliable.UnreliableServiceHandler;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import de.uniluebeck.itm.ubermep.rpc.handler.RpcServiceHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 24.11.11
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public class UbermepApplicationChannelPipelineFactory implements ChannelPipelineFactory {

	private final UnreliableServiceHandler unreliableServiceHandler;
	private final ReliableServiceHandler reliableServiceHandler;
	private final RpcServiceHandler rpcServiceHandler;

	private static final String CHANNEL_PIPELINE_PROTOBUF_ENCODER_MEP_NAME = "ProtobufEncoder(MEP)";
	private static final String CHANNEL_PIPELINE_PROTOBUF_DECODER_MEP_NAME = "ProtobufDecoder(MEP)";
	private static final String CHANNEL_PIPELINE_UNRELIABLE_SERVICE_HANDLER_NAME = "UnreliableServiceHandler";
	private static final String CHANNEL_PIPELINE_RELIABLE_SERVICE_HANDLER = "ReliableServiceHandler";
	public static final String CHANNEL_PIPELINE_RPC_SERVICE_HANDLER = "RpcServiceHandler";

	public UbermepApplicationChannelPipelineFactory(final UnreliableServiceHandler unreliableServiceHandler,
													final ReliableServiceHandler reliableServiceHandler,
													final RpcServiceHandler rpcServiceHandler) {
		this.unreliableServiceHandler = unreliableServiceHandler;
		this.reliableServiceHandler = reliableServiceHandler;
		this.rpcServiceHandler = rpcServiceHandler;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		final ChannelPipeline pipeline = Channels.pipeline();

		pipeline.addLast(CHANNEL_PIPELINE_PROTOBUF_ENCODER_MEP_NAME, new ProtobufEncoder());
		pipeline.addLast(CHANNEL_PIPELINE_PROTOBUF_DECODER_MEP_NAME,
				new ProtobufDecoder(MEP.MEPPacket.getDefaultInstance()));
		pipeline.addLast(CHANNEL_PIPELINE_UNRELIABLE_SERVICE_HANDLER_NAME, unreliableServiceHandler);
		pipeline.addLast(CHANNEL_PIPELINE_RELIABLE_SERVICE_HANDLER, reliableServiceHandler);

		//register rpc-services
		pipeline.addLast(CHANNEL_PIPELINE_RPC_SERVICE_HANDLER, rpcServiceHandler);

		return pipeline;
	}
}
