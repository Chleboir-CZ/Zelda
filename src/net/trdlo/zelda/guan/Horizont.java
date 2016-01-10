package net.trdlo.zelda.guan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.trdlo.zelda.NU;

public class Horizont {

	private static class RayCollision {

		public final Point point;
		public final double distance;
		public final Line line;

		public RayCollision(Point point, double distance, Line line) {
			this.point = point;
			this.distance = distance;
			this.line = line;
		}
	}

	private final Set<Line> lines;
	private final Player observer;
	private final Point observerPoint;

	private final SortedSet<Point> points;

	public static final List<Point> debugCirclePoints = new ArrayList<>();

	public Horizont(Set<Line> lines, Player observer) {
		if (lines == null) {
			throw new NullPointerException("lines");
		}
		if (observer == null) {
			throw new NullPointerException("observer");
		}

		this.lines = lines;
		this.observer = observer;
		observerPoint = new Point(observer.x, observer.y);

		points = new TreeSet<>(new Comparator<Point>() {
			@Override
			public int compare(Point o1, Point o2) {
				return o1.tempAngle < o2.tempAngle ? -1 : (o1.tempAngle > o2.tempAngle ? 1 : o1.hashCode() - o2.hashCode());
			}
		});
	}

	private void loadPoints() {
		debugCirclePoints.clear();
		for (Line line : lines) {
			Point point = line.getA();
			point.tempAngle = NU.normalizeAngle(Math.atan2(point.y - observer.y, point.x - observer.x) - observer.orientation);
			if (Math.abs(point.tempAngle) <= observer.fov / 2) {
				point.setDescription(String.format("%d°", NU.radToDeg(point.tempAngle)));
				points.add(point);
			} else {
				point.setDescription("");
			}

			point = line.getB();
			point.tempAngle = NU.normalizeAngle(Math.atan2(point.y - observer.y, point.x - observer.x) - observer.orientation);
			if (Math.abs(point.tempAngle) <= observer.fov / 2) {
				point.setDescription(String.format("%d°", NU.radToDeg(point.tempAngle)));
				points.add(point);
			} else {
				point.setDescription("");
			}

			for (Point p : line.getSegmentCircleIntersection(observerPoint, observer.vDist)) {
				p.tempAngle = NU.normalizeAngle(Math.atan2(p.y - observer.y, p.x - observer.x) - observer.orientation);
				if (Math.abs(p.tempAngle) <= observer.fov / 2) {
					p.setDescription(String.format("%d°", NU.radToDeg(p.tempAngle)));
					points.add(p);
					debugCirclePoints.add(p);
				}
			}
		}
	}

	private Line createLeftEdgeRay() {
		double dir = observer.orientation - observer.fov / 2;
		Point leftEdgePoint = new Point(observer.x + Math.cos(dir) * observer.vDist, observer.y + Math.sin(dir) * observer.vDist);
		return Line.constructFromTwoPoints(observerPoint, leftEdgePoint);
	}

	private RayCollision getFirstCollision(Line ray, Collection<Line> lines, Point ignored) {
		double nearestSqr = Double.MAX_VALUE;
		Line nearestLine = null;
		Point nearestPoint = null;

		for (Line line : lines) {
			if (ignored == null || !line.hasEndPoint(ignored)) {
				Point ip = ray.getRaySegmentIntersection(line);
				if (ip != null) {
					double distSqr = ray.A.getDistanceSquare(ip);
					if (distSqr < nearestSqr) {
						nearestSqr = distSqr;
						nearestLine = line;
						nearestPoint = ip;
					}
				}
			}
		}

		if (nearestLine != null) {
			return new RayCollision(nearestPoint, nearestSqr, nearestLine);
		} else {
			return null;
		}
	}

	public List<Line> computeHorizont() {
		loadPoints();

		List<Line> horizont = new ArrayList<>();

		Line ray = createLeftEdgeRay();
		RayCollision currentRC = getFirstCollision(ray, lines, null);
		if (currentRC != null) {
			ray.B = currentRC.point;
		}
		horizont.add(ray);

		for (Point point : points) {
			if (currentRC == null) {
				//jeli jsme na zadni stene => mozna vynoreni
				double pointDistSq = observerPoint.getDistanceSquare(point);
				//if ()
			} else if (currentRC.line.hasEndPoint(point)) {
				//konec teto lajny
				horizont.add(Line.constructFromTwoPoints(currentRC.point, point));
				//kudy se bude pokracovat? Bud odsud navazuje lajna dal (pak me zajima ta, co vede nejvic "ke me", nebo hledam lajnu nekde vzadu
				//TODO ... tady jsem skoncil
			} else {
				//bod pred nebo za primkou
				double pointDistSq = observerPoint.getDistanceSquare(point);
				Line pointRay = Line.constructFromTwoPoints(observerPoint, point);
				//prunik paprsku (observer -> zkoumany bod) s aktualni useckou
				Point pointImage = pointRay.getIntersection(currentRC.line);
				double curLineDistSq = observerPoint.getDistanceSquare(pointImage);
				if (pointDistSq < curLineDistSq) {
					//vynoreni
					//prida se usecka od posledni pozice k "pruniku"
					horizont.add(Line.constructFromTwoPoints(currentRC.point, point));
					//pointRay se upravi a zrecykluje jako usecka vynoreni
					pointRay.setA(pointImage);
					horizont.add(pointRay);
				} else {
					//bod za -> ignore
				}
			}
		}

		return horizont;
	}

}
