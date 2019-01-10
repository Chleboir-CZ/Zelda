package net.trdlo.zelda.guan;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import net.trdlo.zelda.Console;
import net.trdlo.zelda.GameInterface;
import net.trdlo.zelda.ZeldaFrame;

public class Guan implements GameInterface {

	private ZeldaFrame zFrame;

	private World world;
	private final AbstractView view;

	public Guan() {
		world = new World();
		try {
			world.loadFromFile("maps/test.map");
		} catch (Exception ex) {
			Console.getInstance().echo("Could not load map! Proceeding with an empty one.");
			world = new World();
		}

		if (Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) {
			view = new GameView(world);
		} else {
			view = new EditorView(world, 0, 0, -8);
		}
	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		view.render(graphics, zFrame.getBounds(), renderFraction);
	}

	@Override
	public void update(long time) {
		world.update();
		view.update();
	}

	@Override
	public void setZeldaFrame(ZeldaFrame zFrame) {
		this.zFrame = zFrame;
	}

	@Override
	public String getWindowCaption() {
		return "Guan tile-less game demo.";
	}

	@Override
	public boolean executeCommand(String command, Console console) {
		return view.executeCommand(command, console) || world.executeCommand(command, console);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		view.keyTyped(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!view.keyPressed(e)) {
			if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
				zFrame.terminate();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		view.keyReleased(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		view.mouseClicked(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		view.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		view.mouseReleased(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		view.mouseEntered(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		view.mouseExited(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		view.mouseDragged(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		view.mouseMoved(e);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		view.mouseWheelMoved(e);
	}

	public static void main(String args[]) {
		ZeldaFrame.buildInstance(new Guan()).run();
	}
}
