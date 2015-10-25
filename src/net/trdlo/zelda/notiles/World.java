package net.trdlo.zelda.notiles;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 *
 * @author chleboir
 */
public class World {

	private class UnregisteringLineList extends ArrayList<WorldLine> {

		@Override
		public boolean remove(Object o) {
			if (o instanceof WorldLine) {
				((WorldLine) o).unregister();
			}
			return super.remove(o);
		}
	}

	List<WorldLine> lines;
	List<Point> points;

	public double worldSizeX;
	public double worldSizeY;

	//dočasný pokusný paprsek, časem pude do kšá
	WorldLine ray;
	Point hero, h2, h3;

	private World() {
		lines = new UnregisteringLineList();
		points = new ArrayList<>();
		Point.setWorldLinesCollection(lines);

//		ray = WorldLine.constructFromTwoPoints(new Point(500, 400), getPointAt(200, 200));
	}

	public static World createTestWorld() {
		World world = new World();
		Point p = new Point(500, 800);
		Point q = new Point(200, 200);
		Point r = new Point(800, 200);
		world.hero = p;

		world.points.add(p);
		world.points.add(q);
		world.points.add(r);
		world.randomPointGenerator(20, q, new Point(p.x - q.x, p.y - q.y), new Point(r.x - q.x, r.y - q.y));
		world.ray = WorldLine.constructFromTwoPoints(new Point(500, 400), new Point(200, 200));
		world.points.get(0).worldLineTo(world.points.get(1)).worldLineTo(world.points.get(2)).worldLineTo(world.points.get(0));
		return world;
	}

	/*public static World loadFromFile(File file, boolean compress) throws ZException {
	 BufferedReader reader = getReader(file, compress);

	 String inputLine;
	 World world = new World();
	 Pattern pointPattern = Pattern.compile("^\\s*point\\s*(\\d+)\\s*\\[\\s*([-+]?\\d*\\.?\\d+)\\s*;\\s*([-+]?\\d*\\.?\\d+)\\s*\\]\\s*(.*)$", Pattern.CASE_INSENSITIVE);
	 Pattern linePattern = Pattern.compile("^\\s*line\\s*(\\d+)\\s*(\\d+)\\s*$", Pattern.CASE_INSENSITIVE);
	 Map<Integer, Point> pointMap = new HashMap<>();
	 try {
	 while ((inputLine = reader.readLine()) != null) {
	 Matcher matcher;
	 if ((matcher = pointPattern.matcher(inputLine)).matches()) {
	 Point p = new Point(Double.valueOf(matcher.group(2)), Double.valueOf(matcher.group(3)), matcher.group(4));
	 pointMap.put(Integer.valueOf(matcher.group(1)), p);

	 world.points.add(p);
	 } else if ((matcher = linePattern.matcher(inputLine)).matches()) {
	 Point A = pointMap.get(Integer.valueOf(matcher.group(1)));
	 Point B = pointMap.get(Integer.valueOf(matcher.group(2)));

	 if (A == null || B == null) {
	 throw new ZException("Invalid input format.");
	 }
	 WorldLine line = WorldLine.constructFromTwoPoints(A, B);
	 world.lines.add(line);
	 }
	 }
	 } catch (IOException ex) {
	 throw new ZException("Could not load. IO error occured", ex);
	 }
	 return world;
	 }*/
	public Point getPointAt(int x, int y) {
		for (Point p : this.points) {
			if (Math.abs(p.x - x) < NoTilesGame.POINT_DISPLAY_SIZE && Math.abs(p.y - y) < NoTilesGame.POINT_DISPLAY_SIZE) {
				return p;
			}
		}
		return null;
	}

	/**
	 * TODO: zobecnit na 4úhelník! Bude potřeba, pokud bude NoTilesGame perspektiva Vrátí kolekci bodů, která je uvnitř obdélníku zadaného dvěma protilehlými rohy
	 *
	 * @param A	jeden roh
	 * @param B	druhý roh
	 * @return	kolekce bodů světa, které jsou uvnitř obdélníku
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
	 *
	 * @param point	bod k odebrání
	 */
	public void removePoint(Point point) {
		point.setIgnoreUnregisters();
		for (WorldLine l : point.changeListeners) {
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

		for (WorldLine l : lines) {
			sb.append("WorldLine ");
			sb.append(points.indexOf(l.A)).append(" ").append(points.indexOf(l.B));
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
	/*@Override
	 protected void saveToWriter(BufferedWriter writer) throws ZException {
	 try {
	 int i = 0;
	 for (Point p : points) {
	 p.saveToWriter(writer, i++);
	 writer.write("\n");
	 }

	 for (WorldLine l : lines) {
	 writer.write("WorldLine ");
	 writer.write(points.indexOf(l.A) + " " + points.indexOf(l.B));
	 writer.write("\n");
	 }
	 } catch (IOException ex) {
	 throw new ZException("An IO exception occured.", ex);
	 }
	 }*/
	public void randomPointGenerator(int number, Point main, Point v, Point u) {
		Random r = new Random();
		List<Point> pointsList = new ArrayList<>();

		for (int i = 0; i < number; i++) {
			double r1, r2;
			do {
				r1 = r.nextDouble();
				r2 = r.nextDouble();
			} while (r1 + r2 > 1);
			double x = main.x + r1 * v.x + r2 * u.x;
			double y = main.y + r1 * v.y + r2 * u.y;
			Point p = new Point(x, y);
			pointsList.add(p);
			if (pointsList.size() >= 2) {
				p.worldLineTo(pointsList.get(pointsList.size() - 2));
			}
		}
		points.addAll(pointsList);
	}
}
