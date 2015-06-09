package net.trdlo.zelda.notiles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
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
enum ViewState {

	NORMAL, DRAG_LINE, DRAG_RECT, POLY_LINE, TYPING
}

public class View extends ZView {

	public static final int POINT_DISPLAY_SIZE = 32;

	private final World world;
	private ZFrame zFrame;

	private ViewState state = ViewState.NORMAL;
	private List<String> console;
	private SynchronousQueue<MouseEvent> mouseEventQueue;
	private SynchronousQueue<KeyEvent> keyEventQueue;

	List<Point> selectedPoints; 

	
	private Line dragLine;
	private Point dragStart;
	private Point dragEnd;
	//private boolean typing;
	private String tmpDescription;
	//private boolean polylineMode;
	private Point movingPoint;
//	private Point movingIndependentPoint;
	private boolean leftMouseDown;
	private boolean rightMouseDown;

	private Stroke defaultStroke;
	private final Stroke dashedStroke;
	private final Stroke selectionStroke;
	private Font defaultFont;

	public View(World world, ZFrame zFrame) {
		this.world = world;
		this.zFrame = zFrame;

		mouseEventQueue = new SynchronousQueue<>();
		keyEventQueue = new SynchronousQueue<>();
		
		selectedPoints = new ArrayList<>();		

		dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
		selectionStroke = new BasicStroke(2);

		console = new ArrayList<>();
	}

	@Override
	public void update() {
		MouseEvent me = null;

		while ((me = mouseEventQueue.poll()) != null) {

			Point clickedPoint = this.getPointAt(me.getX(), me.getY());
			Point tempPoint = new Point(me.getX(), me.getY());
			switch (me.getID()) {
				case MouseEvent.MOUSE_CLICKED: {
//					console.add("Event! Count = " + me.getClickCount() + "; Thread-ID: " + Thread.currentThread().getId() + "\n");
					if (me.getClickCount() > 1) {
						switch (state) {
							case DRAG_LINE:

								break;
							case DRAG_RECT:

								break;
							case POLY_LINE:
								if (me.getButton() == MouseEvent.BUTTON1) {
									if (clickedPoint != null) {
										world.independentPoints.add(clickedPoint);
//										world.lines.add()
									} else {
										world.independentPoints.add(dragLine.B);
									}
									world.lines.add(dragLine);
									dragLine = null;
									state = ViewState.NORMAL;
								}
								break;
							case TYPING:

								break;
							case NORMAL:
								if (me.getButton() == MouseEvent.BUTTON1) {
									if (clickedPoint != null) {
										dragLine = new Line(clickedPoint, clickedPoint);
									} else {
										world.independentPoints.add(tempPoint);
										dragLine = new Line(tempPoint, tempPoint);
									}
									state = ViewState.POLY_LINE;
								}
								break;
							default:
								//tak nedelej nic...
								break;
						}
					} else {
						switch (state) {
							case DRAG_LINE:

								break;
							case DRAG_RECT:

								break;
							case POLY_LINE:
								
							case TYPING:

								break;
							case NORMAL:
								if (me.getButton() == MouseEvent.BUTTON2) {
									world.removePoint(clickedPoint);
								} else if (me.getButton() == MouseEvent.BUTTON3 && clickedPoint != null) {
									clickedPoint.setSelected(!clickedPoint.isSelected());
								}
								break;
							default:
								break;
						}
					}
				}
				break;
				case MouseEvent.MOUSE_PRESSED:
					Point temp = this.getPointAt(me.getX(), me.getY());
					switch (state) {
						case DRAG_LINE:

							break;
						case DRAG_RECT:

							break;
						case POLY_LINE:

							break;
						case TYPING:

							break;
						case NORMAL:
							if (me.getButton() == MouseEvent.BUTTON1) {
								if (clickedPoint != null) {
									dragLine = new Line(clickedPoint, new Point(me.getX(), me.getY()));
									state = ViewState.DRAG_LINE;
								} else {
									dragStart = new Point(me.getX(), me.getY());
									dragEnd = new Point(me.getX(), me.getY());
									state = ViewState.DRAG_RECT;
								}
							} else if (me.getButton() == MouseEvent.BUTTON3 && clickedPoint != null) {
								movingPoint = clickedPoint;
							}
							break;
						default:
							//tak nedelej nic...
							break;
					}

					if (me.getButton() == MouseEvent.BUTTON1) {
						Point startPoint = clickedPoint;
						if (startPoint != null) {
							dragLine = new Line(startPoint, new Point(me.getX(), me.getY()));
						} else {
							dragStart = new Point(me.getX(), me.getY());
							dragEnd = new Point(me.getX(), me.getY());
						}
					} else if (me.getButton() == MouseEvent.BUTTON3) {
						if (clickedPoint != null) {
							movingPoint = clickedPoint;
						}
					}

					break;

//					if (me.getClickCount() < 2) {
//						if (polylineMode) {
//							//				Point iP = new Point((double)me.getX(), (double)me.getY());
//
//						}
//
//					} else {
//						polylineMode = !polylineMode;
//						if (polylineMode) {
//							dragLine = new Line(new Point(me.getX(), me.getY()), new Point(me.getX(), me.getY()));
//							world.independentPoints.add(dragLine.A);
//						} else {
//							world.independentPoints.add(dragLine.B);
//							world.lines.add(dragLine);
//							dragLine = null;
//						}
//					}
//				}
				case MouseEvent.MOUSE_RELEASED:
					switch (state) {
						case DRAG_LINE:
							Point endPoint = this.getPointAt(me.getX(), me.getY());
							if (endPoint != null) {
								dragLine.setB(endPoint);
								world.lines.add(dragLine);
							}
							dragLine = null;
							state = ViewState.NORMAL;
							break;
						case DRAG_RECT:
							dragStart = null;
							dragEnd = null;
							state = ViewState.NORMAL;
							break;
						case POLY_LINE:

							break;
						case TYPING:

							break;
						case NORMAL:
							if (movingPoint != null) {
								movingPoint = null;
							}
							break;
						default:
							//tak nedelej nic...
							break;
					}
//		if (me.getButton() == MouseEvent.BUTTON1) {
//			rightMouseDown = false;
//		} else {
//			leftMouseDown = false;
//		}

					/*if (polylineMode) {
					 return;
					 }
					 if (dragLine != null) {
					 Point endPoint = View.getPointAt(me.getX(), me.getY());
					 if (endPoint != null) {
					 dragLine.setB(endPoint);
					 world.lines.add(dragLine);
					 }
					 dragLine = null;
					 }
					 if (dragStart != null) {
					 if ((Math.abs(dragStart.getX() - dragEnd.getX()) > POINT_DISPLAY_SIZE / 2) || (Math.abs(dragStart.getY() - dragEnd.getY()) > POINT_DISPLAY_SIZE / 2)) {
					 for (Point iP : world.pointsInRect(dragStart, dragEnd)) {
					 iP.setSelected((me.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != MouseEvent.SHIFT_DOWN_MASK);
					 }
					 }
					 dragStart = null;
					 dragEnd = null;
					 }
					 if (movingPoint != null) {
					 movingPoint = null;
					 }*/
					break;
				case MouseEvent.MOUSE_DRAGGED:
					switch (state) {
						case DRAG_LINE:
							dragLine.setB(new Point(me.getX(), me.getY()));
							break;
						case DRAG_RECT:
							dragEnd.setX(me.getX());
							dragEnd.setY(me.getY());
							for (Point iP : world.pointsInRect(dragStart, dragEnd)) {
								iP.setSelected((me.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != MouseEvent.SHIFT_DOWN_MASK);
							}
							break;
						case POLY_LINE:

							break;
						case TYPING:

							break;
						case NORMAL:
							if (movingPoint != null) {
								movingPoint.setXY(me.getX(), me.getY());
							}
							break;
						default:
							//tak nedelej nic...
							break;
					}
					break;
				case MouseEvent.MOUSE_MOVED:
					switch (state) {
						case DRAG_LINE:

							break;
						case DRAG_RECT:

							break;
						case POLY_LINE:
							dragLine.setB(new Point(me.getX(), me.getY()));
							break;
						case TYPING:

							break;
						case NORMAL:
							
							break;
						default:
							//tak nedelej nic...
							break;
					}
					break;
			}
		}

		KeyEvent ke = null;
		while ((ke = keyEventQueue.poll()) != null) {
			switch (ke.getID()) {
				case KeyEvent.KEY_PRESSED:
					zFrame.keyPressed(ke);
					break;
				case KeyEvent.KEY_RELEASED:
					zFrame.keyReleased(ke);
					break;
				case KeyEvent.KEY_TYPED:
					if (!myKeyTyped(ke)) {
						zFrame.keyTyped(ke);
					}
					break;
			}
		}

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

		for (Point p : world.independentPoints) {
			String textToDraw;
			if (p.isSelected()) {
				graphics.setStroke(selectionStroke);
				graphics.setColor(Color.PINK);
			} else {
				graphics.setStroke(defaultStroke);
				graphics.setColor(Color.WHITE);
			}

			if (p.isSelected() && tmpDescription != null) {
				textToDraw = tmpDescription;
			} else {
				textToDraw = p.getDescription();
			}
			
			textToDraw = Integer.toString(p.changeListeners.size());

			graphics.drawRect((int) p.x - POINT_DISPLAY_SIZE / 2, (int) p.y - POINT_DISPLAY_SIZE / 2, POINT_DISPLAY_SIZE, POINT_DISPLAY_SIZE);
			if (textToDraw != null) {
				graphics.drawString(textToDraw, (int) p.x + POINT_DISPLAY_SIZE, (int) p.y + POINT_DISPLAY_SIZE);
			}
		}
		graphics.setStroke(defaultStroke);
		graphics.setColor(Color.WHITE);

		if (dragLine != null) {
			graphics.setStroke(dashedStroke);
			graphics.drawLine((int) dragLine.A.x, (int) dragLine.A.y, (int) dragLine.B.x, (int) dragLine.B.y);
			graphics.setStroke(defaultStroke);
		}

		//graphics.drawString("Paint thread-ID: " + Thread.currentThread().getId() + "\n", 10, 20);
		int outY = 0;
		for (String s : console) {
			graphics.drawString(s, 10, (outY += 16));
		}

		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void mouseClicked(MouseEvent me) {
		try {
			mouseEventQueue.put(me);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public void mousePressed(MouseEvent me) {
		try {
			mouseEventQueue.put(me);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public void mouseReleased(MouseEvent me) {
		try {
			mouseEventQueue.put(me);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public void mouseDragged(MouseEvent me) {
		try {
			mouseEventQueue.put(me);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public void mouseMoved(MouseEvent me) {
		try {
			mouseEventQueue.put(me);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	/**
	 *
	 * @param ke
	 * @return	true, pokud klavesa zpracovana, false, pokud se ma poslat dal
	 */
	public boolean myKeyTyped(KeyEvent ke) {
		boolean keyUsed = false;
		char c = ke.getKeyChar();

		switch (state) {
			case DRAG_LINE:

				break;
			case DRAG_RECT:

				break;
			case POLY_LINE:
				if (c == KeyEvent.VK_ESCAPE) {
					state = ViewState.NORMAL;
					dragLine = null;
					keyUsed = true;
					return keyUsed;
				}
				if(c == KeyEvent.VK_SPACE) {
					world.independentPoints.add(dragLine.B);
					world.lines.add(dragLine);

					dragLine = new Line(dragLine.B, dragLine.B);					
				}
				break;
			case TYPING:
				keyUsed = true;
				if (defaultFont.canDisplay(c) && c != KeyEvent.VK_ENTER) {
					tmpDescription += Character.toString(c);
					keyUsed = true;
				} else if (c == KeyEvent.VK_BACK_SPACE) {
					if (tmpDescription.length() != 0) {
						tmpDescription = tmpDescription.substring(0, tmpDescription.length() - 1);
					}
					keyUsed = true;
				} else if (c == KeyEvent.VK_ESCAPE) {
					tmpDescription = null;
					state = ViewState.NORMAL;
					keyUsed = true;
				} else if (c == KeyEvent.VK_ENTER) {
					for (Point iP : world.independentPoints) {
						if (iP.isSelected()) {
							iP.setDescription(tmpDescription);
						}
					}
					state = ViewState.NORMAL;
					tmpDescription = null;
					keyUsed = true;
				}
				break;

			case NORMAL:
				if (Character.toUpperCase(c) == 'T') {
					state = ViewState.TYPING;
					tmpDescription = "";
					keyUsed = true;
				}
				if (c == KeyEvent.VK_DELETE) {
					world.delSelectedPoints();
				}
				if (c == KeyEvent.VK_SPACE) {
					Point mp = new Point(zFrame.getMousePosition());
					if (this.getPointAt(mp.x, mp.y) == null) {
						world.independentPoints.add(new Point(mp.x, mp.y));
					}
				}
				break;
			default:

				break;
		}
		/*if (defaultFont == null) {
		 return false;
		 }

		 boolean keyUsed;
		 char c = ke.getKeyChar();

		 if (polylineMode) {
		 if (c == KeyEvent.VK_ESCAPE) {
		 polylineMode = false;
		 keyUsed = true;
		 return keyUsed;
		 }
		 }

		 if (typing) {
		 keyUsed = true;
		 if (defaultFont.canDisplay(c) && c != KeyEvent.VK_ENTER) {
		 tmpDescription += Character.toString(c);
		 } else if (c == KeyEvent.VK_BACK_SPACE) {
		 if (tmpDescription.length() != 0) {
		 tmpDescription = tmpDescription.substring(0, tmpDescription.length() - 1);
		 }
		 } else if (c == KeyEvent.VK_ESCAPE) {
		 tmpDescription = null;
		 typing = false;
		 keyUsed = true;
		 } else if (c == KeyEvent.VK_ENTER) {
		 for (Point iP : world.independentPoints) {
		 if (iP.isSelected()) {
		 iP.setDescription(tmpDescription);
		 }
		 }
		 tmpDescription = null;
		 typing = false;
		 keyUsed = true;
		 } else {
		 keyUsed = false;
		 }
		 } else {
		 keyUsed = true;
		 if (Character.toUpperCase(c) == 'T') {
		 typing = true;
		 tmpDescription = "";
		 } else if (c == KeyEvent.VK_DELETE) {
		 world.delSelectedPoints();
		 } else if (c == KeyEvent.VK_ESCAPE) {
		 keyUsed = false;
		 for (Point p : world.independentPoints) {
		 if (p.isSelected()) {
		 p.setSelected(false);
		 keyUsed = true;
		 }
		 }
		 } else {
		 keyUsed = false;
		 }
		 }
		 */
		return keyUsed;
	}

	public Point getPointAt(double x, double y) {
		for (Point p : world.independentPoints) {
			if (Math.abs(p.x - x) < POINT_DISPLAY_SIZE / 2 && Math.abs(p.y - y) < POINT_DISPLAY_SIZE / 2) {
				return p;
			}
		}
		return null;
	}
	
	private void selectPoint(Point p) {
	}

	@Override
	public void keyTyped(KeyEvent ke) {
		try {
			keyEventQueue.put(ke);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		try {
			keyEventQueue.put(ke);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void keyReleased(KeyEvent ke) {
		try {
			keyEventQueue.put(ke);
		} catch (InterruptedException ex) {
			Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
