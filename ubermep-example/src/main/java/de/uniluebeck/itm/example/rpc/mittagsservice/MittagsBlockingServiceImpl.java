package de.uniluebeck.itm.example.rpc.mittagsservice;

import com.google.protobuf.BlockingService;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import de.uniluebeck.itm.rpc.mittagsservice.protocol.MittagsServiceProtocol;
import de.uniluebeck.itm.ubermep.rpc.service.RpcBlockingService;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 05.10.11
 * Time: 15:56
 * To change this template use File | Settings | File Templates.
 */
public class MittagsBlockingServiceImpl implements MittagsServiceProtocol.MittagsService.BlockingInterface, RpcBlockingService {
	@Override
	public MittagsServiceProtocol.BestellResponse bestelle(RpcController controller, MittagsServiceProtocol.BestellRequest request) throws ServiceException {
		MittagsServiceProtocol.Mahlzeit mahlzeit = MittagsServiceProtocol.Mahlzeit.newBuilder().setName("Mahlzeit: " +
				request.getGericht().getName() + " Anzahl: " + request.getAnzahl())
				.build();
		return MittagsServiceProtocol.BestellResponse.newBuilder().setMahlzeit(mahlzeit).build();
	}

	@Override
	public BlockingService getRpcBlockingService() {
		return MittagsServiceProtocol.MittagsService.newReflectiveBlockingService(this);
	}
}
