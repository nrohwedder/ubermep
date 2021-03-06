package de.uniluebeck.itm.uberlay;

import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import de.uniluebeck.itm.uberlay.protocols.up.UP;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.uberlay.protocols.up.UPRoutingTable;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

@Singleton
class UberlayNexusImpl extends AbstractChannelSink implements UberlayNexus {

	private static final Logger log = LoggerFactory.getLogger(UberlayNexusImpl.class);

	private final ChannelGroup uberlayClientChannels = new DefaultChannelGroup("UberlayNexusImpl-UberlayClientChannels");

	private final ChannelGroup uberlayServerChannels = new DefaultChannelGroup("UberlayNexusImpl-UberlayServerChannels");

	private ChannelUpstreamHandler defaultBehaviourChannelHandler = new SimpleChannelHandler();

	private final ChannelConfig config = new DefaultChannelConfig();

	@Inject
	private ScheduledExecutorService scheduledExecutorService;

	@Inject
	private UPRoutingTable routingTable;

	@Inject
	@Named(Injection.LOCAL_ADDRESS)
	private UPAddress localAddress;

	@Inject
	@Named(Injection.UBERLAY_PIPELINE_FACTORY)
	private ChannelPipelineFactory uberlayPipelineFactory;

	@Inject
	@Named(Injection.APPLICATION_CHANNEL)
	private Channel applicationChannel;

	UberlayNexusImpl() {
	}

	@Override
	public void eventSunk(final ChannelPipeline pipeline, final ChannelEvent e) throws Exception {

		if (e instanceof DownstreamMessageEvent) {
			handleDownstreamMessageEvent((DownstreamMessageEvent) e);
		} else if (e instanceof DownstreamChannelStateEvent) {
			handleDownstreamChannelStateEvent((ChannelStateEvent) e);
		}
	}

	private void handleDownstreamChannelStateEvent(final ChannelStateEvent e) {
		if (ChannelState.CONNECTED == e.getState() && e.getValue() == null) {
			shutdown();
			applicationChannel.getCloseFuture().setSuccess();
		}
	}

	@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
	private void handleExceptionEvent(final ExceptionEvent e) {
		log.warn("Caught exception event. Sending it upstream! {}", e);
		applicationChannel.getPipeline().sendUpstream(new DefaultExceptionEvent(e.getChannel(), e.getCause()));
	}

	@Override
	public Future<Channel> connect(final InetSocketAddress remoteAddress) {

		final SettableFuture<Channel> returnFuture = SettableFuture.create();

		ClientBootstrap clientBootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(scheduledExecutorService, scheduledExecutorService)
		);

		clientBootstrap.setPipelineFactory(uberlayPipelineFactory);

		clientBootstrap.connect(remoteAddress).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture future) throws Exception {
				uberlayClientChannels.add(future.getChannel());
				returnFuture.set(future.getChannel());
			}
		}
		);

		return returnFuture;
	}

	@Override
	public Future<Channel> bind(final InetSocketAddress localAddress) {

		final SettableFuture<Channel> returnFuture = SettableFuture.create();

		final ServerBootstrap serverBootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(scheduledExecutorService, scheduledExecutorService)
		);

		serverBootstrap.setPipelineFactory(uberlayPipelineFactory);
		serverBootstrap.setOption("child.tcpNoDelay", true);
		serverBootstrap.setOption("child.keepAlive", true);

		final Channel channel = serverBootstrap.bind(localAddress);
		uberlayServerChannels.add(channel);
		returnFuture.set(channel);

		return returnFuture;
	}

	@Override
	public void exceptionCaught(final ChannelPipeline pipeline, final ChannelEvent e,
								final ChannelPipelineException cause) throws Exception {
		if (e instanceof ExceptionEvent) {
			handleExceptionEvent((ExceptionEvent) e);
		} else {
			ExceptionEvent event = new DefaultExceptionEvent(e.getChannel(), cause);
			handleExceptionEvent(event);
		}
	}

	@Override
	public void handleUpstream(final ChannelHandlerContext ctx, final ChannelEvent e) throws Exception {

		if (e instanceof UpstreamMessageEvent && ((UpstreamMessageEvent) e).getMessage() instanceof UP.UPPacket) {
			handleUpstreamMessageEvent((UpstreamMessageEvent) e);
		} else {
			defaultBehaviourChannelHandler.handleUpstream(ctx, e);
		}
	}

	private void handleUpstreamMessageEvent(final UpstreamMessageEvent e) {

		final UP.UPPacket upPacket = (UP.UPPacket) e.getMessage();
		final ChannelFuture callerFuture = e.getFuture();

		route(upPacket, callerFuture);
	}

	private void handleDownstreamMessageEvent(final DownstreamMessageEvent e) throws NotHandableMessageException {

		if (!(e.getMessage() instanceof ChannelBuffer)) {
			e.getFuture().setFailure(
					new NotHandableMessageException("Could not handle downstream: " + e.getMessage().getClass() + "!\n" +
							"Please verify your service handlers for: " + e.getChannel().getLocalAddress()));
		} else {
			final ChannelBuffer payload = (ChannelBuffer) e.getMessage();
			final UPAddress destination = (UPAddress) e.getRemoteAddress();
			final ChannelFuture callerFuture = e.getFuture();

			route(buildPacket(payload, destination), callerFuture);
		}
	}

	@Override
	public void route(final UP.UPPacket packet, final ChannelFuture callerFuture) {

		final UPAddress destination = new UPAddress(packet.getDestination());
		final boolean isLoopBack = localAddress.equals(destination);

		log.debug("Routing packet: {}", packet);

		if (isLoopBack) {
			sendUpstream(packet, callerFuture);
		} else {
			sendDownstream(packet, callerFuture, destination);
		}
	}

	private void sendDownstream(final UP.UPPacket packet, final ChannelFuture callerFuture,
								final UPAddress destination) {

		final Channel channel = routingTable.getNextHopChannel(destination);
		final UPAddress nextHop = routingTable.getNextHop(destination);
		final boolean noRouteToHost = channel == null;

		if (noRouteToHost) {
			log.info("Warning: No Route to Peer {} found!", destination);
			callerFuture.setFailure(new NoRouteToPeerException(destination));

		} else {
			log.info("Forwarding packet to {}", packet, nextHop);
			final ChannelFutureListener listener = new ChannelFutureListener() {
				@Override
				public void operationComplete(final ChannelFuture future) throws Exception {
					callerFuture.setSuccess();
				}
			};
			channel.write(packet).addListener(listener);
		}

	}

	private UP.UPPacket buildPacket(final ChannelBuffer payload, final UPAddress destination) {
		return UP.UPPacket.newBuilder()
				.setDestination(destination.toString())
				.setSource(localAddress.toString())
				.setPayload(ByteString.copyFrom(payload.toByteBuffer()))
				.build();
	}

	private void sendUpstream(final UP.UPPacket upPacket, final ChannelFuture callerFuture) {

		log.info("Sending packet upstream: {}", upPacket);

		final UPAddress source = new UPAddress(upPacket.getSource());
		final byte[] payloadBytes = upPacket.getPayload().toByteArray();
		final ChannelBuffer payload = ChannelBuffers.wrappedBuffer(payloadBytes);
		final UpstreamMessageEvent event = new UpstreamMessageEvent(applicationChannel, payload, source);

		applicationChannel.getPipeline().sendUpstream(event);
		callerFuture.setSuccess();
	}

	@Override
	public ChannelConfig getConfig() {
		return config;
	}

	@Override
	public boolean isBound() {
		return uberlayServerChannels.size() > 0;
	}

	@Override
	public boolean isConnected() {
		return uberlayClientChannels.size() > 0;
	}

	@Override
	public UPAddress getLocalAddress() {
		return localAddress;
	}

	@Override
	public void shutdown() {
		uberlayServerChannels.close().awaitUninterruptibly();
		uberlayClientChannels.close().awaitUninterruptibly();
	}

	@Override
	public Future<Channel> getApplicationChannel() {
		try {
			final SettableFuture<Channel> future = SettableFuture.create();
			future.set(applicationChannel);
			return future;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
