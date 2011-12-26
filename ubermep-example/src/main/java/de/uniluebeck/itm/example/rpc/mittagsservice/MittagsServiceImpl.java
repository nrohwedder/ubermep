package de.uniluebeck.itm.example.rpc.mittagsservice;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import de.uniluebeck.itm.rpc.mittagsservice.protocol.MittagsServiceProtocol;
import de.uniluebeck.itm.ubermep.rpc.service.RpcService;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 05.10.11
 * Time: 18:14
 * To change this template use File | Settings | File Templates.
 */
public class MittagsServiceImpl implements MittagsServiceProtocol.MittagsService.Interface, RpcService {
	@Override
	public void bestelle(RpcController controller, MittagsServiceProtocol.BestellRequest request, RpcCallback<MittagsServiceProtocol.BestellResponse> done) {
		MittagsServiceProtocol.Mahlzeit mahlzeit = MittagsServiceProtocol.Mahlzeit.newBuilder().setName("Mahlzeit: " +
				request.getGericht().getName() + " Anzahl: " + request.getAnzahl())
				.build();
		done.run(MittagsServiceProtocol.BestellResponse.newBuilder().setMahlzeit(mahlzeit).build());
	}

	@Override
	public Service getRpcService() {
		return MittagsServiceProtocol.MittagsService.newReflectiveService(this);
	}
}
