import de.uniluebeck.itm.uberlay.ApplicationChannel;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.channel.future.impl.MultiResponseChannelFuture;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.MultiRequestMultiResponseServiceHandler;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.SingleRequestMultiResponseServiceHandler;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.handle.MultiResponseHandle;
import de.uniluebeck.itm.ubermep.mep.listener.MultiResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.MultiRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.MultiResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.SingleMultiResponseResponse;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.impl.MultiRequestMultiResponseResponse;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import org.jboss.netty.channel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 09.11.11
 * Time: 19:43
 * To change this template use File | Settings | File Templates.
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiRequestMultiResponseServiceHandlerTest {
	private final String requestPayload = "requestPayload";
	private final String responsePayload1 = "responsePayload1";
	private final String responsePayload2 = "responsePayload2";
	private final UPAddress serverUPAddress1 = new UPAddress("urn:itm:2");
	private final UPAddress serverUPAddress2 = new UPAddress("urn:itm:3");

	private final MultiRequestMultiResponseRequest request =
			new MultiRequestMultiResponseRequest(
					new ArrayList<UPAddress>() {{
						add(serverUPAddress1);
						add(serverUPAddress2);
					}},
					requestPayload.getBytes());

	private final MultiRequestMultiResponseRequest.SingleMultiRequestMultiResponseRequest request1 =
			new MultiRequestMultiResponseRequest.SingleMultiRequestMultiResponseRequest(serverUPAddress1, requestPayload.getBytes());
	private final MultiRequestMultiResponseRequest.SingleMultiRequestMultiResponseRequest request2 =
			new MultiRequestMultiResponseRequest.SingleMultiRequestMultiResponseRequest(serverUPAddress1, requestPayload.getBytes());

	private final List<MultiResponseRequestListener> responseListenerList =
			new ArrayList<MultiResponseRequestListener>() {{
				add(new MultiResponseRequestListener() {
					@Override
					public boolean handleMultiResponseRequest(MultiResponseHandle responseHandle, String senderUrn, byte[] requestPayload) throws UbermepExceptionEvent {
						responseHandle.handleSingleResponse(responsePayload1.getBytes(), 1, 2);
						responseHandle.handleSingleResponse(responsePayload2.getBytes(), 2, 2);
						return true;
					}
				});
			}};

	private final MultiRequestMultiResponseServiceHandler clientServiceHandlerTop =
			new MultiRequestMultiResponseServiceHandler(responseListenerList);
	private final SingleRequestMultiResponseServiceHandler clientServiceHandlerBottom =
			new SingleRequestMultiResponseServiceHandler(30, TimeUnit.SECONDS, responseListenerList);
	private final SingleRequestMultiResponseServiceHandler serverServiceHandler =
			new SingleRequestMultiResponseServiceHandler(30, TimeUnit.SECONDS, responseListenerList);

	private MultiResponseChannelFuture channelFuture;

	@Mock
	private ChannelHandlerContext ctx;

	@Mock
	private DownstreamMessageEvent e;

	@Mock
	private ApplicationChannel channel;

	@Before
	public void setUp() throws ExecutionException, InterruptedException {
		channelFuture = new MultiResponseChannelFuture(channel, 2);
		when(e.getChannel()).thenReturn(channel);
		when(e.getFuture()).thenReturn(channelFuture);
		when(ctx.getChannel()).thenReturn(channel);
		when(channel.write(any(MEP.MEPPacket.class), any(UPAddress.class))).thenReturn(new SucceededChannelFuture(channel));
	}

	@Test
	public void handleDownstreamRequestAndReceiveUpstreamResponseOnClient() throws Exception {
		//get first 2 single-multiresponses from serverUPAddress1
		int sendDownstreamTimes = handleRequest(request1, serverUPAddress1, 0);
		//get next 2 single-multiresponses from serverUPAddress2
		handleRequest(request2, serverUPAddress2, sendDownstreamTimes);

		assertTrue(channelFuture.isDone());
		assertTrue(channelFuture.isSuccess());

		assertTrue(channelFuture.getResponses().size() == 4);

		MultiResponse response = new MultiRequestMultiResponseResponse(request);
		response.setResponses(channelFuture.getResponses());

		assertArrayEquals(response.getPayload(), null);

		assertResponses(response.getResponse(serverUPAddress1), request1);
		assertResponses(response.getResponse(serverUPAddress2), request2);
	}

	private void assertResponses(Collection<Response> serverResponses, MultiRequestMultiResponseRequest.SingleMultiRequestMultiResponseRequest request) {
		assertTrue(serverResponses.size() == 2);
		for (Response singleMultiResponse : serverResponses) {
			assertEquals(singleMultiResponse.getRequest(), request);
			assertTrue(singleMultiResponse instanceof SingleMultiResponseResponse);
			if (((SingleMultiResponseResponse) singleMultiResponse).getCurrent() == 1) {
				assertArrayEquals(singleMultiResponse.getPayload(), responsePayload1.getBytes());
			} else if (((SingleMultiResponseResponse) singleMultiResponse).getCurrent() == 2) {
				assertArrayEquals(singleMultiResponse.getPayload(), responsePayload2.getBytes());
			}
		}
	}

	private int handleRequest(MultiRequestMultiResponseRequest.SingleMultiRequestMultiResponseRequest request, UPAddress serverAddress, int sendDownstreamTimes) throws Exception {
		int newSendDownstreamTimes = sendDownstreamTimes;
		when(e.getMessage()).thenReturn(request);
		when(e.getRemoteAddress()).thenReturn(serverAddress);
		ArgumentCaptor<DownstreamMessageEvent> messageEventArgumentCaptor = ArgumentCaptor.forClass(DownstreamMessageEvent.class);
		ArgumentCaptor<MEP.MEPPacket> mepArgumentCaptor = ArgumentCaptor.forClass(MEP.MEPPacket.class);

		clientServiceHandlerTop.handleDownstream(ctx, e);

		verify(ctx, times(++newSendDownstreamTimes)).sendDownstream(messageEventArgumentCaptor.capture());

		clientServiceHandlerBottom.handleDownstream(ctx, messageEventArgumentCaptor.getValue());

		verify(ctx, times(++newSendDownstreamTimes)).sendDownstream(messageEventArgumentCaptor.capture());

		List<MEP.MEPPacket> serverResponsePackets = verifyHandleUpstreamOnServerAndReturnServerResponse(messageEventArgumentCaptor, mepArgumentCaptor, newSendDownstreamTimes);
		clientServiceHandlerBottom.handleUpstream(ctx, createUpstreamMessageEvent(channel, serverResponsePackets.get(serverResponsePackets.size() - 2), serverAddress));
		clientServiceHandlerBottom.handleUpstream(ctx, createUpstreamMessageEvent(channel, serverResponsePackets.get(serverResponsePackets.size() - 1), serverAddress));
		return newSendDownstreamTimes;
	}

	private List<MEP.MEPPacket> verifyHandleUpstreamOnServerAndReturnServerResponse(
			ArgumentCaptor<DownstreamMessageEvent> messageEventArgumentCaptor, ArgumentCaptor<MEP.MEPPacket> mepArgumentCaptor, int times) throws Exception {
		assertTrue(messageEventArgumentCaptor.getValue() != null);
		DownstreamMessageEvent downstreamMessageEvent = messageEventArgumentCaptor.getValue();

		serverServiceHandler.handleUpstream(
				ctx,
				createUpstreamMessageEvent(
						downstreamMessageEvent.getChannel(),
						downstreamMessageEvent.getMessage(),
						downstreamMessageEvent.getRemoteAddress()));

		verify(channel, times(times)).write(mepArgumentCaptor.capture(), any(UPAddress.class));

		assertTrue(mepArgumentCaptor.getValue() != null);
		return mepArgumentCaptor.getAllValues();
	}

	private UpstreamMessageEvent createUpstreamMessageEvent(Channel channel, Object message, SocketAddress remoteAddress) {
		return new UpstreamMessageEvent(channel, message, remoteAddress);
	}
}
