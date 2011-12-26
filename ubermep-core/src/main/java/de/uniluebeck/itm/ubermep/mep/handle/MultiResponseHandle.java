package de.uniluebeck.itm.ubermep.mep.handle;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 12.08.11
 * Time: 15:53
 * To change this template use File | Settings | File Templates.
 */
public interface MultiResponseHandle {

	public void handleSingleResponse(byte[] payload, int current, int total);

}
