package de.uniluebeck.itm.handlerstack;

import de.uniluebeck.itm.tr.util.Tuple;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandler;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 28.10.11
 * Time: 12:36
 * To change this template use File | Settings | File Templates.
 */
public interface FilterPipeline {

	public interface DownstreamOutputListener {
        void receiveDownstreamOutput(ChannelBuffer message);

        void downstreamExceptionCaught(Throwable e);
    }

    public interface UpstreamOutputListener {
        void receiveUpstreamOutput(Object o);

        void upstreamExceptionCaught(Throwable e);
    }

	void sendDownstream(Object o);

    void sendUpstream(ChannelBuffer message);

    void setChannelPipeline(List<Tuple<String, ChannelHandler>> channelPipeline);

    List<Tuple<String, ChannelHandler>> getChannelPipeline();

    void addListener(final DownstreamOutputListener listener);

    void addListener(final UpstreamOutputListener listener);

    void removeListener(final DownstreamOutputListener listener);

    void removeListener(final UpstreamOutputListener listener);

}
