package net.trdlo.zelda.guan;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import net.trdlo.zelda.CommandExecuter;

abstract class AbstractView implements CommandExecuter {

	public static final double ZOOM_BASE = 1.090507733; //2^(1/8)
	protected World world;
	protected double x, y;
	protected int zoom;

	public abstract void update();

	public abstract void render(Graphics2D graphics, Rectangle componentBounds, float renderFraction);

	public abstract boolean keyTyped(KeyEvent e);

	public abstract boolean keyPressed(KeyEvent e);

	public abstract boolean keyReleased(KeyEvent e);

	public abstract boolean mouseClicked(MouseEvent e);

	public abstract boolean mousePressed(MouseEvent e);

	public abstract boolean mouseReleased(MouseEvent e);

	public abstract boolean mouseEntered(MouseEvent e);

	public abstract boolean mouseExited(MouseEvent e);

	public abstract boolean mouseDragged(MouseEvent e);

	public abstract boolean mouseMoved(MouseEvent e);

	public abstract boolean mouseWheelMoved(MouseWheelEvent e);
}
