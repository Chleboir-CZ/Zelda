package net.trdlo.zelda.tiled;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import net.trdlo.zelda.Console;
import net.trdlo.zelda.FrameFactory;
import net.trdlo.zelda.GameInterface;
import net.trdlo.zelda.InputListener;
import net.trdlo.zelda.XY;
import net.trdlo.zelda.ZeldaFrame;

public class TiledGame implements GameInterface, InputListener {

	private World world;
	private float viewX, viewY, dx, dy;
	private Rectangle bounds;

	private ZeldaFrame zFrame;

	private String debugString;

	public TiledGame(World world, float startX, float startY) {
		this.world = world;
		this.viewX = startX;
		this.viewY = startY;

		dx = -0.1f; //tohle se má brát z vůle hráče a ne být přednastaveno, je to posun o 0.1 * 32px za jeden render
		dy = -0.1f;
	}

	public TiledGame(World world) {
		this(world, world.mapWidth / 2.0f, world.mapHeight / 2.0f);
	}

	public TiledGame() throws Exception {
		this(World.loadFromFile(new File("maps/small.txt"), false));
	}

	@Override
	public void setZeldaFrame(ZeldaFrame frame) {
		this.zFrame = frame;
	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		this.bounds = graphics.getDeviceConfiguration().getBounds();

		limitViewPosition(bounds);

		float rWidth2 = Math.min(world.mapWidth, bounds.width / (float) World.GRID_SIZE) / 2.0f;
		float rHeight2 = Math.min(world.mapHeight, bounds.height / (float) World.GRID_SIZE) / 2.0f;

		int xOffset = (int) Math.floor((bounds.width / 2.0f) - (viewX * World.GRID_SIZE));
		int yOffset = (int) Math.floor((bounds.height / 2.0f) - (viewY * World.GRID_SIZE));

		graphics.setClip(xOffset, yOffset, world.mapWidth * World.GRID_SIZE, world.mapHeight * World.GRID_SIZE);

		for (int j = (int) (viewY - rHeight2); j < (int) Math.ceil(viewY + rHeight2); j++) {
			for (int i = (int) (viewX - rWidth2); i < (int) Math.ceil(viewX + rWidth2); i++) {
				graphics.drawImage(world.map[i + j * world.mapWidth].getTile().getImg(), xOffset + i * World.GRID_SIZE, yOffset + j * World.GRID_SIZE, null);
			}
		}

		XY p = zFrame.getMouseXY();
		if (p != null) {
			int selX = (int) (getWorldX(p.x, p.y) + 0.5f);
			int selY = (int) (getWorldY(p.x, p.y) + 0.5f);
			graphics.setColor(Color.WHITE);
			int x = getPixelX(selX, selY, 0);
			int y = getPixelY(selX, selY, 0);
			graphics.drawRect(x - World.GRID_SIZE / 2, y - World.GRID_SIZE / 2, World.GRID_SIZE - 1, World.GRID_SIZE - 1);
			graphics.setColor(new Color(255, 255, 255, 32));
			graphics.fillRect(x - World.GRID_SIZE / 2 + 1, y - World.GRID_SIZE / 2 + 1, World.GRID_SIZE - 3, World.GRID_SIZE - 3);
		}
		graphics.setColor(Color.WHITE);

		//graphics.setColor(Color.RED);
		//graphics.drawRect(xOffset, yOffset, world.mapWidth * World.GRID_SIZE -1, world.mapHeight * World.GRID_SIZE -1);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		for (GameObjectInstance goi : world.objectInstances) {
			float pixelX = xOffset + (goi.getPosX() + goi.getMoveX() * renderFraction) * World.GRID_SIZE;
			float pixelY = yOffset + (goi.getPosY() + goi.getMoveY() * renderFraction) * World.GRID_SIZE;
			goi.render(graphics, pixelX, pixelY, renderFraction);
		}

		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		graphics.setClip(null);

		graphics.drawString("WorldView looks at [" + viewX + "; " + viewY + "]", 10, 25);
		if (debugString != null) {
			graphics.drawString(debugString, 10, 50);
		}
	}

	public void limitViewPosition(Rectangle bounds) {
		float centerX = world.mapWidth / 2.0f;
		float centerY = world.mapHeight / 2.0f;
		float bWidth = bounds.width / (float) World.GRID_SIZE;
		float bHeight = bounds.height / (float) World.GRID_SIZE;

		float minX = Math.min(centerX, bWidth / 2.0f);
		float maxX = Math.max(centerX, world.mapWidth - bWidth / 2.0f);
		float minY = Math.min(centerY, bHeight / 2.0f);
		float maxY = Math.max(centerY, world.mapHeight - bHeight / 2.0f);

		float old = viewX;
		viewX = Math.min(Math.max(viewX + dx, minX), maxX);
		if (viewX == old) {
			dx = -dx; //tady má být, že nedojde-li k posunu (splněno), pak se zastaví posouvání, dx = 0; nyní obrací směr posunu
		}
		old = viewY;
		viewY = Math.min(Math.max(viewY + dy, minY), maxY);
		if (viewY == old) {
			dy = -dy; //tady taky
		}
	}

	public float getWorldX(int pixelX, int pixelY) {
		if (bounds == null) {
			return 0;
		}
//		if(pixelX > (this.bounds.width / 2)) {
		return ((pixelX - World.GRID_SIZE / 2 - bounds.width / 2) / (float) World.GRID_SIZE) + this.viewX;
		/*		}
		 else {
		 return this.x - (((bounds.width / 2) - pixelX) / World.GRID_SIZE);
		 }*/
	}

	public float getWorldY(int pixelX, int pixelY) {
		if (bounds == null) {
			return 0;
		}
		//if(pixelY > (bounds.height / 2)) {
		return ((pixelY - World.GRID_SIZE / 2 - bounds.height / 2) / (float) World.GRID_SIZE) + this.viewY;
		/*		}
		 else {
		 return this.y - (((bounds.height / 2) - pixelY)/ World.GRID_SIZE);
		 }*/
	}

	public float getWorldZ() {
		return 0.0F;
	}

	public int getPixelX(float x, float y, float z) {
		if (bounds == null) {
			return 0;
		}

		return (bounds.width / 2) + World.GRID_SIZE / 2 + (int) ((x - this.viewX) * World.GRID_SIZE);
	}

	public int getPixelY(float x, float y, float z) {
		if (bounds == null) {
			return 0;
		}

		return (bounds.height / 2) + World.GRID_SIZE / 2 + (int) ((y - this.viewY) * World.GRID_SIZE);
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		float selX = getWorldX(me.getX(), me.getY());
		float selY = getWorldY(me.getX(), me.getY());
		debugString = String.format("Mouse cliced at [%d; %d] -> [%9.4f; %9.4f]", me.getX(), me.getY(), selX, selY);
	}

	@Override
	public void mousePressed(MouseEvent me) {
	}

	@Override
	public void mouseReleased(MouseEvent me) {
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent ke) {
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
			zFrame.terminate();
		}
	}

	@Override
	public void keyReleased(KeyEvent ke) {
	}

	@Override
	public void update() {
		world.update();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mwe) {
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean executeCommand(String command, Console console) {
		return false;
	}

	@Override
	public String getWindowCaption() {
		return "Tiled Zelda game demo";
	}

	public static void main(String[] args) {
		try {
			FrameFactory.buildInstance(new TiledGame()).run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
