package net.trdlo.zelda.guan;

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
	private final AbstractView camera;

	public Guan() {
		world = new World();
		try {
			world.loadFromFile("maps/test.map");
		} catch (Exception ex) {
			Console.getInstance().echo("Could not load map! Proceeding with an empty one.");
		}
		camera = new EditorView(world, 0, 0, 0);

	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		camera.render(graphics, zFrame.getBounds(), renderFraction);
	}

	@Override
	public void update() {
		world.update();
		camera.update();
	}

	@Override
	public void setZeldaFrame(ZeldaFrame zFrame) {
		this.zFrame = zFrame;
	}

	@Override
	public String getWindowCaption() {
		return "Guan tile-less game demo.";
	}

	private static final Pattern PAT_SAVE = Pattern.compile("^\\s*save\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern PAT_SAVE_AS = Pattern.compile("^\\s*save\\s+(?<file>.+)\\s*$", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean executeCommand(String command, Console console) {
		Matcher m;
		if (camera.executeCommand(command, console)) {
			return true;
		} else if (PAT_SAVE.matcher(command).matches()) {
			try {
				world.save();
			} catch (Exception ex) {
				Console.getInstance().echo("Could not save file: " + ex.toString());
			}
		} else if ((m = PAT_SAVE_AS.matcher(command)).matches()) {
			String fileName = m.group("file");
			try {
				world.saveToFile(fileName);
			} catch (Exception ex) {
				Console.getInstance().echo("Could not save file: " + ex.toString());
			}
		} else {
			return false;
		}

		return true;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		camera.keyTyped(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!camera.keyPressed(e)) {
			if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
				zFrame.terminate();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		camera.keyReleased(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		camera.mouseClicked(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		camera.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		camera.mouseReleased(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		camera.mouseEntered(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		camera.mouseExited(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		camera.mouseDragged(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		camera.mouseMoved(e);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		camera.mouseWheelMoved(e);
	}

	public static void main(String args[]) {
		ZeldaFrame.buildInstance(new Guan()).run();
	}
}
