package net.trdlo.zelda.notiles;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Point {

	private static Collection<WorldLine> lineInsertCollection;

	protected double x, y;
	private String description;

	public Set<WorldLine> changeListeners;
	boolean ignoreUnregisters = false;
	
	
	public Point(double x, double y, String description) {
		this.x = x;
		this.y = y;
		this.description = description;
		changeListeners = new HashSet<>(); 
	}
	
	public Point(double x, double y) {
		this(x, y, "");
	}	
	
	public static Point constructPointFromView(double x, double y, String description, NoTilesGame view) {
		return view.createPointAt(x, y, description);
	}

	public Point(java.awt.Point awtPoint) {
		this(awtPoint.x, awtPoint.y, "");
	}

	public java.awt.Point getJavaPoint() {
		return new java.awt.Point((int) x, (int) y);
	}

	public static void setWorldLinesCollection(Collection<WorldLine> lines) {
		lineInsertCollection = lines;
	}

	public Point worldLineTo(Point B) {
		lineInsertCollection.add(WorldLine.constructFromTwoPoints(this, B));
		return B;
	}

	public Point worldLineTo(double x, double y) {
		return Point.this.worldLineTo(new Point(x, y));
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
		return "Point [" + x + ";" + y + "]" + (description != null ? description : "");
	}

	/*public void saveToWriter(BufferedWriter writer, int saveId) throws ZException {
		try {
			writer.write("Point " + saveId + " [" + x + ";" + y + "]" + (description != null ? description : ""));
		} catch (IOException ex) {
			throw new ZException("An IO exception occured while writing point.", ex);
		}
	}*/
	
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

	public void addChangeListener(WorldLine line) {
		changeListeners.add(line);
	}

	public void removeChangeListener(WorldLine line) {
		if (!ignoreUnregisters && !changeListeners.remove(line)) {
			throw new RuntimeException("Line was not a listener of this point!");
		}
	}

	private void notifyListeners() {
		for (WorldLine line : changeListeners) {
			line.refreshCoefs();
		}
	}

	public void setIgnoreUnregisters() {
		ignoreUnregisters = true;
	}

}
