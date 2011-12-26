package de.uniluebeck.itm.example.main.rpc;

import com.google.protobuf.ServiceException;
import de.uniluebeck.itm.example.rpc.flashnodeservice.FlashNodeBlockingServiceImpl;
import de.uniluebeck.itm.example.rpc.flashnodeservice.FlashNodeServiceImpl;
import de.uniluebeck.itm.example.rpc.flashnodeservice.protocol.FlashNodeServiceProtocol;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.PeerImpl;
import de.uniluebeck.itm.ubermep.rpc.callback.MEPRpcCallback;
import de.uniluebeck.itm.ubermep.rpc.callback.RpcCallbackImpl;
import de.uniluebeck.itm.ubermep.rpc.channel.UbermepRpcChannel;
import de.uniluebeck.itm.ubermep.rpc.controller.RpcControllerImpl;
import de.uniluebeck.itm.ubermep.rpc.service.RpcBlockingService;
import de.uniluebeck.itm.ubermep.rpc.service.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.11.11
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public class FlashNodeServiceMain {
		private static final Logger log = LoggerFactory.getLogger(MittagsServiceMain.class);

	private static InetSocketAddress buildSocketAddress(final String host, final String portString) {
		final int port = Integer.parseInt(portString);
		return new InetSocketAddress(host, port);
	}

	//TEMP
	public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
		run();
	}

	private static void run() throws ExecutionException, InterruptedException, IOException {

		UPAddress serverUrn = new UPAddress("urn:itm:1");
		UPAddress blockingServerUrn = new UPAddress("urn:itm:2");
		UPAddress clientUrn = new UPAddress("urn:itm:3");

		RpcService flashNodeService = new FlashNodeServiceImpl();
		RpcBlockingService flashNodeBlockingService = new FlashNodeBlockingServiceImpl();
		PeerImpl client = new PeerImpl(clientUrn, buildSocketAddress("0.0.0.0", "8080"));
		PeerImpl server = new PeerImpl(serverUrn, buildSocketAddress("0.0.0.0", "8081"), buildSocketAddress("0.0.0.0", "8080"));
		PeerImpl blockingServer = new PeerImpl(blockingServerUrn, buildSocketAddress("0.0.0.0", "8082"), buildSocketAddress("0.0.0.0", "8080"));

		client.start();
		server.start();
		blockingServer.start();

		//test flashNodeService

		server.registerService(flashNodeService);
		blockingServer.registerBlockingService(flashNodeBlockingService);

		Thread.sleep(2000);

		UbermepRpcChannel serverRpcChannel = client.getRpcChannel(serverUrn);
		FlashNodeServiceProtocol.FlashNodeService.Interface service = FlashNodeServiceProtocol.FlashNodeService.newStub(serverRpcChannel);
		FlashNodeServiceProtocol.FlashNodeRequest request = FlashNodeServiceProtocol.FlashNodeRequest.newBuilder().setDelay(10).build();

		MEPRpcCallback<FlashNodeServiceProtocol.FlashNodeResponse> callback = new RpcCallbackImpl<FlashNodeServiceProtocol.FlashNodeResponse>();
		RpcControllerImpl controller = new RpcControllerImpl();
		service.flashNode(controller, request, callback);

		log.info("FlashNodeService-Reponse: {}", callback.getResponse());
		if (controller.failed()){
			log.info(controller.errorText());
		}

		//test BlockingService

		UbermepRpcChannel blockingServerRpcChannel = client.getRpcChannel(blockingServerUrn);
		FlashNodeServiceProtocol.FlashNodeService.BlockingInterface blockingService = FlashNodeServiceProtocol.FlashNodeService.newBlockingStub(blockingServerRpcChannel);

		try {
			FlashNodeServiceProtocol.FlashNodeResponse response = blockingService.flashNode(new RpcControllerImpl(), request);

			log.info("FlashNodeBlockingService-Response: {}", response);
		} catch (ServiceException e) {
			log.error("{}", e);
		}


		blockingServer.stop();
		server.stop();
		client.stop();

		System.exit(-1);
	}
}
