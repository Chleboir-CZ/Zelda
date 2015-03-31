
package net.trdlo.zelda.notiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class Line {
	protected Point A, B;
	protected double a, b, c;
	
	public Line(Point A, Point B) {
		if (A.equals(B))
			throw new IllegalArgumentException("Two identical points don't form a line.");
		this.A = A;
		this.B = B;
		a = A.y - B.y;
		b = B.x - A.x;
		c = -a * A.x - b * A.y;
	}
	
	public Line(double a, double b, Point A) {
		B = new Point(A.x + b, A.y - a);
		this.A = A;
		this.a = a;
		this.b = b;
		c = -a * A.x - b * A.y;
	}
	
	private Line() {
		
	}
	
	public static Line constructLineFromPointAndNormal(Point A, double a, double b) {
		//return new Line(A, new Point(A.x + a, A.y + b));		
		Line l = new Line();
		l.a = a;
		l.b = b;
		l.A = A;
		l.B = new Point(A.x - b, A.y + a);
		l.c = -a * A.x - b * A.y;
		return l;
	}

	public static Line constructLineFromPointAndVector(Point A, double a, double b) {		
		//return new Line(A, new Point(A.x + b, A.y - a));		
		Line l = new Line();
		l.a = -b;
		l.b = a;
		l.A = A;
		l.B = new Point(A.x + a, A.y + b);
		l.c = b * A.x - a * A.y;
		return l;
	}

	public Point getA() {
		return A;
	}

	public Point getB() {
		return B;
	}
	
	public Point intersectPoint(Line line) {
		double denominator = (a*line.b - line.a*b);
		if(denominator == 0)
			return null;
		return new Point((b*line.c - c * line.b) / denominator, -(a*line.c - line.a * c) / denominator);
	}

	public double getAngle(Line line) {
		return Math.acos((a*line.a + b*line.b) / (Math.sqrt(a*a + b*b) * Math.sqrt(line.a*line.a + line.b*line.b)));
	}
	
	/**
	 * Spocita obraz teto primky v zrcadle line
	 * @param line zrcadlo
	 * @return odraz sebe pres line
	 */
	public Line mirrorReflection(Line line) {
		Point intersect = this.intersectPoint(line);
		Line lineNormal = constructLineFromPointAndVector(intersect, line.a, line.b);
		Line lineParalell = constructLineFromPointAndNormal(A, line.a, line.b);
		Point S = lineParalell.intersectPoint(lineNormal);
		Point reflectedA = new Point(2 * S.x - A.x, 2 * S.y - A.y);
		return new Line(intersect, reflectedA);
	}
	
	
	public List<Line> rayTraceEffect(Collection<Line> collidableLines) {
		Line currentLine = this;
		List<Line> returnList = new ArrayList<>();
		returnList.add(currentLine);
		
		int i = 0;
		while(i++ < 5000) {
			double vx = currentLine.B.x - currentLine.A.x;
			double vy = currentLine.B.y - currentLine.A.y;
			
			List<PointAndDistanceAndLine> intersectPoints = new ArrayList<>();
			for(Line line : collidableLines) {
				Point intersectPoint = currentLine.intersectPoint(line);
				double dist;
				if(Math.abs(vx) > Math.abs(vy)) {
					dist = (intersectPoint.x - currentLine.A.x) / vx;
				} else {
					dist = (intersectPoint.y - currentLine.A.y) / vy;
				}
				
				if(dist > 0.00001) {
					intersectPoints.add(new PointAndDistanceAndLine(dist, intersectPoint, line));
				}
			}
			Collections.sort(intersectPoints);
			if(intersectPoints.isEmpty())
				break;
			PointAndDistanceAndLine firstContact = intersectPoints.get(0);
			currentLine = currentLine.mirrorReflection(firstContact.line); 
			returnList.add(currentLine);
		}
		return returnList;
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
	
/*	public static Comparator<GameObjectInstance> zIndexComparator = new Comparator<GameObjectInstance>() {
		@Override
		public int compare(GameObjectInstance go1, GameObjectInstance go2) {
			return go1.getZIndex() - go2.getZIndex();
		}
	};*/

	@Override
	public int compareTo(PointAndDistanceAndLine t) {
		return (dist - t.dist) > 0 ? 1 : -1;
	}
}