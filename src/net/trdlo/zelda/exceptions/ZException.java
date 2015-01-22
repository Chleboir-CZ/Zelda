/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda.exceptions;

/**
 *
 * @author chleboir
 */
public class ZException extends Exception {
    public ZException() { super(); }
    public ZException(String message) { super(message); }
    public ZException(String message, Throwable cause) { super(message, cause); }
    public ZException(Throwable cause) { super(cause); }
}
