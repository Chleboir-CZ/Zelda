package net.trdlo.zelda.guan;

import net.trdlo.zelda.XY;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.trdlo.zelda.Console;
import net.trdlo.zelda.NU;
import net.trdlo.zelda.ZeldaFrame;

class EditorView extends AbstractView {

	private static final Stroke DASHED_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
	private static final Stroke DEFAULT_STROKE = new BasicStroke(1);
	private static final Stroke SELECTION_STROKE = new BasicStroke(2);

	private static final java.awt.Cursor DEFAULT_CURSOR = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR);
	private static final java.awt.Cursor DRAG_CURSOR = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR);

	private boolean boundsDebug = false;

	private Point dragStart, dragEnd;
	private boolean snapToGrid = true;
	private Point moveStart, moveEnd, snappedPoint, moveDiff;
	private final Set<Point> snappAvoidedPoints = new HashSet<>();
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

	private List<Point> horizont;
	private Polygon horizPoly;
	boolean horizontEnabled = true;

	public EditorView(World world, double x, double y, int zoom) {
		super(world);

		this.x = x;
		this.y = y;
		this.zoom = zoom;

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

	private void readAsynchronoutInput() {
		Player p = world.getTestPlayer();
		if (ZeldaFrame.isPressed(KeyEvent.VK_LEFT)) {
			p.orientation -= 0.3;
		}
		if (ZeldaFrame.isPressed(KeyEvent.VK_RIGHT)) {
			p.orientation += 0.31;
		}
		if (ZeldaFrame.isPressed(KeyEvent.VK_UP)) {
			p.x += Math.cos(p.orientation) * p.speed;
			p.y += Math.sin(p.orientation) * p.speed;
		}
		if (ZeldaFrame.isPressed(KeyEvent.VK_DOWN)) {
			p.x -= Math.cos(p.orientation) * p.speed;
			p.y -= Math.sin(p.orientation) * p.speed;
		}
	}

	@Override
	public void update() {
		readAsynchronoutInput();

	}

	private void renderGrid(Graphics2D graphics) {
		if (gridDensity >= 0) {
			graphics.setStroke(DEFAULT_STROKE);
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
			Color prevColor = graphics.getColor();
			graphics.setColor(Point.DESCRIPTION_COLOR);
			graphics.setFont(Point.DESCRIPTION_FONT);
			graphics.drawString(desc, vx + Point.DISPLAY_SIZE, vy + Point.DISPLAY_SIZE);
			graphics.setColor(prevColor);
		}
	}

	private void renderPoint(Graphics2D graphics, Point p) {
		renderPoint(graphics, p.x, p.y, p.description);
	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
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
				if (line == nearestLine) {
					graphics.setColor(Line.SELECTION_COLOR);
				} else {
					graphics.setColor(Line.DEFAULT_COLOR);
				}
				graphics.setStroke(Line.DEFAULT_STROKE);
			}

			graphics.drawLine(worldToViewX(lAx), worldToViewY(lAy), worldToViewX(lBx), worldToViewY(lBy));
			if (line == selectedLine) {
				graphics.setColor(Color.PINK);
				graphics.drawString(String.format("len = %.2f", line.getLength()), worldToViewX((lAx + lBx) / 2), worldToViewY((lAy + lBy) / 2));
			}
			renderCrossingDebug(graphics, line);
		}

		if (horizontEnabled) {
			Player player = world.getTestPlayer();
			horizont = TorchLight.getTorchLightPolygon(world.lines, player);
			horizPoly = convertPointListToPoly(horizont);

			Point pp = new Point(player.x, player.y);

			graphics.setStroke(DEFAULT_STROKE);
			graphics.setColor(Color.DARK_GRAY);
			graphics.fillPolygon(horizPoly);
			graphics.setColor(Color.PINK);
			//graphics.drawPolygon(horizPoly);
			Point first = horizont.get(0), prev = null;
			for (Point p : horizont) {
				if (prev != null) {
					graphics.drawLine(worldToViewX(prev.x), worldToViewY(prev.y), worldToViewX(p.x), worldToViewY(p.y));
				}
				prev = p;
				renderPoint(graphics, p);
			}
			graphics.drawLine(worldToViewX(prev.x), worldToViewY(prev.y), worldToViewX(first.x), worldToViewY(first.y));

			graphics.setStroke(DASHED_STROKE);
			int startAngle = -NU.radToDeg(player.orientation - player.fov / 2);
			if ((System.nanoTime() / 1000000000L & 1) == 0) {
				startAngle += (int) ((System.nanoTime() % 1000000000L) * 360 / 1000000000L);
			}
			graphics.drawArc(worldToViewX(pp.x - player.vDist), worldToViewY(pp.y - player.vDist), (int) (player.vDist * 2 * zoomCoef()), (int) (player.vDist * 2 * zoomCoef()), startAngle, -NU.radToDeg(player.fov));
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

		graphics.setColor(Color.GREEN);
		graphics.setStroke(DEFAULT_STROKE);
		for (Player p : world.players) {
			int vx = worldToViewX(p.x);
			int vy = worldToViewY(p.y);
			graphics.drawArc(vx - 8, vy - 8, 16, 16, 0, 360);
			int vx2 = vx + (int) (Math.cos(p.orientation) * 16);
			int vy2 = vy + (int) (Math.sin(p.orientation) * 16);
			graphics.drawLine(vx, vy, vx2, vy2);
		}
	}

	private boolean isBoundsDebug() {
		return boundsDebug;
	}

	private void setBoundsDebug(boolean showBoundsDebug) {
		this.boundsDebug = showBoundsDebug;
	}

	private Point getPointAt(int x, int y, double limitDistance) {
		return world.getPointAt(viewToWorldX(x), viewToWorldY(y), limitDistance / zoomCoef());
	}

	private Point getPointAt(int x, int y) {
		return getPointAt(x, y, Point.DISPLAY_SIZE);
	}

	private Line getLineAt(int x, int y, double limitDistance) {
		return world.getLineAt(viewToWorldX(x), viewToWorldY(y), limitDistance / zoomCoef());
	}

	private Line getLineAt(int x, int y) {
		return getLineAt(x, y, Line.SELECTION_MAX_DISTANCE);
	}

	protected Set<Point> getPointsIn(int x1, int y1, int x2, int y2) {
		return world.getPointsIn(viewToWorldX(x1), viewToWorldY(y1), viewToWorldX(x2), viewToWorldY(y2));
	}

	private void updateSnappAvoidedPoints() {
		assert snappedPoint != null;

		snappAvoidedPoints.clear();
		if (snappedPoint.connectedLines != null) {
			for (Line l : snappedPoint.connectedLines) {
				snappAvoidedPoints.add(l.getA() == snappedPoint ? l.getB() : l.getA());
			}
		}
	}

	private void mouse1pressed(MouseEvent e) {
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
				updateSnappAvoidedPoints();
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

	@Override
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

	private boolean mouse1dragged(MouseEvent e) {
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

			if (pointAt != null && !snappAvoidedPoints.contains(pointAt)) {
				moveEnd.setXY(pointAt.x, pointAt.y);
				moveEnd.moveBy(moveDiff);
			} else {
				moveEnd.setXY(wx, wy);
				if (snapToGrid && gridDensity != -1) {
					moveEnd.roundToGrid(gridStep);
					moveEnd.moveBy(moveDiff);
				}
			}
		} else {
			return false;
		}
		return true;
	}

	private void mouse1released(MouseEvent e) {
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
			snappAvoidedPoints.clear();
		}
	}

	private void deleteSelection() {
		if (selectedLine != null) {
			world.deleteLine(selectedLine);
		} else {
			world.deletePoints(selection);
		}
	}

	private void nextGridDensity() {
		gridDensity--;
		if (gridDensity == -2) {
			gridDensity = 8;
		}
		gridStep = (int) Math.pow(2, gridDensity);
	}

	private void toggleSnapToGrid() {
		snapToGrid = !snapToGrid;
	}

	private boolean cancelOperation() {
		if (insertedLine != null || circleLine != null) {
			insertedLine = null;
			circleLine = null;
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

	private void insertPointAtLine() {
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

		//TODO: zapracovat nejak snap to grid? => mozna snapovat na elipsy 1:4, 2:4, 3:4, 1:1, 3:2, 2:1, 3:1 ...
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

	private void startCircle() {
		if (circleLine == null && insertedLine == null) {
			circleLine = nearestLine;
			updateCirclePoints();
		} else {
			circleLine = null;
		}
	}

	private void incCircleSegments() {
		if (circleLine != null) {
			circlePointCount++;
			updateCirclePoints();
		}
	}

	private void decCircleSegments() {
		if (circleLine != null && circlePointCount > 1) {
			circlePointCount--;
			updateCirclePoints();
		}
	}

	private void insert() {
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

	private void unInsert() {
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

	private void mergePoints() {
		if (selection.size() >= 2) {
			double left = Double.MAX_VALUE, right = -Double.MAX_VALUE, top = Double.MAX_VALUE, bottom = -Double.MAX_VALUE;
			for (Point p : selection) {
				left = Math.min(left, p.getX());
				right = Math.max(right, p.getX());
				top = Math.min(top, p.getY());
				bottom = Math.max(bottom, p.getY());
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

	private void initTypingDesc() {
		if (selection.size() > 1) {
			descBuilder = new StringBuilder();
		} else if (!selection.isEmpty()) {
			descBuilder = new StringBuilder(selection.iterator().next().getDescription());
		}
	}

	private boolean isTyping() {
		return descBuilder != null;
	}

	private void stopTypingDesc() {
		descBuilder = null;
	}

	private static boolean isPrintableChar(char c) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return (!Character.isISOControl(c))
			&& c != KeyEvent.CHAR_UNDEFINED
			&& block != null && block != Character.UnicodeBlock.SPECIALS;
	}

	private boolean charTyped(char c) {
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

	private void setSelectionDescription() {
		String desc = descBuilder.toString();
		for (Point p : selection) {
			p.description = desc;
		}
	}

	private void setSelectionDescription(String desc) {
		for (Point p : selection) {
			p.description = desc;
		}
	}

	@Override
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
				case 'f':
					horizontEnabled = !horizontEnabled;
					break;
				default:
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				return cancelOperation();
			case KeyEvent.VK_DELETE:
				deleteSelection();
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	public boolean keyReleased(KeyEvent e) {
		return false;
	}

	@Override
	public java.awt.Cursor getCursor(Cursor cursor) {
		switch (cursor) {
			case DRAG:
				return DRAG_CURSOR;
			case NORMAL:
			default:
				return DEFAULT_CURSOR;
		}
	}

	@Override
	public boolean mouseClicked(MouseEvent e) {
		switch (e.getButton()) {
			case MouseEvent.BUTTON2:
				Player p = world.getTestPlayer();
				p.x = viewToWorldX(e.getX());
				p.y = viewToWorldY(e.getY());
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	public boolean mousePressed(MouseEvent e) {
		if (!super.mousePressed(e)) {
			switch (e.getButton()) {
				case MouseEvent.BUTTON1:
					mouse1pressed(e);
					break;
				default:
					return false;
			}
			return true;
		}
		return true;
	}

	@Override
	public boolean mouseReleased(MouseEvent e) {
		if (!super.mouseReleased(e)) {
			switch (e.getButton()) {
				case MouseEvent.BUTTON1:
					mouse1released(e);
					break;
				default:
					return false;
			}
			return true;
		}
		return true;
	}

	@Override
	public boolean mouseEntered(MouseEvent e) {
		return false;
	}

	@Override
	public boolean mouseExited(MouseEvent e) {
		return false;
	}

	@Override
	public boolean mouseDragged(MouseEvent e) {
		if (!super.mouseDragged(e)) {
			return mouse1dragged(e);
		}
		return true;
	}

	private static final Pattern PAT_GET_BOUNDS_DEBUG = Pattern.compile("^\\s*bounds-debug\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern PAT_SET_BOUNDS_DEBUG = Pattern.compile("^\\s*bounds-debug\\s+([01])\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern PAT_DESCRIBE = Pattern.compile("^\\s*setdescription\\s*(.*)\\s*$", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean executeCommand(String command, Console console) {
		Matcher m;
		if (PAT_GET_BOUNDS_DEBUG.matcher(command).matches()) {
			console.echo("bounds-debug " + (isBoundsDebug() ? "1" : "0"));
		} else if ((m = PAT_SET_BOUNDS_DEBUG.matcher(command)).matches()) {
			setBoundsDebug("1".equals(m.group(1)));
		} else if ((m = PAT_DESCRIBE.matcher(command)).matches()) {
			setSelectionDescription(m.group(1));
		} else {
			return false;
		}
		return true;
	}

}
