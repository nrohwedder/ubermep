package de.uniluebeck.itm.ubermep.gui;

import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.MultiRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.multiresponse.SingleRequestMultiResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.singleresponse.SingleRequestSingleResponseRequest;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;
import de.uniluebeck.itm.ubermep.service.UbermepService;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableMulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.reliable.ReliableUnicastRequest;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 29.09.11
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
public class MEPReliableMessageServiceController {
	private MEPReliableMessageServiceView view;
	private MEPServiceController mepServiceController;
	private JTextArea responsePane;

	private ActionListener sendReliableUniMulticastRequest = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (view.getDestUrns().getText().equals("")) {
				throw new RuntimeException("Please set up dest urn properly");
			}

			byte[] payload = view.getPayload().getText().getBytes();

			String[] destUrns = view.getDestUrns().getText().split(",");
			ReliableRequest reliableRequest;
			if (destUrns.length > 1) {
				Set<UPAddress> addressSet = new HashSet<UPAddress>();
				for (String destUrn : destUrns) {
					addressSet.add(new UPAddress(destUrn));
				}
				reliableRequest = new ReliableMulticastRequest(addressSet, payload);
			} else {
				UPAddress destUrn = new UPAddress(view.getDestUrns().getText());
				reliableRequest = new ReliableUnicastRequest(destUrn, payload);
			}

			UbermepService mepService = mepServiceController.getPeer();
			try {
				Future<Response> responseFuture = mepService.send(reliableRequest);
				while (responseFuture.get() == null) {
				}
				responsePane.setText("response received: " + responseFuture.get());

			} catch (ExecutionException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (InterruptedException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	};

	private ActionListener sendSingleRequestSingleResponseRequest = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (view.getDestUrns().getText().equals("")) {
				throw new RuntimeException("Please set up dest urn properly");
			}

			byte[] payload = view.getPayload().getText().getBytes();

			UPAddress destUrn = new UPAddress(view.getDestUrns().getText());
			ReliableRequest reliableRequest = new SingleRequestSingleResponseRequest(destUrn, payload);
			UbermepService mepService = mepServiceController.getPeer();
			try {
				Future<Response> responseFuture = mepService.send(reliableRequest);
				while (responseFuture.get() == null) {
				}
				responsePane.setText("response received: " + responseFuture.get());

			} catch (ExecutionException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (InterruptedException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	};

	private ActionListener sendSingleRequestMultiResponseRequest = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (view.getDestUrns().getText().equals("")) {
				throw new RuntimeException("Please set up dest urn properly");
			}
			byte[] payload = view.getPayload().getText().getBytes();

			UPAddress destUrn = new UPAddress(view.getDestUrns().getText());
			ReliableRequest reliableRequest = new SingleRequestMultiResponseRequest(destUrn, payload);
			UbermepService mepService = mepServiceController.getPeer();
			try {
				Future<Response> responseFuture = mepService.send(reliableRequest);
				while (responseFuture.get() == null) {
				}
				responsePane.setText("response received: " + responseFuture.get());

			} catch (ExecutionException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (InterruptedException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	};

	private ActionListener sendMultiRequestMultiResponseRequest = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (view.getDestUrns().getText().equals("")) {
				throw new RuntimeException("Please set up dest urn properly");
			}
			byte[] payload = view.getPayload().getText().getBytes();

			String[] destUrns = view.getDestUrns().getText().split(",");
			Set<UPAddress> addressSet = new HashSet<UPAddress>();
			for (String destUrn : destUrns) {
				addressSet.add(new UPAddress(destUrn));
			}

			ReliableRequest reliableRequest = new MultiRequestMultiResponseRequest(addressSet, payload);
			UbermepService mepService = mepServiceController.getPeer();
			try {
				Future<Response> responseFuture = mepService.send(reliableRequest);
				while (responseFuture.get() == null) {
				}
				responsePane.setText("response received: " + responseFuture.get());

			} catch (ExecutionException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (InterruptedException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	};

	public MEPReliableMessageServiceController(MEPReliableMessageServiceView view,
											   MEPServiceController mepServiceController, JTextArea responsePane) {
		this.view = view;
		this.mepServiceController = mepServiceController;
		this.responsePane = responsePane;
		this.view.getSendReliableUniMulticastRequest().addActionListener(sendReliableUniMulticastRequest);
		this.view.getSendSingleRequestSingleResponseRequest().addActionListener(sendSingleRequestSingleResponseRequest);
		this.view.getSendSingleRequestMultiResponseRequest().addActionListener(sendSingleRequestMultiResponseRequest);
		this.view.getSendMultiRequestMultiResponseRequest().addActionListener(sendMultiRequestMultiResponseRequest);
	}
}
