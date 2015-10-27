package net.trdlo.zelda.guan;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

class World {

	final Set<Point> points;
	final Set<SmartLine> lines;

	Rectangle bounds;

	World() {
		points = new HashSet<>();
		this.lines = new HashSet<SmartLine>() {
			@Override
			public boolean remove(Object o) {
				if (o instanceof SmartLine) {
					((SmartLine) o).disconnect();
				}
				return super.remove(o);
			}
		};

		bounds = new Rectangle(-1000, -1000, 2000, 2000);
	}

	public final void loadFromFile(String fileName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		Map<Integer, Point> pointIdMap = new HashMap<>();
		while ((line = br.readLine()) != null) {
			Matcher m;
			if ((m = Point.PAT_POINT.matcher(line)).matches()) {
				Point newPoint = new Point(Double.valueOf(m.group(2)), Double.valueOf(m.group(3)), m.group(4));
				Integer pointId = Integer.valueOf(m.group(1));
				pointIdMap.put(pointId, newPoint);
				points.add(newPoint);
			} else if ((m = Line.PAT_LINE.matcher(line)).matches()) {
				Point A = pointIdMap.get(Integer.valueOf(m.group(1)));
				if (A == null) {
					throw new Exception("Point index " + m.group(1) + "not found. Can't load world!");
				}
				Point B = pointIdMap.get(Integer.valueOf(m.group(2)));
				if (B == null) {
					throw new Exception("Point index " + m.group(2) + "not found. Can't load world!");
				}
				lines.add(SmartLine.constructFromTwoPoints(A, B));
			}

		}
	}

	void update() {

	}

}
