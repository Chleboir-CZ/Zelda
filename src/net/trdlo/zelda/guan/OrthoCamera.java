package net.trdlo.zelda.guan;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

class OrthoCamera {

	public static final double ZOOM_BASE = 1.1;

	private World world;
	private double x, y;
	private int zoom;
	private double zoomCoefLimit;
	private Rectangle componentBounds, cameraBounds;

	final Stroke defaultStroke, selectionStroke, dashStroke;

	private boolean boundsDebug = false;

	private Point dragStart, dragEnd, moveStart, moveEnd;
	private final Set<Point> selection;
	private Set<Point> tempSelection;
	private boolean additiveSelection;
	//private Set<SmartLine> selectedLines; 

	public OrthoCamera(World world, double x, double y, int zoom) {
		setWorld(world, x, y, zoom);
		defaultStroke = new BasicStroke(1);
		selectionStroke = new BasicStroke(2);
		dashStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);

		selection = new HashSet<>();
		tempSelection = new HashSet<>();
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

	private void renderBoundsDebug(Graphics2D graphics) {
		if (world.bounds == null) {
			graphics.setColor(Color.RED);
			graphics.drawString("Bounds not set!", 50, 10);
			graphics.setColor(Color.WHITE);
			return;
		}

		graphics.setStroke(selectionStroke);
		graphics.setColor(Color.PINK);
		int l = worldToViewX(world.bounds.x), t = worldToViewY(world.bounds.y), w = worldToViewX(world.bounds.x + world.bounds.width) - l, h = worldToViewY(world.bounds.y + world.bounds.height) - t;
		graphics.drawRect(l, t, w, h);
		if (cameraBounds != null) {
			graphics.setStroke(defaultStroke);
			graphics.setColor(Color.RED);
			graphics.drawRect(worldToViewX(x) - Point.DISPLAY_SIZE / 2, worldToViewY(y) - Point.DISPLAY_SIZE / 2, Point.DISPLAY_SIZE, Point.DISPLAY_SIZE);
			l = worldToViewX(cameraBounds.x);
			t = worldToViewY(cameraBounds.y);
			w = worldToViewX(cameraBounds.x + cameraBounds.width) - l;
			h = worldToViewY(cameraBounds.y + cameraBounds.height) - t;
			if (w > 0 && h > 0) {
				graphics.drawRect(l, t, w, h);
			} else if (w > 0) {
				t = worldToViewY(cameraBounds.y + cameraBounds.height / 2);
				graphics.drawLine(l, t, l + w, t);
			} else if (h > 0) {
				l = worldToViewX(cameraBounds.x + cameraBounds.width / 2);
				graphics.drawLine(l, t, l, t + h);
			}

		}
	}

	public void render(Graphics2D graphics, Rectangle componentBounds, float renderFraction) {
		this.componentBounds = componentBounds;

		if (boundsDebug) {
			renderBoundsDebug(graphics);
		}

		//graphics.drawString("DS: " + (dragStart != null ? dragStart.toString() : "null"), 80, 20);
		//graphics.drawString("DE: " + (dragEnd != null ? dragEnd.toString() : "null"), 80, 40);
		double dx = 0, dy = 0;
		if (moveStart != null && moveEnd != null) {
			dx = moveEnd.getX() - moveStart.getX();
			dy = moveEnd.getY() - moveStart.getY();
		}

		graphics.setStroke(defaultStroke);
		graphics.setColor(Color.WHITE);
		for (SmartLine line : world.lines) {
			double adx1 = 0, ady1 = 0;
			double adx2 = 0, ady2 = 0;
			if (selection.contains(line.A)) {
				adx1 = dx;
				ady1 = dy;
			}
			if (selection.contains(line.B)) {
				adx2 = dx;
				ady2 = dy;
			}
			graphics.drawLine(worldToViewX(line.A.x + adx1), worldToViewY(line.A.y + ady1), worldToViewX(line.B.x + adx2), worldToViewY(line.B.y + ady2));
		}

		for (Point point : world.points) {
			double adx = 0, ady = 0;
			if (selection.contains(point) || tempSelection.contains(point)) {
				graphics.setStroke(selectionStroke);
				graphics.setColor(Color.PINK);
				adx = dx;
				ady = dy;
			} else {
				graphics.setStroke(defaultStroke);
				graphics.setColor(Color.WHITE);
			}
			graphics.drawRect(worldToViewX(point.x + adx) - Point.DISPLAY_SIZE / 2, worldToViewY(point.y + ady) - Point.DISPLAY_SIZE / 2, Point.DISPLAY_SIZE, Point.DISPLAY_SIZE);
		}

		if (dragStart != null && dragEnd != null) {
			graphics.setStroke(dashStroke);
			graphics.setColor(Color.LIGHT_GRAY);
			graphics.drawRect(
					worldToViewX(Math.min(dragStart.getX(), dragEnd.getX())),
					worldToViewY(Math.min(dragStart.getY(), dragEnd.getY())),
					(int) (Math.abs(dragStart.getX() - dragEnd.getX()) * zoomCoef()),
					(int) (Math.abs(dragStart.getY() - dragEnd.getY()) * zoomCoef()));
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

		double wx = x;
		double wy = y;
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

	public boolean isBoundsDebug() {
		return boundsDebug;
	}

	public void setBoundsDebug(boolean showBoundsDebug) {
		this.boundsDebug = showBoundsDebug;
	}

	public Point getPointAt(int x, int y) {
		return world.getPointAt(viewToWorldX(x), viewToWorldY(y), Point.DISPLAY_SIZE / zoomCoef());
	}

	public Set<Point> getPointsIn(int x1, int y1, int x2, int y2) {
		return world.getPointsIn(viewToWorldX(x1), viewToWorldY(y1), viewToWorldX(x2), viewToWorldY(y2));
	}

	public void mouse1pressed(MouseEvent e) {
		additiveSelection = e.isShiftDown();
		Point pointAt = getPointAt(e.getX(), e.getY());
		if (pointAt != null) {
			if (!additiveSelection && !selection.contains(pointAt)) {
				selection.clear();
			}
			selection.add(pointAt);
			moveStart = new Point(viewToWorldX(e.getX()), viewToWorldY(e.getY()));
		} else {
			dragStart = new Point(viewToWorldX(e.getX()), viewToWorldY(e.getY()));
			assert dragEnd == null;
		}
		//Guan.echo("MouseDown, pointAt: " + (pointAt != null ? pointAt.toString() : "null"));
	}

	public void mouse1dragged(MouseEvent e) {
		if (dragStart != null) {
			if (dragEnd == null) {
				dragEnd = new Point();
			}
			dragEnd.setX(viewToWorldX(e.getX()));
			dragEnd.setY(viewToWorldY(e.getY()));

			tempSelection = world.getPointsIn(dragStart.getX(), dragStart.getY(), dragEnd.getX(), dragEnd.getY());
			if (!additiveSelection) {
				selection.clear();
			}
		} else if (moveStart != null) {
			if (moveEnd == null) {
				moveEnd = new Point();
			}
			moveEnd.setX(viewToWorldX(e.getX()));
			moveEnd.setY(viewToWorldY(e.getY()));
		}
		//Guan.echo("MouseDrag");
	}

	public void mouse1released(MouseEvent e) {
		if (dragStart != null) {
			if (!additiveSelection) {
				selection.clear();
			}
			if (dragEnd != null) {
				selection.addAll(tempSelection);
				tempSelection.clear();
				dragEnd = null;
			}
			dragStart = null;
		} else if (moveStart != null && moveEnd != null) {
			world.shiftPoints(selection, moveEnd.getX() - moveStart.getX(), moveEnd.getY() - moveStart.getY());
			moveStart = moveEnd = null;
		}

		//Guan.echo("MouseUp");
	}

}
