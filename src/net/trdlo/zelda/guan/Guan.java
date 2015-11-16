package net.trdlo.zelda.guan;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.trdlo.zelda.Console;
import net.trdlo.zelda.GameInterface;
import net.trdlo.zelda.ZeldaFrame;

public class Guan implements GameInterface {

	private ZeldaFrame zFrame;

	private World world;
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
		return "Guan tile-less game demo.";
	}

	private final Pattern PAT_GET_BOUNDS_DEBUG = Pattern.compile("^\\s*bounds-debug\\s*$", Pattern.CASE_INSENSITIVE);
	private final Pattern PAT_SET_BOUNDS_DEBUG = Pattern.compile("^\\s*bounds-debug\\s+([01])\\s*$", Pattern.CASE_INSENSITIVE);
	private final Pattern PAT_SAVE = Pattern.compile("^\\s*save\\s*$", Pattern.CASE_INSENSITIVE);
	private final Pattern PAT_SAVE_AS = Pattern.compile("^\\s*save\\s+(?<file>.+)\\s*$", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean executeCommand(String command, Console console) {
		Matcher m;
		if (PAT_GET_BOUNDS_DEBUG.matcher(command).matches()) {
			console.echo("bounds-debug " + (camera.isBoundsDebug() ? "1" : "0"));
		} else if ((m = PAT_SET_BOUNDS_DEBUG.matcher(command)).matches()) {
			camera.setBoundsDebug("1".equals(m.group(1)));
		} else if (PAT_SAVE.matcher(command).matches()) {
			//String fileName = Word.fileName;
			//TODO Save to fileName
		} else if ((m = PAT_SAVE_AS.matcher(command)).matches()) {
			String fileName = m.group("file");
			//TODO Save to fileName
		} else {
			return false;
		}

		return true;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		switch (e.getKeyChar()) {
			case 'v':
				camera.setBoundsDebug(!camera.isBoundsDebug());
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

	public static void main(String args[]) {
		ZeldaFrame.buildZeldaFrame(new Guan()).run();
	}
}
