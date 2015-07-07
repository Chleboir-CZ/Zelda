package net.trdlo.zelda.tiled;

import java.io.File;
import net.trdlo.zelda.ZFrame;
import net.trdlo.zelda.exceptions.ZException;

public class TiledFrame extends ZFrame {

	public TiledFrame(String frameCaption) throws ZException {
		super(frameCaption);

		addWindowListener(this);
		addKeyListener(this);
//		addMouseListener(this);

		world = World.loadFromFile(new File("maps/small.txt"), false);

		mainView = new WorldView((World) world, this);
		addMouseListener(mainView);
	}

	public static void main(String[] args) {
		try {
			TiledFrame frame = new TiledFrame("Tiled Zelda game demo");
			frame.run();
			frame.doneFrame();
		} catch (ZException ex) {
			ex.printStackTrace();
		}
	}
}
