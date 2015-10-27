package net.trdlo.zelda.guan;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import net.trdlo.zelda.GameInterface;
import net.trdlo.zelda.ZeldaFrame;

public class Guan implements GameInterface {

	final World world;
	private ZeldaFrame zFrame;
	private OrthoCamera camera;

	private static final List<String> console = new ArrayList<>();

	private XY viewDrag = null;

	public Guan() {
		world = new World();
		try {
			world.loadFromFile("save.map");
		} catch (Exception ex) {
			System.err.println("Could not load map!");
		}
		camera = new OrthoCamera(world, 0, 0, 0);
	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		camera.render(graphics, zFrame.getBounds(), renderFraction);

		int y = zFrame.getBounds().y + zFrame.getBounds().height;
		for (String str : console) {
			y -= 16;
			graphics.drawString(str, 10, y);
		}
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
		if (e.getKeyChar() == 'q') {
			zFrame.terminate();
		}
		else {
			echo("Typed: '" + e.getKeyChar() + "'");
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		echo("Pressed: " + e.getKeyCode() + ", char: '" + e.getKeyChar() + "'");
	}

	@Override
	public void keyReleased(KeyEvent e) {
		echo("Released: " + e.getKeyCode() + ", char: '" + e.getKeyChar() + "'");
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			viewDrag = new XY(e);
			zFrame.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
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
	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		camera.zoom(-e.getWheelRotation(), new XY(e));
	}

	public static void echo(String str) {
		console.add(str);
	}

	public static void cls() {
		console.clear();
	}

	public static void main(String args[]) {
		try {
			ZeldaFrame.buildZeldaFrame(new Guan()).run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
