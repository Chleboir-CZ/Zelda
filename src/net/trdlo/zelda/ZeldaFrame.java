package net.trdlo.zelda;

import java.awt.Rectangle;

/**
 *
 * @author bayer
 */
public interface ZeldaFrame {

	void run();
	void terminate();

	void setCursor(ZeldaCursor cursor);

	Rectangle getBounds();

	boolean isPressed(int keyCode);

	XY getMouseXY();
}
