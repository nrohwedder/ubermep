import de.uniluebeck.itm.uberlay.ApplicationChannel;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.channel.future.impl.SingleRequestSingleResponseChannelFuture;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.reliable.impl.SingleRequestSingleResponseServiceHandler;
import de.uniluebeck.itm.ubermep.mep.listener.SingleRequestSingleResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.singleresponse.SingleRequestSingleResponseResponse;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 09.11.11
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */
@RunWith(MockitoJUnitRunner.class)
public class SingleRequestSingleResponseServiceHandlerTest {
	private final String requestPayload = "requestPayload";
	private final String responsePayload = "responsePayload";
	private final UPAddress clientUPAddress = new UPAddress("urn:itm:1");
	private final UPAddress serverUPAddress = new UPAddress("urn:itm:2");
	private final SingleRequestSingleResponseRequest request =
			new SingleRequestSingleResponseRequest(serverUPAddress, requestPayload.getBytes());

	private final List<SingleRequestSingleResponseRequestListener> requestResponseListenerList =
			new ArrayList<SingleRequestSingleResponseRequestListener>() {{
				add(new SingleRequestSingleResponseRequestListener() {
					@Override
					public byte[] handleSingleRequestSingleResponseRequest(String senderUrn, byte[] requestPayload)
							throws UbermepExceptionEvent {
						return responsePayload.getBytes();
					}
				});
			}};
	private final SingleRequestSingleResponseServiceHandler clientServiceHandler =
			new SingleRequestSingleResponseServiceHandler(30, TimeUnit.SECONDS, requestResponseListenerList);

	private final SingleRequestSingleResponseServiceHandler serverServiceHandler =
			new SingleRequestSingleResponseServiceHandler(30, TimeUnit.SECONDS, requestResponseListenerList);

	@Mock
	private ChannelHandlerContext ctx;

	@Mock
	private DownstreamMessageEvent e;

	@Mock
	private ApplicationChannel channel;

	@Mock
	private SingleRequestSingleResponseChannelFuture channelFuture;

	@Before
	public void setUp() throws ExecutionException, InterruptedException {
		when(e.getChannel()).thenReturn(channel);
		when(e.getFuture()).thenReturn(channelFuture);
		when(ctx.getChannel()).thenReturn(channel);
		when(e.getMessage()).thenReturn(request);
		when(e.getRemoteAddress()).thenReturn(serverUPAddress);
		when(channel.write(any(MEP.MEPPacket.class), any(UPAddress.class))).thenReturn(new SucceededChannelFuture(channel));
		doNothing().when(channelFuture).setResponse(any(SingleRequestSingleResponseResponse.class));		
	}

	@Test
	public void handleDownstreamRequestAndReceiveUpstreamResponseOnClient() throws Exception {
		ArgumentCaptor<DownstreamMessageEvent> messageEventArgumentCaptor = ArgumentCaptor.forClass(DownstreamMessageEvent.class);
		ArgumentCaptor<MEP.MEPPacket> mepArgumentCaptor = ArgumentCaptor.forClass(MEP.MEPPacket.class);
		ArgumentCaptor<SingleRequestSingleResponseResponse> responseArgumentCaptor = ArgumentCaptor.forClass(SingleRequestSingleResponseResponse.class);

		clientServiceHandler.handleDownstream(ctx, e);

		verify(ctx).sendDownstream(messageEventArgumentCaptor.capture());

		MEP.MEPPacket serverResponsePacket =
				verifyHandleUpstreamOnServerAndReturnServerResponse(messageEventArgumentCaptor, mepArgumentCaptor);

		clientServiceHandler.handleUpstream(ctx, createUpstreamMessageEvent(channel, serverResponsePacket, clientUPAddress));

		verify(channelFuture).setResponse(responseArgumentCaptor.capture());

		assertTrue(responseArgumentCaptor.getValue() != null);
		SingleRequestSingleResponseResponse response = responseArgumentCaptor.getValue();

		assertArrayEquals(response.getPayload(), responsePayload.getBytes());
		assertEquals(response.getRequest(), request);
	}

	private MEP.MEPPacket verifyHandleUpstreamOnServerAndReturnServerResponse(ArgumentCaptor<DownstreamMessageEvent> messageEventArgumentCaptor, ArgumentCaptor<MEP.MEPPacket> mepArgumentCaptor) throws Exception {
		assertTrue(messageEventArgumentCaptor.getValue() != null);
		DownstreamMessageEvent downstreamMessageEvent = messageEventArgumentCaptor.getValue();

		serverServiceHandler.handleUpstream(
				ctx,
				createUpstreamMessageEvent(
						downstreamMessageEvent.getChannel(),
						downstreamMessageEvent.getMessage(),
						downstreamMessageEvent.getRemoteAddress()));

		verify(channel).write(mepArgumentCaptor.capture(), any(UPAddress.class));

		assertTrue(mepArgumentCaptor.getValue() != null);
		return mepArgumentCaptor.getValue();
	}

	private UpstreamMessageEvent createUpstreamMessageEvent(Channel channel, Object message, SocketAddress remoteAddress) {
		return new UpstreamMessageEvent(channel, message, remoteAddress);
	}

}
