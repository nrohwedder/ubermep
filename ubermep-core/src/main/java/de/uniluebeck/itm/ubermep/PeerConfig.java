package de.uniluebeck.itm.ubermep;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 24.11.11
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */
public class PeerConfig {
	public static final int CORE_POOL_SIZE = 10;

	public static final int DEFAULT_TIMEOUT = 30;
	public static final TimeUnit DEFAULT_TIMEOUT_TIMEUNIT = TimeUnit.SECONDS;

	public static final class UberlayModule {
		public static final int RTT_REQUEST_INTERVAL = 10;
		public static final TimeUnit RTT_REQUEST_INTERVAL_TIMEUNIT = TimeUnit.SECONDS;
	}
}
