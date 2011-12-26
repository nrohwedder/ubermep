package evaluation;

import com.google.protobuf.BlockingService;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import de.uniluebeck.itm.ubermep.rpc.service.RpcBlockingService;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 07.12.11
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
public class RpcEvaluationBlockingServiceImpl implements RpcBlockingService, RpcEvaluationServiceProtocol.EvaluationService.BlockingInterface{
	@Override
	public BlockingService getRpcBlockingService() {
		return RpcEvaluationServiceProtocol.EvaluationService.newReflectiveBlockingService(this);
	}

	@Override
	public RpcEvaluationServiceProtocol.EvaluationServiceMsg run(RpcController controller, RpcEvaluationServiceProtocol.EvaluationServiceMsg request) throws ServiceException {
		return request;
	}
}
