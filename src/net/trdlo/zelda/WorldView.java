/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 *
 * @author bayer
 */
public class WorldView {
	private World world;
	private float x, y, dx, dy;

	public WorldView(World world, float startX, float startY) {
		this.world = world;
		this.x = startX;
		this.y = startY;
		
		dx = -0.1f; //tohle se má brát z vůle hráče a ne být přednastaveno, je to posun o 0.1 * 32px za jeden render
		dy = -0.1f;
	}

	public WorldView(World world) {
		this(world, world.mapWidth / 2.0f, world.mapHeight/2.0f);
	}

	public void render(Graphics2D graphics, float renderFraction) {
		Rectangle bounds = graphics.getDeviceConfiguration().getBounds();
		
		limitViewPosition(bounds);

		float rWidth2 = Math.min(world.mapWidth, bounds.width / (float)World.GRID_SIZE) / 2.0f;
		float rHeight2 = Math.min(world.mapHeight, bounds.height / (float)World.GRID_SIZE) / 2.0f;
		
		int xOffset = (int)Math.floor((bounds.width / 2.0f) - (x * World.GRID_SIZE));
		int yOffset = (int)Math.floor((bounds.height / 2.0f) - (y * World.GRID_SIZE));

		graphics.setClip(xOffset, yOffset, world.mapWidth * World.GRID_SIZE, world.mapHeight * World.GRID_SIZE);
		
		for(int j = (int)(y - rHeight2); j < (int)Math.ceil(y + rHeight2); j++) {
			for(int i = (int)(x - rWidth2); i < (int)Math.ceil(x + rWidth2); i++) {
				graphics.drawImage(world.map[i + j * world.mapWidth].getTile().getImg(), xOffset + i * World.GRID_SIZE, yOffset + j * World.GRID_SIZE, null);
			}
		}

		//graphics.setColor(Color.RED);
		//graphics.drawRect(xOffset, yOffset, world.mapWidth * World.GRID_SIZE -1, world.mapHeight * World.GRID_SIZE -1);

		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		for (GameObjectInstance goi : world.objectInstances) {			
			float pixelX = xOffset + (goi.getPosX() + goi.getMoveX() * renderFraction) * World.GRID_SIZE;
			float pixelY = yOffset + (goi.getPosY() + goi.getMoveY() * renderFraction) * World.GRID_SIZE;
			goi.render(graphics, pixelX, pixelY, renderFraction);
		}
		
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		
		graphics.setClip(null);
		
		graphics.drawString("WorldView looks at [" + x + "; " + y + "]", 100, 100);

	}
	
	public void limitViewPosition(Rectangle bounds) {
		float centerX = world.mapWidth / 2.0f;
		float centerY = world.mapHeight / 2.0f;
		float bWidth = bounds.width / (float)World.GRID_SIZE;
		float bHeight = bounds.height / (float)World.GRID_SIZE;
		
		float minX = Math.min(centerX, bWidth / 2.0f);
		float maxX = Math.max(centerX, world.mapWidth - bWidth / 2.0f);
		float minY = Math.min(centerY, bHeight / 2.0f);
		float maxY = Math.max(centerY, world.mapHeight - bHeight / 2.0f);
		
		float old = x;
		x = Math.min(Math.max(x+dx, minX), maxX);
		if (x == old)
			dx = -dx; //tady má být, že nedojde-li k posunu (splněno), pak se zastaví posouvání, dx = 0; nyní obrací směr posunu
		old = y;
		y = Math.min(Math.max(y+dy, minY), maxY);
		if (y == old)
			dy = -dy; //tady taky
	}
}
