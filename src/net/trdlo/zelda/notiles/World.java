package net.trdlo.zelda.notiles;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
		
		for (int i = 0; i < points.size(); i++) {
//			sb.append(i);
			sb.append(points.get(i).toString());
			sb.append("\n");
			i++;
		}
		
		for (Line l : lines) {
			sb.append("Line ");
			sb.append(points.indexOf(l.A) + " " + points.indexOf(l.B));
			sb.append("\n");
		}
		return sb.toString();
	}
	
//	public static World fromString(String s) {
//		World world = new World();
//		Pattern intPattern = Pattern.compile("");
//		Pattern descrPattern = Pattern.compile("\".*\"");
//		String[] stgArray = s.split("\n");
//		for(int i = 0; i < stgArray.length; i++) {
//			Matcher intMatcher = intPattern.matcher(stgArray[i]);
//			
////			String[] readLine = stgArray[i].split(" ");
////			if("Point".equals(readLine[0])) {
////				readLine[1] = readLine[1].replaceAll("\\D+", " ");
////				world.points.add(new Point(Integer.parseInt(readLine[1].split(" ")[0]), Integer.parseInt(readLine[1].split(" ")[1]), readLine[2]));
//		}
//	}
}

