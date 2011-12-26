package de.uniluebeck.itm.ubermep.mep;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.AbstractPeer;
import de.uniluebeck.itm.ubermep.PeerImpl;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableMulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableUnicastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.SingleRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.UnreliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl.UnreliableMulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl.UnreliableUnicastRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.service.UbermepService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.07.11
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
public class UbermepMainTest {
	private static final byte[] SINGLE_REQUEST_SINGLE_RESPONSE_TEST = "Request: SingleRequestSingleResponseResponse-Test".getBytes();
	private static final byte[] SINGLE_REQUEST_MULTI_RESPONSE_TEST = "Request: SingleRequestMultiResponse-Test".getBytes();
	private static final byte[] MULTI_REQUEST_MULTI_RESPONSE_TEST = "Request: MultiRequestMultiResponse-Test".getBytes();
	private static final Logger log = LoggerFactory.getLogger(UbermepMainTest.class);

	//TEMP
	public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
		run();
	}

	private static void run() throws ExecutionException, InterruptedException, IOException {

		UPAddress urn1 = new UPAddress("urn:itm:1");
		UPAddress urn2 = new UPAddress("urn:itm:2");
		UPAddress urn3 = new UPAddress("urn:itm:3");
		UPAddress urn4 = new UPAddress("urn:itm:4");
		String urn5 = "urn:itm:5";

		PeerImpl s1 = new PeerImpl(urn1, AbstractPeer.buildSocketAddress("0.0.0.0", "8080"));
		PeerImpl s2 = new PeerImpl(urn2, AbstractPeer.buildSocketAddress("0.0.0.0", "8081"),
				AbstractPeer.buildSocketAddress("0.0.0.0", "8080"));
		PeerImpl s3 = new PeerImpl(urn3, AbstractPeer.buildSocketAddress("0.0.0.0", "8082"),
				AbstractPeer.buildSocketAddress("0.0.0.0", "8080"));
		/*PeerImpl s4 = new PeerImpl(urn4, buildSocketAddress("0.0.0.0", "8083"), buildSocketAddress("0.0.0.0", "8080"));
		PeerImpl s5 = new PeerImpl(urn5, buildSocketAddress("0.0.0.0", "8084"), buildSocketAddress("0.0.0.0", "8080"));*/

		s1.start();
		s2.start();
		s3.start();
		/*s4.start();
		s5.start();*/

		Set<UPAddress> destUrns = new HashSet<UPAddress>();
		destUrns.add(urn2);
		destUrns.add(urn3);
		destUrns.add(urn4);


		/**
		 * testUnreliableUnicastRequest Unreliable Service
		 */

		//Thread.sleep(2000);

		/*sendUnrealiableUnicastRequest(s2, urn1, "Unicast-Test 1".getBytes(), 1);

		Thread.sleep(2000);*/

		/*		sendUnreliableMulticastRequest(s2, destUrns);*/

		/**
		 * testUnreliableUnicastRequest Reliable Service
		 */

		/*Response response = sendRealiableUnicastRequest(s2, urn1, "Unicast-Test 1".getBytes(), 1);
		log.info(response.toString());*/

		/*Response response = sendRealiableMulticastRequest(s2, destUrns, "Unicast-Test 1".getBytes());
		log.info(response.toString());*/

		//sendSingleRequestSingleResponseRequests(urn1, urn2, urn3, s1, s2, s3);
		//Response response = sendSingleRequestMultiResponseRequest(s1, urn2);

		//ReliableUnicastRequest request = new ReliableUnicastRequest(urn2, "RELIABLE-UNICAST-REQUEST".getBytes());
		//ReliableMulticastRequest request = new ReliableMulticastRequest(destUrns, "RELIABLE-UNICAST-REQUEST".getBytes());
		//SingleRequestSingleResponseRequest request = new SingleRequestSingleResponseRequest(urn2, SINGLE_REQUEST_SINGLE_RESPONSE_TEST);
		//SingleRequestMultiResponseRequest request = new SingleRequestMultiResponseRequest(urn2, SINGLE_REQUEST_MULTI_RESPONSE_TEST);
		//MultiRequestMultiResponseRequest request = new MultiRequestMultiResponseRequest(destUrns, MULTI_REQUEST_MULTI_RESPONSE_TEST);

		/*Response response = sendRealiableUnicastRequest(s1, urn2, "blubb".getBytes());
		log.info("{}", response);

		response = sendRealiableMulticastRequest(s1, destUrns, "blubb".getBytes());
		log.info("{}", response);*/

		//Future<Response> future = s1.send(request);

		//while(future.get() == null){}

		//log.info("{}", future.get());

		//System.out.println(s1);

		/*Set<String> destUrns2 = new HashSet<String>();
		destUrns2.add(urn1);
		destUrns2.add(urn3);
		destUrns2.add(urn4);
		destUrns2.add(urn5);
		destUrns2.add("urn:itm:6");

		List<Future<Response>> responseList = new ArrayList<Future<Response>>();

		Future<Response> de.uniluebeck.itm.mep.channel.future = sendSingleRequestMultiResponseRequest(s2, destUrns2);

		while (!de.uniluebeck.itm.mep.channel.future.isDone()) {
		}

		MultiResponseResponse response = (MultiResponseResponse) de.uniluebeck.itm.mep.channel.future.get();
		log.info("Response: {}", response);

		while(!response.receivedAllResponses()){
			//log.info("Response: {}", response);
		}
		log.info("Response: {}", response);*/

		/*s5.stop();
		s4.stop();*/
		s3.stop();
		s2.stop();
		s1.stop();
		
		System.exit(-1);
	}

	private static void sendSingleRequestSingleResponseRequests(UPAddress urn1, UPAddress urn2, UPAddress urn3,
																PeerImpl s1, PeerImpl s2, PeerImpl s3)
			throws InterruptedException, ExecutionException {

		List<Future<Response>> futureList = new ArrayList<Future<Response>>();
		futureList.add(sendSingleRequestSingleResponseRequest(s2, urn3));
		/*futureList.add(sendSingleRequestSingleResponseRequest(s3, urn1));
		futureList.add(sendSingleRequestSingleResponseRequest(s1, urn3));
		futureList.add(sendSingleRequestSingleResponseRequest(s2, urn1));
		futureList.add(sendSingleRequestSingleResponseRequest(s3, urn2));
		futureList.add(sendSingleRequestSingleResponseRequest(s1, urn2));*/

		List<Response> responseList = new ArrayList<Response>();
		for (Future<Response> future : futureList) {
			while (!future.isDone()) {
			}
			responseList.add(future.get());
		}

		log.info("{}", responseList);
	}

	private static void sendUnrealiableUnicastRequest(UbermepService service, UPAddress urn, byte[] payload) throws InterruptedException, ExecutionException {
		UnreliableRequest request = new UnreliableUnicastRequest(urn, payload);
		service.send(request);
	}

	private static void sendUnreliableMulticastRequest(UbermepService service, Set<UPAddress> destUrns) throws InterruptedException, ExecutionException {
		//waiting for overlay build up
		Thread.sleep(15000);
		UnreliableRequest request = new UnreliableMulticastRequest(destUrns, "Multicast-Test".getBytes());
		service.send(request);
		Thread.sleep(2000);
	}

	private static Response sendRealiableUnicastRequest(UbermepService service, UPAddress urn, byte[] payload) throws InterruptedException, ExecutionException {
		Thread.sleep(2000);
		ReliableRequest request = new ReliableUnicastRequest(urn, payload);
		Future<Response> future = service.send(request);
		return future.get();
	}

	private static Response sendRealiableMulticastRequest(UbermepService service, Set<UPAddress> destUrns, byte[] payload) throws InterruptedException, ExecutionException {
		Thread.sleep(2000);
		ReliableRequest request = new ReliableMulticastRequest(destUrns, payload);
		Future<Response> future = service.send(request);
		while (!future.isDone()) {
		}
		return future.get();
	}

	private static Future<Response> sendSingleRequestSingleResponseRequest(UbermepService service, UPAddress urn) throws InterruptedException, ExecutionException {
		Thread.sleep(20000);
		ReliableRequest request = new SingleRequestSingleResponseRequest(urn, SINGLE_REQUEST_SINGLE_RESPONSE_TEST);
		return service.send(request);
	}

	private static Response sendSingleRequestMultiResponseRequest(UbermepService service, UPAddress urn) throws ExecutionException, InterruptedException {
		Thread.sleep(2000);
		ReliableRequest request = new SingleRequestMultiResponseRequest(urn, SINGLE_REQUEST_MULTI_RESPONSE_TEST);
		Future<Response> future = service.send(request);
		while (!future.isDone()) {
		}
		return future.get();
	}
}
