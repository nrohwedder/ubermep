package de.uniluebeck.itm.example.main.mep;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.AbstractPeer;
import de.uniluebeck.itm.ubermep.Peer;
import de.uniluebeck.itm.ubermep.PeerImpl;
import de.uniluebeck.itm.ubermep.mep.listener.UnicastMulticastRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.UnreliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl.UnreliableMulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl.UnreliableUnicastRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.11.11
 * Time: 11:10
 * To change this template use File | Settings | File Templates.
 */
public class UnreliableUnicastMulticastMain {
	private static final Logger log = LoggerFactory.getLogger(ReliableUnicastMulticastMain.class);

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		UPAddress serverUrn = new UPAddress("urn:itm:1");
		UPAddress transitHostUrn = new UPAddress("urn:itm:2");
		UPAddress clientUrn = new UPAddress("urn:itm:3");

		//create peers
		final Peer server = new PeerImpl(serverUrn, AbstractPeer.buildSocketAddress("0.0.0.0", "8080"));
		final Peer transitHost = new PeerImpl(transitHostUrn, AbstractPeer.buildSocketAddress("0.0.0.0", "8081"),
				AbstractPeer.buildSocketAddress("0.0.0.0", "8080"));
		final Peer client = new PeerImpl(clientUrn, AbstractPeer.buildSocketAddress("0.0.0.0", "8082"),
				AbstractPeer.buildSocketAddress("0.0.0.0", "8081"));

		//add RequestListener on server for Unicast/Multicast message
		server.addRequestListener(new UnicastMulticastRequestListener() {
			@Override
			public boolean handleUnicastMulticastRequest(String senderUrn, byte[] payload) {
				log.info("received Unicast/Multicast from: {}", senderUrn);
				return true;
			}
		});


		//add RequestListener on transitHost for Multicast message
		transitHost.addRequestListener(new UnicastMulticastRequestListener() {
			@Override
			public boolean handleUnicastMulticastRequest(String senderUrn, byte[] payload) {
				log.info("received Unicast/Multicast from: {}", senderUrn);
				return true;
			}
		});

		//starting peers
		server.start();
		transitHost.start();
		client.start();

		//wait for network to build up
		Thread.sleep(11000);

		List<UPAddress> urns = new ArrayList<UPAddress>() {{
			add(transitHost.getLocalUPAddress());
			add(server.getLocalUPAddress());
		}};

		sendUnreliableUnicast(client, server.getLocalUPAddress());
		sendUnreliableMulticast(client, new ArrayList<UPAddress>() {{
			add(transitHost.getLocalUPAddress());
			add(server.getLocalUPAddress());
		}});

		//stopping peers
		client.stop();
		transitHost.stop();
		server.stop();

		System.exit(0);
	}

	private static void sendUnreliableUnicast(Peer client, UPAddress senderUrn) throws ExecutionException, InterruptedException {
		UnreliableRequest request = new UnreliableUnicastRequest(senderUrn, ("resetNode").getBytes());
		client.send(request);
	}

	private static void sendUnreliableMulticast(Peer client, ArrayList<UPAddress> senderUrns) throws ExecutionException, InterruptedException {
		UnreliableRequest request = new UnreliableMulticastRequest(senderUrns, ("sending requests to: " + senderUrns).getBytes());
		client.send(request);
	}
}
