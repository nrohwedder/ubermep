package evaluation;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import de.uniluebeck.itm.uberlay.DefaultLoggingHandler;
import de.uniluebeck.itm.uberlay.UberlayBootstrap;
import de.uniluebeck.itm.uberlay.UberlayModule;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.Peer;
import de.uniluebeck.itm.ubermep.PeerConfig;
import de.uniluebeck.itm.ubermep.PeerImpl;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.listener.SingleRequestSingleResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.listener.UnicastMulticastRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableUnicastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl.UnreliableUnicastRequest;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.netty.util.internal.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.11.11
 * Time: 21:52
 * To change this template use File | Settings | File Templates.
 */
public class UbermepToUberlay {
	private static final Logger logger = LoggerFactory.getLogger(UbermepToUberlay.class);
	private static UPAddress uberlayClientUrn = new UPAddress("urn:itm:1");
	private static UPAddress uberlayServerUrn = new UPAddress("urn:itm:2");
	private static InetSocketAddress uberlayClientLocalSocketAddress = new InetSocketAddress("0.0.0.0", 8080);
	private static InetSocketAddress uberlayServerLocalSocketAddress = new InetSocketAddress("0.0.0.0", 8081);

	private static UPAddress ubermepClientUrn = new UPAddress("urn:itm:3");
	private static UPAddress ubermepServerUrn = new UPAddress("urn:itm:4");
	private static InetSocketAddress ubermepClientLocalSocketAddress = new InetSocketAddress("0.0.0.0", 8082);
	private static InetSocketAddress ubermepServerLocalSocketAddress = new InetSocketAddress("0.0.0.0", 8083);

	private static boolean errorOccurs = false;
	private static int CURRENT_RUNS = 0;
	private static final int TOTAL_RUNS = 50;
	private static final String FILE_NAME = EvaluationTool.formatOutput("UbermepToUberlay{}Runs.txt", TOTAL_RUNS);

	private static HashMap<String, EvaluationTool.TimeInterval> currentIntervalMap = new HashMap<String, EvaluationTool.TimeInterval>();

	private static class UberlayPeer {
		private final UberlayBootstrap bootstrap;
		private final ScheduledExecutorService executorService;

		private UberlayPeer(UberlayBootstrap bootstrap, ScheduledExecutorService executorService) {
			this.bootstrap = bootstrap;
			this.executorService = executorService;
		}

		private Channel getChannel() throws ExecutionException, InterruptedException {
			return bootstrap.getApplicationChannel().get();
		}

		private void shutdown() {
			logger.info("Shutting down...");
			bootstrap.shutdown();
			logger.info("Shutdown complete!");

			ExecutorUtil.terminate(executorService);
		}
	}

	private static class UberlayUpstreamHandler extends SimpleChannelUpstreamHandler {
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (e.getMessage() instanceof String) {
				long stoppageTime = System.currentTimeMillis();
				String key = (String) e.getMessage();
				EvaluationTool.TimeInterval interval = currentIntervalMap.get(key);
				interval.setStopTimestamp(stoppageTime);
			} else {
				super.messageReceived(ctx, e);
			}
		}
	}

	private static UnicastMulticastRequestListener unicastMulticastRequestListener = new UnicastMulticastRequestListener() {
		@Override
		public boolean handleUnicastMulticastRequest(String senderUrn, byte[] payload) {
			long stoppageTime = System.currentTimeMillis();
			String key = new String(payload);
			EvaluationTool.TimeInterval interval = currentIntervalMap.get(key);
			interval.setStopTimestamp(stoppageTime);
			return true;
		}
	};

	private static SingleRequestSingleResponseRequestListener singleRequestSingleResponseRequestListener = new SingleRequestSingleResponseRequestListener() {
		@Override
		public byte[] handleSingleRequestSingleResponseRequest(String senderUrn, byte[] requestPayload) throws UbermepExceptionEvent {
			long stoppageTime = System.currentTimeMillis();
			String key = new String(requestPayload);
			EvaluationTool.TimeInterval interval = currentIntervalMap.get(key);
			interval.setStopTimestamp(stoppageTime);
			return "Done".getBytes();
		}
	};

	public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
		runEvaluation();
	}

	public static void runEvaluation() throws ExecutionException, InterruptedException, IOException {
		UberlayPeer uberlayClient = startUberlayPeer(uberlayClientUrn, uberlayClientLocalSocketAddress, null);
		UberlayPeer uberlayServer = startUberlayPeer(uberlayServerUrn, uberlayServerLocalSocketAddress, uberlayClientLocalSocketAddress);

		Peer ubermepClient = new PeerImpl(ubermepClientUrn, ubermepClientLocalSocketAddress);
		Peer ubermepServer = new PeerImpl(ubermepServerUrn, ubermepServerLocalSocketAddress, ubermepClientLocalSocketAddress);

		ubermepServer.addRequestListener(unicastMulticastRequestListener);
		ubermepServer.addRequestListener(singleRequestSingleResponseRequestListener);

		ubermepClient.start();
		ubermepServer.start();
		
		Thread.sleep(200);

		EvaluationTool.createFile(FILE_NAME, TOTAL_RUNS, EvaluationTool.EVALUATION_TYPE.UBERMEP_TO_UBERLAY);

		String[] keys = EvaluationTool.createKeyArrayForUbermepToUberlay();

		while (CURRENT_RUNS < TOTAL_RUNS) {
			for (String key : keys) {
				if (key.equals(EvaluationTool.KEY_UBERLAY)) {
					writeUberlay(uberlayClient, key);
				} else if (key.equals(EvaluationTool.KEY_UBERMEP_UNRELIABLE_UNICAST)){
					writeUbermepUnreliableUnicast(ubermepClient, key);
				} else if (key.equals(EvaluationTool.KEY_UBERMEP_RELIABLE_UNICAST)){
					writeUbermepReliableUnicast(ubermepClient, key);
				} else if (key.equals(EvaluationTool.KEY_UBERMEP_SINGLE_REQUEST_SINGLE_RESPONSE)){
					writeSingleRequestSingleResponse(ubermepClient, key);
				}
			}

			if (errorOccurs) {
				logger.error("ErrorOccurs!");
			} else {
				for (String key : currentIntervalMap.keySet()){
					EvaluationTool.TimeInterval interval = currentIntervalMap.get(key);
					while(!interval.isDone()){}
				}
				EvaluationTool.appendCurrentTimeMapToFile(FILE_NAME,
						EvaluationTool.createLineForAppendToFileForUbermepToUberlay(currentIntervalMap));
				currentIntervalMap = new HashMap<String, EvaluationTool.TimeInterval>();
				CURRENT_RUNS++;
			}
		}

		Thread.sleep(200);

		ubermepServer.stop();
		ubermepClient.stop();

		uberlayServer.shutdown();
		uberlayClient.shutdown();

		System.exit(-1);
	}

	private static void writeUberlay(UberlayPeer client, String key){
		currentIntervalMap.put(key, new EvaluationTool.TimeInterval(System.currentTimeMillis()));
		try {
			client.getChannel().write(key, uberlayServerUrn);
		} catch (Exception e) {
			logger.error("{}", e);
			errorOccurs = true;
		}
	}

	private static void writeUbermepUnreliableUnicast(Peer client, String key){
		currentIntervalMap.put(key, new EvaluationTool.TimeInterval(System.currentTimeMillis()));
		try {
			client.send(new UnreliableUnicastRequest(ubermepServerUrn, key.getBytes()));
		} catch (Exception e) {
			logger.error("{}", e);
			errorOccurs = true;
		}

	}

	private static void writeUbermepReliableUnicast(Peer client, String key){
		currentIntervalMap.put(key, new EvaluationTool.TimeInterval(System.currentTimeMillis()));
		try {
			client.send(new ReliableUnicastRequest(ubermepServerUrn, key.getBytes()));
		} catch (Exception e) {
			logger.error("{}", e);
			errorOccurs = true;
		}
	}

	private static void writeSingleRequestSingleResponse(Peer client, String key){
		currentIntervalMap.put(key, new EvaluationTool.TimeInterval(System.currentTimeMillis()));
		try {
			client.send(new SingleRequestSingleResponseRequest(ubermepServerUrn, key.getBytes()));
		} catch (Exception e) {
			logger.error("{}", e);
			errorOccurs = true;
		}
	}

	public static UberlayPeer startUberlayPeer(UPAddress localUrn, InetSocketAddress localSocketAddress,
											   InetSocketAddress remoteSocketAddress) throws ExecutionException, InterruptedException {
		final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(
				PeerConfig.CORE_POOL_SIZE,
				new ThreadFactoryBuilder().setNameFormat("UbermepToUberlay -" + localUrn + "- %d").build()
		);

		final ChannelPipeline applicationPipeline = Channels.pipeline(
				new StringEncoder(CharsetUtil.UTF_8),
				new StringDecoder(CharsetUtil.UTF_8),
				new DefaultLoggingHandler(),
				new UberlayUpstreamHandler()
		);

		final UberlayModule uberlayModule = new UberlayModule(executorService, localUrn, applicationPipeline,
				PeerConfig.UberlayModule.RTT_REQUEST_INTERVAL, PeerConfig.UberlayModule.RTT_REQUEST_INTERVAL_TIMEUNIT);
		final UberlayBootstrap bootstrap = Guice.createInjector(uberlayModule).getInstance(UberlayBootstrap.class);

		logger.info("Binding local server socket on {}:{}...", localSocketAddress.getHostName(),
				localSocketAddress.getPort()
		);
		final Channel serverChannel = bootstrap.bind(localSocketAddress).get();
		logger.info("Bound to {}:{}!", localSocketAddress.getHostName(), localSocketAddress.getPort());

		if (remoteSocketAddress != null) {

			logger.info("Connecting to remote peer on {}:{}...", remoteSocketAddress.getHostName(),
					remoteSocketAddress.getPort()
			);
			bootstrap.connect(remoteSocketAddress).get();
			logger.info("Connected to remote peer on {}:{}!", remoteSocketAddress.getHostName(),
					remoteSocketAddress.getPort()
			);
		}

		return new UberlayPeer(bootstrap, executorService);
	}

}
