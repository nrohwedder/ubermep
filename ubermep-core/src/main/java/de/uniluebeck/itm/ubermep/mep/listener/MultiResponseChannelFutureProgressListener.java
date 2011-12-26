package de.uniluebeck.itm.ubermep.mep.listener;

import org.jboss.netty.channel.ChannelFutureListener;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 05.12.11
 * Time: 18:30
 * To change this template use File | Settings | File Templates.
 */
public interface MultiResponseChannelFutureProgressListener extends ChannelFutureListener{
	public void progress(String senderUrn, byte[] payload, int current, int total);
}
