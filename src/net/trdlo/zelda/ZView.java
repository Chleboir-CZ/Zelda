package net.trdlo.zelda;

import java.awt.Graphics2D;
import java.awt.event.KeyListener;
import javax.swing.event.MouseInputListener;


public abstract class ZView implements MouseInputListener, KeyListener {
	
	public abstract void render(Graphics2D graphics, float renderFraction);
}
