package net.trdlo.zelda;

import java.awt.Graphics2D;

public interface GameInterface extends InputListener, CommandExecuter {

	void render(Graphics2D graphics, float renderFraction);

	void update();

	void setZeldaFrame(ZeldaFrame zFrame);

	String getWindowCaption();
}
