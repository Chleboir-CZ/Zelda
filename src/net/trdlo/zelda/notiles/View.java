package net.trdlo.zelda.notiles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.trdlo.zelda.ZView;

/*
TODO

 - nelze vložit 2 body na stejnou souřadnici (equals je true)
 - LOW PRIORITY (right-click maže body)
 - double-click zapíná mód lomené čáry, končí buď v ex. bodě, nebo right-click
 - tažením myši z prázdna se objeví čárkovaný čtverec výběru => vznikne množina vybraných bodů
 - vybrané body lze DEL smazat
 - vybrané body se vykreslují výrazněji
 - po stisku T lze do ENTER zadávat popisek bodu (ukládá se do bodu), ESC stornovat zadávání
 
 - vytvořit třídu GeometryUtils, která převezme špinavé výpočty (vše budou statické metody!)

*/

public class View extends ZView {
	
	public static final int POINT_DISPLAY_SIZE = 8;
	private final World world;
	private Line dragLine;
	private Point dragStart;
	private Point dragEnd;
	
	private Stroke defaultStroke;
	private final Stroke dashedStroke;
	private final Stroke selectionStroke;

	public View(World world) {
		this.world = world;
		
		dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
		selectionStroke = new BasicStroke(2);
	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		for (Line line : world.lines) {
			graphics.drawLine((int) line.A.x, (int) line.A.y, (int) line.B.x, (int) line.B.y);
		}

		defaultStroke = graphics.getStroke();
		graphics.setStroke(dashedStroke);
		for (Line line : world.ray.rayTraceEffect(world.lines)) {
			graphics.drawLine((int) line.A.x, (int) line.A.y, (int) line.B.x, (int) line.B.y);
		}
		if (dragStart != null && dragEnd != null) {
			Rectangle rect = new Rectangle(dragStart.getJavaPoint());
			rect.add(dragEnd.getJavaPoint());
			
			graphics.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
		
		for (IndependentPoint p : world.independentPoints) {
			if(p.isSelected()) {
				graphics.setStroke(selectionStroke);
				graphics.setColor(Color.PINK);
			}
			else {
				graphics.setStroke(defaultStroke);
				graphics.setColor(Color.WHITE);
			}
				
			graphics.drawRect((int) p.x - POINT_DISPLAY_SIZE / 2, (int) p.y - POINT_DISPLAY_SIZE / 2, POINT_DISPLAY_SIZE, POINT_DISPLAY_SIZE);
		}
		graphics.setStroke(defaultStroke);
		graphics.setColor(Color.WHITE);
		
		if (dragLine != null) {
			graphics.setStroke(dashedStroke);
			graphics.drawLine((int) dragLine.A.x, (int) dragLine.A.y, (int) dragLine.B.x, (int) dragLine.B.y);
			graphics.setStroke(defaultStroke);
		}

		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		IndependentPoint clickedPoint = world.getPointAt(me.getX(), me.getY());
		if(clickedPoint == null) {
			world.independentPoints.add(new IndependentPoint(me.getX(), me.getY()));
		}
		else {
			clickedPoint.setSelected(!clickedPoint.isSelected());
		}
//		if (me.getButton() == MouseEvent.BUTTON2 && world.getPointAt(me.getX(), me.getY()) != null) {
//			
//		}
	}

	@Override
	public void mousePressed(MouseEvent me) {
		Point startPoint = world.getPointAt(me.getX(), me.getY());
		if (startPoint != null) {
			dragLine = new Line(startPoint, new Point(me.getX(), me.getY()));
		}
		else {
			dragStart = new Point(me.getX(), me.getY());
			dragEnd = new Point(me.getX(), me.getY());
		}
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		if (dragLine != null) {
			Point endPoint = world.getPointAt(me.getX(), me.getY());
			if (endPoint != null) {
				dragLine.setB(endPoint);
				world.lines.add(dragLine);
			}
			dragLine = null;
		}
		if (dragStart != null) {
			if ((Math.abs(dragStart.getX() - dragEnd.getX()) > POINT_DISPLAY_SIZE / 2) || (Math.abs(dragStart.getY() - dragEnd.getY()) > POINT_DISPLAY_SIZE / 2)) {
				for (IndependentPoint iP : world.pointsInRect(dragStart, dragEnd)) {
					iP.setSelected((me.getModifiersEx() &  MouseEvent.SHIFT_DOWN_MASK) != MouseEvent.SHIFT_DOWN_MASK);
				}
			}
			dragStart = null;
			dragEnd = null;
		}
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		if (dragLine != null) {
			dragLine.setB(new Point(me.getX(), me.getY()));
		}
		if (dragStart != null) {
			dragEnd.setX(me.getX());
			dragEnd.setY(me.getY());
		}
	}

	@Override
	public void mouseMoved(MouseEvent me) {

	}

}
