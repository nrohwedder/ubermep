package de.uniluebeck.itm.ubermep.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.09.11
 * Time: 13:58
 * To change this template use File | Settings | File Templates.
 */
public class MEPReliableMessageServiceView extends JPanel{
	private JTextField destUrns;
	private JTextField payload;
	private JTextField responsePayload;
	private JTextField totalMultiMessages;
	private JButton sendReliableUniMulticastRequest;
	private JButton sendSingleRequestSingleResponseRequest;
	private JButton sendSingleRequestMultiResponseRequest;
	private JButton sendMultiRequestMultiResponseRequest;

	public MEPReliableMessageServiceView() {
		super(new FlowLayout());
		((FlowLayout) super.getLayout()).setAlignment(FlowLayout.LEFT);

		JPanel panel = new JPanel(new GridLayout(6, 2));

		{
			JLabel destUrnLabel = new JLabel("dest urns (comma seperated)");
			destUrns = new JTextField(20);

			panel.add(destUrnLabel);
			panel.add(destUrns);

			JLabel payloadLabel = new JLabel("payload");
			payload = new JTextField(20);

			panel.add(payloadLabel);
			panel.add(payload);

			JLabel responsePayloadLabel = new JLabel("response payload (comma seperated for multiresponses)");
			responsePayload = new JTextField(20);

			panel.add(responsePayloadLabel);
			panel.add(responsePayload);

			JLabel totalMultiMessageLabel = new JLabel("number of multi-messages to be send");
			totalMultiMessages = new JTextField(5);

			panel.add(totalMultiMessageLabel);
			panel.add(totalMultiMessages);

			sendReliableUniMulticastRequest = new JButton("send Reliable Uni-/Multicast Message");
			panel.add(sendReliableUniMulticastRequest);

			sendSingleRequestSingleResponseRequest = new JButton("send SingleRequestSingleResponse Message");
			panel.add(sendSingleRequestSingleResponseRequest);

			sendSingleRequestMultiResponseRequest = new JButton("send SingleRequestMultiResponse Message");
			panel.add(sendSingleRequestMultiResponseRequest);

			sendMultiRequestMultiResponseRequest = new JButton("send MultiRequestMultiResponse Message");
			panel.add(sendMultiRequestMultiResponseRequest);
		}

		add(panel);
	}

	public JTextField getDestUrns() {
		return destUrns;
	}

	public JTextField getPayload() {
		return payload;
	}

	public JButton getSendReliableUniMulticastRequest() {
		return sendReliableUniMulticastRequest;
	}

	public JButton getSendSingleRequestSingleResponseRequest() {
		return sendSingleRequestSingleResponseRequest;
	}

	public JButton getSendSingleRequestMultiResponseRequest() {
		return sendSingleRequestMultiResponseRequest;
	}

	public JButton getSendMultiRequestMultiResponseRequest() {
		return sendMultiRequestMultiResponseRequest;
	}

	public JTextField getResponsePayload() {
		return responsePayload;
	}

	public JTextField getTotalMultiMessages() {
		return totalMultiMessages;
	}
}
