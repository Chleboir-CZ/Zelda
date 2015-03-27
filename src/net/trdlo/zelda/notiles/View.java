
package net.trdlo.zelda.notiles;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.trdlo.zelda.ZView;


public class View extends ZView {
	private World world;

	public View(World world) {
		this.world = world;
	}
	
	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		for(Line line : world.lines) {
			graphics.drawLine((int)line.A.x, (int)line.A.y, (int)line.B.x, (int)line.B.y);
		}
		
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void mouseClicked(MouseEvent me) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
	}

	@Override
	public void mouseReleased(MouseEvent me) {
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mouseDragged(MouseEvent me) {
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		
	}
	
}
