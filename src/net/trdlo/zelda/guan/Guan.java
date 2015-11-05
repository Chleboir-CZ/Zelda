package net.trdlo.zelda.guan;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import net.trdlo.zelda.GameInterface;
import net.trdlo.zelda.ZeldaFrame;

public class Guan implements GameInterface {

	final World world;
	private ZeldaFrame zFrame;
	private final OrthoCamera camera;

	private XY viewDrag = null;

	public Guan() {
		world = new World();
		try {
			world.loadFromFile("save.map");
		} catch (Exception ex) {
			System.err.println("Could not load map! Proceeding with an empty one.");
		}
		camera = new OrthoCamera(world, 0, 0, 0);

	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		camera.render(graphics, zFrame.getBounds(), renderFraction);

	}

	private void readAsynchronoutInput() {
		if (zFrame.isPressed(KeyEvent.VK_ADD)) {
			camera.zoom(1, null);
		}
		if (zFrame.isPressed(KeyEvent.VK_SUBTRACT)) {
			camera.zoom(-1, null);
		}
	}

	@Override
	public void update() {
		world.update();
		camera.update();

		readAsynchronoutInput();
	}

	@Override
	public void setZeldaFrame(ZeldaFrame zFrame) {
		this.zFrame = zFrame;
	}

	@Override
	public String getWindowCaption() {
		return "Guam tile-less game demo.";
	}

	@Override
	public void keyTyped(KeyEvent e) {
		switch (e.getKeyChar()) {
			case 'q':
				zFrame.terminate();
				break;
			case 'v':
				camera.setBoundsDebug(!camera.isBoundsDebug());
				break;
			case ';':
				ZeldaFrame.console.setVisible(!ZeldaFrame.console.isVisible());
				break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyChar()) {
			case KeyEvent.VK_ESCAPE:
				zFrame.terminate();
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			camera.mouse1pressed(e);
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
			viewDrag = new XY(e);
			zFrame.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			camera.mouse1released(e);
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
			viewDrag = null;
			zFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (viewDrag != null) {
			XY current = new XY(e);
			camera.move(viewDrag.diff(current));
			viewDrag = current;
		}
		camera.mouse1dragged(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		camera.zoom(-e.getWheelRotation(), new XY(e));
	}

	public static void main(String args[]) throws Exception {
		ZeldaFrame.buildZeldaFrame(new Guan()).run();
	}
}
