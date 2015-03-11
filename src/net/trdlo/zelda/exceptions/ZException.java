package net.trdlo.zelda.exceptions;


public class ZException extends Exception {

	public ZException() {
		super();
	}

	public ZException(String message) {
		super(message);
	}

	public ZException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZException(Throwable cause) {
		super(cause);
	}
}
