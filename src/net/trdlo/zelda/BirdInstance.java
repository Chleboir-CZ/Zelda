/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda;

import java.util.Random;

/**
 *
 * @author chleboir
 */
public class BirdInstance extends GameObjectInstance {
	private float moveX = 2, moveY = 3;
	World world;
	
	public BirdInstance(GameObject gameObject, float posX, float posY, World world) {
		super(gameObject, posX, posY);
		this.world = world;
	}
	
	private void randomPlaceBird() {
		Random r = new Random();
		boolean isVert = r.nextBoolean();
		boolean isMax = r.nextBoolean();

		//if(!isVert && !isMax)
		{
			posX = -gameObject.size / 2;
			posY = 0 + r.nextInt(world.mapHeight - 1);

			float alpha = -0.785398163f + r.nextFloat() * 2 * 0.785398163f;
			float speed = 5.0f;
			
			moveX = speed * (float)(Math.cos(alpha));
			moveY = speed * (float)(Math.sin(alpha));
		}
	}
	
	private void checkMapBorders() {
		if(posX < -gameObject.size / 2 || posY < -gameObject.size / 2 || posX > world.mapWidth + gameObject.size / 2 || posY > world.mapHeight + gameObject.size / 2) 
			randomPlaceBird();
	}
	
	@Override
	public void update() {
		posX += moveX;
		posY += moveY;

		checkMapBorders();
	}

	@Override
	public float getMoveX() {
		return moveX;
	}

	@Override
	public float getMoveY() {
		return moveY;
	}
}
