package net.trdlo.zelda.guan;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

class OrthoCamera {

	public static final double ZOOM_BASE = 1.1;

	private World world;
	private double x, y;
	private int zoom;
	private double zoomCoefLimit;
	private Rectangle componentBounds, cameraBounds;

	private final Stroke defaultStroke, selectionStroke, dashStroke;

	public OrthoCamera(World world, double x, double y, int zoom) {
		setWorld(world, x, y, zoom);
		defaultStroke = new BasicStroke(1);
		selectionStroke = new BasicStroke(2);
		dashStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
	}

	public final void setWorld(World world, double x, double y, int zoom) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.zoom = zoom;
	}

	public void update() {

	}

	private int worldToViewX(double x) {
		return (int) ((componentBounds.width / 2) + (x - this.x) * zoomCoef());
	}

	public int worldToViewY(double y) {
		return (int) ((componentBounds.height / 2) + (y - this.y) * zoomCoef());
	}

	public double viewToWorldX(int x) {
		return (x - (componentBounds.width / 2)) / zoomCoef() + this.x;
	}

	public double viewToWorldY(int y) {
		return (y - (componentBounds.height / 2)) / zoomCoef() + this.y;
	}

	public void render(Graphics2D graphics, Rectangle componentBounds, float renderFraction) {
		this.componentBounds = componentBounds;

		/*graphics.setStroke(selectionStroke);
		graphics.setColor(Color.PINK);
		int l = worldToViewX(world.bounds.x), t = worldToViewY(world.bounds.y), w = worldToViewX(world.bounds.x + world.bounds.width) - l, h = worldToViewY(world.bounds.y + world.bounds.height) - t;
		graphics.drawRect(l, t, w, h);
		graphics.setStroke(defaultStroke);
		graphics.setColor(Color.RED);
		 graphics.drawRect(worldToViewX(x) - Point.DISPLAY_SIZE / 2, worldToViewY(y) - Point.DISPLAY_SIZE / 2, Point.DISPLAY_SIZE, Point.DISPLAY_SIZE);
		 if (cameraBounds != null) {
		 l = worldToViewX(cameraBounds.x);
		 t = worldToViewY(cameraBounds.y);
		 w = worldToViewX(cameraBounds.x + cameraBounds.width) - l;
		 h = worldToViewY(cameraBounds.y + cameraBounds.height) - t;
		 graphics.drawRect(l, t, w, h);
		 }
		 */
		graphics.setColor(Color.WHITE);

		for (SmartLine line : world.lines) {
			graphics.drawLine(worldToViewX(line.A.x), worldToViewY(line.A.y), worldToViewX(line.B.x), worldToViewY(line.B.y));
		}

		for (Point point : world.points) {
			graphics.drawRect(worldToViewX(point.x) - Point.DISPLAY_SIZE / 2, worldToViewY(point.y) - Point.DISPLAY_SIZE / 2, Point.DISPLAY_SIZE, Point.DISPLAY_SIZE);
		}
	}

	private double zoomCoef() {
		double zoomCoef = Math.pow(ZOOM_BASE, zoom);
		return (zoomCoefLimit > zoomCoef) ? zoomCoefLimit : zoomCoef;
	}

	public void zoom(int change, XY fixedPoint) {
		if (componentBounds == null) {
			return;
		}

		double wx = 0;
		double wy = 0;
		if (fixedPoint != null) {
			wx = viewToWorldX(fixedPoint.x);
			wy = viewToWorldY(fixedPoint.y);
		}

		double c1 = zoomCoef();
		if (zoomCoefLimit < c1 || change > 0) {
			zoom += change;
		}
		double coefChange = zoomCoef() / c1;

		double dx = (wx - x) / coefChange;
		double dy = (wy - y) / coefChange;

		x = wx - dx;
		y = wy - dy;
		checkBounds();
	}

	public void move(XY diff) {
		x += diff.x / zoomCoef();
		y += diff.y / zoomCoef();
		checkBounds();
	}

	public void checkBounds() {
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

}
