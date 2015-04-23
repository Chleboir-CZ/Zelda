package net.trdlo.zelda.notiles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.trdlo.zelda.ZFrame;
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
	
	public static final int POINT_DISPLAY_SIZE = 12;

	private final World world;
	private ZFrame zFrame;

	
	private Line dragLine;
	private Point dragStart;
	private Point dragEnd;
	private Point mousePosition;
	private Point typingPoint;
	private boolean typing;
	private String tmpDescription;
	private boolean polylineMode;
	private IndependentPoint previousPoint;
	private boolean movingPoint;
	
	
	private Stroke defaultStroke;
	private final Stroke dashedStroke;
	private final Stroke selectionStroke;
	private Font defaultFont;

	public View(World world, ZFrame zFrame) {		
		this.world = world;
		this.zFrame = zFrame;
		mousePosition = new Point(0,0);
		
		dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
		selectionStroke = new BasicStroke(2);
	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		for (Line line : world.lines) {
			graphics.drawLine((int) line.A.x, (int) line.A.y, (int) line.B.x, (int) line.B.y);
		}

		defaultStroke = graphics.getStroke();
		defaultFont = graphics.getFont();		
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
			String textToDraw;
			if(p.isSelected()) {
				graphics.setStroke(selectionStroke);
				graphics.setColor(Color.PINK);
			}
			else {
				graphics.setStroke(defaultStroke);
				graphics.setColor(Color.WHITE);
			}
	
			if (p.isSelected() && typing)
				textToDraw = tmpDescription;
			else
				textToDraw = p.getDescription();
				
			graphics.drawRect((int) p.x - POINT_DISPLAY_SIZE / 2, (int) p.y - POINT_DISPLAY_SIZE / 2, POINT_DISPLAY_SIZE, POINT_DISPLAY_SIZE);
			if (textToDraw != null)
				graphics.drawString(textToDraw, (int)p.x + POINT_DISPLAY_SIZE, (int)p.y + POINT_DISPLAY_SIZE);
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
		if(me.getClickCount() < 2) {
			if(polylineMode) {
				IndependentPoint iP = new IndependentPoint((double)me.getX(), (double)me.getY());
				previousPoint.lineTo(iP);
				world.independentPoints.add(iP);
				previousPoint = iP;
			}
			if(clickedPoint == null) {
				world.independentPoints.add(new IndependentPoint(me.getX(), me.getY()));
			}
			else {
				if(me.getButton() == MouseEvent.BUTTON2) {
					world.removePoint(clickedPoint);
				}
				clickedPoint.setSelected(!clickedPoint.isSelected());
			}
	//		if (me.getButton() == MouseEvent.BUTTON2 && world.getPointAt(me.getX(), me.getY()) != null) {
	//			
		}
		else {
			polylineMode = !polylineMode;
			previousPoint = new IndependentPoint(me.getX(), me.getY());
		}
	}

	@Override
	public void mousePressed(MouseEvent me) {
		if(world.getPointAt(me.getX(), me.getY()) != null && me.getButton() == MouseEvent.BUTTON2) {
			movingPoint = true;
		}
		else {
			Point startPoint = world.getPointAt(me.getX(), me.getY());
			if (startPoint != null) {
				dragLine = new Line(startPoint, new Point(me.getX(), me.getY()));
			}
			else {
				dragStart = new Point(me.getX(), me.getY());
				dragEnd = new Point(me.getX(), me.getY());
			}
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
		if(polylineMode) {
			dragLine = new Line(previousPoint, new Point((double)me.getX(), me.getY()));
		}
		if(movingPoint) {
			
		}
		mousePosition.setX(me.getX());
		mousePosition.setY(me.getY());
	}

		/**
	 * 
	 * @param ke
	 * @return		true, pokud klavesa zpracovana, false, pokud se ma poslat dal
	 */
	public boolean myKeyTyped(KeyEvent ke) {
		if (defaultFont == null)
			return false;
		
		boolean keyUsed;
		char c = ke.getKeyChar();
		
		if(typing) {
			keyUsed = true;
			if (defaultFont.canDisplay(c) && c != KeyEvent.VK_ENTER) {
				tmpDescription += Character.toString(c);
			}
			else if (c == KeyEvent.VK_BACK_SPACE) {
				if (tmpDescription.length() != 0) {
					tmpDescription = tmpDescription.substring(0, tmpDescription.length() - 1);
				}
			}
			else if (c == KeyEvent.VK_ESCAPE) {
				tmpDescription = null;
				typing = false;
				keyUsed = true;
			}
			else if (c == KeyEvent.VK_ENTER) {
				for(IndependentPoint iP : world.independentPoints) {
					if(iP.isSelected()) {
						iP.setDescription(tmpDescription);
					}
				}
				tmpDescription = null;
				typing = false;
				keyUsed = true;
			}
			else {
				keyUsed = false;
			}
		}
		else {
			keyUsed = true;
			if(Character.toUpperCase(c) == 'T') {
				typing = true;
				tmpDescription = "";
			}
			else if(c == KeyEvent.VK_DELETE) {
				world.delSelectedPoints();
			} 
			else if(c == KeyEvent.VK_ESCAPE) {
				keyUsed = false;
				for (IndependentPoint p : world.independentPoints) {
					if (p.isSelected()) {
						p.setSelected(false);
						keyUsed = true;
					}
				}
			} else {
				keyUsed = false;
			}
		}

		return keyUsed;
	}

	
	public void myKeyPressed(KeyEvent ke) {
		
	}

	public void myKeyReleased(KeyEvent ke) {
		
	}
	
	@Override
	public void keyTyped(KeyEvent ke) {
		if (!myKeyTyped(ke))
			zFrame.keyTyped(ke);
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		zFrame.keyPressed(ke);
	}

	@Override
	public void keyReleased(KeyEvent ke) {
		zFrame.keyReleased(ke);
	}}
