package de.uniluebeck.itm.uberlay;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 13.10.11
 * Time: 17:46
 * To change this template use File | Settings | File Templates.
 */
public class NotHandableMessageException extends Exception{
	public NotHandableMessageException() {
	}

	public NotHandableMessageException(String message) {
		super(message);
	}

	public NotHandableMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotHandableMessageException(Throwable cause) {
		super(cause);
	}
}
