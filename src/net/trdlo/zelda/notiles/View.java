package net.trdlo.zelda.notiles;

import java.awt.BasicStroke;
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

	private World world;
	private Line dragLine;
	private Rectangle dragRectangle;

	public View(World world) {
		this.world = world;
	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		for (Line line : world.lines) {
			graphics.drawLine((int) line.A.x, (int) line.A.y, (int) line.B.x, (int) line.B.y);
		}
		for (Point p : world.independentPoints) {
			graphics.drawRect((int) p.x - 4, (int) p.y - 4, 8, 8);
		}
		if (dragLine != null) {
			Stroke previousStroke = graphics.getStroke();
			graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
			graphics.drawLine((int) dragLine.A.x, (int) dragLine.A.y, (int) dragLine.B.x, (int) dragLine.B.y);
			graphics.setStroke(previousStroke);
		}

		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		if(!new Point(me.getX(), me.getY()).equals(world.getPointAt(me.getX(), me.getY()))) {
			world.independentPoints.add(new IndependentPoint(me.getX(), me.getY(), false));
		}
	}

	@Override
	public void mousePressed(MouseEvent me) {
		Point startPoint = world.getPointAt(me.getX(), me.getY());
		if (startPoint != null) {
			dragLine = new Line(startPoint, new Point(me.getX(), me.getY()));
		}
		else {
			
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
	}

	@Override
	public void mouseMoved(MouseEvent me) {

	}

}
