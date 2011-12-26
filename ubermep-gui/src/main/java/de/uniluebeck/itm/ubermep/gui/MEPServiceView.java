package de.uniluebeck.itm.ubermep.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 28.09.11
 * Time: 09:43
 * To change this template use File | Settings | File Templates.
 */
public class MEPServiceView extends JPanel {
	private JTextField localUrn;
	private JTextField localSocketAddress;
	private JTextField remoteSocketAddress;
	private JButton startServiceButton;
	private JButton stopServiceButton;
	private JButton exitButton;

	public MEPServiceView() {
		super(new FlowLayout());
		((FlowLayout) super.getLayout()).setAlignment(FlowLayout.LEFT);

		JPanel panel = new JPanel(new GridLayout(6, 2));

		{
			JLabel localUrnLabel = new JLabel("local urn");
			localUrn = new JTextField(20);

			panel.add(localUrnLabel);
			panel.add(localUrn);

			JLabel localSocketAddressLabel = new JLabel("local socket address");
			localSocketAddress = new JTextField(20);

			panel.add(localSocketAddressLabel);
			panel.add(localSocketAddress);

			JLabel remoteSocketAddressLabel = new JLabel("remote socket address (optional)");
			remoteSocketAddress = new JTextField(20);

			panel.add(remoteSocketAddressLabel);
			panel.add(remoteSocketAddress);

			startServiceButton = new JButton("Start MEP-Service");
			panel.add(startServiceButton);

			stopServiceButton = new JButton("Stop MEP-Service");
			panel.add(stopServiceButton);

			exitButton = new JButton("Exit");
			panel.add(exitButton);
		}

		add(panel);
	}

	public JButton getStopServiceButton() {
		return stopServiceButton;
	}

	public JButton getStartServiceButton() {
		return startServiceButton;
	}

	public JTextField getLocalUrn() {
		return localUrn;
	}

	public JTextField getLocalSocketAddress() {
		return localSocketAddress;
	}

	public JTextField getRemoteSocketAddress() {
		return remoteSocketAddress;
	}

	public JButton getExitButton() {
		return exitButton;
	}
}
