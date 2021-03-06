package de.uniluebeck.itm.uberlay;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.uniluebeck.itm.uberlay.protocols.ProtocolRegistry;
import de.uniluebeck.itm.uberlay.protocols.pvp.PathVectorProtocolHandler;
import de.uniluebeck.itm.uberlay.protocols.rtt.RoundtripTimeProtocolHandler;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.uberlay.protocols.up.UPRouter;
import de.uniluebeck.itm.uberlay.protocols.up.UPRoutingTable;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.protobuf.MultiProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.MultiProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class UberlayPipelineFactory implements ChannelPipelineFactory {

	@Inject
	private ScheduledExecutorService executorService;

	@Inject
	@Named(Injection.LOCAL_ADDRESS)
	private UPAddress localAddress;

	@Inject
	private UPRoutingTable routingTable;

	@Inject
	private UPRouter router;

	private final int rttRequestInterval;

	private final TimeUnit rttRequestIntervalTimeunit;

	public UberlayPipelineFactory(final int rttRequestInterval, final TimeUnit rttRequestIntervalTimeunit) {
		this.rttRequestInterval = rttRequestInterval;
		this.rttRequestIntervalTimeunit = rttRequestIntervalTimeunit;
	}

	@Override
	public ChannelPipeline getPipeline() {

		final ChannelPipeline pipeline = Channels.pipeline();

		//for evaluation only
		//pipeline.addLast("TransmissionRateEmulator", new TransmissionRateEmulationHandler());

		// upstream handlers
		pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
		pipeline.addLast("protobufDecoders",
				new MultiProtobufDecoder(ProtocolRegistry.REGISTRY, ProtocolRegistry.HEADER_FIELD_LENGTH)
		);

		// downstream handlers
		pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
		pipeline.addLast("protobufEncoders",
				new MultiProtobufEncoder(ProtocolRegistry.REGISTRY, ProtocolRegistry.HEADER_FIELD_LENGTH)
		);

		// application logic
		pipeline.addLast("rttProtocolHandler",
				new RoundtripTimeProtocolHandler(executorService, rttRequestInterval, rttRequestIntervalTimeunit)
		);
		pipeline.addLast("pvpHandler",
				new PathVectorProtocolHandler(
						localAddress.toString(), routingTable, executorService, 10, TimeUnit.SECONDS
				)
		);
		pipeline.addLast("router", router);
		pipeline.addLast("loggingHandler", new DefaultLoggingHandler());

		return pipeline;
	}
}
