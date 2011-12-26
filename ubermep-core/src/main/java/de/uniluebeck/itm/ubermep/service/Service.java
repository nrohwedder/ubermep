package de.uniluebeck.itm.ubermep.service;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 03.08.11
 * Time: 13:00
 * To change this template use File | Settings | File Templates.
 */
public interface Service {
	/**
	 * starts the service
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void start() throws ExecutionException, InterruptedException;

	/**
	 * stops the service
	 */
	public void stop();
}
