package net.trdlo.zelda.guan;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Point implements Selectable {

	public static final Pattern PAT_POINT = Pattern.compile("^\\s*Point\\s+(\\d+)\\s*\\[\\s*([-+]?\\d*\\.?\\d+)\\s*;\\s*([-+]?\\d*\\.?\\d+)\\s*\\](.*)\\z", Pattern.CASE_INSENSITIVE);
	public static final int DISPLAY_SIZE = 8;

	public static Point fromAWTPoint(java.awt.Point awtPoint) {
		return new Point(awtPoint.x, awtPoint.y);
	}

	protected double x, y;
	protected String description;

	public Set<Line> connectedLines = null;

	public Point() {
		description = "";
	}

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
		this.description = "";
	}

	public Point(double x, double y, String description) {
		this.x = x;
		this.y = y;
		this.description = (description != null ? description : "");
	}

	public java.awt.Point getJavaPoint() {
		return new java.awt.Point((int) x, (int) y);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
		notifyChange();
	}

	public void setY(double y) {
		this.y = y;
		notifyChange();
	}

	public void setXY(double x, double y) {
		this.x = x;
		this.y = y;
		notifyChange();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void addConnectedLine(Line line) {
		if (connectedLines == null) {
			connectedLines = new HashSet<>();
		}
		connectedLines.add(line);
	}

	public void removeConnectedLine(Line line) {
		if (!connectedLines.remove(line)) {
			throw new RuntimeException("Line was not a listener of this point!");
		}
	}

	private void notifyChange() {
		if (connectedLines != null) {
			for (Line line : connectedLines) {
				line.refreshCoefs();
			}
		}
	}

	public String toStringSimple() {
		return "[" + x + ";" + y + "]";
	}

	@Override
	public String toString() {
		return "Point [" + x + ";" + y + "] " + description;
	}

	public boolean inRect(double x1, double y1, double x2, double y2) {
		return x >= Math.min(x1, x2) && x < Math.max(x1, x2) && y >= Math.min(y1, y2) && y < Math.max(y1, y2);
	}
}
