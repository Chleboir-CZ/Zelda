package net.trdlo.zelda.guan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.trdlo.zelda.NU;

public class TorchLight {

	/**
	 * Jednoduchá dvojice Line a Point použitá jako "current line/point" ve výpočtu horizontu
	 */
	private static class LinePoint {

		public final Point point;
		public final Line line;

		public LinePoint(Point point, Line line) {
			this.point = point;
			this.line = line;
		}
	}

	private static final int HORIZONT_SEGMENTS = 24;
	private static final double HORIZONT_ANGLE_LIMIT = 2 * Math.PI / HORIZONT_SEGMENTS;

	private final Set<Line> lines;
	private final Player observer;
	private final Point observerPoint;

	private final SortedSet<Point> radarPoints;

	public static List<Point> getTorchLightPointList(Set<Line> lines, Player observer) {
		TorchLight tl = new TorchLight(lines, observer);

		return tl.computeHorizont();
	}

	private TorchLight(Set<Line> lines, Player observer) {
		if (lines == null) {
			throw new NullPointerException("lines");
		}
		if (observer == null) {
			throw new NullPointerException("observer");
		}

		this.lines = lines;
		this.observer = observer;
		observerPoint = new Point(observer.x, observer.y);

		radarPoints = new TreeSet<>(new Comparator<Point>() {
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

	private boolean fullCircleView() {
		return observer.fov >= Math.PI * 2;
	}

	private void loadPoints() {
		double distSqrLimit = NU.sqr(observer.vDist);
		for (Line line : lines) {
			Point point = line.getA();
			point.tempDistSqr = point.getDistanceSquare(observerPoint);
			if (point.tempDistSqr <= distSqrLimit) {
				point.tempAngleByObserver(observer);
				if (Math.abs(point.tempAngle) <= observer.fov / 2) {
					//point.setDescription(String.format("%d°", NU.radToDeg(point.tempAngle)));
					radarPoints.add(point);
				}
			}// else {
			//	point.setDescription("");
			//}

			point = line.getB();
			point.tempDistSqr = point.getDistanceSquare(observerPoint);
			if (point.tempDistSqr <= distSqrLimit) {
				point.tempAngleByObserver(observer);
				if (Math.abs(point.tempAngle) <= observer.fov / 2) {
					//point.setDescription(String.format("%d°", NU.radToDeg(point.tempAngle)));
					radarPoints.add(point);
				}
			}// else {
			//	point.setDescription("");
			//}

			for (Point p : line.getSegmentCircleIntersection(observerPoint, observer.vDist)) {
				p.addConnectedLine(line);
				p.tempAngleByObserver(observer);
				if (Math.abs(p.tempAngle) <= observer.fov / 2) {
					p.tempDistSqr = observer.vDistSqr;
					//p.setDescription(String.format("%d°", NU.radToDeg(p.tempAngle)));
					radarPoints.add(p);
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
	 * Vytvoří dvojici úsečka+bod, který splňuje kritéria: nejbližší kolize v množině lajn, nebo nový bod na hranici
	 * horizontu (line pak zůstává null)
	 *
	 * @param ray	paprsek, na kterém se hledá
	 * @param lines	množina lajn pro hledání kolize
	 * @param ignored	bod, s nímž související lany se ignorují
	 * @return
	 */
	private LinePoint getFirstCollision(Line ray, Collection<Line> lines, Point ignored) {
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
			nearestPoint.tempAngleByObserver(observer);
			return new LinePoint(nearestPoint, nearestLine);
		} else {
			Point horizontPoint = ray.getPointAtDistanceFromA(observer.vDist);
			horizontPoint.tempDistSqr = observer.vDistSqr;
			horizontPoint.tempAngleByObserver(observer);
			return new LinePoint(horizontPoint, null);
		}
	}

	/**
	 * Vybere lajnu (nebo vrati null), ktera vede od raye v "leve" polorovine nejostreji zpet k pozorovateli
	 *
	 * @param ray	ray od pozorovatele na referencni bod
	 * @param ignoredLine	lajna, kterou ignorujeme
	 * @return
	 */
	private Line getSharpestLine(Line ray, Line ignoredLine, boolean allowRayCollinear) {
		Line sharpestLine = null;
		Point point = ray.B;
		double bestAngle = -Double.MAX_VALUE;
		if (point.connectedLines != null) {
			for (Line otherConnectedLine : point.connectedLines) {
				if (otherConnectedLine != ignoredLine) {
					Point otherPoint = otherConnectedLine.getOtherPoint(point);
					if (otherPoint != null) {
						if (ray.isInLeftHalfPane(otherPoint) || (allowRayCollinear && ray.contains(otherPoint) && ray.getPosition(point) > 0)) {
							double alpha = ray.getHalfNormalizedCosAlpha(otherPoint);
							if (alpha > bestAngle) {
								bestAngle = alpha;
								sharpestLine = otherConnectedLine;
							}
						}
					} else {
						//případ, kdy bod má navázanou lajnu, které není koncový, pak je jen jediná taková lajna
						return otherConnectedLine;
					}
				}
			}
		}
		return sharpestLine;
	}

	public List<Point> computeHorizont() {
		loadPoints();

		List<Point> horizont = new ArrayList<>();/* {
			private int i = 0;
			@Override
			public boolean add(Point e) {
				e.setDescription(String.valueOf(i++));
				return super.add(e);
			}
		};*/

		if (!fullCircleView()) {
			horizont.add(observerPoint);
		}

		LinePoint current = getFirstCollision(createLeftEdgeRay(), lines, null);
		current.point.tempAngle = -observer.fov / 2; //fix +3.14 possible by Atan2

		Point lastPoint = getFirstCollision(createRightEdgeRay(), lines, null).point;
		lastPoint.tempAngle = +observer.fov / 2; //fix -3.14 possible by Atan2

		radarPoints.add(lastPoint);

		//int pointCounter = 0;
		for (Point point : radarPoints) {
			//pointCounter++;

			if (current.line == null) {
				//pripad: neni current.line => chodime po obvodu (vnejsi kruznice)

				//pripad, kdy ex. dva identcko body na vnejsi kruznici X point-ray -> preskocit!
				//NEVZNIKA VYRESENIM ***: zanorenim na horizont podel "svisle" lajny
				//VZNIKA DALE: pokud kruznice protne vrchol (spojnici dvou usecek) a nadetekuje tak 2 body, jeden pro kazdou z usecek
				//vyjimka: 2 shodné body, ale jeden -pi a druhy +pi (pri pohledu 360° a neni nic na dohled, jde se z prvniho bodu primo na posledni)
				if (point.getDistanceSquare(current.point) < World.MINIMAL_DETECTABLE_DISTANCE && Math.abs(point.tempAngle - current.point.tempAngle) < Math.PI) {
					continue;
//					try {
//						Thread.sleep(1000 * 5);
//					} catch (InterruptedException ex) {
//
//					}
//					assert false : "Stále mohou existovat dva body na stejné souřadnici? Podařilo se následující: \n"
//						+ current.point.toString() + " #" + current.point.hashCode() + "); connected lines: " + (current.point.connectedLines != null ? current.point.connectedLines.toString() : "null") + "\n"
//						+ point.toString() + " #" + point.hashCode() + "); connected lines: " + (point.connectedLines != null ? point.connectedLines.toString() : "null");
				}

				//na konci krokovani obvodu se vynorime k bodu nebo narazime na bod na obvodu, skrz který vede lajna
				Line sharpestLine = getSharpestLine(Line.constructFromTwoPoints(observerPoint, point), null, false);
				//z tohoto bodu vede lajna doprava, vybereme tu nejlepší
				//nebo extrém: bod sice není jedináček, ale jediná lajna z něho vede "svisle" -> není sharp
				//výjimka z extrému: lastPoint
				if (sharpestLine == null && point != lastPoint) {
					//pak chceme další bod na řadě! Dokrokujeme to ke smysluplnému bodu
					continue;
				}

				double angleDiff = point.tempAngle - current.point.tempAngle;
				int segments = 1 + (int) (angleDiff / HORIZONT_ANGLE_LIMIT);
				int steps = segments + (point.tempDistSqr < observer.vDistSqr ? 1 : 0);

				double angle = observer.orientation + current.point.tempAngle;
				double angleStep = angleDiff / segments;

				horizont.add(current.point);
				for (int i = 1; i < steps; i++) {
					angle += angleStep;
					horizont.add(new Point(observer.x + Math.cos(angle) * observer.vDist, observer.y + Math.sin(angle) * observer.vDist));
				}

				current = new LinePoint(point, sharpestLine);

			} else if (point == lastPoint || (point.connectedLines != null && point.connectedLines.contains(current.line))) {
				//line není null, bod na radaru je její součástí (koncový nebo i uprostřed)

				horizont.add(current.point);

				if (current.line.hasEndPoint(point)) {
					//bod je koncovým lajny
					Line pointRay = Line.constructFromTwoPoints(observerPoint, point);
					Line sharpestLine = getSharpestLine(pointRay, current.line, true); //OK *** povolen i "svislý" krok

					if (sharpestLine == null) {
						//nic relevantního z něho nevede doprava => tečna, zanoříme a uvidíme
						horizont.add(point);
						//to mohlo vybrat lajnu a na ní bod, nebo bod na obvodu (current.line je null)
						current = getFirstCollision(pointRay, lines, point);
					} else {
						//sharpest lajna doprava je vybrana, pokracujeme po ni
						current = new LinePoint(point, sharpestLine);
					}
				} else {
					//bod je uprostřed lajny (lajna mizí za obvod)
					current = new LinePoint(point, null);
				}
			} else {
				//line není null, bod na radaru není její součástí (koncový ani uprostřed)

				//bod pred nebo za primkou
				double pointDistSq = observerPoint.getDistanceSquare(point);
				Line pointRay = Line.constructFromTwoPoints(observerPoint, point);
				//prunik paprsku (observer -> zkoumany bod) s aktualni useckou
				Point pointImage = pointRay.getIntersection(current.line);

				double curLineDistSq;
				if (pointImage != null) {
					//bezny pripad: prunik pointRay a current line urcuje bod, kdery je bud bliz nebo dal nez zkoumany point
					curLineDistSq = observerPoint.getDistanceSquare(pointImage);
				} else {
					//specialni pripad: current line je "svisla" => neni prunik. Vzdalenost current line je blizsi z koncovych bodu
					curLineDistSq = Math.min(observerPoint.getDistanceSquare(current.line.A), observerPoint.getDistanceSquare(current.line.B));
				}
				if (pointDistSq < curLineDistSq) {
					//bod je bliz => vynoreni (jinak nic, vezme se dalsi bod)

					//vybere se nejlepsi navazujici lajna (nej-sharp)
					Line sharpestLine = getSharpestLine(pointRay, null, false);
					if (sharpestLine != null) {
						//prida se pocatecni bod
						horizont.add(current.point);
						//a bod pocatku vynorovani
						horizont.add(pointImage);

						current = new LinePoint(point, sharpestLine);
					} else {
						//existuje "svisla" lajna s timto bodem pred current.lajnou
						//ignorovat, dalsi bod!
					}
				}

			}
		}

		horizont.add(current.point);
		return horizont;
	}
}
