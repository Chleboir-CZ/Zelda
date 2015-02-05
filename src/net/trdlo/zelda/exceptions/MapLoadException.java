package net.trdlo.zelda.exceptions;


public class MapLoadException extends ZException {
    public MapLoadException() { super(); }
    public MapLoadException(String message) { super(message); }
    public MapLoadException(String message, Throwable cause) { super(message, cause); }
    public MapLoadException(Throwable cause) { super(cause); }
}
