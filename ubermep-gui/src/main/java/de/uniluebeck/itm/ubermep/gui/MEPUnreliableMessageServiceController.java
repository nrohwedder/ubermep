package de.uniluebeck.itm.ubermep.gui;

import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.UnreliableRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl.UnreliableMulticastRequest;
import de.uniluebeck.itm.ubermep.mep.message.request.unreliable.impl.UnreliableUnicastRequest;
import de.uniluebeck.itm.ubermep.service.UbermepService;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 28.09.11
 * Time: 12:50
 * To change this template use File | Settings | File Templates.
 */
public class MEPUnreliableMessageServiceController {
	private MEPUnreliableMessageServiceView view;
	private MEPServiceController mepServiceController;

	private ActionListener sendUnreliableUniMulticastRequest = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (view.getDestUrns().getText().equals("")) {
				throw new RuntimeException("Please set up dest urn properly");
			}

			byte[] payload = view.getPayload().getText().getBytes();

			String[] destUrns = view.getDestUrns().getText().split(",");
			UnreliableRequest unreliableRequest;
			if (destUrns.length > 1) {
				Set<UPAddress> addressSet = new HashSet<UPAddress>();
				for (String destUrn : destUrns) {
					addressSet.add(new UPAddress(destUrn));
				}
				unreliableRequest = new UnreliableMulticastRequest(addressSet, payload);
			} else {
				UPAddress destUrn = new UPAddress(view.getDestUrns().getText());
				unreliableRequest = new UnreliableUnicastRequest(destUrn, payload);
			}

			UbermepService mepService = mepServiceController.getPeer();
			try {
				mepService.send(unreliableRequest);
			} catch (ExecutionException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (InterruptedException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	};

	public MEPUnreliableMessageServiceController(MEPUnreliableMessageServiceView mepUnreliableMessageServiceView, MEPServiceController mepServiceController) {
		this.view = mepUnreliableMessageServiceView;
		this.mepServiceController = mepServiceController;

		this.view.getSendUnreliableUniMulticastRequest().addActionListener(sendUnreliableUniMulticastRequest);
	}


}
