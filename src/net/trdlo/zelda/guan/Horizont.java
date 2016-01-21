package net.trdlo.zelda.guan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.trdlo.zelda.NU;
import net.trdlo.zelda.ZeldaFrame;

public class Horizont {

	private static class RayCollision {

		public final Point point;
		public final Line line;

		public RayCollision(Point point, Line line) {
			this.point = point;
			this.line = line;
		}
	}

	public static final int HORIZONT_SEGMENTS = 24;
	public static final double HORIZONT_ANGLE_LIMIT = 2 * Math.PI / HORIZONT_SEGMENTS;

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
				if (o1.tempAngle != o2.tempAngle) {
					return o1.tempAngle < o2.tempAngle ? -1 : 1;
				} else {
					double distDiff = o1.tempDistSqr - o2.tempDistSqr;
					if (distDiff != 0) {
						return distDiff < 0 ? -1 : 1;
					} else {
						return o1.hashCode() - o2.hashCode();
					}
				}
			}
		});
	}

	private void loadPoints() {
		debugCirclePoints.clear();
		double distSqrLimit = NU.sqr(observer.vDist);
		for (Line line : lines) {
			Point point = line.getA();
			point.tempDistSqr = point.getDistanceSquare(observerPoint);
			if (point.tempDistSqr <= distSqrLimit) {
				point.tempAngle = NU.normalizeAngle(Math.atan2(point.y - observer.y, point.x - observer.x) - observer.orientation);
				if (Math.abs(point.tempAngle) <= observer.fov / 2) {
					point.setDescription(String.format("%d°", NU.radToDeg(point.tempAngle)));
					points.add(point);
				}
			} else {
				point.setDescription("");
			}

			point = line.getB();
			point.tempDistSqr = point.getDistanceSquare(observerPoint);
			if (point.tempDistSqr <= distSqrLimit) {
				point.tempAngle = NU.normalizeAngle(Math.atan2(point.y - observer.y, point.x - observer.x) - observer.orientation);
				if (Math.abs(point.tempAngle) <= observer.fov / 2) {
					point.setDescription(String.format("%d°", NU.radToDeg(point.tempAngle)));
					points.add(point);
				}
			} else {
				point.setDescription("");
			}

			for (Point p : line.getSegmentCircleIntersection(observerPoint, observer.vDist)) {
				p.addConnectedLine(line);
				p.tempAngle = NU.normalizeAngle(Math.atan2(p.y - observer.y, p.x - observer.x) - observer.orientation);
				if (Math.abs(p.tempAngle) <= observer.fov / 2) {
					p.tempDistSqr = observer.vDistSqr;
					p.setDescription(String.format("%d°", NU.radToDeg(p.tempAngle)));
					points.add(p);
					debugCirclePoints.add(p);
				}
			}
		}
	}

	private Line createLeftEdgeRay() {
		double dir = observer.orientation - observer.fov / 2;
		Point point = new Point(observer.x + Math.cos(dir), observer.y + Math.sin(dir));
		point.tempAngle = -observer.fov / 2;
		return Line.constructFromTwoPoints(observerPoint, point);
	}

	private Line createRightEdgeRay() {
		double dir = observer.orientation + observer.fov / 2;
		Point point = new Point(observer.x + Math.cos(dir), observer.y + Math.sin(dir));
		point.tempAngle = observer.fov / 2;
		return Line.constructFromTwoPoints(observerPoint, point);
	}

	/**
	 * Vytvoří RayCollision, který splňuje kritéria: nejbližší kolize v množině lajn, nebo nový bod na hranici
	 * horizontu, line pak zůstává null
	 *
	 * @param ray	paprsek, na kterém se hledá
	 * @param lines	množina lajn pro hledání kolize
	 * @param ignored	bod, s nímž související lany se ignorují
	 * @return
	 */
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

		if (nearestPoint != null && nearestSqr <= observer.vDistSqr) {
			nearestPoint.tempDistSqr = nearestSqr;
			nearestPoint.tempAngle = NU.normalizeAngle(Math.atan2(nearestPoint.y - observer.y, nearestPoint.x - observer.x) - observer.orientation);
			return new RayCollision(nearestPoint, nearestLine);
		} else {
			Point horizontPoint = ray.getPointAtDistanceFromA(observer.vDist);
			horizontPoint.tempDistSqr = observer.vDistSqr;
			horizontPoint.tempAngle = NU.normalizeAngle(Math.atan2(horizontPoint.y - observer.y, horizontPoint.x - observer.x) - observer.orientation);
			return new RayCollision(horizontPoint, null);
		}
	}

	/**
	 * Vybere lajnu (nebo vrati null), ktera vede od raye v "leve" polorovine nejostreji zpet k pozorovateli
	 *
	 * @param ray	ray od pozorovatele na referencni bod
	 * @param ignoredLine	lajna, kterou ignorujeme
	 * @return
	 */
	public Line getSharpestLine(Line ray, Line ignoredLine) {
		Line sharpestLine = null;
		Point point = ray.B;
		double bestAngle = -Double.MAX_VALUE;
		if (point.connectedLines != null) {
			for (Line otherConnectedLine : point.connectedLines) {
				if (otherConnectedLine != ignoredLine) {
					Point otherPoint = otherConnectedLine.getOtherPoint(point);
					if (otherPoint != null) {
						if (ray.isInLeftHalfPane(otherPoint)) {
							double alpha = ray.getHalfNormalizedCosAlpha(otherPoint);
							if (alpha > bestAngle) {
								bestAngle = alpha;
								sharpestLine = otherConnectedLine;
							}
						}
					} else {
						//případ, kdy bod má navázanou lajnu, které není koncový, pak je jen jediná taková lajna
						if (!ray.isParallel(otherConnectedLine)) {
							return otherConnectedLine;
						} else {
							return null;
						}
					}
				}
			}
		}
		return sharpestLine;
	}

	public List<Line> computeHorizont() {
		loadPoints();

		List<Line> horizont = new ArrayList<>();

		Line ray = createLeftEdgeRay();
		RayCollision currentRC = getFirstCollision(ray, lines, null);
		ray.B = currentRC.point;
		horizont.add(ray);

		int pointCounter = 0;
		for (Point point : points) {
			pointCounter++;

			if (currentRC.line == null) {
				if (point.getDistanceSquare(currentRC.point) < World.MINIMAL_DETECTABLE_DISTANCE) {
					continue;
				}

				double angleDiff = point.tempAngle - currentRC.point.tempAngle;
				int segments = 1 + (int) (angleDiff / HORIZONT_ANGLE_LIMIT);
				int steps = segments + (point.tempDistSqr < observer.vDistSqr ? 1 : 0);

				double angle = observer.orientation + currentRC.point.tempAngle, angleStep = angleDiff / segments;
				Point from = currentRC.point;
				Point to;
				for (int i = 1; i < steps; i++) {
					angle += angleStep;
					to = new Point(observer.x + Math.cos(angle) * observer.vDist, observer.y + Math.sin(angle) * observer.vDist);
					horizont.add(Line.constructFromTwoPoints(from, to));
					from = to;
				}

				//nakonec propojit s aktualnim bodem - pokud nebyl na horizontu, udelala se lajna az pod nej (steps = segments + 1)
				horizont.add(Line.constructFromTwoPoints(from, point));

				Line sharpestLine = getSharpestLine(Line.constructFromTwoPoints(observerPoint, point), null);
				if (sharpestLine != null) {
					currentRC = new RayCollision(point, sharpestLine);
				} else {
					//TODO: Zdokumentovat, kdy k tomuto stavu dojde, pak smazat assert
					assert false;
				}
			} else if (point.connectedLines != null && point.connectedLines.contains(currentRC.line)) {
				//konec teto lajny
				horizont.add(Line.constructFromTwoPoints(currentRC.point, point));

				Line pointRay = Line.constructFromTwoPoints(observerPoint, point);
				Line sharpestLine = getSharpestLine(pointRay, currentRC.line);
				if (sharpestLine == null) {
					//nenavazuje vhodná lajna? => tečna - zanořujeme, nebo je bod na hranicni kruznici
					if (point.tempDistSqr == observer.vDistSqr) {
						currentRC = new RayCollision(point, null);
					} else {
						RayCollision diveRC = getFirstCollision(pointRay, lines, point);
						horizont.add(Line.constructFromTwoPoints(point, diveRC.point));
						currentRC = diveRC;
					}
				} else {
					//lajna doprava je vybrana, pokracujeme po ni
					currentRC = new RayCollision(point, sharpestLine);
				}
			} else {
				//bod pred nebo za primkou
				double pointDistSq = observerPoint.getDistanceSquare(point);
				Line pointRay = Line.constructFromTwoPoints(observerPoint, point);
				//prunik paprsku (observer -> zkoumany bod) s aktualni useckou
				Point pointImage = pointRay.getIntersection(currentRC.line);
				double curLineDistSq;
				if (pointImage != null) {
					//bezny pripad: prunik pointRay a current line urcuje bod, kdery je bud bliz nebo dal nez zkoumany point
					curLineDistSq = observerPoint.getDistanceSquare(pointImage);
				} else {
					//specialni pripad: current line je stejna jako pointRay => neni spolecny bod. Vzdalenost current line je blizsi z koncovych bodu
					curLineDistSq = Math.min(observerPoint.getDistanceSquare(currentRC.line.A), observerPoint.getDistanceSquare(currentRC.line.B));
				}
				if (pointDistSq < curLineDistSq) {
					//vynoreni
					//prida se usecka od posledni pozice k "pruniku"
					horizont.add(Line.constructFromTwoPoints(currentRC.point, point));
					//pointRay se upravi a zrecykluje jako usecka vynoreni
					Line sharpestLine = getSharpestLine(pointRay, null);
					if (sharpestLine != null) { //null by bylo, kdyby existovala izolovana lajna ve smeru k pozorovateli pred current lajnou
						pointRay.setA(pointImage);
						horizont.add(pointRay);

						currentRC = new RayCollision(point, sharpestLine);
					}
				}

			}
		}

		ray = createRightEdgeRay();
		RayCollision lastRC = getFirstCollision(ray, lines, null);

		horizont.add(Line.constructFromTwoPoints(currentRC.point, lastRC.point));
		horizont.add(Line.constructFromTwoPoints(lastRC.point, observerPoint));

		return horizont;
	}
}
