package de.uniluebeck.itm.ubermep.gui;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.AbstractPeer;
import de.uniluebeck.itm.ubermep.Peer;
import de.uniluebeck.itm.ubermep.PeerImpl;
import de.uniluebeck.itm.ubermep.service.UbermepService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 28.09.11
 * Time: 09:44
 * To change this template use File | Settings | File Templates.
 */
public class MEPServiceController {
	private Peer peer;
	private MEPServiceView view;
	private GuiRequestListener listener;

	private ActionListener startService = new ActionListener() {
		@Override
		public void actionPerformed(final ActionEvent e) {
			try {
				if (view.getLocalUrn().getText().equals("")) {
					throw new RuntimeException("Please set up local urn properly");
				}
				UPAddress localUrn = new UPAddress(view.getLocalUrn().getText());

				String[] localSocketAddressArray = view.getLocalSocketAddress().getText().split(":");
				InetSocketAddress localSocketAddress = null;
				if (localSocketAddressArray.length == 2) {
					String localSocketAddressHost = localSocketAddressArray[0];
					String localSocketAddressPort = localSocketAddressArray[1];
					localSocketAddress = AbstractPeer.buildSocketAddress(localSocketAddressHost, localSocketAddressPort);
				} else {
					throw new RuntimeException("Please set up localSocketAddress properly");
				}
				String[] remoteSocketAddressArray = view.getRemoteSocketAddress().getText().split(":");
				InetSocketAddress remoteSocketAddress = null;
				if (remoteSocketAddressArray.length == 2) {
					String remoteSocketAddressHost = remoteSocketAddressArray[0];
					String remoteSocketAddressPort = remoteSocketAddressArray[1];
					remoteSocketAddress = AbstractPeer.buildSocketAddress(remoteSocketAddressHost, remoteSocketAddressPort);
				}

				peer = new PeerImpl(localUrn, localSocketAddress, remoteSocketAddress);

				peer.addRequestListener(listener.getUnicastMulticastRequestListener());
				peer.addRequestListener(listener.getSingleRequestSingleResponseRequestListener());
				peer.addRequestListener(listener.getMultiResponseRequestListener());
				
				peer.start();

			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), e1.getClass().getSimpleName(), 0);
			}

		}
	};

	private ActionListener stopService = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if (peer == null){
					throw new NullPointerException("MEPService not yet started");
				}
				peer.stop();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), e1.getClass().getSimpleName(), 0);
			}
		}
	};

	private ActionListener exitService = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	};

	public MEPServiceController(MEPServiceView mepServiceView, GuiRequestListener listener) {
		this.view = mepServiceView;
		this.listener = listener;
		this.view.getStartServiceButton().addActionListener(startService);
		this.view.getStopServiceButton().addActionListener(stopService);
		this.view.getExitButton().addActionListener(exitService);
	}

	public UbermepService getPeer() {
		return peer;
	}

	public GuiRequestListener getListener() {
		return listener;
	}
}
