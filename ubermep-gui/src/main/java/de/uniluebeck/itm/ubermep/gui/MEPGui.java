package de.uniluebeck.itm.ubermep.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 13.09.11
 * Time: 08:41
 * To change this template use File | Settings | File Templates.
 */
public class MEPGui {
    private JFrame frame;
    private JTextArea outputTextPane = new JTextArea();
	private JTextArea receivedPayloadPane = new JTextArea();
	private JTextArea receivedResponsePane = new JTextArea();

	public MEPGui(){

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		JTabbedPane tabs = new JTabbedPane();
		Dimension preferredSize = new Dimension(800, 250);
		Dimension preferredSize2 = new Dimension(800, 100);
		Dimension preferredSize3 = new Dimension(800, 100);

		splitPane.add(tabs);

		{
			GuiRequestListener requestListener = new GuiRequestListener(receivedPayloadPane);

			MEPServiceView mepServiceView = new MEPServiceView();
			JScrollPane mepServiceScrollPane = new JScrollPane(mepServiceView);
			mepServiceScrollPane.setPreferredSize(preferredSize);
			MEPServiceController mepServiceController = new MEPServiceController(mepServiceView, requestListener);

			MEPUnreliableMessageServiceView mepUnreliableMessageServiceView = new MEPUnreliableMessageServiceView();
			JScrollPane mepUnreliableMessageServiceScrollPane = new JScrollPane(mepUnreliableMessageServiceView);
			mepUnreliableMessageServiceScrollPane.setPreferredSize(preferredSize);
			new MEPUnreliableMessageServiceController(mepUnreliableMessageServiceView, mepServiceController);

			MEPReliableMessageServiceView mepReliableMessageServiceView = new MEPReliableMessageServiceView();
			JScrollPane mepReliableMessageServiceScrollPane = new JScrollPane(mepReliableMessageServiceView);
			mepReliableMessageServiceView.setPreferredSize(preferredSize);
			new MEPReliableMessageServiceController(mepReliableMessageServiceView, mepServiceController, receivedResponsePane);

			requestListener.setReliableMessageServiceView(mepReliableMessageServiceView);
			
			tabs.addTab("MEPService", mepServiceScrollPane);
			tabs.addTab("Unreliable Messaging", mepUnreliableMessageServiceScrollPane);
			tabs.addTab("Reliable Messaging", mepReliableMessageServiceScrollPane);
		}

		outputTextPane.setEditable(false);
		receivedPayloadPane.setEditable(false);
		receivedResponsePane.setEditable(false);

		TextAreaAppender.setTextArea(outputTextPane);

		JScrollPane outputScrollPane = new JScrollPane(outputTextPane);
		outputScrollPane.setPreferredSize(preferredSize);
		outputScrollPane.setAutoscrolls(true);

		JScrollPane payloadScrollPane = new JScrollPane(receivedPayloadPane);
		payloadScrollPane.setPreferredSize(preferredSize2);
		payloadScrollPane.setAutoscrolls(true);

		JScrollPane responseScrollPane = new JScrollPane(receivedResponsePane);
		responseScrollPane.setPreferredSize(preferredSize3);
		responseScrollPane.setAutoscrolls(true);

		splitPane.add(outputScrollPane);
		splitPane2.add(splitPane);
		splitPane2.add(payloadScrollPane);
		splitPane3.add(splitPane2);
		splitPane3.add(responseScrollPane);

		frame = new JFrame("MEP Debugging client");
		frame.setContentPane(splitPane3);
		frame.pack();

	}

	public static void main(String[] args){
		MEPGui gui = new MEPGui();
		gui.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gui.frame.setVisible(true);
	}
}
