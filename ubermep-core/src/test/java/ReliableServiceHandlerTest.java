import com.google.common.util.concurrent.SettableFuture;
import de.uniluebeck.itm.uberlay.ApplicationChannel;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.PeerConfig;
import de.uniluebeck.itm.ubermep.mep.channel.channelnexus.RequestResponseChannelNexus;
import de.uniluebeck.itm.ubermep.mep.channel.channels.impl.MultiResponseChannel;
import de.uniluebeck.itm.ubermep.mep.channel.channels.impl.SingleRequestSingleResponseChannel;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.ReliableServiceHandler;
import de.uniluebeck.itm.ubermep.mep.listener.UnicastMulticastRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableMulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableUnicastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.MultiRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.SingleRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.impl.ReliableMulticastResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse.ReliableUnicastResponse;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.SucceededChannelFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 09.11.11
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReliableServiceHandlerTest {
	private final String requestPayload = "payload";
	private final UPAddress remoteAddress1 = new UPAddress("urn:itm:1");
	private final UPAddress remoteAddress2 = new UPAddress("urn:itm:2");

	List<UnicastMulticastRequestListener> requestListenerList = new ArrayList<UnicastMulticastRequestListener>() {{
		add(new UnicastMulticastRequestListener() {
			@Override
			public boolean handleUnicastMulticastRequest(String senderUrn, byte[] payload) {
				//Do nothing
				return true;
			}
		});
	}};

	@Mock
	private ApplicationChannel channel;

	private ReliableServiceHandler reliableServiceHandler = new ReliableServiceHandler(
			PeerConfig.DEFAULT_TIMEOUT, PeerConfig.DEFAULT_TIMEOUT_TIMEUNIT, requestListenerList);

	@Mock
	private SingleRequestSingleResponseChannel singleRequestSingleResponseChannel;
	private RequestResponseChannelNexus<SingleRequestSingleResponseChannel> singleRequestSingleResponseChannelNexus;

	@Mock
	private MultiResponseChannel multiResponseChannel;
	private RequestResponseChannelNexus<MultiResponseChannel> multiResponseChannelNexus;

	@Before
	public void setUp() {
		singleRequestSingleResponseChannelNexus =
				new RequestResponseChannelNexus<SingleRequestSingleResponseChannel>(singleRequestSingleResponseChannel);
		multiResponseChannelNexus =
				new RequestResponseChannelNexus<MultiResponseChannel>(multiResponseChannel);
		reliableServiceHandler.setSingleRequestSingleResponseChannelNexus(singleRequestSingleResponseChannelNexus);
		reliableServiceHandler.setMultiResponseChannelNexus(multiResponseChannelNexus);
	}

	@Test
	public void sendReliableUnicast() throws ExecutionException, InterruptedException {
		when(channel.write(any(MEP.class), eq(remoteAddress1))).thenReturn(new SucceededChannelFuture(channel));

		ReliableUnicastRequest request = new ReliableUnicastRequest(remoteAddress1, requestPayload.getBytes());
		Future<Response> responseFuture = reliableServiceHandler.send(request, channel);

		while (responseFuture.get() == null) {
		}
		verify(channel).write(any(MEP.class), eq(remoteAddress1));

		assertTrue(responseFuture.get() instanceof ReliableUnicastResponse);

		ReliableUnicastResponse response = (ReliableUnicastResponse) responseFuture.get();

		assertEquals(response.getRequest(), request);
		assertArrayEquals(response.getPayload(), null);
	}

	@Test
	public void sendReliableMulticast() throws ExecutionException, InterruptedException {
		List<UPAddress> addressList = new ArrayList<UPAddress>() {{
			add(remoteAddress1);
			add(remoteAddress2);
		}};

		Map<UPAddress, ChannelFuture> succeededChannelFutureMap = new HashMap<UPAddress, ChannelFuture>() {{
			put(remoteAddress1, new SucceededChannelFuture(channel));
			put(remoteAddress2, new SucceededChannelFuture(channel));
		}};

		when(channel.write(any(MEP.class), eq(addressList))).thenReturn(succeededChannelFutureMap);

		ReliableMulticastRequest request = new ReliableMulticastRequest(addressList, requestPayload.getBytes());
		Future<Response> responseFuture = reliableServiceHandler.send(request, channel);

		while (responseFuture.get() == null) {
		}
		verify(channel).write(any(MEP.class), eq(addressList));

		assertTrue(responseFuture.get() instanceof ReliableMulticastResponse);

		ReliableMulticastResponse response = (ReliableMulticastResponse) responseFuture.get();

		assertEquals(response.getResponses().size(), 2);
		assertEquals(response.getRequest(), request);
		assertArrayEquals(response.getPayload(), null);
	}

	@Test
	public void sendSingleRequestSingleResponseRequest() throws ExecutionException, InterruptedException {
		final SettableFuture<Response> returnFuture = SettableFuture.create();

		when(singleRequestSingleResponseChannel.write(any(SingleRequestSingleResponseRequest.class)))
				.thenReturn(returnFuture);

		SingleRequestSingleResponseRequest request =
				new SingleRequestSingleResponseRequest(remoteAddress1, requestPayload.getBytes());

		reliableServiceHandler.send(request, channel);

		//Verify that reliableServiceHandler forwards request to SingleRequestSingleResponseChannel
		verify(singleRequestSingleResponseChannel).write(any(SingleRequestSingleResponseRequest.class));
	}

	@Test
	public void sendSingleRequestMultiResponseRequest() throws ExecutionException, InterruptedException {
		final SettableFuture<Response> returnFuture = SettableFuture.create();

		when(multiResponseChannel.write(any(SingleRequestMultiResponseRequest.class)))
				.thenReturn(returnFuture);

		SingleRequestMultiResponseRequest request =
				new SingleRequestMultiResponseRequest(remoteAddress1, requestPayload.getBytes());

		reliableServiceHandler.send(request, channel);

		//Verify that reliableServiceHandler forwards request to MultiResponseChannel
		verify(multiResponseChannel).write(any(SingleRequestMultiResponseRequest.class));
	}

	@Test
	public void sendMultiRequestMultiResponseRequest() throws ExecutionException, InterruptedException {
		List<UPAddress> addressList = new ArrayList<UPAddress>() {{
			add(remoteAddress1);
			add(remoteAddress2);
		}};
		final SettableFuture<Response> returnFuture = SettableFuture.create();

		when(multiResponseChannel.write(any(MultiRequestMultiResponseRequest.class)))
				.thenReturn(returnFuture);

		MultiRequestMultiResponseRequest request =
				new MultiRequestMultiResponseRequest(addressList, requestPayload.getBytes());

		reliableServiceHandler.send(request, channel);

		//Verify that reliableServiceHandler forwards request to MultiResponseChannel
		verify(multiResponseChannel).write(any(MultiRequestMultiResponseRequest.class));
	}
}
