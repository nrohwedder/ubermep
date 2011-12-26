package evaluation;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.Peer;
import de.uniluebeck.itm.ubermep.PeerImpl;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.listener.SingleRequestSingleResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse.SingleRequestSingleResponseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.11.11
 * Time: 21:52
 * To change this template use File | Settings | File Templates.
 */
public class PayloadToOverhead {
	private static final Logger logger = LoggerFactory.getLogger(PayloadToOverhead.class);
	private static UPAddress clientUrn = new UPAddress("urn:itm:1");
	private static UPAddress serverUrn = new UPAddress("urn:itm:2");
	private static InetSocketAddress clientLocalSocketAddress = new InetSocketAddress("0.0.0.0", 8080);
	private static InetSocketAddress serverLocalSocketAddress = new InetSocketAddress("0.0.0.0", 8081);
	private static Peer client;
	private static Peer server;

	private static boolean errorOccurs = false;
	private static int CURRENT_RUNS = 0;
	private static final int TOTAL_RUNS = 10;
	private static final String FILE_NAME = EvaluationTool.formatOutput("PayloadToOverheadFor{}Runs.txt", TOTAL_RUNS);

	private static HashMap<Integer, EvaluationTool.TimeInterval> currentIntervalMap = new HashMap<Integer, EvaluationTool.TimeInterval>();

	private static SingleRequestSingleResponseRequestListener requestListener = new SingleRequestSingleResponseRequestListener() {
		@Override
		public byte[] handleSingleRequestSingleResponseRequest(String senderUrn, byte[] requestPayload) throws UbermepExceptionEvent {
			long stopTimestamp = System.currentTimeMillis();
			EvaluationTool.TimeInterval interval = currentIntervalMap.get(requestPayload.length);
			interval.setStopTimestamp(stopTimestamp);
			return "Done".getBytes();
		}
	};

	public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
		startNetwork();

		int[] sizes = EvaluationTool.createKeyArrayForPayloadToOverhead();
		EvaluationTool.createFile(FILE_NAME, TOTAL_RUNS, EvaluationTool.EVALUATION_TYPE.PAYLOAD_TO_OVERHEAD);

		//write file
		while (CURRENT_RUNS < TOTAL_RUNS) {
			for (int i : sizes) {
				sendBytes(i);
			}

			if (errorOccurs) {
				logger.error("ErrorOccurs!");
				//ignore run
			} else {
				EvaluationTool.appendCurrentTimeMapToFile(FILE_NAME,
						EvaluationTool.createLineForAppendToFileForPayloadToOverhead(currentIntervalMap));
				CURRENT_RUNS++;
			}
			currentIntervalMap = new HashMap<Integer, EvaluationTool.TimeInterval>();
		}
		stopNetwork();

		System.exit(-1);
	}

	private static void startNetwork() throws ExecutionException, InterruptedException {
		client = new PeerImpl(clientUrn, clientLocalSocketAddress);
		server = new PeerImpl(serverUrn, serverLocalSocketAddress, clientLocalSocketAddress);

		server.addRequestListener(requestListener);
		client.start();
		server.start();

		Thread.sleep(1000);
	}

	private static void stopNetwork() {
		server.stop();
		client.stop();
	}

	private static void sendBytes(int size) throws ExecutionException, InterruptedException, IOException {
		currentIntervalMap.put(size, new EvaluationTool.TimeInterval(System.currentTimeMillis()));
		SingleRequestSingleResponseRequest request = new SingleRequestSingleResponseRequest(serverUrn, setPayload(size));
		request.setTimeOut(5);
		request.setTimeOutUnit(TimeUnit.MINUTES);
		Future<Response> responseFuture = client.send(request);
		Response response = responseFuture.get();
		if (!(response instanceof SingleRequestSingleResponseResponse)) {
			logger.error("Response not instance of SingleRequestSingleResponseResponse! \n Response: {}", response);
			errorOccurs = true;
		}
	}

	private static byte[] setPayload(int size) {
		byte[] payload = new byte[size];
		for (int i = 0; i < size; i++) {
			payload[i] = 127;
		}
		return payload;
	}

}
