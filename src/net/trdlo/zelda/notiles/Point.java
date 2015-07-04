
package net.trdlo.zelda.notiles;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class Point {
	private static Collection<Line> lineInsertCollection;

	//TODO - logiku přesunout do view (tam bude kolekce výběru - množina)
	
	protected double x, y;
	private String description;
	
	public Set<Line> changeListeners;
	boolean ignoreUnregisters = false;
	
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
		changeListeners = new HashSet<>(); 
	}
	
	public Point(java.awt.Point awtPoint) {
		this(awtPoint.x, awtPoint.y);
	}
	
	public java.awt.Point getJavaPoint() {
		return new java.awt.Point((int)x, (int)y);
	}
	
	public static void setLinesCollection(Collection<Line> lines) {
		lineInsertCollection = lines;
	}
	
	public Point lineTo(Point B) {
		lineInsertCollection.add(Line.constructFromTwoPoints(this, B));
		return B;
	}
	
	public Point lineTo(double x, double y) {
		return lineTo(new Point(x,y));
	}
	
//	@Override
//	public boolean equals(Object o) {
//		if (o instanceof Point) {
//			Point p = (Point)o;
//			return p.x == x && p.y == y;
//		}
//		else
//			return false;
//	}
//
//	@Override
//	public int hashCode() {
//		return (int)(255.0 * x + 65535.0 * y);
//	}
	
	@Override
	public String toString() {
		return "[" + x + "; " + y + "] \"" + (description != null ? description : "") + "\"";
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
		notifyListeners();
	}

	public void setY(double y) {
		this.y = y;
		notifyListeners();
	}
	
	public void setXY(double x, double y) {
		this.x = x;
		this.y = y;
		notifyListeners();
	}
	
	public String getDescription() {
		return description;
	}
	

	public void setDescription(String description) {
		this.description = description;
	}

	
	public void addChangeListener(Line line) {
		changeListeners.add(line);
	}
	
	public void removeChangeListener(Line line) {
		if (ignoreUnregisters)
			return;
		
		if (!changeListeners.remove(line))
			throw new RuntimeException("Line was not a listener of this point!");
	}
	
	private void notifyListeners() {
		for(Line line : changeListeners) {
			line.refreshCoefs();
		}
	}
	
	public void setIgnoreUnregisters() {
		ignoreUnregisters = true;
	}
	
}
