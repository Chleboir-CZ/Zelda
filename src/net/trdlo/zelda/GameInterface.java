package net.trdlo.zelda;

import java.awt.Graphics2D;

public interface GameInterface extends InputListener {

	public void render(Graphics2D graphics, float renderFraction);

	public void update();
}
