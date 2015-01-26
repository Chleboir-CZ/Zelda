/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author chleboir
 */
public abstract class SimplyAnimatedObject extends GameObject {
	protected BufferedImage[] images; 
	
	/**
	 *
	 * @param identifier
	 * @param size
	 */
	public SimplyAnimatedObject(char identifier, float size, int zIndex) {
		super(identifier, size, zIndex);
//		this.images = images;
	}

	public void setImages(BufferedImage[] images) {
		this.images = images;
	}
	
	protected void renderFrame(Graphics2D graphics, int x, int y, int frameIndex, int frameIndex2, float frameFraction) {
        graphics.drawImage(images[frameIndex], x, y, null);
		Composite originalComp = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, frameFraction));
        graphics.drawImage(images[frameIndex2], x, y, null);
		graphics.setComposite(originalComp);
	}

	protected void renderFrame(Graphics2D graphics, int x, int y, int frameIndex) {
		renderFrame(graphics, x, y, frameIndex, 0, 0);
	}
}
