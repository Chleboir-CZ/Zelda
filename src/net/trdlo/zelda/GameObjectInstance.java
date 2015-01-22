/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda;


public class GameObjectInstance {
    protected GameObject gameObject;
    protected float posX, posY;

    public GameObjectInstance(GameObject gameObject, float posX, float posY) {
        this.gameObject = gameObject;
        this.posX = posX;
        this.posY = posY;
    }
	
	public void update() {
		
	}

    public GameObject getGameObject() {
        return gameObject;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setGameObject(GameObject gameObject) {
        this.gameObject = gameObject;
    }

    /*public void setPosX(float posX) {
        //this.posX = posX;
    }

    public void setPosY(float posY) {
        //this.posY = posY;
    }*/
    
    public float getMoveX() {
        return 0;
    }

    public float getMoveY() {
        return 0;
    }
}
