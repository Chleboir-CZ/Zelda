package net.trdlo.zelda.notiles;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.trdlo.zelda.ZWorld;

/**
 *
 * @author chleboir
 */
public class World extends ZWorld {

	private class UnregisteringLineList extends ArrayList<Line> {

		@Override
		public boolean remove(Object o) {
			if (o instanceof Line) {
				((Line) o).unregister();
			}
			return super.remove(o);
		}
	}

	List<Line> lines;
	List<Point> points;

	//dočasný pokusný paprsek, časem pude do kšá
	Line ray;

	public World() {
		lines = new UnregisteringLineList();
		points = new ArrayList<>();
//		lines.add(new Line(new Point(789, 150), new Point(900, 300)));
		Point.setLinesCollection(lines);

		points.add(new Point(200, 200));
		points.add(new Point(200, 390));
		points.add(new Point(407, 400));
		points.get(0).lineTo(points.get(1)).lineTo(points.get(2));

		ray = Line.constructFromTwoPoints(new Point(500, 400), new Point(180, 100));
	}

	@Override
	public void update() {
		//independentPoints.get(0).y += 1;
	}

	public Point getPointAt(int x, int y) {
		for (Point p : this.points) {
			if (Math.abs(p.x - x) < View.POINT_DISPLAY_SIZE && Math.abs(p.y - y) < View.POINT_DISPLAY_SIZE) {
				return p;
			}
		}
		return null;
	}

	/**
	 * TODO: zobecnit na 4úhelník! Bude potřeba, pokud bude View perspektiva
	 * Vrátí kolekci bodů, která je uvnitř obdélníku zadaného dvěma protilehlými rohy
	 * @param A		jeden roh
	 * @param B		druhý roh
	 * @return		kolekce bodů světa, které jsou uvnitř obdélníku
	 */
	public Collection<Point> pointsInRect(Point A, Point B) {
		Collection<Point> pointsInRect = new ArrayList<>();

		Rectangle rect = new Rectangle(A.getJavaPoint());
		rect.add(B.getJavaPoint());

		for (Point p : points) {
			if (rect.contains(p.getJavaPoint())) {
				pointsInRect.add(p);
			}
		}
		return pointsInRect;
	}

	/**
	 * Odebere bod ze světa a dle jeho seznamu listenerů i napojené lajny
	 * @param point		bod k odebrání
	 */
	public void removePoint(Point point) {
		point.setIgnoreUnregisters();
		for (Line l : point.changeListeners) {
			lines.remove(l);
		}
		points.remove(point);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Point p : points) {
			sb.append(p.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}

