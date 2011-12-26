package de.uniluebeck.itm.example.main.rpc;

import com.google.protobuf.ServiceException;
import de.uniluebeck.itm.example.rpc.mittagsservice.MittagsBlockingServiceImpl;
import de.uniluebeck.itm.example.rpc.mittagsservice.MittagsServiceImpl;
import de.uniluebeck.itm.ubermep.PeerImpl;
import de.uniluebeck.itm.ubermep.rpc.callback.MEPRpcCallback;
import de.uniluebeck.itm.ubermep.rpc.callback.RpcCallbackImpl;
import de.uniluebeck.itm.ubermep.rpc.channel.UbermepRpcChannel;
import de.uniluebeck.itm.ubermep.rpc.controller.RpcControllerImpl;
import de.uniluebeck.itm.rpc.mittagsservice.protocol.MittagsServiceProtocol;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
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
 * Date: 25.07.11
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
public class MittagsServiceMain {
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

		RpcService mittagsService = new MittagsServiceImpl();
		RpcBlockingService mittagsBlockingService = new MittagsBlockingServiceImpl();
		PeerImpl client = new PeerImpl(clientUrn, buildSocketAddress("0.0.0.0", "8080"));
		PeerImpl server = new PeerImpl(serverUrn, buildSocketAddress("0.0.0.0", "8081"), buildSocketAddress("0.0.0.0", "8080"));
		PeerImpl blockingServer = new PeerImpl(blockingServerUrn, buildSocketAddress("0.0.0.0", "8082"), buildSocketAddress("0.0.0.0", "8080"));

		client.start();
		server.start();
		blockingServer.start();

		//test mittagsService

		server.registerService(mittagsService);
		blockingServer.registerBlockingService(mittagsBlockingService);

		Thread.sleep(2000);

		UbermepRpcChannel serverRpcChannel = client.getRpcChannel(serverUrn);
		MittagsServiceProtocol.MittagsService.Interface service = MittagsServiceProtocol.MittagsService.newStub(serverRpcChannel);
		MittagsServiceProtocol.Gericht gericht = MittagsServiceProtocol.Gericht.newBuilder().setName("Pizza").build();
		MittagsServiceProtocol.BestellRequest request = MittagsServiceProtocol.BestellRequest.newBuilder()
				.setAnzahl(2)
				.setGericht(gericht)
				.build();
		MEPRpcCallback<MittagsServiceProtocol.BestellResponse> callback = new RpcCallbackImpl<MittagsServiceProtocol.BestellResponse>();
		RpcControllerImpl controller = new RpcControllerImpl();
		service.bestelle(controller, request, callback);

		log.info("MittagsService-Reponse: {}", callback.getResponse());
		if (controller.failed()){
			log.info(controller.errorText());
		}

		//test BlockingService

		UbermepRpcChannel blockingServerRpcChannel = client.getRpcChannel(blockingServerUrn);
		MittagsServiceProtocol.MittagsService.BlockingInterface blockingService = MittagsServiceProtocol.MittagsService.newBlockingStub(blockingServerRpcChannel);

		try {
			MittagsServiceProtocol.BestellResponse response = blockingService.bestelle(new RpcControllerImpl(), request);

			log.info("MittagsBlockingService-Response: {}", response);
		} catch (ServiceException e) {
			log.error("{}", e);
		}


		blockingServer.stop();
		server.stop();
		client.stop();

		System.exit(-1);
	}
}
