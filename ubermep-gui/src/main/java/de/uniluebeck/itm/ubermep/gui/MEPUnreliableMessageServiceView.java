package de.uniluebeck.itm.ubermep.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 28.09.11
 * Time: 12:47
 * To change this template use File | Settings | File Templates.
 */
public class MEPUnreliableMessageServiceView extends JPanel{
	private JTextField destUrns;
	private JTextField payload;
	private JButton sendUnreliableUniMulticastRequest;

	public MEPUnreliableMessageServiceView() {
		super(new FlowLayout());
		((FlowLayout) super.getLayout()).setAlignment(FlowLayout.LEFT);

		JPanel panel = new JPanel(new GridLayout(3, 2));

		{
			JLabel destUrnLabel = new JLabel("dest urns (comma seperated)");
			destUrns = new JTextField(20);

			panel.add(destUrnLabel);
			panel.add(destUrns);

			JLabel payloadLabel = new JLabel("payload");
			payload = new JTextField(20);

			panel.add(payloadLabel);
			panel.add(payload);

			sendUnreliableUniMulticastRequest = new JButton("send Unreliable Uni-/Multicast Message");
			panel.add(sendUnreliableUniMulticastRequest);
		}

		add(panel);
	}

	public JTextField getDestUrns() {
		return destUrns;
	}

	public JTextField getPayload() {
		return payload;
	}

	public JButton getSendUnreliableUniMulticastRequest() {
		return sendUnreliableUniMulticastRequest;
	}
}
