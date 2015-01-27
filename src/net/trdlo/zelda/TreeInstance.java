/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;


public class TreeInstance extends GameObjectInstance {
	private final Tree gameObject;
	
	public TreeInstance(Tree gameObject, float posX, float posY) {
		super(posX, posY);
		this.gameObject = gameObject;
	}

	@Override
	public void render(Graphics2D graphics, float x, float y, float renderFraction) {
		AffineTransform t = new AffineTransform();
		t.translate(x, y);
		graphics.drawImage(gameObject.img, t, null);
	}
	
	@Override
	public int getZIndex() {
		return gameObject.zIndex;
	}
}
