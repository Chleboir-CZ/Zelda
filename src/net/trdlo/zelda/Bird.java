/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.trdlo.zelda.exceptions.MapLoadException;


public class Bird extends GameObject {
	private BufferedImage img;
	private World world;

	public Bird(World world) throws MapLoadException {
		super('^', 1.0f);
		this.world = world;

		try {
			img = ImageIO.read(new File("images/bird.png"));
		} catch (IOException ex) {
			throw new MapLoadException("Bird instantion did not load it's graphics correctly.", ex);
		}
	}

	@Override
	public void render(Graphics2D graphics, int x, int y) {
		graphics.drawImage(img, x, y, null);
	}

	@Override
	public GameObjectInstance getInstance(float x, float y, String args) {
		return new BirdInstance(this, x, y, world);
	}
	
}
