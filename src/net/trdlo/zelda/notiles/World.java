/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda.notiles;

import java.util.ArrayList;
import java.util.Collection;
import net.trdlo.zelda.ZWorld;

/**
 *
 * @author chleboir
 */
public class World extends ZWorld {

	Collection<Line> lines;
	Collection<Line> collidableLines;
	Collection<IndependentPoint> independentPoints;
	Line ray;

	public World() {
		lines = new ArrayList<>();
		collidableLines = new ArrayList<>();
		independentPoints = new ArrayList<>();
//		lines.add(new Line(new Point(789, 150), new Point(900, 300)));
		Point.setLinesCollection(lines);

		Point X = new Point(200, 200);
		X.lineTo(200, 390).lineTo(407, 400);
		Line ray = new Line(new Point(500, 400), new Point(180, 100));
		lines.addAll(ray.rayTraceEffect(lines));
//		Line ray = new Line(X, new Point(300, 300));
//		Line mirror = new Line(new Point(200, 400), new Point(600, 320));
//		Line reflectedRay = ray.mirrorReflection(mirror);
//		lines.add(ray);
//		lines.add(mirror);
//		lines.add(reflectedRay);

	}

	@Override
	public void update() {

	}

	public Point getPointAt(int x, int y) {
		for(Point p : this.independentPoints) {
			if (Math.abs(p.x - x) < 4 && Math.abs(p.y - y) < 4) {
				return p;
			}
		}
		return null;
	}
	public Collection<Point> pointsInRect(Point A, Point B) {
		Collection<Point> pointsInRect = new ArrayList();
		for(Point p : independentPoints) {
			if(p.x > A.x && p.x < B.x && p.y > A.y && p.y < B.y) {
				pointsInRect.add(p);
			}
		}
		return pointsInRect;
	}
}
