package net.trdlo.zelda.notiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author chleboir
 */
public class GeometryUtils {

	private static final int RAYTRACE_MAX_DEPTH = 64;

	public static Point iPOfSegmentAndRay(Line segment, Line ray) {
		assert segment != null;
		assert ray != null;

		Point iP = ray.intersectPoint(segment);
		if (iP != null) {
			if (segment.isPointOnSegment(iP) && ray.isPointOnRay(iP)) {
				return iP;
			}
		}
		//buď není na úsečce a rayi, nebo je úsečka s rayem rovnoběžná
		return null;
	}

	public static List<Line> constructRayPath(Line currentLine, Collection<WorldLine> collidableLines) {
		List<Line> returnList = new ArrayList<>(RAYTRACE_MAX_DEPTH);
		returnList.add(currentLine);

		List<PointAndDistanceAndLine> intersectPoints = new ArrayList<>();

		int i = 0;
		while (i++ < RAYTRACE_MAX_DEPTH) {
			//vektor paprsku A -> B
			double rayVX = currentLine.B.x - currentLine.A.x;
			double rayVY = currentLine.B.y - currentLine.A.y;

			intersectPoints.clear();

			for (WorldLine line : collidableLines) {
				Point intersectPoint = currentLine.intersectPoint(line);
				if (intersectPoint == null) {
					continue;
				}

				//vektor potenciálního zrcadla A -> B
				double mirrorVX = line.B.x - line.A.x;
				double mirrorVY = line.B.y - line.A.y;

				//vzdálenost průsečíku na polopřímce paprsku (jednotka je délka paprsku)
				double rayStartToIntersectDistance;
				if (Math.abs(rayVX) > Math.abs(rayVY)) {
					rayStartToIntersectDistance = (intersectPoint.x - currentLine.A.x) / rayVX;
				} else {
					rayStartToIntersectDistance = (intersectPoint.y - currentLine.A.y) / rayVY;
				}

				//vzdálenost průsečíku na od počátku zrcadla (jednotka je délka zrcadla)
				double mirrorStartToIntesectDistance;
				if (Math.abs(mirrorVX) > Math.abs(mirrorVY)) {
					mirrorStartToIntesectDistance = (intersectPoint.x - line.A.x) / mirrorVX;
				} else {
					mirrorStartToIntesectDistance = (intersectPoint.y - line.A.y) / mirrorVY;
				}

				//vzdálenost bodu A paprsku od průniku musí být kladná (průsečík na správné straně polopřímky)
				//vzdálenost bodu A zrcadla od průniku musí být v intervalu (0; 1) (průsečík je na úsečce vyjma krajních bodů)
				if (rayStartToIntersectDistance > 0.00001 && mirrorStartToIntesectDistance > 0.00001 && mirrorStartToIntesectDistance < 1) {
					intersectPoints.add(new PointAndDistanceAndLine(rayStartToIntersectDistance, intersectPoint, line));
				}
			}

			if (intersectPoints.isEmpty()) {
				break;
			}

			Collections.sort(intersectPoints);
			PointAndDistanceAndLine firstContact = intersectPoints.get(0);
			currentLine = currentLine.mirrorReflection(firstContact.line);
			returnList.add(currentLine);
		}
		for (int j = 0; j < returnList.size() - 1; j++) {
			returnList.get(j).setB(returnList.get(j + 1).A);
		}
		return returnList;
	}

	public static Line getPerpendicularLine(Line l, Point p) {
		return Line.constructFromPointAndNormal(p, l.b, -l.a);
	}
}

class PointAndDistanceAndLine implements Comparable<PointAndDistanceAndLine> {

	double dist;
	Point p;
	Line line;

	public PointAndDistanceAndLine(double dist, Point p, Line line) {
		this.dist = dist;
		this.p = p;
		this.line = line;
	}

	@Override
	public int compareTo(PointAndDistanceAndLine t) {
		return (dist - t.dist) > 0 ? 1 : -1;
	}
}
