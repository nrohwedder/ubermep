package de.uniluebeck.itm.example.rpc.flashnodeservice;

import com.google.protobuf.BlockingService;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import de.uniluebeck.itm.example.rpc.flashnodeservice.protocol.FlashNodeServiceProtocol;
import de.uniluebeck.itm.ubermep.rpc.service.RpcBlockingService;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 25.11.11
 * Time: 16:27
 * To change this template use File | Settings | File Templates.
 */
public class FlashNodeBlockingServiceImpl implements FlashNodeServiceProtocol.FlashNodeService.BlockingInterface, RpcBlockingService{
	@Override
	public FlashNodeServiceProtocol.FlashNodeResponse flashNode(RpcController controller, FlashNodeServiceProtocol.FlashNodeRequest request) throws ServiceException {
		//call flashNode(request.getDelay());
		return FlashNodeServiceProtocol.FlashNodeResponse.newBuilder().setSuccess(true).build();
	}

	@Override
	public BlockingService getRpcBlockingService() {
		return FlashNodeServiceProtocol.FlashNodeService.newReflectiveBlockingService(this);
	}
}
