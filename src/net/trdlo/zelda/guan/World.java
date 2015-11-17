package net.trdlo.zelda.guan;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import net.trdlo.zelda.Console;

class World {

	public static final double MINIMAL_DETECTABLE_DISTANCE = 0.01;

	final Set<Point> points;
	final Set<Line> lines;

	Rectangle bounds;

	World() {
		points = new LinkedHashSet<>();
		this.lines = new LinkedHashSet<Line>() {
			@Override
			public boolean add(Line l) {
				l.connect();
				return super.add(l);
			}

			@Override
			public boolean remove(Object o) {
				if (o instanceof Line) {
					((Line) o).disconnect();
				}
				return super.remove(o);
			}
		};

		bounds = new Rectangle(-1000, -1000, 2000, 2000);
	}

	public final void loadFromFile(String fileName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		Map<Integer, Point> pointIdMap = new HashMap<>();
		while ((line = br.readLine()) != null) {
			Matcher m;
			if ((m = Point.PAT_POINT.matcher(line)).matches()) {
				Point newPoint = new Point(Double.valueOf(m.group(2)), Double.valueOf(m.group(3)), m.group(4));
				Integer pointId = Integer.valueOf(m.group(1));
				pointIdMap.put(pointId, newPoint);
				points.add(newPoint);
				Console.getInstance().echo("%s", newPoint);
			} else if ((m = Line.PAT_LINE.matcher(line)).matches()) {
				Point A = pointIdMap.get(Integer.valueOf(m.group(1)));
				if (A == null) {
					throw new Exception("Point index " + m.group(1) + "not found. Can't load world!");
				}
				Point B = pointIdMap.get(Integer.valueOf(m.group(2)));
				if (B == null) {
					throw new Exception("Point index " + m.group(2) + "not found. Can't load world!");
				}
				lines.add(Line.constructFromTwoPoints(A, B));
			}

		}
	}

	public void update() {

	}

	private static double sqr(double d) {
		return d * d;
	}

	public Point getPointAt(double x, double y, double rectSize) {
		Point nearest = null;
		double minDistSq = Double.MAX_VALUE;
		for (Point p : points) {
			double distSq = sqr(p.getX() - x) + sqr(p.getY() - y);
			if (distSq < minDistSq) {
				minDistSq = distSq;
				nearest = p;
			}
		}
		if (nearest != null && Math.max(Math.abs(nearest.getX() - x), Math.abs(nearest.getY() - y)) < (rectSize / 2.0)) {
			return nearest;
		} else {
			return null;
		}
	}

	public Set<Point> getPointsIn(double x1, double y1, double x2, double y2) {
		Set<Point> resultSet = new LinkedHashSet<>();
		for (Point p : points) {
			if (p.inRect(x1, y1, x2, y2)) {
				resultSet.add(p);
			}
		}
		return resultSet;
	}

	public void shiftPoints(Set<Point> shiftPoints, double dx, double dy) {
		for (Point p : points) {
			if (shiftPoints.contains(p)) {
				p.setXY(p.getX() + dx, p.getY() + dy);
			}
		}
	}

}
