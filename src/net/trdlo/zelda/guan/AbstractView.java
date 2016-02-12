package net.trdlo.zelda.guan;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import net.trdlo.zelda.CommandExecuter;
import net.trdlo.zelda.XY;
import net.trdlo.zelda.ZeldaFrame;

abstract class AbstractView implements CommandExecuter {

	public static enum Cursor {
		NORMAL, DRAG
	}

	public static final double ZOOM_BASE = 1.090507733; //2^(1/8)
	public static final double ZOOM_MAX = 32;
	protected World world;
	protected double x, y;
	protected int zoom;
	protected double zoomCoefLimit;
	protected Rectangle componentBounds, cameraBounds;

	private XY viewDrag = null;
	private int renderCount = 0;

	public AbstractView(World world) {
		this.world = world;
	}

	public abstract void update();

	protected final Polygon convertPointListToPoly(List<Point> horiz) {
		int count = horiz.size();
		int[] xPoints = new int[count];
		int[] yPoints = new int[count];
		for (int i = 0; i < count; i++) {
			xPoints[i] = worldToViewX(horiz.get(i).x);
			yPoints[i] = worldToViewY(horiz.get(i).y);
		}
		return new Polygon(xPoints, yPoints, count);
	}

	public abstract void render(Graphics2D graphics, float renderFraction);

	private void firstRender() {
		ZeldaFrame.getInstance().setCursor(getCursor(Cursor.NORMAL));
	}

	public final void render(Graphics2D graphics, Rectangle componentBounds, float renderFraction) {
		if (renderCount++ == 0) {
			firstRender();
		}
		this.componentBounds = componentBounds;
		render(graphics, renderFraction);
	}

	public abstract boolean keyTyped(KeyEvent e);

	public abstract boolean keyPressed(KeyEvent e);

	public abstract boolean keyReleased(KeyEvent e);

	public abstract java.awt.Cursor getCursor(Cursor cursor);

	public abstract boolean mouseClicked(MouseEvent e);

	public boolean mousePressed(MouseEvent e) {
		switch (e.getButton()) {
			case MouseEvent.BUTTON3:
				viewDrag = new XY(e);
				ZeldaFrame.getInstance().setCursor(getCursor(Cursor.DRAG));
				break;
			default:
				return false;
		}
		return true;
	}

	public boolean mouseReleased(MouseEvent e) {
		switch (e.getButton()) {
			case MouseEvent.BUTTON3:
				viewDrag = null;
				ZeldaFrame.getInstance().setCursor(getCursor(Cursor.NORMAL));
				break;
			default:
				return false;
		}
		return true;
	}

	public abstract boolean mouseEntered(MouseEvent e);

	public abstract boolean mouseExited(MouseEvent e);

	private void move(XY diff) {
		x += diff.x / zoomCoef();
		y += diff.y / zoomCoef();
		checkBounds();
	}

	public boolean mouseDragged(MouseEvent e) {
		if (viewDrag != null) {
			XY current = new XY(e);
			move(viewDrag.diff(current));
			viewDrag = current;
			return true;
		}
		return false;
	}

	public abstract boolean mouseMoved(MouseEvent e);

	public boolean mouseWheelMoved(MouseWheelEvent e) {
		zoom(-e.getWheelRotation(), new XY(e));
		return true;
	}

	protected int worldToViewX(double x) {
		assert componentBounds != null;
		return (int) ((componentBounds.width / 2) + (x - this.x) * zoomCoef());
	}

	protected int worldToViewY(double y) {
		assert componentBounds != null;
		return (int) ((componentBounds.height / 2) + (y - this.y) * zoomCoef());
	}

	protected XY worldToView(Point p) {
		return new XY(worldToViewX(p.x), worldToViewY(p.y));
	}

	protected XY worldToView(Player p) {
		return new XY(worldToViewX(p.x), worldToViewY(p.y));
	}

	protected double viewToWorldX(int x) {
		assert componentBounds != null;
		return (x - (componentBounds.width / 2)) / zoomCoef() + this.x;
	}

	protected double viewToWorldY(int y) {
		assert componentBounds != null;
		return (y - (componentBounds.height / 2)) / zoomCoef() + this.y;
	}

	protected Point viewToWorld(XY xy) {
		return new Point(viewToWorldX(xy.x), viewToWorldY(xy.y));
	}

	protected void checkBounds() {
		if (world.bounds == null) {
			return;
		}

		zoomCoefLimit = Math.min(componentBounds.width / (double) world.bounds.width, componentBounds.height / (double) world.bounds.height);

		double left = world.bounds.x + (componentBounds.width / 2) / zoomCoef();
		double right = world.bounds.x + world.bounds.width - (componentBounds.width / 2) / zoomCoef();
		double top = world.bounds.y + (componentBounds.height / 2) / zoomCoef();
		double bottom = world.bounds.y + world.bounds.height - (componentBounds.height / 2) / zoomCoef();

		cameraBounds = new Rectangle((int) left, (int) top, (int) (right - left), (int) (bottom - top));

		if (left < right) {
			if (x < left) {
				x = left;
			}
			if (x > right) {
				x = right;
			}
		} else {
			x = world.bounds.x + world.bounds.width / 2;
		}
		if (top < bottom) {
			if (y < top) {
				y = top;
			}
			if (y > bottom) {
				y = bottom;
			}
		} else {
			y = world.bounds.y + world.bounds.height / 2;
		}
	}

	protected double zoomCoef() {
		return Math.max(zoomCoefLimit, Math.pow(ZOOM_BASE, zoom));
	}

	protected void zoom(int change, XY fixedPoint) {
		if (componentBounds == null) {
			return;
		}

		double wx = x;
		double wy = y;
		if (fixedPoint != null) {
			wx = viewToWorldX(fixedPoint.x);
			wy = viewToWorldY(fixedPoint.y);
		}

		double c1 = zoomCoef();
		if ((zoomCoefLimit < c1 || change > 0) && zoom + change < ZOOM_MAX) {
			zoom += change;
		}
		double coefChange = zoomCoef() / c1;

		double dx = (wx - x) / coefChange;
		double dy = (wy - y) / coefChange;

		x = wx - dx;
		y = wy - dy;
		checkBounds();
	}

}
