/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda;

import java.awt.Graphics2D;
import java.util.Random;


public class BirdInstance extends GameObjectInstance {
	private final Bird gameObject;
	private float moveX, moveY;
	private final World world;

	private float flightSpeed = 0.1f;
	private int flatterSpeed = 5;
	private int updateCounter = 0;
	
	public BirdInstance(Bird gameObject, float posX, float posY, World world) {
		super(posX, posY);
		this.gameObject = gameObject;
		this.world = world;
		randomPlaceBird();
	}
	
	private void randomPlaceBird() {
		Random r = new Random();
		boolean isVert = r.nextBoolean();
		boolean isMax = r.nextBoolean();

		//if(!isVert && !isMax)
		{
			posX = -gameObject.size / 2;
			posY = 0 + r.nextInt(world.mapHeight - 1);

			float angle = -0.785398163f + r.nextFloat() * 2 * 0.785398163f;
			
			moveX = flightSpeed * (float)(Math.cos(angle));
			moveY = flightSpeed * (float)(Math.sin(angle));
		}
	}
	
	private void checkMapBorders() {
		if(posX < -gameObject.size / 2 || posY < -gameObject.size / 2 || posX > world.mapWidth + gameObject.size / 2 || posY > world.mapHeight + gameObject.size / 2) 
			randomPlaceBird();
	}
	
	@Override
	public void update() {
		updateCounter++;
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

	@Override
	public void render(Graphics2D graphics, int x, int y, float renderFraction) {
		gameObject.renderFrame(graphics, x, y, (updateCounter/flatterSpeed)%gameObject.FRAME_COUNT);
	}

	@Override
	public int getZIndex() {
		return gameObject.zIndex;
	}
}
