/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.chleboir.helloworld;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author chleboir
 */
public class CollectionsDemo {
    
    
    public CollectionsDemo() {
        List<Integer> lst = new LinkedList<Integer>();
        Set set = new HashSet();
        Map map = new HashMap();
        
        SortedSet ss = new TreeSet();
        
        
        //lst.add(new Object());
        lst.add(175);
        //lst.add("Karel");
        
        int c = 0;
        
        for(Integer i: lst) {
            System.err.println(i.toString());
            //Integer i = (Integer)o;
            c += i;
        }
        
    }
    
    
}
