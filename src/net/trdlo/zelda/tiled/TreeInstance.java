package net.trdlo.zelda.tiled;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class TreeInstance extends GameObjectInstance {

	private final Tree tree;

	public TreeInstance(Tree tree, float posX, float posY) {
		super(tree, posX, posY);
		this.tree = tree;
	}

	@Override
	public void render(Graphics2D graphics, float x, float y, float renderFraction) {
		AffineTransform t = new AffineTransform();
		t.translate(x, y);
		graphics.drawImage(tree.img, t, null);
	}

	@Override
	public void stateFromString(String args) {
	}

	@Override
	public String stateToString() {
		return "";
	}
}
