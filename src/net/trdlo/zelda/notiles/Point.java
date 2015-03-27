package net.trdlo.zelda.notiles;

import java.util.Collection;



public class Point {
	
	protected double x, y;
	private static Collection<Line> linesCollection;
	
	Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public static void setLinesCollection(Collection<Line> lines) {
		linesCollection = lines;
	}
	
	public Point lineTo(Point B) {
		linesCollection.add(new Line(this, B));
		return B;
	}
	
	public Point lineTo(double x, double y) {
		return lineTo(new Point(x,y));
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Point) {
			Point p = (Point)o;
			return p.x == x && p.y == y;
		}
		else
			return false;
	}

	@Override
	public int hashCode() {
		return (int)(255.0 * x + 65535.0 * y);
	}
	
	@Override
	public String toString() {
		return "[" + x + "; " + y + "]";
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double x) {
		//TODO inform listeners
		this.x = x;		
	}

	public void setY(double y) {
		//TODO inform listeners
		this.y = y;
	}
}
