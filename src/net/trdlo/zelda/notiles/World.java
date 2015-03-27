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
	Line ray;

	
	public World() {
		lines = new ArrayList<>();
//		lines.add(new Line(new Point(789, 150), new Point(900, 300)));
		Point.setLinesCollection(lines);
		
		Point X = new Point(200, 200);
		Line ray = new Line(X, new Point(300, 300));
		Line mirror = new Line(new Point(200, 400), new Point(600, 320));
		Line reflectedRay = ray.mirrorReflection(mirror);
		lines.add(ray);
		lines.add(mirror);
		lines.add(reflectedRay);
	}
	

	@Override
	public void update() {

	}

}
