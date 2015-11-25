package net.trdlo.zelda.guan;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import net.trdlo.zelda.NU;

public final class Point implements Selectable {

	public static final Pattern PAT_POINT = Pattern.compile("^\\s*Point\\s+(\\d+)\\s*\\[\\s*([-+]?\\d*\\.?\\d+)\\s*;\\s*([-+]?\\d*\\.?\\d+)\\s*\\](.*)\\z", Pattern.CASE_INSENSITIVE);
	public static final int DISPLAY_SIZE = 8;
	public static final int HIGHLIGHT_MAX_DISTANCE = 8;

	public static final Stroke DEFAULT_STROKE = new BasicStroke(1);
	public static final Color DEFAULT_COLOR = new Color(0, 192, 0);
	public static final Stroke SELECTION_STROKE = new BasicStroke(2);
	public static final Color SELECTION_COLOR = Color.YELLOW;
	public static final Font DESCRIPTION_FONT = new Font("Monospaced", Font.BOLD, 12);
	public static final Color DESCRIPTION_COLOR = Color.LIGHT_GRAY;

	public static Point fromAWTPoint(java.awt.Point awtPoint) {
		return new Point(awtPoint.x, awtPoint.y);
	}

	protected double x, y;
	protected String description;

	public Set<Line> connectedLines = null;

	public Point(double x, double y, String description) {
		this.x = x;
		this.y = y;
		this.description = (description != null ? description : "");
	}

	public Point(double x, double y) {
		this(x, y, "");
	}

	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
		this.description = p.description;
	}

	public Point() {
		this(0, 0, "");
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

	public void moveBy(Point d) {
		x += d.x;
		y += d.y;
		notifyChange();
	}

	public Point diff(Point d) {
		return new Point(x - d.x, y - d.y);
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
		if (connectedLines != null && !connectedLines.remove(line)) {
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

	public double getDistanceSquare(double px, double py) {
		return NU.sqr(px - x) + NU.sqr(py - y);
	}

	public double getDistance(double px, double py) {
		return Math.sqrt(getDistanceSquare(px, py));
	}

	public double getDistanceSquare(Point p) {
		return NU.sqr(p.x - x) + NU.sqr(p.y - y);
	}

	public double getDistance(Point p) {
		return Math.sqrt(getDistanceSquare(p));
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

	public void roundToGrid(int gridStep) {
		x = NU.roundToMultipleOf(x, gridStep);
		y = NU.roundToMultipleOf(y, gridStep);
	}

}
