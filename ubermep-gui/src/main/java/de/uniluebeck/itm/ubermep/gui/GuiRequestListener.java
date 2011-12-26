package de.uniluebeck.itm.ubermep.gui;

import de.uniluebeck.itm.ubermep.mep.exception.event.UbermepExceptionEvent;
import de.uniluebeck.itm.ubermep.mep.handle.MultiResponseHandle;
import de.uniluebeck.itm.ubermep.mep.listener.MultiResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.listener.SingleRequestSingleResponseRequestListener;
import de.uniluebeck.itm.ubermep.mep.listener.UnicastMulticastRequestListener;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.09.11
 * Time: 13:33
 * To change this template use File | Settings | File Templates.
 */
public class GuiRequestListener {
	private JTextArea receivedPayloadPane;
	private MEPReliableMessageServiceView reliableMessageServiceView;
	private int totalMultiMessages;
	private UnicastMulticastRequestListener unicastMulticastRequestListener =
			new UnicastMulticastRequestListener() {
				@Override
				public boolean handleUnicastMulticastRequest(String senderUrn, byte[] payload) {
					receivedPayloadPane.setText(
							"Received payload from: " + senderUrn + ": payload: " + new String(payload));
					return true;
				}
			};
	private SingleRequestSingleResponseRequestListener singleRequestSingleResponseRequestListener =
			new SingleRequestSingleResponseRequestListener() {
				@Override
				public byte[] handleSingleRequestSingleResponseRequest(String senderUrn, byte[] requestPayload) throws UbermepExceptionEvent {
					if (reliableMessageServiceView == null) {
						JOptionPane.showMessageDialog(null, "Warning: " +
								"Could not handle SingleRequestSingleResponseRequest!\n" +
								" ServiceViewInstance is Null!!!");
						return null;
					}
					String responsePayload = reliableMessageServiceView.getResponsePayload().getText();
					receivedPayloadPane.setText(
							"Set payload from: " + senderUrn + ": payload: " + new String(requestPayload) +
									" to: " + responsePayload);
					return responsePayload.getBytes();
				}
			};

	private MultiResponseRequestListener multiResponseRequestListener =
			new MultiResponseRequestListener() {
				@Override
				public boolean handleMultiResponseRequest(MultiResponseHandle responseHandle, String senderUrn, byte[] requestPayload) throws UbermepExceptionEvent {
					String[] responsePayload = reliableMessageServiceView.getResponsePayload().getText().split(",");
					setTotalMultiMessages(reliableMessageServiceView.getTotalMultiMessages());
					String payload = responsePayload[0];
					for (int i = 0; i < totalMultiMessages; i++) {
						try {
							payload = responsePayload[i];
						} catch (Exception e) {
						}
						String receivedPayloadText = "Set payload from: " + senderUrn + ": payload: " + new String(requestPayload) +
								" to: " + payload + " for message " + (i + 1) + " from " + totalMultiMessages;

						if (i == 0) {
							receivedPayloadPane.setText(receivedPayloadText);
						} else {
							receivedPayloadPane.append("\n" + receivedPayloadText);
						}
						responseHandle.handleSingleResponse(payload.getBytes(), i, totalMultiMessages);

					}
					return true;
				}
			};

	public GuiRequestListener(JTextArea receivedPayloadPane) {
		this.receivedPayloadPane = receivedPayloadPane;
	}

	public void setTotalMultiMessages(JTextField totalMultiMessages) {
		try {
			this.totalMultiMessages = Integer.parseInt(totalMultiMessages.getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Could not parse: '" + totalMultiMessages.getText() +
					"'! \nUsing default value 2 for number of total MultiMessages to be send! ", "Warning:", 0);
			this.totalMultiMessages = 2;
		}
	}

	public void setReliableMessageServiceView(MEPReliableMessageServiceView reliableMessageServiceView) {
		this.reliableMessageServiceView = reliableMessageServiceView;
	}

	public UnicastMulticastRequestListener getUnicastMulticastRequestListener() {
		return unicastMulticastRequestListener;
	}

	public SingleRequestSingleResponseRequestListener getSingleRequestSingleResponseRequestListener() {
		return singleRequestSingleResponseRequestListener;
	}

	public MultiResponseRequestListener getMultiResponseRequestListener() {
		return multiResponseRequestListener;
	}
}
