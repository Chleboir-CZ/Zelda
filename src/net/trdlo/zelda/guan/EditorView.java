package net.trdlo.zelda.guan;

import net.trdlo.zelda.XY;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.trdlo.zelda.Console;
import net.trdlo.zelda.NU;
import net.trdlo.zelda.ZeldaFrame;

class EditorView extends AbstractView {

	public static final double ZOOM_BASE = 1.090507733; //2^(1/8)
	private static final Stroke DASHED_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);

	private boolean boundsDebug = false;

	private World world;
	private double x, y;
	private int zoom;

	private double zoomCoefLimit;
	private Rectangle componentBounds, cameraBounds;

	private Point dragStart, dragEnd;
	private XY viewDrag = null;
	private boolean snapToGrid = true;
	private Point moveStart, moveEnd, snappedPoint, moveDiff;
	private final Set<Point> avoidedPoints = new HashSet<>();
	private Point nearestPoint;
	private final Set<Point> selection;
	private Set<Point> tempSelection;
	private boolean additiveSelection;

	private Line insertedLine;
	private Point preservedInsertionStartPoint;

	private Line selectedLine, nearestLine;

	private Line circleLine;
	private int circlePointCount = 5;
	private final List<Point> tempCirclePoints = new ArrayList<>();
	private Point circleStartPoint;
	private Point circleEndPoint;
	private Point circleCenter;
	private double circleStartAngle, circleEndAngle, circleRadius;

	private int gridDensity = 6;
	private int gridStep = 64;

	private StringBuilder descBuilder;

	public EditorView(World world, double x, double y, int zoom) {
		setWorld(world, x, y, zoom);

		selection = new HashSet<Point>() {
			@Override
			public boolean remove(Object o) {
				boolean removed = super.remove(o);
				if (removed && isEmpty()) {
					stopTypingDesc();
				}
				return removed;
			}

			@Override
			public void clear() {
				super.clear();
				stopTypingDesc();
			}
		};
		tempSelection = new HashSet<>();
	}

	public final void setWorld(World world, double x, double y, int zoom) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.zoom = zoom;
	}

	private void readAsynchronoutInput() {
		if (ZeldaFrame.isPressed(KeyEvent.VK_ADD)) {
			zoom(1, null);
		}
		if (ZeldaFrame.isPressed(KeyEvent.VK_SUBTRACT)) {
			zoom(-1, null);
		}
	}

	public void update() {
		readAsynchronoutInput();
	}

	private int worldToViewX(double x) {
		assert componentBounds != null;
		return (int) ((componentBounds.width / 2) + (x - this.x) * zoomCoef());
	}

	public int worldToViewY(double y) {
		assert componentBounds != null;
		return (int) ((componentBounds.height / 2) + (y - this.y) * zoomCoef());
	}

	public XY worldToView(Point p) {
		return new XY(worldToViewX(p.x), worldToViewY(p.y));
	}

	public double viewToWorldX(int x) {
		assert componentBounds != null;
		return (x - (componentBounds.width / 2)) / zoomCoef() + this.x;
	}

	public double viewToWorldY(int y) {
		assert componentBounds != null;
		return (y - (componentBounds.height / 2)) / zoomCoef() + this.y;
	}

	public Point viewToWorld(XY xy) {
		return new Point(viewToWorldX(xy.x), viewToWorldY(xy.y));
	}

	private void renderGrid(Graphics2D graphics) {
		if (gridDensity >= 0) {
			graphics.setColor(Color.DARK_GRAY);

			int left = (int) NU.ceilToMultipleOf(viewToWorldX(0), gridStep);
			int right = (int) NU.floorToMultipleOf(viewToWorldX(componentBounds.width), gridStep);
			for (int i = left; i <= right; i += gridStep) {
				int vx = worldToViewX(i);
				graphics.drawLine(vx, 0, vx, componentBounds.height);
			}

			int top = (int) NU.ceilToMultipleOf(viewToWorldY(0), gridStep);
			int bottom = (int) NU.floorToMultipleOf(viewToWorldY(componentBounds.height), gridStep);
			for (int i = top; i <= bottom; i += (int) Math.pow(2, gridDensity)) {
				int vy = worldToViewY(i);
				graphics.drawLine(0, vy, componentBounds.width, vy);
			}
		}
	}

	private static final Stroke DEFAULT_STROKE = new BasicStroke(1);
	private static final Stroke SELECTION_STROKE = new BasicStroke(2);

	private void renderBoundsDebug(Graphics2D graphics) {
		if (world.bounds == null) {
			graphics.setColor(Color.RED);
			graphics.drawString("Bounds not set!", 50, 10);
			graphics.setColor(Color.WHITE);
			return;
		}
		graphics.drawString(String.format("Zoom = %f", zoomCoef()), 50, 20);

		graphics.setStroke(SELECTION_STROKE);
		graphics.setColor(Color.PINK);
		int l = worldToViewX(world.bounds.x), t = worldToViewY(world.bounds.y), w = worldToViewX(world.bounds.x + world.bounds.width) - l, h = worldToViewY(world.bounds.y + world.bounds.height) - t;
		graphics.drawRect(l, t, w, h);
		if (cameraBounds != null) {
			graphics.setStroke(DEFAULT_STROKE);
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

	private void renderReflectionsDebug(Graphics2D graphics) {
		if (world.lines.size() < 2) {
			return;
		}
		Iterator<Line> it = world.lines.iterator();
		Line t = it.next(), m = it.next();

		graphics.setStroke(DASHED_STROKE);
		graphics.setColor(Color.DARK_GRAY);

		Line r = m.reflect(t);
		graphics.drawLine(worldToViewX(r.A.x), worldToViewY(r.A.y), worldToViewX(r.B.x), worldToViewY(r.B.y));
		Line b = m.bounceOff(t);
		if (b != null) {
			graphics.drawLine(worldToViewX(b.A.x), worldToViewY(b.A.y), worldToViewX(b.B.x), worldToViewY(b.B.y));
		}
		Line br = m.bounceOffRay(t);
		if (br != null) {
			graphics.drawLine(worldToViewX(br.A.x), worldToViewY(br.A.y), worldToViewX(br.B.x), worldToViewY(br.B.y));
		}
	}

	private void renderCrossingDebug(Graphics2D graphics, Line line) {
		if ((System.nanoTime() / 500000000L & 1) == 0) {
			return;
		}

		Point iP;
		for (Line cross : world.lines) {
			if (cross != line && (iP = cross.getSegmentSegmentIntersection(line)) != null) {
				if (cross.A != line.A && cross.B != line.B && cross.A != line.B && cross.B != line.A) {
					XY iP1 = worldToView(iP);
					graphics.setColor(Color.RED);
					graphics.drawArc(iP1.x - 8, iP1.y - 8, 16, 16, 0, 360);
				}
			}
		}
	}

	private void renderPoint(Graphics2D graphics, double px, double py, String desc) {
		int vx = worldToViewX(px);
		int vy = worldToViewY(py);
		graphics.drawRect(vx - Point.DISPLAY_SIZE / 2, vy - Point.DISPLAY_SIZE / 2, Point.DISPLAY_SIZE, Point.DISPLAY_SIZE);

		if (!desc.isEmpty()) {
			graphics.setColor(Point.DESCRIPTION_COLOR);
			graphics.setFont(Point.DESCRIPTION_FONT);
			graphics.drawString(desc, vx + Point.DISPLAY_SIZE, vy + Point.DISPLAY_SIZE);
		}
	}

	private void renderPoint(Graphics2D graphics, Point p) {
		renderPoint(graphics, p.x, p.y, p.description);
	}

	public void render(Graphics2D graphics, Rectangle componentBounds, float renderFraction) {
		this.componentBounds = componentBounds;

		renderGrid(graphics);

		if (boundsDebug) {
			renderBoundsDebug(graphics);
		}

		double dx = 0, dy = 0;
		if (moveStart != null && moveEnd != null) {
			dx = moveEnd.getX() - moveStart.getX();
			dy = moveEnd.getY() - moveStart.getY();
		}

		graphics.setStroke(Line.DEFAULT_STROKE);
		graphics.setColor(Line.DEFAULT_COLOR);
		for (Line line : world.lines) {
			if (line == circleLine) {
				continue;
			}

			double lAx = line.getA().x, lAy = line.getA().y;
			double lBx = line.getB().x, lBy = line.getB().y;
			if (selection.contains(line.getA())) {
				lAx += dx;
				lAy += dy;
			}
			if (selection.contains(line.getB())) {
				lBx += dx;
				lBy += dy;
			}

			if (line == selectedLine) {
				graphics.setColor(Line.SELECTION_COLOR);
				graphics.setStroke(Line.SELECTION_STROKE);
			} else {
				graphics.setColor(Line.DEFAULT_COLOR);
				graphics.setStroke(Line.DEFAULT_STROKE);
			}

			if (line == nearestLine) {
				graphics.setColor(Color.YELLOW);
			}

			graphics.drawLine(worldToViewX(lAx), worldToViewY(lAy), worldToViewX(lBx), worldToViewY(lBy));
			renderCrossingDebug(graphics, line);
		}

		for (Point point : world.points) {
			double px = point.x, py = point.y;
			if (selection.contains(point) || tempSelection.contains(point)) {
				graphics.setStroke(Point.SELECTION_STROKE);
				graphics.setColor(Point.SELECTION_COLOR);
				px += dx;
				py += dy;
				if (isTyping()) {
					renderPoint(graphics, px, py, descBuilder.toString() + ((System.nanoTime() / 500000000L & 1) == 0 ? "_" : ""));
				} else {
					renderPoint(graphics, px, py, point.description);
				}
			} else if (point == nearestPoint) {
				graphics.setStroke(Point.DEFAULT_STROKE);
				graphics.setColor(Point.SELECTION_COLOR);
				renderPoint(graphics, px, py, point.description);
			} else {
				graphics.setStroke(Point.DEFAULT_STROKE);
				graphics.setColor(Point.DEFAULT_COLOR);
				renderPoint(graphics, px, py, point.description);
			}
		}

		//renderReflectionsDebug(graphics);
		if (dragStart != null && dragEnd != null) {
			graphics.setStroke(DASHED_STROKE);
			graphics.setColor(Color.LIGHT_GRAY);
			graphics.drawRect(
					worldToViewX(Math.min(dragStart.getX(), dragEnd.getX())),
					worldToViewY(Math.min(dragStart.getY(), dragEnd.getY())),
					(int) (Math.abs(dragStart.getX() - dragEnd.getX()) * zoomCoef()),
					(int) (Math.abs(dragStart.getY() - dragEnd.getY()) * zoomCoef()));
		}

		if (insertedLine != null) {
			graphics.setStroke(DASHED_STROKE);
			graphics.setColor(Line.SELECTION_COLOR);
			graphics.drawLine(worldToViewX(insertedLine.A.x), worldToViewY(insertedLine.A.y), worldToViewX(insertedLine.B.x), worldToViewY(insertedLine.B.y));
		}

		if (circleLine != null && !tempCirclePoints.isEmpty()) {
			if (circleCenter != null) {
				graphics.setStroke(DASHED_STROKE);
				graphics.setColor(Color.LIGHT_GRAY);
				int radius = (int) (circleRadius * zoomCoef());
				graphics.drawArc(worldToViewX(circleCenter.x) - radius, worldToViewY(circleCenter.y) - radius, 2 * radius, 2 * radius, 360 - NU.radToDeg(circleEndAngle), NU.radToDeg(circleEndAngle - circleStartAngle));
			}
			graphics.setStroke(DEFAULT_STROKE);
			graphics.setColor(Color.YELLOW);
			Point lastPoint = circleStartPoint;
			for (Point p : tempCirclePoints) {
				graphics.drawLine(worldToViewX(lastPoint.x), worldToViewY(lastPoint.y), worldToViewX(p.x), worldToViewY(p.y));
				renderPoint(graphics, p);
				lastPoint = p;
			}
			graphics.drawLine(worldToViewX(lastPoint.x), worldToViewY(lastPoint.y), worldToViewX(circleEndPoint.x), worldToViewY(circleEndPoint.y));
		}

		for (Player p : world.players) {
			int vx = worldToViewX(p.x);
			int vy = worldToViewY(p.y);
			graphics.drawArc(vx - 8, vy - 8, 16, 16, 0, 360);
			int vx2 = vx + (int) (Math.cos(p.orientation) * 16);
			int vy2 = vy + (int) (Math.sin(p.orientation) * 16);
			graphics.drawLine(vx, vy, vx2, vy2);
		}
	}

	private double zoomCoef() {
		return Math.max(zoomCoefLimit, Math.pow(ZOOM_BASE, zoom));
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

	public Point getPointAt(int x, int y, double limitDistance) {
		return world.getPointAt(viewToWorldX(x), viewToWorldY(y), limitDistance / zoomCoef());
	}

	public Point getPointAt(int x, int y) {
		return getPointAt(x, y, Point.DISPLAY_SIZE);
	}

	public Line getLineAt(int x, int y, double limitDistance) {
		return world.getLineAt(viewToWorldX(x), viewToWorldY(y), limitDistance / zoomCoef());
	}

	public Line getLineAt(int x, int y) {
		return getLineAt(x, y, Line.SELECTION_MAX_DISTANCE);
	}

	public Set<Point> getPointsIn(int x1, int y1, int x2, int y2) {
		return world.getPointsIn(viewToWorldX(x1), viewToWorldY(y1), viewToWorldX(x2), viewToWorldY(y2));
	}

	private void avoidedPointsBySnapped() {
		assert snappedPoint != null;

		avoidedPoints.clear();
		if (snappedPoint.connectedLines != null) {
			for (Line l : snappedPoint.connectedLines) {
				avoidedPoints.add(l.getA() == snappedPoint ? l.getB() : l.getA());
			}
		}
	}

	public void mouse1pressed(MouseEvent e) {
		additiveSelection = e.isShiftDown();
		Point pointAt;
		Line lineAt;
		if ((pointAt = getPointAt(e.getX(), e.getY())) != null) {
			if (additiveSelection && selection.contains(pointAt)) {
				selection.remove(pointAt);
			} else {
				if (!additiveSelection && !selection.contains(pointAt)) {
					selection.clear();
				}
				selection.add(pointAt);
				moveStart = new Point(viewToWorldX(e.getX()), viewToWorldY(e.getY()));
				snappedPoint = pointAt;
				avoidedPointsBySnapped();
				moveDiff = moveStart.diff(snappedPoint);
			}
			selectedLine = null;
		} else if (!additiveSelection && (lineAt = getLineAt(e.getX(), e.getY())) != null) {
			selection.clear();
			selectedLine = lineAt;
		} else {
			dragStart = new Point(viewToWorldX(e.getX()), viewToWorldY(e.getY()));
			selectedLine = null;
		}
	}

	public boolean mouseMoved(MouseEvent e) {
		int vx = e.getX(), vy = e.getY();
		nearestPoint = getPointAt(vx, vy, Point.HIGHLIGHT_MAX_DISTANCE);
		nearestLine = getLineAt(vx, vy, Line.HIGHLIGHT_MAX_DISTANCE);

		if (insertedLine != null) {
			Point ilPB = insertedLine.getB();
			Point pointAt = getPointAt(e.getX(), e.getY());

			if (pointAt != null && pointAt != insertedLine.getA()) {
				ilPB.setXY(pointAt.x, pointAt.y);
			} else {
				ilPB.setXY(viewToWorldX(vx), viewToWorldY(vy));
				if (snapToGrid && gridDensity != -1) {
					ilPB.roundToGrid(gridStep);
				}
			}
		} else if (circleLine != null) {
			updateCirclePoints();
		}
		return true;
	}

	public void mouse1dragged(MouseEvent e) {
		double wx = viewToWorldX(e.getX()), wy = viewToWorldY(e.getY());
		if (dragStart != null) {
			if (dragEnd == null) {
				dragEnd = new Point();
			}
			dragEnd.setXY(wx, wy);

			tempSelection = world.getPointsIn(dragStart.getX(), dragStart.getY(), dragEnd.getX(), dragEnd.getY());
			if (!additiveSelection) {
				selection.clear();
			}
		} else if (moveStart != null) {
			if (moveEnd == null) {
				moveEnd = new Point();
			}

			Point pointAt = world.getPointAt(wx, wy, Point.DISPLAY_SIZE / zoomCoef());

			if (pointAt != null && !avoidedPoints.contains(pointAt)) {
				moveEnd.setXY(pointAt.x, pointAt.y);
				moveEnd.moveBy(moveDiff);
			} else {
				moveEnd.setXY(wx, wy);
				if (snapToGrid && gridDensity != -1) {
					moveEnd.roundToGrid(gridStep);
					moveEnd.moveBy(moveDiff);
				}
			}
		}
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
		} else if (moveStart != null) {
			if (moveEnd != null) {
				world.shiftPoints(selection, moveEnd.getX() - moveStart.getX(), moveEnd.getY() - moveStart.getY());
				moveEnd = null;
			}
			moveStart = null;
			snappedPoint = null;
			avoidedPoints.clear();
		}
	}

	public void deleteSelection() {
		if (selectedLine != null) {
			world.deleteLine(selectedLine);
		} else {
			world.deletePoints(selection);
		}
	}

	public void nextGridDensity() {
		gridDensity--;
		if (gridDensity == -2) {
			gridDensity = 8;
		}
		gridStep = (int) Math.pow(2, gridDensity);
	}

	public void toggleSnapToGrid() {
		snapToGrid = !snapToGrid;
	}

	public boolean cancelOperation() {
		if (insertedLine != null) {
			insertedLine = null;
		} else if ((moveStart != null) || (dragStart != null)) {
			moveStart = null;
			moveEnd = null;
			dragStart = null;
			dragEnd = null;
		} else if ((!selection.isEmpty()) || selectedLine != null) {
			selection.clear();
			selectedLine = null;
		} else {
			return false;
		}
		return true;
	}

	public void insertPointAtLine() {
		if (nearestLine != null) {
			Point p = nearestLine.getNearestPointInSegment(viewToWorld(ZeldaFrame.getInstance().getMouseXY()));
			if (p != null) {
				if (snapToGrid && gridDensity != -1) {
					p.roundToGrid(gridStep);
				}
				world.points.add(p);
				Point b = nearestLine.getB();
				nearestLine.setB(p);
				world.lines.add(Line.constructFromTwoPoints(p, b));
			}
		}
	}

	private void updateCirclePointsCount() {
		while (tempCirclePoints.size() < circlePointCount) {
			tempCirclePoints.add(new Point());
		}
		while (tempCirclePoints.size() > circlePointCount) {
			tempCirclePoints.remove(tempCirclePoints.size() - 1);
		}
	}

	private void updateCirclePoints() {
		if (circleLine == null) {
			return;
		}

		Point s = new Point((circleLine.A.x + circleLine.B.x) / 2, (circleLine.A.y + circleLine.B.y) / 2);
		Line circleLineAxis = circleLine.getPerpendicular(s);
		Point mP = viewToWorld(ZeldaFrame.getInstance().getMouseXY());
		s = new Point((circleLine.A.x + mP.x) / 2, (circleLine.A.y + mP.y) / 2);
		double nx = circleLine.A.x - mP.x;
		double ny = circleLine.A.y - mP.y;
		Line secondAxis = Line.constructFromPointAndNormal(s, nx, ny);
		circleCenter = circleLineAxis.getIntersection(secondAxis);

		if (circleCenter == null /*TODO: tolerance 5 deg*/) {
			if (NU.inRange(0, circleLine.getPosition(mP), 1)) {
				updateCirclePointsCount();
				double vx = circleLine.B.x - circleLine.A.x;
				double vy = circleLine.B.y - circleLine.A.y;
				int i = 0;
				int divisor = tempCirclePoints.size() + 1;
				for (Point p : tempCirclePoints) {
					double phase = (i + 1.0) / divisor;
					p.setXY(circleLine.A.x + vx * phase, circleLine.A.y + vy * phase);
					i++;
				}
				circleStartPoint = circleLine.A;
				circleEndPoint = circleLine.B;

			} else {
				tempCirclePoints.clear();
			}
		} else {
			updateCirclePointsCount();

			double alpha = Math.atan2(circleLine.A.y - circleCenter.y, circleLine.A.x - circleCenter.x);
			double beta = Math.atan2(circleLine.B.y - circleCenter.y, circleLine.B.x - circleCenter.x);
			double gamma = Math.atan2(mP.y - circleCenter.y, mP.x - circleCenter.x);

			if ((alpha < beta && beta < gamma) || (beta < gamma && gamma < alpha) || (gamma < alpha && alpha < beta)) { //B to A
				circleStartAngle = beta;
				circleEndAngle = alpha;
				circleStartPoint = circleLine.B;
				circleEndPoint = circleLine.A;
			} else { //A to B
				circleStartAngle = alpha;
				circleEndAngle = beta;
				circleStartPoint = circleLine.A;
				circleEndPoint = circleLine.B;
			}
			if (circleEndAngle < circleStartAngle) {
				circleEndAngle += 2 * Math.PI;
			}
			double angleStep = (circleEndAngle - circleStartAngle) / (circlePointCount + 1);
			circleRadius = circleCenter.getDistance(mP);
			double fi = circleStartAngle;
			for (Point p : tempCirclePoints) {
				fi += angleStep;
				p.x = circleCenter.x + Math.cos(fi) * circleRadius;
				p.y = circleCenter.y + Math.sin(fi) * circleRadius;
			}
		}
	}

	public void startCircle() {
		if (circleLine == null && insertedLine == null) {
			circleLine = nearestLine;
			updateCirclePoints();
		} else {
			circleLine = null;
		}
	}

	public void incCircleSegments() {
		if (circleLine != null) {
			circlePointCount++;
			updateCirclePoints();
		}
	}

	public void decCircleSegments() {
		if (circleLine != null && circlePointCount > 1) {
			circlePointCount--;
			updateCirclePoints();
		}
	}

	public void insert() {
		XY mouseXY = ZeldaFrame.getInstance().getMouseXY();
		Point mP = viewToWorld(mouseXY);

		if (circleLine != null) {
			world.points.addAll(tempCirclePoints);
			Point lastPoint = circleStartPoint;
			for (Point p : tempCirclePoints) {
				world.lines.add(Line.constructFromTwoPoints(lastPoint, p));
				lastPoint = p;
			}
			world.lines.add(Line.constructFromTwoPoints(lastPoint, circleEndPoint));
			world.lines.remove(circleLine);
			circleLine = null;
			tempCirclePoints.clear();
		} else {

			Point wP = getPointAt(mouseXY.x, mouseXY.y);

			if (wP == null && snapToGrid && gridDensity != -1) {
				mP.roundToGrid(gridStep);
				wP = world.getPointAt(mP.x, mP.y, World.MINIMAL_DETECTABLE_DISTANCE);
			}

			if (insertedLine == null) {
				preservedInsertionStartPoint = wP;
				if (wP == null) {
					wP = new Point(mP);
					world.points.add(wP);
				}
				insertedLine = Line.constructFromTwoPoints(wP, mP);
			} else if (wP == null) {
				Point oldB = insertedLine.getB();
				world.points.add(oldB);
				world.lines.add(insertedLine);
				insertedLine = Line.constructFromTwoPoints(oldB, mP);
			} else if (insertedLine.getA() != wP) {
				insertedLine.setB(wP);
				world.lines.add(insertedLine);
				insertedLine = null;
			}
		}
	}

	public void unInsert() {
		if (insertedLine == null) {
			return;
		}

		Point oldA = insertedLine.getA();

		if (oldA == preservedInsertionStartPoint) {
			insertedLine = null;
		} else if (oldA.connectedLines == null || oldA.connectedLines.isEmpty()) {
			world.points.remove(oldA);
			insertedLine = null;
		} else {
			assert oldA.connectedLines.size() <= 1;

			Line previous = oldA.connectedLines.iterator().next();
			world.points.remove(oldA);
			world.lines.remove(previous);
			insertedLine = previous;
		}
	}

	public void mergePoints() {
		if (selection.size() >= 2) {
			double left = Double.MAX_VALUE, right = -Double.MAX_VALUE, top = Double.MAX_VALUE, bottom = -Double.MAX_VALUE;
			Set<Line> linesAffected = new HashSet<>();
			boolean linesConflict = false;
			POINT_LOOP:
			for (Point p : selection) {
				left = Math.min(left, p.getX());
				right = Math.max(right, p.getX());
				top = Math.min(top, p.getY());
				bottom = Math.max(bottom, p.getY());

				if (p.connectedLines != null) {
					for (Line l : p.connectedLines) {
						linesConflict = !linesAffected.add(l);
					}
				}
			}

			if (right - left < World.MINIMAL_DETECTABLE_DISTANCE && bottom - top < World.MINIMAL_DETECTABLE_DISTANCE) {
				Iterator<Point> it = selection.iterator();
				Point preservedFirst = it.next();
				while (it.hasNext()) {
					Point merged = it.next();
					if (merged.connectedLines != null) {
						for (Line l : new ArrayList<>(merged.connectedLines)) {
							if ((l.getA() == preservedFirst && l.getB() == merged) || (l.getB() == preservedFirst && l.getA() == merged)) {
								world.lines.remove(l);
							} else {
								l.changePoint(merged, preservedFirst);
							}
						}
					}
					world.points.remove(merged);
				}
				Console.getInstance().echo(5000, "%d points merged.", selection.size());

				selection.clear();
				selection.add(preservedFirst);
			} else {
				Console.getInstance().echo(5000, "Only overlapping points can be merged!");
			}
		}
	}

	public void initTypingDesc() {
		if (selection.size() > 1) {
			descBuilder = new StringBuilder();
		} else if (!selection.isEmpty()) {
			descBuilder = new StringBuilder(selection.iterator().next().getDescription());
		}
	}

	public boolean isTyping() {
		return descBuilder != null;
	}

	public void stopTypingDesc() {
		descBuilder = null;
	}

	public static boolean isPrintableChar(char c) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return (!Character.isISOControl(c))
				&& c != KeyEvent.CHAR_UNDEFINED
				&& block != null && block != Character.UnicodeBlock.SPECIALS;
	}

	public boolean charTyped(char c) {
		assert descBuilder != null;

		if (c == '\n') {
			setSelectionDescription();
			stopTypingDesc();
		} else if (c == '\b') {
			if (descBuilder.length() > 0) {
				descBuilder.deleteCharAt(descBuilder.length() - 1);
			}
		} else if (isPrintableChar(c)) {
			descBuilder.append(c);
		} else {
			return false;
		}

		return true;
	}

	public void setSelectionDescription() {
		String desc = descBuilder.toString();
		for (Point p : selection) {
			p.description = desc;
		}
	}

	public void setSelectionDescription(String desc) {
		for (Point p : selection) {
			p.description = desc;
		}
	}

	public boolean keyTyped(KeyEvent e) {
		if (isTyping()) {
			return charTyped(e.getKeyChar());
		} else {
			switch (e.getKeyChar()) {
				case 'g':
					nextGridDensity();
					break;
				case 's':
					toggleSnapToGrid();
					break;
				case 'v':
					setBoundsDebug(!isBoundsDebug());
					break;
				case 'i':
					insertPointAtLine();
					break;
				case ' ':
					insert();
					break;
				case '\b':
					unInsert();
					break;
				case 'm':
					mergePoints();
					break;
				case 'c':
					startCircle();
					break;
				case '+':
					incCircleSegments();
					break;
				case '-':
					decCircleSegments();
					break;
				case 't':
					initTypingDesc();
					break;
				default:
					return false;
			}
		}
		return true;
	}

	public boolean keyPressed(KeyEvent e) {
		switch (e.getKeyChar()) {
			case KeyEvent.VK_DELETE:
				deleteSelection();
				break;
			default:
				return false;
		}
		return true;
	}

	public boolean keyReleased(KeyEvent e) {
		return false;
	}

	public boolean mouseClicked(MouseEvent e) {
		return false;
	}

	public boolean mousePressed(MouseEvent e) {
		switch (e.getButton()) {
			case MouseEvent.BUTTON1:
				mouse1pressed(e);
				break;
			case MouseEvent.BUTTON3:
				viewDrag = new XY(e);
				ZeldaFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				break;
			default:
				return false;
		}
		return true;
	}

	public boolean mouseReleased(MouseEvent e) {
		switch (e.getButton()) {
			case MouseEvent.BUTTON1:
				mouse1released(e);
				break;
			case MouseEvent.BUTTON3:
				viewDrag = null;
				ZeldaFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				break;
			default:
				return false;
		}
		return true;
	}

	public boolean mouseEntered(MouseEvent e) {
		return false;
	}

	public boolean mouseExited(MouseEvent e) {
		return false;
	}

	public boolean mouseDragged(MouseEvent e) {
		if (viewDrag != null) {
			XY current = new XY(e);
			move(viewDrag.diff(current));
			viewDrag = current;
		}
		mouse1dragged(e);
		return true;
	}

	public boolean mouseWheelMoved(MouseWheelEvent e) {
		zoom(-e.getWheelRotation(), new XY(e));
		return true;
	}

}
