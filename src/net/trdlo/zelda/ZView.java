package net.trdlo.zelda;

import java.awt.Graphics2D;
import javax.swing.event.MouseInputListener;


public abstract class ZView implements MouseInputListener {
	
	public abstract void render(Graphics2D graphics, float renderFraction);
}
