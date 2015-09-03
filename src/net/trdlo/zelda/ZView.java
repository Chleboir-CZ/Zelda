package net.trdlo.zelda;

import java.awt.Graphics2D;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelListener;
import javax.swing.event.MouseInputListener;

public abstract class ZView implements MouseInputListener, KeyListener, MouseWheelListener {

	public abstract void render(Graphics2D graphics, float renderFraction);

	public abstract void update();
}
