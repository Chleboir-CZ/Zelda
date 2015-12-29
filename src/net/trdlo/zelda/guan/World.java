package net.trdlo.zelda.guan;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.trdlo.zelda.NU;

class World {

	public static final double MINIMAL_DETECTABLE_DISTANCE = 0.01;

	private String loadedFrom;

	final Set<Point> points;
	final Set<Line> lines;
	final Set<Player> players;

	Rectangle bounds;

	World() {
		points = new LinkedHashSet<>();
		lines = new LinkedHashSet<Line>() {
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
		players = new LinkedHashSet<>();
		players.add(new Player());

		bounds = new Rectangle(-1000, -1000, 2000, 2000);
	}

	public final void loadFromFile(String fileName) throws Exception {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			Map<Integer, Point> idPointMap = new HashMap<>();
			while ((line = br.readLine()) != null) {
				if (Point.lineMatchesPattern(line)) {
					LoadedPoint lp = Point.loadFromString(line);
					idPointMap.put(lp.id, lp.point);
					points.add(lp.point);
				} else if (Line.lineMatchesPattern(line)) {
					LoadedLine ll = Line.loadFromString(line);
					Point A = idPointMap.get(ll.idA);
					if (A == null) {
						throw new Exception("Point index " + ll.idA + " not found. Can't load world!");
					}
					Point B = idPointMap.get(ll.idB);
					if (B == null) {
						throw new Exception("Point index " + ll.idB + " not found. Can't load world!");
					}
					lines.add(Line.constructFromTwoPoints(A, B));
				}
			}
			loadedFrom = fileName;
		}
	}

	public void saveToFile(String fileName) throws Exception {
		try (Writer w = new BufferedWriter(new FileWriter(fileName))) {
			int id = 0;
			Map<Point, Integer> pointIdMap = new HashMap<>();
			for (Point p : points) {
				w.write(p.saveToString(id));
				w.write("\n");
				pointIdMap.put(p, id);
				id++;
			}
			for (Line l : lines) {
				w.write(l.saveToString(pointIdMap.get(l.A), pointIdMap.get(l.B)));
				w.write("\n");
			}
		}
	}

	public void save() throws Exception {
		if (loadedFrom != null) {
			saveToFile(loadedFrom);
		}
	}

	public void update() {

	}

	public Point getPointAt(double x, double y, double rectSize) {
		Point nearest = null;
		double minDistSq = Double.MAX_VALUE;
		for (Point p : points) {
			double distSq = p.getDistanceSquare(x, y);
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

	public Line getLineAt(double x, double y, double maxDist) {
		Line nearest = null;
		double maxDistSqr = NU.sqr(maxDist);
		double minDistSq = Double.MAX_VALUE;
		for (Line l : lines) {
			if (l.isValid()) {
				double distSq = l.getSegmentDistanceSquare(x, y);
				if (distSq < minDistSq) {
					minDistSq = distSq;
					nearest = l;
				}
			}
		}
		if (nearest != null && minDistSq < maxDistSqr) {
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

	public void deletePoints(Set<Point> delPoints) {
		for (Point p : delPoints) {
			points.remove(p);
			if (p.connectedLines != null) {
				for (Line l : new ArrayList<>(p.connectedLines)) {
					lines.remove(l);
				}
			}
		}
	}

	public void deleteLine(Line delLine) {
		lines.remove(delLine);
	}
}
