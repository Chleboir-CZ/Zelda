/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda;

import java.awt.Graphics2D;

public abstract class GameObject implements Identifiable {

    private final char identifier;
	final float size;

    public GameObject(char identifier, float size) {
        this.identifier = identifier;
		this.size = size;
    }

    @Override
    public char getIdentifier() {
        return identifier;
    }

    public boolean isColliding() {
        return false;
    }
    
    public abstract void render(Graphics2D graphics, int x, int y );
    
    public abstract GameObjectInstance getInstance(float x,float y, String args);
}
