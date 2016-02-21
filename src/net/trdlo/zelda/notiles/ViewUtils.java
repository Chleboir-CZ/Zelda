/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda.notiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import net.trdlo.zelda.NU;

/**
 *
 * @author chleboir
 */
public class ViewUtils {

	public static List<Line> getViewPolygon(double fOV, double orientation, double sight, Point heroPos, List<WorldLine> lineColl, Collection<Point> pointColl) {
		Line currentRay; //momentální paprsek
		Line currentLine; // momentální úsečka, po které jdeme
		List<Line> viewPolygon = new ArrayList<>();

		//dostat trojúhelník, kterým se zjednoduší výseč kruhu
		Line viewTriangleBack = ViewUtils.getViewTriangleBack(sight, fOV, orientation, heroPos);
		//Přidat zadní stranu trojúhelníku do kolekce line
		WorldLine helperBackLine = WorldLine.constructFromTwoPoints(viewTriangleBack.A, viewTriangleBack.B);
		lineColl.add(helperBackLine);
		pointColl.add(helperBackLine.A);
		pointColl.add(helperBackLine.B);
//
//		//Srovnat body podle úhlu
		SortedSet<Point> sortedPointSet = ViewUtils.sortPointsByAngle(pointColl, heroPos, orientation, fOV);
//		//Provést první krok - vyslat paprsek po levé straně a čekat, až do něčeho narazí.
		currentRay = Line.constructFromTwoPoints(heroPos, viewTriangleBack.A);
		SortedSet<IntersectInfo> sortedIntersectSet = getSortedRayIntersects(currentRay, lineColl);
		if (sortedIntersectSet.isEmpty()) {
			viewPolygon.add(Line.constructFromTwoPoints(heroPos, helperBackLine.A));
			currentLine = helperBackLine;
		} else {
//		//přidat první hranu do kolekce
			viewPolygon.add(Line.constructFromTwoPoints(heroPos, sortedIntersectSet.first().p));
//		//Dostaneme první currentLine
			currentLine = sortedIntersectSet.first().line;
		}
//		//iterovat skrze body

		for (Point p : sortedPointSet) {
			//zkontrolovat, jesli není bod za hranicí.
			if (p.tempAngle >= (fOV)) {
//				Point preEndPoint = GeometryUtils.iPOfSegmentAndRay(currentLine, Line.constructFromTwoPoints(heroPos, viewTriangleBack.B));
				Point preEndPoint = currentLine.intersectPoint(Line.constructFromTwoPoints(heroPos, viewTriangleBack.B));
				assert preEndPoint != null;
				viewPolygon.add(Line.constructFromTwoPoints(viewPolygon.get(viewPolygon.size() - 1).B, preEndPoint));
				viewPolygon.add(Line.constructFromTwoPoints(preEndPoint, heroPos));
				break;
			}
			if (p.tempAngle < 0) {
				continue;
			}
			//vystřelit ray skrze momentální bod
			currentRay = Line.constructFromTwoPoints(heroPos, p);
			//Dostat intersecty s úsečkami
			sortedIntersectSet = getSortedRayIntersects(currentRay, lineColl);
			//Dostat intersect s currentLine pro porovnání
			Point currentLineIP = GeometryUtils.iPOfSegmentAndRay(currentLine, currentRay);
//			assert currentLineIP != null;
			if (currentLineIP != null) {
				IntersectInfo currentLineIntersect = new IntersectInfo(getDistFromIntersect(currentLineIP, currentRay), currentLineIP, currentLine);

				//Pokud došlo k intersectu na currentLine
				if (sortedIntersectSet.first().line == currentLine) {
					if (p == currentLine.B || p == currentLine.A) {
						//PODEZŘELÉ
						Line nextLine = Line.constructFromTwoPoints(viewPolygon.get(viewPolygon.size() - 1).B, p);
						viewPolygon.add(nextLine); //Musíme přidat currentLine od bodu, kdy se na ní dostaneme
						Line lineToContinue = getLineToContinue(currentRay, p.changeListeners);
						if (lineToContinue == null) {
							for (IntersectInfo ii : sortedIntersectSet) {
								//nenašli jsme line vpravo, iterujeme tak dlouho, až nenarazíme na line, která není s bodem spojena

								if (!p.changeListeners.contains((WorldLine) ii.line)) {
									currentLine = ii.line;
									viewPolygon.add(Line.constructFromTwoPoints(currentLineIP, ii.p));
								}
							}
						} else {
							//našli jsme line navázanou na momentální bod, která vede vpravo
							currentLine = lineToContinue;
						}
					}
				} else if (sortedIntersectSet.first().dist < currentLineIntersect.dist) {
					Line lineToContinue = getLineToContinue(currentRay, p.changeListeners);
					assert lineToContinue != null;
					//Přidat náležité úsečky
					viewPolygon.add(Line.constructFromTwoPoints(currentLine.A, currentLineIP));
					viewPolygon.add(Line.constructFromTwoPoints(currentLineIP, p));
					currentLine = lineToContinue;
				}
			} else {
				//System.out.println("" + p);
			}
		}
		//Odebrat zadní hranu trojúhelníku
		lineColl.remove(helperBackLine);
		pointColl.remove(helperBackLine.A);
		pointColl.remove(helperBackLine.B);
		return viewPolygon;
	}

	public static List<Line> getViewPolygon2(double fOV, double orientation, double sight, Point heroPos, List<WorldLine> lineColl, Collection<Point> pointColl) {
		//INIT - vypálit ray po levém okraji a získat první currentLine, srovnat body atp
		Line currentRay; //momentální paprsek
		Line currentLine; // momentální úsečka, po které jdeme
		List<Line> viewPolygon = new ArrayList<>();

		//dostat trojúhelník, kterým se zjednoduší výseč kruhu
		Line viewTriangleBack = ViewUtils.getViewTriangleBack(sight, fOV, orientation, heroPos);
		//Přidat zadní stranu trojúhelníku do kolekce line
		WorldLine helperBackLine = WorldLine.constructFromTwoPoints(viewTriangleBack.A, viewTriangleBack.B);
		lineColl.add(helperBackLine);
		pointColl.add(helperBackLine.A);
		pointColl.add(helperBackLine.B);
//
//		//Srovnat body podle úhlu
		SortedSet<Point> sortedPointSet = ViewUtils.sortPointsByAngle(pointColl, heroPos, orientation, fOV);
//		//Provést první krok - vyslat paprsek po levé straně a čekat, až do něčeho narazí.
		currentRay = Line.constructFromTwoPoints(heroPos, viewTriangleBack.A);
		SortedSet<IntersectInfo> sortedIntersectSet = getSortedRayIntersects(currentRay, lineColl);
		if (sortedIntersectSet.isEmpty()) {
			viewPolygon.add(Line.constructFromTwoPoints(heroPos, helperBackLine.A));
			currentLine = helperBackLine;
		} else {
//		//přidat první hranu do kolekce
			viewPolygon.add(Line.constructFromTwoPoints(heroPos, sortedIntersectSet.first().p));
//		//Dostaneme první currentLine
			currentLine = sortedIntersectSet.first().line;
		}
		//ITEROVAT skrze body

		for (Point p : sortedPointSet) {
			//základní podmínky, jestli není za fOV a jestli není před začátkem
			if (p.tempAngle > (fOV)) {
//				Point preEndPoint = GeometryUtils.iPOfSegmentAndRay(currentLine, Line.constructFromTwoPoints(heroPos, viewTriangleBack.B));
				Point preEndPoint = currentLine.intersectPoint(Line.constructFromTwoPoints(heroPos, viewTriangleBack.B));
				assert preEndPoint != null;
				viewPolygon.add(Line.constructFromTwoPoints(viewPolygon.get(viewPolygon.size() - 1).B, preEndPoint));
				viewPolygon.add(Line.constructFromTwoPoints(preEndPoint, heroPos));
				break;
			}
			if (p.tempAngle < 0) {
				continue;
			}
			//NOVÝ RAY
			currentRay = Line.constructFromTwoPoints(heroPos, p);
			//POKUD JE KONCOVÝM BODEM currentLine
			//ZANOŘOVÁNÍ
			if (p == currentLine.A || p == currentLine.B) {
				Line nextLine = Line.constructFromTwoPoints(viewPolygon.get(viewPolygon.size() - 1).B, p);
				viewPolygon.add(nextLine); //Musíme přidat currentLine od bodu, kdy se na ní dostaneme
				//Sehnat Line, po které by se dalo pokračovat
				Line lineToContinue = getLineToContinue(currentRay, p.changeListeners);

				if (lineToContinue == null) {
					//Průsečíky raye
					sortedIntersectSet = getSortedRayIntersects(currentRay, lineColl);
					for (IntersectInfo ii : sortedIntersectSet) {
						//nenašli jsme line vpravo, iterujeme tak dlouho, až nenarazíme na line, která není s bodem spojena

						if (!p.changeListeners.contains((WorldLine) ii.line)) {
							currentLine = ii.line;
							viewPolygon.add(Line.constructFromTwoPoints(p, ii.p));
						}
					}
				} else {
					//našli jsme line navázanou na momentální bod, která vede vpravo
					currentLine = lineToContinue;
				}
			} else {
				//VYNOŘOVÁNÍ
				//Potřebujeme průsečík s currentLine pro vynoření
//				Point currentLineIP = GeometryUtils.iPOfSegmentAndRay(currentLine, currentRay);
//				assert currentLineIP != null;
				Point currentLineIP = currentLine.intersectPoint(currentRay);
				//také potřebujeme všechny průsečíky raye
				sortedIntersectSet = getSortedRayIntersects(currentRay, lineColl);
				if (sortedIntersectSet.first().line != currentLine) {
					Line nextLine = Line.constructFromTwoPoints(viewPolygon.get(viewPolygon.size() - 1).B, currentLineIP);
					viewPolygon.add(nextLine);
					viewPolygon.add(Line.constructFromTwoPoints(currentLineIP, sortedIntersectSet.first().p));
					currentLine = sortedIntersectSet.first().line;
				}
			}
		}
		lineColl.remove(helperBackLine);
		pointColl.remove(helperBackLine.A);
		pointColl.remove(helperBackLine.B);

		return viewPolygon;
	}

	public static SortedSet<Point> sortPointsByAngle(Collection<Point> pointColl, Point heroPos, double orientation, double fOV) {
		ViewUtils.evaluatePointsByAngle(pointColl, heroPos, orientation, fOV);
		SortedSet<Point> sortedPointSet = new TreeSet<>(new Comparator<Point>() {
			@Override
			public int compare(Point p, Point q) {
				double delta = p.tempAngle - q.tempAngle;
				return delta < 0 ? -1 : (delta > 0 ? 1 : p.hashCode() - q.hashCode());
			}
		});
		sortedPointSet.addAll(pointColl);
		return sortedPointSet;
	}

	public static void evaluatePointsByAngle(Collection<Point> pointColl, Point heroPos, double orientation, double fOV) {
		for (Point p : pointColl) {
			p.tempAngle = NU.normalizeAngle(Math.atan2(p.y - heroPos.y, p.x - heroPos.x) - orientation);
			p.setDescription(String.format("%.0f", 180 / Math.PI * p.tempAngle));
		}
	}

	public static Line getViewTriangleBack(double sight, double fOV, double orientation, Point heroPos) {
		double cosinus = Math.cos(fOV / 2);
		Point leftPoint = new Point(heroPos.x + sight * (1 / cosinus) * Math.cos(orientation), heroPos.y + sight * (1 / cosinus) * Math.sin(orientation));
//		double mrd = heroPos.x + sight * /*(1 / cosinus)*/ Math.cos(orientation);
//		double mrd2 = heroPos.y + sight /* (1 / cosinus)*/ * Math.sin(orientation + fOV);

		Point rightPoint = new Point(heroPos.x + sight * (1 / cosinus) * Math.cos(orientation + (fOV)), heroPos.y + sight * (1 / cosinus) * Math.sin(orientation + (fOV)));
		Line viewBackLine = Line.constructFromTwoPoints(leftPoint, rightPoint);
		return viewBackLine;
	}
	/*
	 static PointAndDistanceAndLine getValidPointAndDistanceAndLine(Point iP, Line segment, Line ray) {
	 double rayVX = ray.B.x - ray.A.x;
	 double rayVY = ray.B.y - ray.A.y;
	 if (iP != null) {
	 if (rayVX > rayVY) {
	 return new PointAndDistanceAndLine((iP.x - ray.A.x) / rayVX, iP, segment);
	 } else {
	 return new PointAndDistanceAndLine((iP.y - ray.A.y) / rayVY, iP, segment);
	 }
	 }
	 return null;
	 }
	 */
	/*
	 public static IntersectInfo getFirstIntersect(List<WorldLine> lineColl, Line ray, Line viewTriangleBack) {

	 Point iP;
	 List<PointAndDistanceAndLine> intersectList = new ArrayList<>();

	 for (Line l : lineColl) {
	 iP = GeometryUtils.iPOfSegmentAndRay(l, ray);
	 intersectList.add(getValidPointAndDistanceAndLine(iP, l, ray));
	 }
	 iP = GeometryUtils.iPOfSegmentAndRay(viewTriangleBack, ray);
	 intersectList.add(getValidPointAndDistanceAndLine(iP, viewTriangleBack, ray));

	 Collections.sort(intersectList);
	 if (!intersectList.isEmpty()) {
	 Point touchPoint = null;
	 for (PointAndDistanceAndLine intersect : intersectList) {
	 int smallerThanZero = 0;
	 int biggerThanZero = 0;
	 for (Line l : intersect.p.changeListeners) {
	 double n;
	 double vx = (intersect.line.B.x - intersect.line.A.x);
	 double vy = (intersect.line.B.y - intersect.line.A.y);
	 if (l.A == intersect.p) {
	 n = ray.a * l.B.x + ray.b * l.B.y + ray.c;
	 } else {
	 n = ray.a * l.A.x + ray.b * l.A.y + ray.c;
	 }
	 if (n < 0) {
	 smallerThanZero++;
	 } else if (n > 0) {
	 biggerThanZero++;
	 }
	 }
	 if (smallerThanZero == 0 || biggerThanZero == 0) {
	 if (touchPoint == null) {
	 touchPoint = intersect.p;
	 }
	 } else {
	 IntersectInfo pADALAT = new IntersectInfo(intersect.dist, intersect.p, intersect.line);
	 pADALAT.touchPoint = touchPoint;
	 return pADALAT;
	 }
	 }
	 }
	 return null;
	 }

	 public static IntersectInfo getFirstIntersect() {
		
	 return null;
	 }
	 */

	public static SortedSet<IntersectInfo> getSortedRayIntersects(Line ray, Collection<WorldLine> lineColl) {
		SortedSet<IntersectInfo> sortedPointSet = new TreeSet<>(new Comparator<IntersectInfo>() {
			@Override
			public int compare(IntersectInfo i, IntersectInfo j) {
				double distance = i.dist - j.dist;
				return distance > 0 ? 1 : distance < 0 ? -1 : 0;
			}
		});
		sortedPointSet.addAll(getRayIntersects(ray, lineColl));
		return sortedPointSet;
	}

	public static Collection<IntersectInfo> getRayIntersects(Line ray, Collection<WorldLine> lineColl) {
		Collection<IntersectInfo> intersectColl = new HashSet<>();
		for (Line l : lineColl) {
			IntersectInfo intersect;
			Point iP = GeometryUtils.iPOfSegmentAndRay(l, ray);
			if (iP != null) {
				intersect = new IntersectInfo(getDistFromIntersect(iP, ray), iP, l);
				intersectColl.add(intersect);
			}
		}
		return intersectColl;
	}

	public static double getDistFromIntersect(Point iP, Line ray) {
		double vx = ray.B.x - ray.A.x;
		double vy = ray.B.y - ray.A.y;
		if (Math.abs(vx) > Math.abs(vy)) {
			return (iP.x - ray.A.x) / vx;
		} else {
			return (iP.y - ray.A.y) / vy;
		}
	}

//	public static double getDistFromIntersect(Line ray, Point iP) {
//		double vx = ray.B.x - ray.A.x;
//		double vy = ray.B.y - ray.A.y;
//		if (vx > vy) {
//			return (ray.A.x - iP.x) / vx;
//		} else {
//			return (ray.A.y - iP.y) / vy;
//		}
//	}
	//TODO Přejmenovat
	//funkce používaná, když narazíme přímo na bodu a nevíme, po které Line pokračovat
	public static Line getLinetoContinue(IntersectInfo intersect, Point currentPoint) {

		return null;
	}

	public static double getVectorCrossProductZ(Line v1, Line v2) {
		double v1x = v1.B.x - v1.A.x;
		double v1y = v1.B.y - v1.A.y;
		double v2x = v2.B.x - v2.A.x;
		double v2y = v2.B.y - v2.A.y;

		double lengthV1 = Math.sqrt(v1x * v1x + v1y * v1y);
		double lengthV2 = Math.sqrt(v2x * v2x + v2y * v2y);
		v1x = v1x / lengthV1;
		v1y = v1y / lengthV1;
		v2x = v2x / lengthV2;
		v2y = v2y / lengthV2;

		return (v1x * v2y) - (v2x * v1y);
	}

	public static Line getLineToContinue(Line ray, Collection<WorldLine> changeListeners) {
		Collection<WorldLine> linesRightFromRay = new HashSet<>();
		for (WorldLine l : changeListeners) {
			double vCrossProduct = getVectorCrossProductZ(ray, l);
			if (vCrossProduct >= 0) {
				linesRightFromRay.add(l);
			}
		}
		Line perpendicular = GeometryUtils.getPerpendicularLine(ray, ray.B);
		Line returnLine = null;
		Double returnLineCrossProduct = null;
		for (Line l : linesRightFromRay) {
			if (returnLine != null) {
				if (returnLineCrossProduct < getVectorCrossProductZ(perpendicular, l)) {
					returnLineCrossProduct = getVectorCrossProductZ(perpendicular, l);
					returnLine = l;
				}
			} else {
				returnLine = l;
				returnLineCrossProduct = getVectorCrossProductZ(perpendicular, l);
			}
		}
		return returnLine;
	}
}
