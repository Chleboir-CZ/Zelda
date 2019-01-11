package net.trdlo.zelda.tiled;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import net.trdlo.zelda.CommandExecuter;
import net.trdlo.zelda.ZeldaFrame;

public interface MenuInterface extends CommandExecuter {
	
	void render(Graphics2D graphics, float renderFraction);

	void update(long time);

	void setZeldaFrame(ZeldaFrame zFrame);
	
	boolean keyTyped(KeyEvent e);

	boolean keyPressed(KeyEvent e);

	boolean keyReleased(KeyEvent e);

	boolean mousePressed(MouseEvent me);

	boolean mouseReleased(MouseEvent me);

	boolean mouseClicked(MouseEvent me);

	boolean mouseDragged(MouseEvent me);
}
