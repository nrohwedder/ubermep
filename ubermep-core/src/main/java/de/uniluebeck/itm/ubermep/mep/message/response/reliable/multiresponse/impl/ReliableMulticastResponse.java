package de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.impl;

import com.google.common.collect.Multimap;
import de.uniluebeck.itm.ubermep.mep.message.response.reliable.multiresponse.MultiResponse;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import de.uniluebeck.itm.ubermep.mep.message.request.Request;
import de.uniluebeck.itm.ubermep.mep.message.response.Response;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 05.08.11
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class ReliableMulticastResponse extends MultiResponse {
	public static class SingleReliableMulticastResponse extends Response {
		private UPAddress address;
		public SingleReliableMulticastResponse(Request request, UPAddress address) {
			super(request, null);
			this.address = address;
		}

		public UPAddress getAddress() {
			return address;
		}

		@Override
		public String toString() {
			return "SingleReliableMulticastResponse{" +
					"address=" + address +
					'}';
		}
	}

	public ReliableMulticastResponse(Request request, Multimap<UPAddress, Response> singleReliableMulticastResponses){
		super(request, singleReliableMulticastResponses);
	}

	@Override
	public String toString() {
		return super.toString() + "ReliableMulticastResponse{" +
				"singleReliableMulticastResponses=" + super.getResponses() +
				'}';
	}
}
