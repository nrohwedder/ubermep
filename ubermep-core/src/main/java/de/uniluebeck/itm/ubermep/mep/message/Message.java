package de.uniluebeck.itm.ubermep.mep.message;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 19.07.11
 * Time: 14:04
 * To change this template use File | Settings | File Templates.
 */
public abstract class Message {
	protected final byte[] payload;

	protected Message(byte[] payload) {
		this.payload = payload;
	}

	public byte[] getPayload() {
		return this.payload;
	}

	@Override
	public String toString() {
		return "Message{" +
				"payload=" + (payload == null ? "null" : new String(payload)) +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Message message = (Message) o;

		if (!Arrays.equals(payload, message.payload)) return false;

		return true;
	}
}
