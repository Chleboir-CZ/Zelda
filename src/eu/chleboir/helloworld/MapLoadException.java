/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.chleboir.helloworld;

/**
 *
 * @author chleboir
 */
public class MapLoadException extends Exception {
    public MapLoadException() { super(); }
    public MapLoadException(String message) { super(message); }
    public MapLoadException(String message, Throwable cause) { super(message, cause); }
    public MapLoadException(Throwable cause) { super(cause); }
}
