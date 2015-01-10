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


public class HelloWorld {
    
    public void say() {
        System.err.println("What sould I say?");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Hello world!");
        for(String arg: args) {
            System.out.println(arg);
        }
        
        //say();
        
    }
    
}
