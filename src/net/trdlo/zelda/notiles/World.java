/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
				((Line)o).unregister();
			}
			return super.remove(o);
		}
	}
	
//	private class LineDeletingPointList extends ArrayList<Point> {
//		@Override
//		public boolean remove(Object o) {
//			if(o instanceof Point) {
//				for(Line l : ((Point)o).changeListeners) {
//					
//				}
//			}
//		}
//	}
//	
	
	Collection<Line> lines;
	Collection<Line> collidableLines;
	List<Point> independentPoints;
	List<Point> selectedPoints;

	Line ray;

	public World() {
		lines = new UnregisteringLineList();
		collidableLines = new ArrayList<>();
		independentPoints = new ArrayList<>();
//		lines.add(new Line(new Point(789, 150), new Point(900, 300)));
		Point.setLinesCollection(lines);

		independentPoints.add(new Point(200, 200));
		independentPoints.add(new Point(200, 390));
		independentPoints.add(new Point(407, 400));
		independentPoints.get(0).lineTo(independentPoints.get(1)).lineTo(independentPoints.get(2));
		ray = new Line(new Point(500, 400), new Point(180, 100));
		//lines.addAll(ray.rayTraceEffect(lines));
//		Line ray = new Line(X, new Point(300, 300));
//		Line mirror = new Line(new Point(200, 400), new Point(600, 320));
//		Line reflectedRay = ray.mirrorReflection(mirror);
//		lines.add(ray);
//		lines.add(mirror);
//		lines.add(reflectedRay);

	}

	@Override
	public void update() {
		//independentPoints.get(0).y += 1;
	}

	public Point getPointAt(int x, int y) {
		for(Point p : this.independentPoints) {
			if (Math.abs(p.x - x) < 6 && Math.abs(p.y - y) < 6) {
				return p;
			}
		}
		return null;
	}
	
	public Collection<Point> pointsInRect(Point A, Point B) {
		Collection<Point> pointsInRect = new ArrayList<>();
		
		Rectangle rect = new Rectangle(A.getJavaPoint());
		rect.add(B.getJavaPoint());
		
		
		for(Point p : independentPoints) {
			//if(p.x > A.x && p.x < B.x && p.y > A.y && p.y < B.y) {
			if (rect.contains(p.getJavaPoint())) {
				pointsInRect.add(p);
			}
		}
		return pointsInRect;
	}
	
	public void delSelectedPoints() {
		boolean deleted;
		do {
			deleted = false;
			for(Point iP : independentPoints) {
				if(iP.isSelected()) {
					removePoint(iP);
					deleted = true;
					break;
				}
			}
		} while (deleted);
	}
	
	public Collection<LineAndBool> linesPoint(Point iP) {
		Collection<LineAndBool> linesLeadingInto = new ArrayList<>();
		
		for(Line line : lines) {
			if(line.A.equals(iP)) {
				linesLeadingInto.add(new LineAndBool(true, line));
			}
			if(line.B.equals(iP)) {
				linesLeadingInto.add(new LineAndBool(false, line));
				
			}
		}
		return linesLeadingInto;
	}
	
	public void removePoint(Point iP) {
		iP.setIgnoreUnregisters();
		for(Line l : iP.changeListeners) {
			lines.remove(l);
		}
		independentPoints.remove(iP);
	}
}

class LineAndBool {
	public Line line;
	public boolean whichLine;
	// true means A, false means B
	
	public LineAndBool(boolean whichLine, Line line) {
		this.whichLine = whichLine;
		this.line = line;
	}
}