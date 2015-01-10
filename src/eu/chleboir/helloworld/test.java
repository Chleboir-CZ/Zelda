/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.chleboir.helloworld;


interface Idiot{
    void beAsshole();
    void beAnIdiot();
}
/**
 *
 * @author chleboir
 */
public class test implements Idiot {
    int huh;
    void printShit() {
        System.out.println(" die " + this.huh);
    }

    @Override
    public void beAsshole() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void beAnIdiot() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

