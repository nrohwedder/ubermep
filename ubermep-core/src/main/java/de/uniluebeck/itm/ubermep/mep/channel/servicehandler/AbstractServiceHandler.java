package de.uniluebeck.itm.ubermep.mep.channel.servicehandler;

import de.uniluebeck.itm.ubermep.mep.exception.RequestListenerNotFoundException;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepMultiResponseExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepSingleResponseExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.handle.MultiResponseHandle;
import de.uniluebeck.itm.ubermep.mep.listener.MultiResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.listener.SingleRequestSingleResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.listener.UnicastMulticastRequestListener;
import de.uniluebeck.itm.ubermep.mep.protocol.MEP;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.09.11
 * Time: 13:10
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractServiceHandler extends SimpleChannelHandler implements RequestListenerService,
		UnicastMulticastRequestListener, SingleRequestSingleResponseRequestListener, MultiResponseRequestListener {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	protected List<UnicastMulticastRequestListener> unicastMulticastRequestListeners;
	protected List<SingleRequestSingleResponseRequestListener> singleRequestSingleResponseRequestListeners;
	protected List<MultiResponseRequestListener> multiResponseRequestListeners;

	private void checkForNullRequestListeners(Object listenerArray, Class requestListenerClass) {
		if (listenerArray == null) {
			throw new RuntimeException("RequestListener for type: '" + requestListenerClass + "' not supported!");
		}
	}

	@Override
	public void addRequestListener(UnicastMulticastRequestListener requestListener) {
		checkForNullRequestListeners(unicastMulticastRequestListeners, UnicastMulticastRequestListener.class);
		this.unicastMulticastRequestListeners.add(requestListener);
	}

	@Override
	public void addRequestListener(SingleRequestSingleResponseRequestListener requestListener) {
		checkForNullRequestListeners(singleRequestSingleResponseRequestListeners, SingleRequestSingleResponseRequestListener.class);
		this.singleRequestSingleResponseRequestListeners.add(requestListener);
	}

	@Override
	public void addRequestListener(MultiResponseRequestListener requestListener) {
		checkForNullRequestListeners(multiResponseRequestListeners, MultiResponseRequestListener.class);
		this.multiResponseRequestListeners.add(requestListener);
	}

	public boolean handleUnicastMulticastRequest(String urn, byte[] payload) {
		checkForNullRequestListeners(unicastMulticastRequestListeners, UnicastMulticastRequestListener.class);
		if (unicastMulticastRequestListeners.size() != 0) {
			for (UnicastMulticastRequestListener listener : unicastMulticastRequestListeners) {
				boolean handled = listener.handleUnicastMulticastRequest(urn, payload);
				if (handled) {
					logRequestListenerInfoMessage(listener, UnicastMulticastRequestListener.class);
					return handled;
				}
			}
		}
		log.warn("Could not find responsible UnicastMulticastRequestListener! None used!");
		return false;
	}

	public byte[] handleSingleRequestSingleResponseRequest(String urn, byte[] payload) throws UbermepExceptionEvent {
		checkForNullRequestListeners(singleRequestSingleResponseRequestListeners, SingleRequestSingleResponseRequestListener.class);
		if (singleRequestSingleResponseRequestListeners.size() != 0) {
			for (SingleRequestSingleResponseRequestListener listener : singleRequestSingleResponseRequestListeners) {
				byte[] response = listener.handleSingleRequestSingleResponseRequest(urn, payload);
				if (response != null) {
					logRequestListenerInfoMessage(listener, SingleRequestSingleResponseRequestListener.class);
					return response;
				}
			}
		}
		log.warn(getListenerWarningMessage(urn, "SingleRequestSingleResponseRequest"));
		throw new UbermepSingleResponseExceptionEvent(
				new RequestListenerNotFoundException(getListenerWarningMessage(urn, "SingleRequestSingleResponseRequest"))
		);
	}

	public boolean handleMultiResponseRequest(MultiResponseHandle handle, String urn, byte[] payload) throws UbermepExceptionEvent {
		checkForNullRequestListeners(multiResponseRequestListeners, MultiResponseRequestListener.class);
		if (multiResponseRequestListeners.size() != 0) {
			for (MultiResponseRequestListener listener : multiResponseRequestListeners) {
				boolean handled = listener.handleMultiResponseRequest(handle, urn, payload);
				if (handled) {
					logRequestListenerInfoMessage(listener, MultiResponseRequestListener.class);
					return handled;
				}
			}
		}
		log.warn(getListenerWarningMessage(urn, "MultiResponseRequest"));
		throw new UbermepMultiResponseExceptionEvent(
				new RequestListenerNotFoundException(getListenerWarningMessage(urn, "MultiResponseRequest"))
		);
	}

	private void logRequestListenerInfoMessage(Object listener, Class listenerInterface) {
		logListenerInfoMessage(listener, listenerInterface, "Request");
	}

	private void logListenerInfoMessage(Object listener, Class listenerInterface, String listenerType){
		log.info("Responsible {} found! {}Listener of type: '{}' used!",
				new Object[]{listenerInterface.getSimpleName(), listenerType, getClassName(listener)});
	}

	private String getListenerWarningMessage(String urn, String requestType){
		return "Could not find responsible " + requestType + "Listener! " +
				"Handling of " + requestType + " on '" + urn + "' aborted!";
	}

	private String getClassName(Object o) {
		if (o.getClass().getSimpleName().equals("")) {
			return o.getClass().getName();
		} else {
			return o.getClass().getSimpleName();
		}
	}

	public static boolean isUpstreamMessageEvent(ChannelEvent e) {
		return (e instanceof UpstreamMessageEvent);
	}

	public static boolean isDownstreamMessageEvent(ChannelEvent e) {
		return (e instanceof DownstreamMessageEvent);
	}

	protected boolean checkNullRequestEntry(Object o, MEP.MEPPacket message) {
		if (o == null) {
			log.warn("Warning: RequestEntry is null! Could not find Request for Response: " + message + "!");
			return true;
		}
		return false;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		log.error("{}", e);
	}
}
