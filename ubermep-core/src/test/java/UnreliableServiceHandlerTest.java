import de.uniluebeck.itm.uberlay.ApplicationChannel;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.channel.servicehandler.unreliable.UnreliableServiceHandler;
import de.uniluebeck.itm.ubermep.mep.listener.UnicastMulticastRequestListener;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl.UnreliableMulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl.UnreliableUnicastRequest;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 09.11.11
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */

@RunWith(MockitoJUnitRunner.class)
public class UnreliableServiceHandlerTest {

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

	private UnreliableServiceHandler unreliableServiceHandler = new UnreliableServiceHandler(requestListenerList);

	@Test
	public void sendUnreliableUnicast() throws ExecutionException, InterruptedException {
		UnreliableUnicastRequest request = new UnreliableUnicastRequest(remoteAddress1, requestPayload.getBytes());
		unreliableServiceHandler.send(request, channel);

		verify(channel, times(1)).write(any(MEP.class), eq(remoteAddress1));
	}

	@Test
	public void sendUnreliableMulticast() throws ExecutionException, InterruptedException {
		List<UPAddress> addressList = new ArrayList<UPAddress>() {{
			add(remoteAddress1);
			add(remoteAddress2);
		}};

		UnreliableMulticastRequest request = new UnreliableMulticastRequest(addressList, requestPayload.getBytes());
		unreliableServiceHandler.send(request, channel);
		verify(channel, times(1)).write(any(MEP.class), eq(addressList));
	}
}
