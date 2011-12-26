package de.uniluebeck.itm.ubermep.main;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.AbstractPeer;
import de.uniluebeck.itm.ubermep.PeerImpl;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 07.11.11
 * Time: 20:39
 * To change this template use File | Settings | File Templates.
 */
public class UbermepMain {
	//TODO refactor comment for not valid input
	public static void main(String[] args) throws ExecutionException, InterruptedException {
		final AbstractPeer peer;
		UPAddress urn;
		InetSocketAddress localAddress;
		InetSocketAddress remoteAddress;
		if (args.length >= 2) {
			urn = new UPAddress(args[0]);
			localAddress = createInetSocketAddress(args[1]);
		} else {
			throw new RuntimeException("Not valid params");
		}
		if (args.length == 2) {
			peer = new PeerImpl(urn, localAddress);
		} else if (args.length == 3) {
			remoteAddress = createInetSocketAddress(args[2]);
			peer = new PeerImpl(urn, localAddress, remoteAddress);
		} else {
			throw new RuntimeException("Not valid params");
		}
		peer.start();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				peer.stop();
			}
		}));
	}

	private static InetSocketAddress createInetSocketAddress(String arg) {
		String[] address = arg.split(":");
		if (address.length != 2) {
			throw new RuntimeException("Please verify address!");
		}
		return AbstractPeer.buildSocketAddress(address[0], address[1]);
	}
}
