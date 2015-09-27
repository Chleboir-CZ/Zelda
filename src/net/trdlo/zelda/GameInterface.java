package net.trdlo.zelda;

import java.awt.Graphics2D;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

public interface GameInterface extends KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	public abstract void render(Graphics2D graphics, float renderFraction);

	public abstract void update();
}
