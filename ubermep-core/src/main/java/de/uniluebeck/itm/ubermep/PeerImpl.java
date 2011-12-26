package de.uniluebeck.itm.ubermep;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import de.uniluebeck.itm.uberlay.UberlayBootstrap;
import de.uniluebeck.itm.uberlay.UberlayModule;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.channel.channelnexus.RequestResponseChannelNexus;
import de.uniluebeck.itm.ubermep.mep.channel.channels.impl.MultiResponseChannel;
import de.uniluebeck.itm.ubermep.mep.channel.channels.impl.SingleRequestSingleResponseChannel;
import de.uniluebeck.itm.ubermep.mep.channel.future.UbermepAbstractChannelFuture;
import de.uniluebeck.itm.ubermep.mep.channel.pipelinefactory.MultiResponseChannelPipelineFactory;
import de.uniluebeck.itm.ubermep.mep.channel.pipelinefactory.SingleRequestSingleResponseChannelPipelineFactory;
import de.uniluebeck.itm.ubermep.mep.channel.pipelinefactory.UbermepApplicationChannelPipelineFactory;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.MultiRequestMultiResponseServiceHandler;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.ReliableServiceHandler;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.SingleRequestMultiResponseServiceHandler;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.SingleRequestSingleResponseServiceHandler;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.unreliable.UnreliableServiceHandler;
import de.uniluebeck.itm.ubermep.mep.listener.MultiResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.listener.SingleRequestSingleResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.listener.UnicastMulticastRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.UnreliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.rpc.channel.UbermepRpcChannel;
import de.uniluebeck.itm.ubermep.rpc.handler.RpcServiceHandler;
import de.uniluebeck.itm.ubermep.rpc.service.RpcBlockingService;
import de.uniluebeck.itm.ubermep.rpc.service.RpcService;
import org.jboss.netty.channel.*;
import org.jboss.netty.util.internal.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 14.07.11
 * Time: 16:54
 * To change this template use File | Settings | File Templates.
 */

public class PeerImpl extends AbstractPeer {
	private static final Logger log = LoggerFactory.getLogger(PeerImpl.class);

	private final ScheduledExecutorService executorService;
	private final UberlayBootstrap bootstrap;
	private final UnreliableServiceHandler unreliableServiceHandler;
	private final ReliableServiceHandler reliableServiceHandler;
	private final SingleRequestSingleResponseServiceHandler singleRequestSingleResponseServiceHandler;
	private final SingleRequestMultiResponseServiceHandler singleRequestMultiResponseServiceHandler;
	private final MultiRequestMultiResponseServiceHandler multiRequestMultiResponseServiceHandler;
	private final Map<UPAddress, UbermepRpcChannel> rpcChannelMap;
	private final RpcServiceHandler rpcServiceHandler;

	public PeerImpl(UPAddress urn, InetSocketAddress localSocketAddress) throws ExecutionException, InterruptedException {
		this(urn, localSocketAddress, null,
				PeerConfig.DEFAULT_TIMEOUT, PeerConfig.DEFAULT_TIMEOUT_TIMEUNIT);
	}

	public PeerImpl(UPAddress urn, InetSocketAddress localSocketAddress, InetSocketAddress remoteSocketAddress)
			throws ExecutionException, InterruptedException {
		this(urn, localSocketAddress, remoteSocketAddress,
				PeerConfig.DEFAULT_TIMEOUT, PeerConfig.DEFAULT_TIMEOUT_TIMEUNIT);
	}

	//possible TODO:
	// Configuration from file
	// Configuration: local-urn, local-ip, optional remote-ip, optional default-timeout
	public PeerImpl(UPAddress urn, InetSocketAddress localSocketAddress, @Nullable InetSocketAddress remoteSocketAddress,
					int timeOut, TimeUnit timeUnit) throws ExecutionException, InterruptedException {
		super(urn, localSocketAddress, remoteSocketAddress);

		List<UnicastMulticastRequestListener> unicastMulticastRequestListeners = new ArrayList<UnicastMulticastRequestListener>();
		List<MultiResponseRequestListener> multiResponseRequestListeners = new ArrayList<MultiResponseRequestListener>();

		this.unreliableServiceHandler = new UnreliableServiceHandler(unicastMulticastRequestListeners);
		this.reliableServiceHandler = new ReliableServiceHandler(timeOut, timeUnit, unicastMulticastRequestListeners);
		this.singleRequestSingleResponseServiceHandler = new SingleRequestSingleResponseServiceHandler(
				timeOut, timeUnit, new ArrayList<SingleRequestSingleResponseRequestListener>());
		this.singleRequestMultiResponseServiceHandler = new SingleRequestMultiResponseServiceHandler(
				timeOut, timeUnit, multiResponseRequestListeners);
		this.multiRequestMultiResponseServiceHandler = new MultiRequestMultiResponseServiceHandler(
				multiResponseRequestListeners);
		this.rpcServiceHandler = new RpcServiceHandler(timeOut, timeUnit);

		this.rpcChannelMap = new HashMap<UPAddress, UbermepRpcChannel>();

		this.executorService = Executors.newScheduledThreadPool(
				PeerConfig.CORE_POOL_SIZE,
				new ThreadFactoryBuilder().setNameFormat("MEP %d").build()
		);

		ChannelPipelineFactory ubermepApplicationChannelPipelineFactory = new UbermepApplicationChannelPipelineFactory(
				unreliableServiceHandler, reliableServiceHandler, rpcServiceHandler);
		ChannelPipelineFactory singleRequestSingleResponseChannelPipelineFactory =
				new SingleRequestSingleResponseChannelPipelineFactory(singleRequestSingleResponseServiceHandler);
		ChannelPipelineFactory multiResponseChannelPipelineFactory = new MultiResponseChannelPipelineFactory(
				singleRequestMultiResponseServiceHandler, multiRequestMultiResponseServiceHandler);

		final UberlayModule uberlayModule;
		final RequestResponseChannelNexus<SingleRequestSingleResponseChannel> singleRequestSingleResponseChannelNexus;
		final RequestResponseChannelNexus<MultiResponseChannel> multiResponseChannelNexus;
		try {
			uberlayModule = new UberlayModule(
					executorService, localUPAddress, ubermepApplicationChannelPipelineFactory.getPipeline(),
					PeerConfig.UberlayModule.RTT_REQUEST_INTERVAL, PeerConfig.UberlayModule.RTT_REQUEST_INTERVAL_TIMEUNIT
			);

			bootstrap = Guice.createInjector(uberlayModule).getInstance(UberlayBootstrap.class);

			singleRequestSingleResponseChannelNexus = new RequestResponseChannelNexus<SingleRequestSingleResponseChannel>(
					new SingleRequestSingleResponseChannel(
							bootstrap,
							singleRequestSingleResponseChannelPipelineFactory.getPipeline()
					));

			multiResponseChannelNexus =
					new RequestResponseChannelNexus<MultiResponseChannel>(new MultiResponseChannel(
							bootstrap,
							multiResponseChannelPipelineFactory.getPipeline()
					));
		} catch (Exception e) {
			throw new ExecutionException(e);
		}

		this.reliableServiceHandler.setSingleRequestSingleResponseChannelNexus(
				singleRequestSingleResponseChannelNexus
		);

		this.reliableServiceHandler.setMultiResponseChannelNexus(
				multiResponseChannelNexus
		);
	}

	public void start() throws ExecutionException, InterruptedException {
		log.info("Received startup signal. Starting up...");
		log.info("Binding local server socket on {}:{}...", localSocketAddress.getHostName(),
				localSocketAddress.getPort()
		);
		bootstrap.bind(localSocketAddress).get();
		log.info("Bound to {}:{}!", localSocketAddress.getHostName(), localSocketAddress.getPort());

		if (remoteSocketAddress != null) {
			connect(remoteSocketAddress);
		}
		log.info("Startup complete!");
	}

	public void stop() {
		log.info("Received shutdown signal. Shutting down...");
		bootstrap.shutdown();
		log.info("Shutdown complete!");

		ExecutorUtil.terminate(executorService);
	}

	@Override
	public void connect(InetSocketAddress remoteAddress) throws ExecutionException, InterruptedException {
		log.info("Connecting to remote peer on {}:{}...", remoteSocketAddress.getHostName(), remoteSocketAddress.getPort());
		bootstrap.connect(remoteAddress).get();
		log.info("Connected to remote peer on {}:{}!", remoteSocketAddress.getHostName(), remoteSocketAddress.getPort());
	}

	@Override
	public void send(UnreliableRequest request) throws ExecutionException, InterruptedException {
		final Channel uberlayApplicationChannel = bootstrap.getApplicationChannel().get();
		unreliableServiceHandler.send(request, uberlayApplicationChannel);
	}

	@Override
	public ListenableFuture<Response> send(ReliableRequest request) throws ExecutionException, InterruptedException {
		final Channel uberlayApplicationChannel = bootstrap.getApplicationChannel().get();
		return reliableServiceHandler.send(request, uberlayApplicationChannel);
	}

	@Override
	public void send(Object object, UPAddress urn) throws ExecutionException, InterruptedException {
		final Channel channel = bootstrap.getApplicationChannel().get();
		unreliableServiceHandler.send(object, channel, urn);
	}

	@Override
	public <T extends UbermepAbstractChannelFuture> T send(Object object, UPAddress urn, Class<T> channelFutureClass) throws ExecutionException, InterruptedException {
		final Channel channel = bootstrap.getApplicationChannel().get();
		return reliableServiceHandler.send(object, channel, urn, channelFutureClass);
	}

	@Override
	public void addRequestListener(UnicastMulticastRequestListener listener) {
		this.unreliableServiceHandler.addRequestListener(listener);
		this.reliableServiceHandler.addRequestListener(listener);
	}

	@Override
	public void addRequestListener(SingleRequestSingleResponseRequestListener listener) {
		this.singleRequestSingleResponseServiceHandler.addRequestListener(listener);
	}

	@Override
	public void addRequestListener(MultiResponseRequestListener listener) {
		this.singleRequestMultiResponseServiceHandler.addRequestListener(listener);
		this.multiRequestMultiResponseServiceHandler.addRequestListener(listener);
	}

	@Override
	public UbermepRpcChannel getRpcChannel(UPAddress urn) throws ExecutionException, InterruptedException {
		UbermepRpcChannel channel = rpcChannelMap.get(urn);
		if (channel == null) {
			channel = new UbermepRpcChannel(rpcServiceHandler, this.bootstrap.getApplicationChannel().get(), urn);
			rpcChannelMap.put(urn, channel);
		}
		return channel;
	}

	@Override
	public void registerService(RpcService service) {
		rpcServiceHandler.registerService(service.getRpcService());
	}

	@Override
	public void registerBlockingService(RpcBlockingService service) {
		rpcServiceHandler.registerService(service.getRpcBlockingService());
	}

	@Override
	public void registerChannelHandler(SimpleChannelHandler channelHandler) throws ExecutionException, InterruptedException {
		bootstrap.getApplicationChannel().get().getPipeline().addBefore(
				UbermepApplicationChannelPipelineFactory.CHANNEL_PIPELINE_RPC_SERVICE_HANDLER,
				channelHandler.toString(), channelHandler
		);
	}

	@Override
	public void registerUpstreamHandler(ChannelUpstreamHandler upstreamHandler) throws ExecutionException, InterruptedException {
		bootstrap.getApplicationChannel().get().getPipeline().addBefore(
				UbermepApplicationChannelPipelineFactory.CHANNEL_PIPELINE_RPC_SERVICE_HANDLER,
				upstreamHandler.toString(), upstreamHandler
		);
	}

	@Override
	public void registerDownstreamHandler(ChannelDownstreamHandler downstreamHandler) throws ExecutionException, InterruptedException {
		bootstrap.getApplicationChannel().get().getPipeline().addBefore(
				UbermepApplicationChannelPipelineFactory.CHANNEL_PIPELINE_RPC_SERVICE_HANDLER,
				downstreamHandler.toString(), downstreamHandler
		);
	}
}
