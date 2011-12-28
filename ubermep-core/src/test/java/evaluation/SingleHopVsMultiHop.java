package evaluation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Guice;
import com.google.protobuf.ServiceException;
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
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse.SingleRequestSingleResponseResponse;
import de.uniluebeck.itm.ubermep.rpc.channel.UbermepRpcChannel;
import de.uniluebeck.itm.ubermep.rpc.controller.RpcControllerImpl;
import de.uniluebeck.itm.ubermep.rpc.service.RpcBlockingService;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.util.internal.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 07.12.11
 * Time: 12:07
 * To change this template use File | Settings | File Templates.
 */
public class SingleHopVsMultiHop {
	private static final Logger logger = LoggerFactory.getLogger(UbermepToUberlay.class);
	private static UPAddress clientUrn = new UPAddress("urn:itm:1");
	private static InetSocketAddress clientLocalSocketAddress = new InetSocketAddress("0.0.0.0", 8080);
	private static UPAddress serverUrn = new UPAddress("urn:itm:2");
	private static InetSocketAddress serverLocalSocketAddress = new InetSocketAddress("0.0.0.0", 8081);
	private static UPAddress transitHost1Urn = new UPAddress("urn:itm:3");
	private static InetSocketAddress transitHost1LocalSocketAddress = new InetSocketAddress("0.0.0.0", 8082);
	private static UPAddress transitHost2Urn = new UPAddress("urn:itm:4");
	private static InetSocketAddress transitHost2LocalSocketAddress = new InetSocketAddress("0.0.0.0", 8083);
	private static UPAddress transitHost3Urn = new UPAddress("urn:itm:5");
	private static InetSocketAddress transitHost3LocalSocketAddress = new InetSocketAddress("0.0.0.0", 8084);
	private static UPAddress transitHost4Urn = new UPAddress("urn:itm:6");
	private static InetSocketAddress transitHost4LocalSocketAddress = new InetSocketAddress("0.0.0.0", 8085);
	private static UPAddress transitHost5Urn = new UPAddress("urn:itm:7");
	private static InetSocketAddress transitHost5LocalSocketAddress = new InetSocketAddress("0.0.0.0", 8086);
	private static UPAddress transitHost6Urn = new UPAddress("urn:itm:8");
	private static InetSocketAddress transitHost6LocalSocketAddress = new InetSocketAddress("0.0.0.0", 8087);
	private static UPAddress transitHost7Urn = new UPAddress("urn:itm:9");
	private static InetSocketAddress transitHost7LocalSocketAddress = new InetSocketAddress("0.0.0.0", 8088);
	private static UPAddress transitHost8Urn = new UPAddress("urn:itm:10");
	private static InetSocketAddress transitHost8LocalSocketAddress = new InetSocketAddress("0.0.0.0", 8089);
	private static UPAddress transitHost9Urn = new UPAddress("urn:itm:11");
	private static InetSocketAddress transitHost9LocalSocketAddress = new InetSocketAddress("0.0.0.0", 8090);

	private static Peer ubermepClient;
	private static Peer ubermepServer;
	private static Peer ubermepTransitHost1;
	private static Peer ubermepTransitHost2;
	private static Peer ubermepTransitHost3;
	private static Peer ubermepTransitHost4;
	private static Peer ubermepTransitHost5;
	private static Peer ubermepTransitHost6;
	private static Peer ubermepTransitHost7;
	private static Peer ubermepTransitHost8;
	private static Peer ubermepTransitHost9;

	private static UberlayPeer uberlayClient;
	private static UberlayPeer uberlayServer;
	private static UberlayPeer uberlayTransitHost1;
	private static UberlayPeer uberlayTransitHost2;
	private static UberlayPeer uberlayTransitHost3;
	private static UberlayPeer uberlayTransitHost4;
	//private static UberlayPeer uberlayTransitHost5;
	//private static UberlayPeer uberlayTransitHost6;
	//private static UberlayPeer uberlayTransitHost7;

	private static Multimap<Integer, EvaluationTool.TimeInterval> intervalMap = HashMultimap.create();
	private static EvaluationTool.TimeInterval interval;
	private static boolean buildUp = false;
	private static final String WAIT_FOR_BUILDUP = "WAIT FOR BUILDUP";
	private static boolean errorOccurs = false;

	private static class UberlayEvaluationProtocolHandler extends SimpleChannelUpstreamHandler {
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (e.getMessage() instanceof UberlayEvaluationProtocol.UberlayEvaluationMsg) {
				if (!buildUp) {
					buildUp = true;
				} else {
					long stoppageTime = System.currentTimeMillis();
					UberlayEvaluationProtocol.UberlayEvaluationMsg msg = (UberlayEvaluationProtocol.UberlayEvaluationMsg) e.getMessage();
					int key = msg.getHops();
					if (key != 0) {
						//EvaluationTool.TimeInterval interval = currentIntervalMap.get(key);
						interval.setStopTimestamp(stoppageTime);
					}
				}
			} else {
				super.messageReceived(ctx, e);
			}
		}
	}

	private static UnicastMulticastRequestListener unicastMulticastRequestListener = new UnicastMulticastRequestListener() {
		@Override
		public boolean handleUnicastMulticastRequest(String senderUrn, byte[] payload) {
			if (messageType == MessageType.UnreliableUnicast) {
				long stoppageTime = System.currentTimeMillis();
				//int key = Integer.valueOf(new String(payload));
				//EvaluationTool.TimeInterval interval = currentIntervalMap.get(key);
				interval.setStopTimestamp(stoppageTime);
			}
			return true;
		}
	};

	private static final String MESSAGE = "Message";
	private static SingleRequestSingleResponseRequestListener singleRequestSingleResponseRequestListener = new SingleRequestSingleResponseRequestListener() {
		@Override
		public byte[] handleSingleRequestSingleResponseRequest(String senderUrn, byte[] requestPayload) throws UbermepExceptionEvent {
			long stoppageTime = System.currentTimeMillis();
			if (new String(requestPayload).equals(MESSAGE)){
				interval.setStopTimestamp(stoppageTime);
				//((EvaluationTool.SingleRequestSingleResponseTimeInterval) interval)
						//.setHandlerStoppageTime(System.currentTimeMillis());
			}
			return "done".getBytes();
		}
	};

	private static RpcBlockingService evaluationBlockingService = new RpcEvaluationBlockingServiceImpl();


	private static final int TOTAL_RUNS = 100;

	private static String FILE_NAME;
	private static MessageType messageType;

	public static void main(String[] args) throws ExecutionException, InterruptedException, IOException, ServiceException {
		//setMessageType(MessageType.UnreliableUnicast);
		//setMessageType(MessageType.SingleRequestSingleResponse);
		setMessageType(MessageType.Uberlay);
		//setMessageType(MessageType.RPC);

		int[] hops = EvaluationTool.createKeyArrayForSingleHopVsMultiHop();
		for (int hop : hops) {
			//int hop = 1;
			startNetwork(hop);

			int CURRENT_RUN = 0;
			buildUp = false;

			waitForBuildUp();

			while (CURRENT_RUN < TOTAL_RUNS) {
				//for (int hop : hops) {
				send(hop);
				//}
				if (!errorOccurs) {
					while (!interval.isDone()) {
					}
					/*for (int key : currentIntervalMap.keySet()) {
						EvaluationTool.TimeInterval interval = currentIntervalMap.get(key);
					}*/
					//EvaluationTool.appendCurrentTimeMapToFile(FILE_NAME,
					//		EvaluationTool.createLineForAppendToFileForSingleHopVsMultiHop(hop, interval));
					intervalMap.put(hop, interval);
					CURRENT_RUN++;
				}
				//currentIntervalMap = new HashMap<Integer, EvaluationTool.TimeInterval>();
			}

			stopNetwork(hop);

			Thread.sleep(1000);
		}

		//append to File
		EvaluationTool.appendCurrentTimeMapToFile(FILE_NAME, intervalMap, TOTAL_RUNS);

		System.exit(-1);
	}

	private static void waitForBuildUp() throws ExecutionException, InterruptedException {
		while (!buildUp) {
			switch (messageType) {
				case Uberlay:
					UberlayEvaluationProtocol.UberlayEvaluationMsg msg = UberlayEvaluationProtocol.UberlayEvaluationMsg.newBuilder()
							.setHops(0).build();
					uberlayClient.getChannel().write(msg, serverUrn);
					break;
				default:
					Future<Response> responseFuture = ubermepClient.send(new SingleRequestSingleResponseRequest(serverUrn, WAIT_FOR_BUILDUP.getBytes()));
					Response response = responseFuture.get();
					if (response instanceof SingleRequestSingleResponseResponse) {
						buildUp = true;
					}
					break;
			}
		}
	}

	private static void send(int hop) throws ExecutionException, InterruptedException, ServiceException {
		byte[] payload = MESSAGE.getBytes();

		UPAddress destUrn = serverUrn;

		long startingTime = System.currentTimeMillis();
		//interval = new EvaluationTool.SingleRequestSingleResponseTimeInterval(startingTime);
		interval = new EvaluationTool.TimeInterval(startingTime);
		long stoppageTime;
		Future<Response> responseFuture;

		switch (messageType) {
			case Uberlay:
				UberlayEvaluationProtocol.UberlayEvaluationMsg message = UberlayEvaluationProtocol.UberlayEvaluationMsg.newBuilder().
						setHops(hop).build();
				uberlayClient.getChannel().write(message, destUrn);
				break;
			case UnreliableUnicast:
				ubermepClient.send(new UnreliableUnicastRequest(destUrn, payload));
				break;
			case ReliableUnicast:
				responseFuture = ubermepClient.send(new ReliableUnicastRequest(destUrn, payload));
				responseFuture.get();
				stoppageTime = System.currentTimeMillis();
				//interval = currentIntervalMap.get(hop);
				interval.setStopTimestamp(stoppageTime);
				break;
			case SingleRequestSingleResponse:
				responseFuture = ubermepClient.send(new SingleRequestSingleResponseRequest(destUrn, payload));
				Response response = responseFuture.get();
				if (response instanceof SingleRequestSingleResponseResponse) {
					stoppageTime = System.currentTimeMillis();
					//interval = currentIntervalMap.get(hop);
					interval.setStopTimestamp(stoppageTime);
				} else {
					errorOccurs = true;
				}
				break;
			case RPC:
				UbermepRpcChannel channel = ubermepClient.getRpcChannel(destUrn);

				RpcEvaluationServiceProtocol.EvaluationServiceMsg msg = RpcEvaluationServiceProtocol.EvaluationServiceMsg.newBuilder()
						.setStartTimeInMillis(System.currentTimeMillis()).build();
				RpcEvaluationServiceProtocol.EvaluationService.BlockingInterface service =
						RpcEvaluationServiceProtocol.EvaluationService.newBlockingStub(channel);
				RpcEvaluationServiceProtocol.EvaluationServiceMsg serviceMsg = service.run(new RpcControllerImpl(), msg);
				stoppageTime = System.currentTimeMillis();

				interval = new EvaluationTool.TimeInterval(serviceMsg.getStartTimeInMillis());
				interval.setStopTimestamp(stoppageTime);
				break;
		}
	}

	private static void setMessageType(MessageType type) throws IOException {
		messageType = type;
		FILE_NAME = EvaluationTool.formatOutput("SingleHopVsMultiHop{}{}Runs.txt", messageType, TOTAL_RUNS);
		EvaluationTool.createFile(FILE_NAME, TOTAL_RUNS, EvaluationTool.EVALUATION_TYPE.SINGLEHOP_VS_MULTIHOP);
	}

	private static void startNetwork(int hop) throws ExecutionException, InterruptedException {
		if (messageType == MessageType.Uberlay) {
			switch (hop) {
				case 1:
					uberlayClient = startUberlayPeer(clientUrn, clientLocalSocketAddress, null);
					uberlayServer = startUberlayPeer(serverUrn, serverLocalSocketAddress, clientLocalSocketAddress);
					break;
				case 2:
					uberlayClient = startUberlayPeer(clientUrn, clientLocalSocketAddress, null);
					uberlayTransitHost1 = startUberlayPeer(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					uberlayServer = startUberlayPeer(serverUrn, serverLocalSocketAddress, transitHost1LocalSocketAddress);
					break;
				case 3:
					uberlayClient = startUberlayPeer(clientUrn, clientLocalSocketAddress, null);
					uberlayTransitHost1 = startUberlayPeer(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					uberlayTransitHost2 = startUberlayPeer(transitHost2Urn, transitHost2LocalSocketAddress, transitHost1LocalSocketAddress);
					uberlayServer = startUberlayPeer(serverUrn, serverLocalSocketAddress, transitHost2LocalSocketAddress);
					break;
				case 4:
					uberlayClient = startUberlayPeer(clientUrn, clientLocalSocketAddress, null);
					uberlayTransitHost1 = startUberlayPeer(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					uberlayTransitHost2 = startUberlayPeer(transitHost2Urn, transitHost2LocalSocketAddress, transitHost1LocalSocketAddress);
					uberlayTransitHost3 = startUberlayPeer(transitHost3Urn, transitHost3LocalSocketAddress, transitHost2LocalSocketAddress);
					uberlayServer = startUberlayPeer(serverUrn, serverLocalSocketAddress, transitHost3LocalSocketAddress);
					break;
				case 5:
					uberlayClient = startUberlayPeer(clientUrn, clientLocalSocketAddress, null);
					uberlayTransitHost1 = startUberlayPeer(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					uberlayTransitHost2 = startUberlayPeer(transitHost2Urn, transitHost2LocalSocketAddress, transitHost1LocalSocketAddress);
					uberlayTransitHost3 = startUberlayPeer(transitHost3Urn, transitHost3LocalSocketAddress, transitHost2LocalSocketAddress);
					uberlayTransitHost4 = startUberlayPeer(transitHost4Urn, transitHost4LocalSocketAddress, transitHost3LocalSocketAddress);
					uberlayServer = startUberlayPeer(serverUrn, serverLocalSocketAddress, transitHost4LocalSocketAddress);
					break;
			}
		} else {
			switch (hop) {
				case 1:
					ubermepClient = new PeerImpl(clientUrn, clientLocalSocketAddress);
					ubermepServer = new PeerImpl(serverUrn, serverLocalSocketAddress, clientLocalSocketAddress);

					ubermepServer.addRequestListener(unicastMulticastRequestListener);
					ubermepServer.addRequestListener(singleRequestSingleResponseRequestListener);
					ubermepServer.registerBlockingService(evaluationBlockingService);

					ubermepClient.start();
					ubermepServer.start();
					break;
				case 2:
					ubermepClient = new PeerImpl(clientUrn, clientLocalSocketAddress);
					ubermepTransitHost1 = new PeerImpl(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					ubermepServer = new PeerImpl(serverUrn, serverLocalSocketAddress, transitHost1LocalSocketAddress);

					ubermepServer.addRequestListener(unicastMulticastRequestListener);
					ubermepServer.addRequestListener(singleRequestSingleResponseRequestListener);
					ubermepServer.registerBlockingService(evaluationBlockingService);

					ubermepClient.start();
					ubermepTransitHost1.start();
					ubermepServer.start();
					break;
				case 3:
					ubermepClient = new PeerImpl(clientUrn, clientLocalSocketAddress);
					ubermepTransitHost1 = new PeerImpl(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					ubermepTransitHost2 = new PeerImpl(transitHost2Urn, transitHost2LocalSocketAddress, transitHost1LocalSocketAddress);
					ubermepServer = new PeerImpl(serverUrn, serverLocalSocketAddress, transitHost2LocalSocketAddress);

					ubermepServer.addRequestListener(unicastMulticastRequestListener);
					ubermepServer.addRequestListener(singleRequestSingleResponseRequestListener);
					ubermepServer.registerBlockingService(evaluationBlockingService);

					ubermepClient.start();
					ubermepTransitHost1.start();
					ubermepTransitHost2.start();
					ubermepServer.start();
					break;
				case 4:
					ubermepClient = new PeerImpl(clientUrn, clientLocalSocketAddress);
					ubermepTransitHost1 = new PeerImpl(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					ubermepTransitHost2 = new PeerImpl(transitHost2Urn, transitHost2LocalSocketAddress, transitHost1LocalSocketAddress);
					ubermepTransitHost3 = new PeerImpl(transitHost3Urn, transitHost3LocalSocketAddress, transitHost2LocalSocketAddress);
					ubermepServer = new PeerImpl(serverUrn, serverLocalSocketAddress, transitHost3LocalSocketAddress);

					ubermepServer.addRequestListener(unicastMulticastRequestListener);
					ubermepServer.addRequestListener(singleRequestSingleResponseRequestListener);
					ubermepServer.registerBlockingService(evaluationBlockingService);

					ubermepClient.start();
					ubermepTransitHost1.start();
					ubermepTransitHost2.start();
					ubermepTransitHost3.start();
					ubermepServer.start();
					break;
				case 5:
					ubermepClient = new PeerImpl(clientUrn, clientLocalSocketAddress);
					ubermepTransitHost1 = new PeerImpl(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					ubermepTransitHost2 = new PeerImpl(transitHost2Urn, transitHost2LocalSocketAddress, transitHost1LocalSocketAddress);
					ubermepTransitHost3 = new PeerImpl(transitHost3Urn, transitHost3LocalSocketAddress, transitHost2LocalSocketAddress);
					ubermepTransitHost4 = new PeerImpl(transitHost4Urn, transitHost4LocalSocketAddress, transitHost3LocalSocketAddress);
					ubermepServer = new PeerImpl(serverUrn, serverLocalSocketAddress, transitHost4LocalSocketAddress);

					ubermepServer.addRequestListener(unicastMulticastRequestListener);
					ubermepServer.addRequestListener(singleRequestSingleResponseRequestListener);
					ubermepServer.registerBlockingService(evaluationBlockingService);

					ubermepClient.start();
					ubermepTransitHost1.start();
					ubermepTransitHost2.start();
					ubermepTransitHost3.start();
					ubermepTransitHost4.start();
					ubermepServer.start();
					break;
				case 6:
					ubermepClient = new PeerImpl(clientUrn, clientLocalSocketAddress);
					ubermepTransitHost1 = new PeerImpl(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					ubermepTransitHost2 = new PeerImpl(transitHost2Urn, transitHost2LocalSocketAddress, transitHost1LocalSocketAddress);
					ubermepTransitHost3 = new PeerImpl(transitHost3Urn, transitHost3LocalSocketAddress, transitHost2LocalSocketAddress);
					ubermepTransitHost4 = new PeerImpl(transitHost4Urn, transitHost4LocalSocketAddress, transitHost3LocalSocketAddress);
					ubermepTransitHost5 = new PeerImpl(transitHost5Urn, transitHost5LocalSocketAddress, transitHost4LocalSocketAddress);
					ubermepServer = new PeerImpl(serverUrn, serverLocalSocketAddress, transitHost5LocalSocketAddress);

					ubermepServer.addRequestListener(unicastMulticastRequestListener);
					ubermepServer.addRequestListener(singleRequestSingleResponseRequestListener);
					ubermepServer.registerBlockingService(evaluationBlockingService);

					ubermepClient.start();
					ubermepTransitHost1.start();
					ubermepTransitHost2.start();
					ubermepTransitHost3.start();
					ubermepTransitHost4.start();
					ubermepTransitHost5.start();
					ubermepServer.start();
					break;
				case 7:
					ubermepClient = new PeerImpl(clientUrn, clientLocalSocketAddress);
					ubermepTransitHost1 = new PeerImpl(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					ubermepTransitHost2 = new PeerImpl(transitHost2Urn, transitHost2LocalSocketAddress, transitHost1LocalSocketAddress);
					ubermepTransitHost3 = new PeerImpl(transitHost3Urn, transitHost3LocalSocketAddress, transitHost2LocalSocketAddress);
					ubermepTransitHost4 = new PeerImpl(transitHost4Urn, transitHost4LocalSocketAddress, transitHost3LocalSocketAddress);
					ubermepTransitHost5 = new PeerImpl(transitHost5Urn, transitHost5LocalSocketAddress, transitHost4LocalSocketAddress);
					ubermepTransitHost6 = new PeerImpl(transitHost6Urn, transitHost6LocalSocketAddress, transitHost5LocalSocketAddress);
					ubermepServer = new PeerImpl(serverUrn, serverLocalSocketAddress, transitHost6LocalSocketAddress);

					ubermepServer.addRequestListener(unicastMulticastRequestListener);
					ubermepServer.addRequestListener(singleRequestSingleResponseRequestListener);
					ubermepServer.registerBlockingService(evaluationBlockingService);

					ubermepClient.start();
					ubermepTransitHost1.start();
					ubermepTransitHost2.start();
					ubermepTransitHost3.start();
					ubermepTransitHost4.start();
					ubermepTransitHost5.start();
					ubermepTransitHost6.start();
					ubermepServer.start();
					break;
				case 8:
					ubermepClient = new PeerImpl(clientUrn, clientLocalSocketAddress);
					ubermepTransitHost1 = new PeerImpl(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					ubermepTransitHost2 = new PeerImpl(transitHost2Urn, transitHost2LocalSocketAddress, transitHost1LocalSocketAddress);
					ubermepTransitHost3 = new PeerImpl(transitHost3Urn, transitHost3LocalSocketAddress, transitHost2LocalSocketAddress);
					ubermepTransitHost4 = new PeerImpl(transitHost4Urn, transitHost4LocalSocketAddress, transitHost3LocalSocketAddress);
					ubermepTransitHost5 = new PeerImpl(transitHost5Urn, transitHost5LocalSocketAddress, transitHost4LocalSocketAddress);
					ubermepTransitHost6 = new PeerImpl(transitHost6Urn, transitHost6LocalSocketAddress, transitHost5LocalSocketAddress);
					ubermepTransitHost7 = new PeerImpl(transitHost7Urn, transitHost7LocalSocketAddress, transitHost6LocalSocketAddress);
					ubermepServer = new PeerImpl(serverUrn, serverLocalSocketAddress, transitHost7LocalSocketAddress);

					ubermepServer.addRequestListener(unicastMulticastRequestListener);
					ubermepServer.addRequestListener(singleRequestSingleResponseRequestListener);
					ubermepServer.registerBlockingService(evaluationBlockingService);

					ubermepClient.start();
					ubermepTransitHost1.start();
					ubermepTransitHost2.start();
					ubermepTransitHost3.start();
					ubermepTransitHost4.start();
					ubermepTransitHost5.start();
					ubermepTransitHost6.start();
					ubermepTransitHost7.start();
					ubermepServer.start();
					break;
				case 9:
					ubermepClient = new PeerImpl(clientUrn, clientLocalSocketAddress);
					ubermepTransitHost1 = new PeerImpl(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					ubermepTransitHost2 = new PeerImpl(transitHost2Urn, transitHost2LocalSocketAddress, transitHost1LocalSocketAddress);
					ubermepTransitHost3 = new PeerImpl(transitHost3Urn, transitHost3LocalSocketAddress, transitHost2LocalSocketAddress);
					ubermepTransitHost4 = new PeerImpl(transitHost4Urn, transitHost4LocalSocketAddress, transitHost3LocalSocketAddress);
					ubermepTransitHost5 = new PeerImpl(transitHost5Urn, transitHost5LocalSocketAddress, transitHost4LocalSocketAddress);
					ubermepTransitHost6 = new PeerImpl(transitHost6Urn, transitHost6LocalSocketAddress, transitHost5LocalSocketAddress);
					ubermepTransitHost7 = new PeerImpl(transitHost7Urn, transitHost7LocalSocketAddress, transitHost6LocalSocketAddress);
					ubermepTransitHost8 = new PeerImpl(transitHost8Urn, transitHost8LocalSocketAddress, transitHost7LocalSocketAddress);
					ubermepServer = new PeerImpl(serverUrn, serverLocalSocketAddress, transitHost8LocalSocketAddress);

					ubermepServer.addRequestListener(unicastMulticastRequestListener);
					ubermepServer.addRequestListener(singleRequestSingleResponseRequestListener);
					ubermepServer.registerBlockingService(evaluationBlockingService);

					ubermepClient.start();
					ubermepTransitHost1.start();
					ubermepTransitHost2.start();
					ubermepTransitHost3.start();
					ubermepTransitHost4.start();
					ubermepTransitHost5.start();
					ubermepTransitHost6.start();
					ubermepTransitHost7.start();
					ubermepTransitHost8.start();
					ubermepServer.start();
					break;
				case 10:
					ubermepClient = new PeerImpl(clientUrn, clientLocalSocketAddress);
					ubermepTransitHost1 = new PeerImpl(transitHost1Urn, transitHost1LocalSocketAddress, clientLocalSocketAddress);
					ubermepTransitHost2 = new PeerImpl(transitHost2Urn, transitHost2LocalSocketAddress, transitHost1LocalSocketAddress);
					ubermepTransitHost3 = new PeerImpl(transitHost3Urn, transitHost3LocalSocketAddress, transitHost2LocalSocketAddress);
					ubermepTransitHost4 = new PeerImpl(transitHost4Urn, transitHost4LocalSocketAddress, transitHost3LocalSocketAddress);
					ubermepTransitHost5 = new PeerImpl(transitHost5Urn, transitHost5LocalSocketAddress, transitHost4LocalSocketAddress);
					ubermepTransitHost6 = new PeerImpl(transitHost6Urn, transitHost6LocalSocketAddress, transitHost5LocalSocketAddress);
					ubermepTransitHost7 = new PeerImpl(transitHost7Urn, transitHost7LocalSocketAddress, transitHost6LocalSocketAddress);
					ubermepTransitHost8 = new PeerImpl(transitHost8Urn, transitHost8LocalSocketAddress, transitHost7LocalSocketAddress);
					ubermepTransitHost9 = new PeerImpl(transitHost9Urn, transitHost9LocalSocketAddress, transitHost8LocalSocketAddress);
					ubermepServer = new PeerImpl(serverUrn, serverLocalSocketAddress, transitHost9LocalSocketAddress);

					ubermepServer.addRequestListener(unicastMulticastRequestListener);
					ubermepServer.addRequestListener(singleRequestSingleResponseRequestListener);
					ubermepServer.registerBlockingService(evaluationBlockingService);

					ubermepClient.start();
					ubermepTransitHost1.start();
					ubermepTransitHost2.start();
					ubermepTransitHost3.start();
					ubermepTransitHost4.start();
					ubermepTransitHost5.start();
					ubermepTransitHost6.start();
					ubermepTransitHost7.start();
					ubermepTransitHost8.start();
					ubermepTransitHost9.start();
					ubermepServer.start();
					break;
			}
		}
	}

	private static void stopNetwork(int hop) {
		if (messageType == MessageType.Uberlay) {
			switch (hop) {
				case 1:
					uberlayServer.shutdown();
					uberlayClient.shutdown();
					break;
				case 2:
					uberlayServer.shutdown();
					uberlayTransitHost1.shutdown();
					uberlayClient.shutdown();
					break;
				case 3:
					uberlayServer.shutdown();
					uberlayTransitHost2.shutdown();
					uberlayTransitHost1.shutdown();
					uberlayClient.shutdown();
					break;
				case 4:
					uberlayServer.shutdown();
					uberlayTransitHost3.shutdown();
					uberlayTransitHost2.shutdown();
					uberlayTransitHost1.shutdown();
					uberlayClient.shutdown();
					break;
				case 5:
					uberlayServer.shutdown();
					uberlayTransitHost4.shutdown();
					uberlayTransitHost3.shutdown();
					uberlayTransitHost2.shutdown();
					uberlayTransitHost1.shutdown();
					uberlayClient.shutdown();
					break;
			}
		} else {
			switch (hop) {
				case 1:
					ubermepServer.stop();
					ubermepClient.stop();
					break;
				case 2:
					ubermepServer.stop();
					ubermepTransitHost1.stop();
					ubermepClient.stop();
					break;
				case 3:
					ubermepServer.stop();
					ubermepTransitHost2.stop();
					ubermepTransitHost1.stop();
					ubermepClient.stop();
					break;
				case 4:
					ubermepServer.stop();
					ubermepTransitHost3.stop();
					ubermepTransitHost2.stop();
					ubermepTransitHost1.stop();
					ubermepClient.stop();
					break;
				case 5:
					ubermepServer.stop();
					ubermepTransitHost4.stop();
					ubermepTransitHost3.stop();
					ubermepTransitHost2.stop();
					ubermepTransitHost1.stop();
					ubermepClient.stop();
					break;
				case 6:
					ubermepServer.stop();
					ubermepTransitHost5.stop();
					ubermepTransitHost4.stop();
					ubermepTransitHost3.stop();
					ubermepTransitHost2.stop();
					ubermepTransitHost1.stop();
					ubermepClient.stop();
					break;
				case 7:
					ubermepServer.stop();
					ubermepTransitHost6.stop();
					ubermepTransitHost5.stop();
					ubermepTransitHost4.stop();
					ubermepTransitHost3.stop();
					ubermepTransitHost2.stop();
					ubermepTransitHost1.stop();
					ubermepClient.stop();
					break;
				case 8:
					ubermepServer.stop();
					ubermepTransitHost7.stop();
					ubermepTransitHost6.stop();
					ubermepTransitHost5.stop();
					ubermepTransitHost4.stop();
					ubermepTransitHost3.stop();
					ubermepTransitHost2.stop();
					ubermepTransitHost1.stop();
					ubermepClient.stop();
					break;
				case 9:
					ubermepServer.stop();
					ubermepTransitHost8.stop();
					ubermepTransitHost7.stop();
					ubermepTransitHost6.stop();
					ubermepTransitHost5.stop();
					ubermepTransitHost4.stop();
					ubermepTransitHost3.stop();
					ubermepTransitHost2.stop();
					ubermepTransitHost1.stop();
					ubermepClient.stop();
					break;
				case 10:
					ubermepServer.stop();
					ubermepTransitHost9.stop();
					ubermepTransitHost8.stop();
					ubermepTransitHost7.stop();
					ubermepTransitHost6.stop();
					ubermepTransitHost5.stop();
					ubermepTransitHost4.stop();
					ubermepTransitHost3.stop();
					ubermepTransitHost2.stop();
					ubermepTransitHost1.stop();
					ubermepClient.stop();
					break;
			}
		}
	}


	private enum MessageType {
		Uberlay, UnreliableUnicast, ReliableUnicast, SingleRequestSingleResponse, RPC
	}

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

	public static UberlayPeer startUberlayPeer(UPAddress localUrn, InetSocketAddress localSocketAddress,
											   @Nullable InetSocketAddress remoteSocketAddress) throws ExecutionException, InterruptedException {
		final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(
				PeerConfig.CORE_POOL_SIZE,
				new ThreadFactoryBuilder().setNameFormat("UbermepToUberlay -" + localUrn + "- %d").build()
		);

		final ChannelPipeline applicationPipeline = Channels.pipeline(
				new ProtobufEncoder(),
				new ProtobufDecoder(UberlayEvaluationProtocol.UberlayEvaluationMsg.getDefaultInstance()),
				new DefaultLoggingHandler(),
				new UberlayEvaluationProtocolHandler()
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
