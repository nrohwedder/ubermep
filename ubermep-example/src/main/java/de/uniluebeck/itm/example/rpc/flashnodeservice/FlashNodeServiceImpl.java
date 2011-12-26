package de.uniluebeck.itm.example.rpc.flashnodeservice;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import de.uniluebeck.itm.example.rpc.flashnodeservice.protocol.FlashNodeServiceProtocol;
import de.uniluebeck.itm.ubermep.rpc.service.RpcService;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.11.11
 * Time: 16:21
 * To change this template use File | Settings | File Templates.
 */
public class FlashNodeServiceImpl implements FlashNodeServiceProtocol.FlashNodeService.Interface, RpcService{
	@Override
	public void flashNode(RpcController controller, FlashNodeServiceProtocol.FlashNodeRequest request, RpcCallback<FlashNodeServiceProtocol.FlashNodeResponse> done) {
		//call flashNode(request.getDelay());
		done.run(FlashNodeServiceProtocol.FlashNodeResponse.newBuilder().setSuccess(true).build());
	}

	@Override
	public Service getRpcService() {
		return FlashNodeServiceProtocol.FlashNodeService.newReflectiveService(this);
	}
}
