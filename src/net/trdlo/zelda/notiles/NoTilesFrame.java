package net.trdlo.zelda.notiles;

import net.trdlo.zelda.ZFrame;
import net.trdlo.zelda.exceptions.ZException;

public class NoTilesFrame extends ZFrame {

	public NoTilesFrame(String frameCaption) throws ZException {
		super(frameCaption);

		addWindowListener(this);
		addKeyListener(this);
//		addMouseListener(this);

		world = new World();

		mainView = new View((World) world);
		addMouseListener(mainView);
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
}
