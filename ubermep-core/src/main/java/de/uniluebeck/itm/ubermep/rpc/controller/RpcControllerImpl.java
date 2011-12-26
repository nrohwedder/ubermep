package de.uniluebeck.itm.ubermep.rpc.controller;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 14.09.11
 * Time: 11:49
 * To change this template use File | Settings | File Templates.
 */
public class RpcControllerImpl implements RpcController {
	private boolean failed = false;
	private String reason = null;
	private boolean canceled = false;

	@Override
	public void reset() {
		failed = false;
		reason = null;
		canceled = false;
	}

	@Override
	public boolean failed() {
		return failed;
	}

	@Override
	public String errorText() {
		return reason;
	}

	@Override
	public void startCancel() {
		canceled = true;
	}

	@Override
	public void setFailed(String reason) {
		this.reason = reason;
		this.failed = true;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void notifyOnCancel(RpcCallback<Object> callback) {
		if (isCanceled()){
			callback.run(null);
		}
	}
}
