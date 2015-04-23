package net.trdlo.zelda.notiles;

import java.awt.event.KeyEvent;
import net.trdlo.zelda.ZFrame;
import net.trdlo.zelda.exceptions.ZException;

public class NoTilesFrame extends ZFrame {

	public NoTilesFrame(String frameCaption) throws ZException {
		super(frameCaption);

		addWindowListener(this);
//		addMouseListener(this);

		world = new World();

		mainView = new View((World) world, this);
		addMouseListener(mainView);
		addMouseMotionListener(mainView);
		addKeyListener(mainView);
	}

	public static void main(String[] args) {
		try {
			NoTilesFrame frame = new NoTilesFrame("Tiled Zelda game demo");
			frame.run();
			frame.doneFrame();
		} catch (ZException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
			this.terminate = true;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}
}
