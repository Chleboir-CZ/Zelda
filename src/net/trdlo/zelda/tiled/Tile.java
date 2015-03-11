package net.trdlo.zelda.tiled;

import java.awt.image.BufferedImage;
import net.trdlo.zelda.Identifiable;


public class Tile implements Identifiable {

	private boolean passable;
	private BufferedImage img;
	private final char identifier;

	public Tile(boolean passable, BufferedImage img, char identifier) {
		this.passable = passable;
		this.img = img;
		this.identifier = identifier;
	}

	public boolean isPassable() {
		return passable;
	}

	public void setPassable(boolean passable) {
		this.passable = passable;
	}

	public BufferedImage getImg() {
		return img;
	}

	public void setImg(BufferedImage img) {
		this.img = img;
	}

	@Override
	public char getIdentifier() {
		return identifier;
	}
}
