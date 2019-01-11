package net.trdlo.zelda.tiled;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import net.trdlo.zelda.Console;
import net.trdlo.zelda.ZeldaFrame;
import net.trdlo.zelda.GameInterface;
import net.trdlo.zelda.InputListener;
import net.trdlo.zelda.XY;

public class TiledGame implements GameInterface, InputListener {

	public static final float SCROLL_INCREMENT = 0.05f;
	public static final float SCROLL_MAX = 0.5f;

	private World world;

	private float viewX, viewY, dx, dy;
	private Rectangle bounds;

	private ZeldaFrame zFrame;

	private String debugString;

	private long updateTime;

	public TiledGame(World world, float startX, float startY) {
		this.world = world;
		this.viewX = startX;
		this.viewY = startY;
	}

	public TiledGame(World world) {
		this(world, world.mapWidth / 2.0f, world.mapHeight / 2.0f);
	}

	public TiledGame() throws Exception {
		this(World.loadFromFile(new File("maps/default.txt"), false));
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

		for (int y = (int) (viewY - rHeight2); y < (int) Math.ceil(viewY + rHeight2); y++) {
			for (int x = (int) (viewX - rWidth2); x < (int) Math.ceil(viewX + rWidth2); x++) {
				TileInstance tileInstance = world.getTileInstance(x, y);
				int drawX = xOffset + x * World.GRID_SIZE;
				int drawY = yOffset + y * World.GRID_SIZE;
				for (BufferedImage img : tileInstance.getImgs(updateTime)) {
					if (img != null) {
						graphics.drawImage(img, drawX, drawY, null);
					}
				}
			}
		}

		Point p = zFrame.getMousePosition();
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
			dx = 0;
		}
		old = viewY;
		viewY = Math.min(Math.max(viewY + dy, minY), maxY);
		if (viewY == old) {
			dy = 0;
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
		float selXf = getWorldX(me.getX(), me.getY());
		float selYf = getWorldY(me.getX(), me.getY());
		int selX = (int) Math.round(selXf);
		int selY = (int) Math.round(selYf);
		if (me.getButton() == MouseEvent.BUTTON1) {
			if (!(world.getTileInstance(selX, selY) instanceof RoadInstance)) {
				world.setTile(selX, selY, new RoadInstance(world.roadTile));
			}
		}
		if (me.getButton() == MouseEvent.BUTTON3) {
			if (!(world.getTileInstance(selX, selY) instanceof WaterInstance)) {
				world.setTile(selX, selY, new WaterInstance(world.waterTile, selX, selY));
			}
		}
		if (me.getButton() == MouseEvent.BUTTON2) {
			debugString = String.format("Mouse clicked at [%d; %d] -> [%9.4f; %9.4f]", me.getX(), me.getY(), selXf, selYf);
			TileInstance ti = world.getTileInstance(selX, selY);
			if (ti != null) {
				debugString += String.format(" Tile is of %s", ti.getClass().getName());
				if (ti instanceof WaterInstance) {
					WaterInstance wi = (WaterInstance) ti;
					debugString += String.format(" Sides are [%d] Corners are [", wi.getGrassNeighbourCombo());
					for (Integer c : wi.getGrassCorners()) {
						debugString += c.toString();
					}
					debugString += "]";
				}
			}
		}
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
	public void mouseDragged(MouseEvent me) {
		int selX = (int) Math.round(getWorldX(me.getX(), me.getY()));
		int selY = (int) Math.round(getWorldY(me.getX(), me.getY()));
		if ((me.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK) {
			if (!(world.getTileInstance(selX, selY) instanceof RoadInstance)) {
				world.setTile(selX, selY, new RoadInstance(world.roadTile));
			}
		}
		if ((me.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK) {
			if (!(world.getTileInstance(selX, selY) instanceof WaterInstance)) {
				world.setTile(selX, selY, new WaterInstance(world.waterTile, selX, selY));
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent me) {
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
	public void update(long time) {
		updateTime = time;

		XY mouse = zFrame.getMouseXY();
		if (mouse.x < World.GRID_SIZE) {
			dx = Math.max(-SCROLL_MAX, dx - SCROLL_INCREMENT);
		} else if (mouse.x >= bounds.width - World.GRID_SIZE) {
			dx = Math.min(SCROLL_MAX, dx + SCROLL_INCREMENT);
		} else {
			dx = dx > SCROLL_INCREMENT ? dx - SCROLL_INCREMENT : (dx < -SCROLL_INCREMENT ? dx + SCROLL_INCREMENT : 0);
		}
		if (mouse.y < World.GRID_SIZE) {
			dy = Math.max(-SCROLL_MAX, dy - SCROLL_INCREMENT);
		} else if (mouse.y >= bounds.height - World.GRID_SIZE) {
			dy = Math.min(SCROLL_MAX, dy + SCROLL_INCREMENT);
		} else {
			dy = dy > SCROLL_INCREMENT ? dy - SCROLL_INCREMENT : (dy < -SCROLL_INCREMENT ? dy + SCROLL_INCREMENT : 0);
		}

		world.update(time);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mwe) {
	}

	@Override
	public boolean executeCommand(String command, Console console) {
		return false;
	}

	@Override
	public void listCommands(String command, Console console) {
	}

	@Override
	public String getWindowCaption() {
		return "Tiled Zelda game demo";
	}

	public static void main(String[] args) {
		try {
			ZeldaFrame.buildInstance(new TiledGame()).run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
