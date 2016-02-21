package net.trdlo.zelda.notiles;

import java.util.List;

public class TestRay {

	private static void testRay1() {

	}

	public static void main(String[] args) {
		double fOV = Math.PI / 2;
		double orientation = 0;
		double sight = 1000;
		World world = World.createTestWorld();
		NoTilesGame game = new NoTilesGame(world);
		Line vTB = ViewUtils.getViewTriangleBack(sight, fOV, orientation, world.hero);

		System.out.println(vTB.A.toString());
		System.out.println(vTB.B.toString());
		List<Line> viewPolygon;
		ViewUtils.evaluatePointsByAngle(world.points, world.hero, orientation, fOV);
		viewPolygon = ViewUtils.getViewPolygon(fOV, orientation, sight, world.hero, world.lines, world.points);

		for (Line l : viewPolygon) {
			System.out.println("[" + l.A.x + ";" + l.A.y + "]");
			System.out.println("[" + l.B.x + ";" + l.B.y + "]");
		}
	}
}
