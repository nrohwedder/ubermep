package de.uniluebeck.itm.example.main.mep;

import com.google.common.util.concurrent.ListenableFuture;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.AbstractPeer;
import de.uniluebeck.itm.ubermep.PeerImpl;
import de.uniluebeck.itm.ubermep.mep.channel.runnable.ResponsePercentProgressRunnable;
import de.uniluebeck.itm.ubermep.mep.channel.runnable.ResponseProgressRunnable;
import de.uniluebeck.itm.ubermep.mep.channel.runnable.ResponseListenerRunnable;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.handle.MultiResponseHandle;
import de.uniluebeck.itm.ubermep.mep.listener.MultiResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.listener.SingleRequestSingleResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.MultiRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.SingleRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 10.11.11
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public class RequestResponseMain {
	private static final Logger log = LoggerFactory.getLogger(RequestResponseMain.class);

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		UPAddress serverUrn = new UPAddress("urn:itm:1");
		UPAddress transitHostUrn = new UPAddress("urn:itm:2");
		UPAddress clientUrn = new UPAddress("urn:itm:3");

		//create peers
		final PeerImpl server = new PeerImpl(serverUrn, AbstractPeer.buildSocketAddress("0.0.0.0", "8080"));
		final PeerImpl transitHost = new PeerImpl(transitHostUrn, AbstractPeer.buildSocketAddress("0.0.0.0", "8081"),
				AbstractPeer.buildSocketAddress("0.0.0.0", "8080"));
		final PeerImpl client = new PeerImpl(clientUrn, AbstractPeer.buildSocketAddress("0.0.0.0", "8082"),
				AbstractPeer.buildSocketAddress("0.0.0.0", "8081"));

		//add RequestListener on server for SingleRequestSingleResponseRequest
		server.addRequestListener(new SingleRequestSingleResponseRequestListener() {
			@Override
			public byte[] handleSingleRequestSingleResponseRequest(String senderUrn, byte[] requestPayload) throws UbermepExceptionEvent {
				log.info("received request from: {}", senderUrn);
				return ("sending response back to: " + senderUrn).getBytes();
			}
		});

		//add RequestListener on server for MultiResponseRequest
		server.addRequestListener(new MultiResponseRequestListener() {
			@Override
			public boolean handleMultiResponseRequest(MultiResponseHandle responseHandle, String senderUrn, byte[] requestPayload)
					throws UbermepExceptionEvent {
				log.info("received request from: {}", senderUrn);
				responseHandle.handleSingleResponse(("sending response 1 of 2 back to: " + senderUrn).getBytes(), 1, 2);
				responseHandle.handleSingleResponse(("sending response 2 of 2 back to: " + senderUrn).getBytes(), 2, 2);
				return true;
			}
		});
		//add RequestListener on transitHost for MultiResponseRequest
		transitHost.addRequestListener(new MultiResponseRequestListener() {
			@Override
			public boolean handleMultiResponseRequest(MultiResponseHandle responseHandle, String senderUrn, byte[] requestPayload)
					throws UbermepExceptionEvent {
				log.info("received request from: {}", senderUrn);
				responseHandle.handleSingleResponse(("sending response 1 of 3 back to: " + senderUrn).getBytes(), 1, 3);
				responseHandle.handleSingleResponse(("sending response 2 of 3 back to: " + senderUrn).getBytes(), 2, 3);
				responseHandle.handleSingleResponse(("sending response 3 of 3 back to: " + senderUrn).getBytes(), 3, 3);
				return true;
			}
		});

		//starting peers
		server.start();
		transitHost.start();
		client.start();

		//wait for network to build up
		Thread.sleep(11000);

		sendSingleRequestSingleResponseRequest(client, server.getLocalUPAddress());
		sendSingleRequestMultiResponseRequest(client, server.getLocalUPAddress());
		sendMultiRequestMultiResponseRequest(client, new ArrayList<UPAddress>() {{
			add(transitHost.getLocalUPAddress());
			add(server.getLocalUPAddress());
		}});

		//stopping peers
		client.stop();
		transitHost.stop();
		server.stop();

		System.exit(0);
	}

	private static void sendSingleRequestSingleResponseRequest(AbstractPeer client, UPAddress senderUrn)
			throws InterruptedException, ExecutionException {
		//create request
		ReliableRequest request =
				new SingleRequestSingleResponseRequest(senderUrn, ("send request to: " + senderUrn).getBytes());

		final ListenableFuture<Response> responseFuture = client.send(request);

		ResponseListenerRunnable runnable = new ResponseListenerRunnable(responseFuture);
		responseFuture.addListener(runnable, new ScheduledThreadPoolExecutor(1));

		//blocking call
		log.info("Received response: {}", responseFuture.get());

		//non-blocking call
		log.info("Received response: {}", runnable.getResponse());
	}

	private static void sendSingleRequestMultiResponseRequest(AbstractPeer client, UPAddress senderUrn)
			throws InterruptedException, ExecutionException {
		//create request
		ReliableRequest request =
				new SingleRequestMultiResponseRequest(senderUrn, ("send request to: " + senderUrn).getBytes());

		final ListenableFuture<Response> responseFuture = client.send(request);

		ResponseProgressRunnable progressRunnable = new ResponseProgressRunnable(responseFuture) {
			@Override
			public void progress(String senderUrn, byte[] payload, int current, int total) {
				//do something with received single-response
				log.info("received: " + current + " of " + total);
			}
		};
		responseFuture.addListener(progressRunnable, new ScheduledThreadPoolExecutor(2));

		//blocking call
		log.info("Received response: {}", responseFuture.get());

		//non-blocking call
		log.info("Received response: {}", progressRunnable.getResponse());
	}

	private static void sendMultiRequestMultiResponseRequest(AbstractPeer client, Collection<UPAddress> senderUrns)
			throws InterruptedException, ExecutionException {
		//create request
		ReliableRequest request =
				new MultiRequestMultiResponseRequest(senderUrns, ("sending requests to: " + senderUrns).getBytes());

		final ListenableFuture<Response> responseFuture = client.send(request);

		ResponseProgressRunnable progressRunnable = new ResponsePercentProgressRunnable(responseFuture);
		responseFuture.addListener(progressRunnable, new ScheduledThreadPoolExecutor(2));

		//blocking call
		log.info("Received response: {}", responseFuture.get());

		//non-blocking call
		log.info("Received response: {}", progressRunnable.getResponse());
	}

}
