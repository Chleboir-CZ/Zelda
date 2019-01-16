package net.trdlo.zelda.tiled;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public abstract class SimplyAnimatedObject extends GameObject {

	protected BufferedImage[] images;

	public SimplyAnimatedObject(char identifier, String identifier2, float size, int zIndex) {
		super(identifier, size, zIndex);
	}

	public void setImages(BufferedImage[] images) {
		this.images = images;
	}

	public void renderFrame(Graphics2D graphics, float x, float y, int frameIndex, int frameIndex2, float frameFraction) {
		AffineTransform t = new AffineTransform();
		t.translate(x, y);
		//tohle je jen ukázka zneužití aktuálního času na animaci
		t.rotate(Math.sin(System.currentTimeMillis() / 200.0) * 0.5, World.GRID_SIZE / 2, World.GRID_SIZE / 2);

		graphics.drawImage(images[frameIndex], t, null);

		if (frameFraction > 0 && frameFraction <= 1) {
			Composite originalComp = graphics.getComposite();
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, frameFraction));
			graphics.drawImage(images[frameIndex2], t, null);
			graphics.setComposite(originalComp);
		}

		graphics.drawString(String.format("px [%9.4f; %9.4f]", x, y), x, y);
	}

	public void renderFrame(Graphics2D graphics, float x, float y, int frameIndex) {
		renderFrame(graphics, x, y, frameIndex, 0, 0);
	}
}
