package de.uniluebeck.itm.example.main.servlet;

import de.uniluebeck.itm.example.servlet.HttpServletChannelDownstreamHandler;
import de.uniluebeck.itm.example.servlet.HttpServletChannelFuture;
import de.uniluebeck.itm.example.servlet.HttpServletRequestListener;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.AbstractPeer;
import de.uniluebeck.itm.ubermep.PeerImpl;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.07.11
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
public class HttpServletMain {
	private static final Logger log = LoggerFactory.getLogger(HttpServletMain.class);

	//TEMP
	public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
		run();
	}

	private static void run() throws ExecutionException, InterruptedException, IOException {
		UPAddress serverUrn = new UPAddress("urn:itm:1");
		UPAddress transitHostUrn = new UPAddress("urn:itm:2");
		UPAddress clientUrn = new UPAddress("urn:itm:3");

		PeerImpl server = new PeerImpl(serverUrn, AbstractPeer.buildSocketAddress("0.0.0.0", "8080"));
		PeerImpl transitHost = new PeerImpl(transitHostUrn, AbstractPeer.buildSocketAddress("0.0.0.0", "8081"),
				AbstractPeer.buildSocketAddress("0.0.0.0", "8080"));
		PeerImpl client = new PeerImpl(clientUrn, AbstractPeer.buildSocketAddress("0.0.0.0", "8082"),
				AbstractPeer.buildSocketAddress("0.0.0.0", "8081"));

		server.start();
		transitHost.start();
		client.start();

		//wait for network to build up
		Thread.sleep(11000);

		HttpServletChannelDownstreamHandler handler = new HttpServletChannelDownstreamHandler();
		client.registerDownstreamHandler(handler);

		server.addRequestListener(new HttpServletRequestListener());

		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "index.html");
		try {
			HttpServletChannelFuture future = client.send(request, serverUrn, HttpServletChannelFuture.class);
			while(!future.isDone()){}
			log.info("{}", future.getResponse());
		} catch (Exception e) {
			log.error("{}", e);
		}

		client.stop();
		transitHost.stop();
		server.stop();

		System.exit(0);
	}
}
