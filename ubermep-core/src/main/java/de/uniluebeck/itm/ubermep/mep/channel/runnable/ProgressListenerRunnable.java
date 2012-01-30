package de.uniluebeck.itm.ubermep.mep.channel.runnable;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 05.12.11
 * Time: 18:59
 * To change this template use File | Settings | File Templates.
 */
public interface ProgressListenerRunnable extends Runnable {
	public void singleResponseReceived(String senderUrn, byte[] payload, int current, int total);
}
