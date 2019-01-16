package net.trdlo.zelda.tiled;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import net.trdlo.zelda.Console;
import net.trdlo.zelda.ZeldaFrame;

public class Menu implements MenuInterface {

	private ZeldaFrame zFrame;
	private TiledGame tiledGame;
	private Rectangle bounds;

	private boolean visible = false;
	private boolean mouseCapture = false;
	private boolean mouseCaptureClick = false;

	public Menu(TiledGame tiledGame) {
		this.tiledGame = tiledGame;
	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		this.bounds = graphics.getDeviceConfiguration().getBounds();

		if (!visible) {
			return;
		}

		graphics.setColor(new Color(255, 255, 255, 64));
		graphics.fillRect(bounds.width / 4, bounds.height / 4, bounds.width / 2, bounds.height / 2);
		graphics.setColor(Color.WHITE);
		graphics.drawString("Menu, pyčo!", bounds.width / 4 + 32, bounds.height / 4 + 32);
		graphics.drawString("[x] je konec", bounds.width / 4 + 32, bounds.height / 4 + 32 + 32);
		graphics.drawString("[d] je velká mapa", bounds.width / 4 + 32, bounds.height / 4 + 32 + 64);
		graphics.drawString("[s] je malá mapa", bounds.width / 4 + 32, bounds.height / 4 + 32 + 96);
		graphics.drawString("[h] je horizontálně velká", bounds.width / 4 + 32, bounds.height / 4 + 32 + 128);
		graphics.drawString("[v] je vertikálně velkámapa", bounds.width / 4 + 32, bounds.height / 4 + 32 + 160);

	}

	@Override
	public void update(long time) {

	}

	@Override
	public void setZeldaFrame(ZeldaFrame zFrame) {
		this.zFrame = zFrame;
	}

	private void setVisible(boolean value) {
		if (visible != value) {
			visible = value;
			if (value) {
				ZeldaFrame.getInstance().clearPressedKeys();
			}
		}
	}

	private boolean isIncidentalWithMenu(int x, int y) {
		return visible && x >= bounds.width / 4 && x < bounds.width * 3 / 4 && y >= bounds.height / 4 && y < bounds.height * 3 / 4;
	}

	@Override
	public boolean executeCommand(String command, Console console) {
		return false;
	}

	@Override
	public void listCommands(String command, Console console) {
	}

	@Override
	public boolean keyTyped(KeyEvent e) {
		if (visible) {
			char typedLower = Character.toLowerCase(e.getKeyChar());
			if (typedLower == 'x') {
				zFrame.terminate();
			}

			if (typedLower == 'd' || typedLower == 's' || typedLower == 'h' || typedLower == 'v') {
				try {
					World w = new World();
					w.loadMapFromFile(new File("maps/" + typedLower + ".txt"));
					tiledGame.setWorld(w);
				} catch (IOException | MapLoadException ex) {
					Console.getInstance().echo("Could not load map " + typedLower + ".txt");
					Console.getInstance().echo(ex.getMessage());
				}
			}
		}

		return visible;
	}

	@Override
	public boolean keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setVisible(!visible);
		}

		return visible;
	}

	@Override
	public boolean keyReleased(KeyEvent e) {
		return visible;
	}

	@Override
	public boolean mousePressed(MouseEvent me) {
		mouseCapture = isIncidentalWithMenu(me.getX(), me.getY());
		//echo("mousePressed at [%d; %d], %s", me.getX(), me.getY(), mouseCapture ? "capturing" : "ignoring" );
		return mouseCapture;
	}

	@Override
	public boolean mouseReleased(MouseEvent me) {
		//echo("mouseReleased at [%d; %d], %s", me.getX(), me.getY(), mouseCapture ? "capturing" : "ignoring" );
		mouseCaptureClick = mouseCapture;
		boolean retVal = mouseCapture;
		mouseCapture = false;
		return retVal;
	}

	@Override
	public boolean mouseClicked(MouseEvent me) {
		//echo("mouseClicked at [%d; %d], %s", me.getX(), me.getY(), mouseCaptureClick ? "capturing" : "ignoring");
		boolean retVal = mouseCaptureClick;
		mouseCaptureClick = false;
		return retVal;
	}

	@Override
	public boolean mouseDragged(MouseEvent me) {
		//echo(1000, "mouseDragged at [%d; %d], %s", me.getX(), me.getY(), mouseCapture ? "capturing" : "ignoring" );
		return mouseCapture;
	}

}
