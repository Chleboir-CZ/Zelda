package net.trdlo.zelda.notiles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import net.trdlo.zelda.ZFrame;
import net.trdlo.zelda.ZView;
import net.trdlo.zelda.exceptions.ZException;

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

	NORMAL, DRAG_LINE, DRAG_RECT, POLY_LINE, TYPING, MOVING_POINTS
}

public class View extends ZView {

	public static final int POINT_DISPLAY_SIZE = 32;

	private final World world;
	private final ZFrame zFrame;

	private ViewState state = ViewState.NORMAL;
	private final List<String> console;
	private final SynchronousQueue<MouseEvent> mouseEventQueue;
	private final SynchronousQueue<KeyEvent> keyEventQueue;

	Set<Point> selectedPoints;

	private Line dragLine;
	private Point dragStart;
	private Point dragEnd;

	private String tmpDescription;
	private Point movingPoint;

	private Stroke defaultStroke;
	private final Stroke dashedStroke;
	private final Stroke selectionStroke;
	private Font defaultFont;
	
	private java.awt.Point lastMPosition;
	double offsetX;
	double offsetY;	
	
	JFileChooser fileChooser;

	public View(World world, ZFrame zFrame) {
		this.world = world;
		this.zFrame = zFrame;

		mouseEventQueue = new SynchronousQueue<>();
		keyEventQueue = new SynchronousQueue<>();

		selectedPoints = new HashSet<>();

		dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
		selectionStroke = new BasicStroke(2);

		console = new ArrayList<>();

		fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().endsWith(".txt");
			}

			@Override
			public String getDescription() {
				return "Zelda files";
			}
		});
	}

	@Override
	public void update() {
		MouseEvent me;

		while ((me = mouseEventQueue.poll()) != null) {

			Point clickedPoint = this.getPointAt(me.getX(), me.getY());
			//Point tempPoint = ;
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
										dragLine.setB(clickedPoint);
//										world.lines.add()
									} else {
										world.points.add(dragLine.B);
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
										dragLine = Line.constructFromTwoPoints(clickedPoint, new Point(me.getX(), me.getY()));
									} else {
										Point newPoint = new Point(me.getX(), me.getY());
										world.points.add(newPoint);
										dragLine = Line.constructFromTwoPoints(newPoint, new Point(me.getX(), me.getY()));
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
								}
								//TODO
								selectedPoints.clear();
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
									dragLine = Line.constructFromTwoPoints(clickedPoint, new Point(clickedPoint.x + 1, clickedPoint.y + 1));
									state = ViewState.DRAG_LINE;
								} else {
									dragStart = new Point(me.getX(), me.getY());
									dragEnd = new Point(me.getX(), me.getY());
									state = ViewState.DRAG_RECT;
								}
							} else if (me.getButton() == MouseEvent.BUTTON3 && clickedPoint != null) {
								double offsetX = clickedPoint.getX() - me.getX();
								double offsetY = clickedPoint.getY() - me.getY();
							}
							break;
						case MOVING_POINTS:
							
							break;
						default:
							//tak nedelej nic...
							break;
					}
					break;
//					if (me.getButton() == MouseEvent.BUTTON1) {
//						Point startPoint = clickedPoint;
//						if (startPoint != null) {
//							dragLine = new Line(startPoint, new Point(me.getX(), me.getY()));
//						} else {
//							dragStart = new Point(me.getX(), me.getY());
//							dragEnd = new Point(me.getX(), me.getY());
//						}
//					} else if (me.getButton() == MouseEvent.BUTTON3) {
//						if (clickedPoint != null) {
//							movingPoint = clickedPoint;
//						}
//					}
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
//							world.points.add(dragLine.A);
//						} else {
//							world.points.add(dragLine.B);
//							world.lines.add(dragLine);
//							dragLine = null;
//						}
//					}
//				}
				case MouseEvent.MOUSE_RELEASED:
					switch (state) {
						case DRAG_LINE:
							if (clickedPoint != null) {
								dragLine.setB(clickedPoint);
								world.lines.add(dragLine);
							} else {
								dragLine.unregister();
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
								selectedPoints.add(iP);
							}
							break;
						case POLY_LINE:

							break;
						case TYPING:

							break;
						case NORMAL:
							if (movingPoint != null) {
								double offsetX = movingPoint.getX() - me.getX();
								double offsetY = movingPoint.getY() - me.getY();
								movingPoint.setXY(me.getX() + offsetX, me.getY() + offsetY);
								for(Point p : selectedPoints) {
									offsetX = p.getX() - lastMPosition.x;
									offsetY = p.getY() - lastMPosition.y;
									p.setXY(me.getX() + offsetX, me.getY() + offsetY);
								}
							}
							break;
						case MOVING_POINTS:
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
			lastMPosition = new java.awt.Point(me.getX(), me.getY());
		}

		KeyEvent ke;
		while ((ke = keyEventQueue.poll()) != null) {
			switch (ke.getID()) {
				case KeyEvent.KEY_PRESSED:
					if (!myKeyPressed(ke)) {
						zFrame.keyPressed(ke);
					}
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
		for (Line line : GeometryUtils.constructRayPath(world.ray, world.lines)) {
			graphics.drawLine((int) line.A.x, (int) line.A.y, (int) line.B.x, (int) line.B.y);
		}
		if (dragStart != null && dragEnd != null) {
			Rectangle rect = new Rectangle(dragStart.getJavaPoint());
			rect.add(dragEnd.getJavaPoint());

			graphics.drawRect(rect.x, rect.y, rect.width, rect.height);
		}

		for (Point p : world.points) {
			String textToDraw;
			if (selectedPoints.contains(p)) {
				graphics.setStroke(selectionStroke);
				graphics.setColor(Color.PINK);
			} else {
				graphics.setStroke(defaultStroke);
				graphics.setColor(Color.WHITE);
			}

			if (selectedPoints.contains(p) && tmpDescription != null) {
				textToDraw = tmpDescription;
			} else {
				textToDraw = p.getDescription();
			}

			textToDraw = textToDraw + "(" + Integer.toString(p.changeListeners.size()) + ")";

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

	@Override
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
					dragLine.unregister();
					dragLine = null;
					keyUsed = true;
					return keyUsed;
				}
				if (c == KeyEvent.VK_SPACE) {
					Point clickedPoint = world.getPointAt((int) dragLine.B.getX(), (int) dragLine.B.getY());
					if (clickedPoint != null) {
						dragLine.setB(clickedPoint);
						world.points.add(dragLine.B);
						world.lines.add(dragLine);
						state = ViewState.NORMAL;
					} else {
						world.lines.add(dragLine);
						world.points.add(dragLine.B);
						dragLine = Line.constructFromTwoPoints(dragLine.B, new Point(dragLine.B.x + 1, dragLine.B.y + 1));
					}
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
					for (Point iP : world.points) {
						if (selectedPoints.contains(iP)) {
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
					delSelectedPoints();
				}
				if (c == KeyEvent.VK_SPACE) {
					Point mp = new Point(zFrame.getMousePosition());
					if (this.getPointAt(mp.x, mp.y) == null) {
						world.points.add(new Point(mp.x, mp.y));
					}
				}
				break;
			default:

				break;
		}
		return keyUsed;
	}

	private void save() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				int returnVal = fileChooser.showSaveDialog(zFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					try {
						world.saveToFile(file, false);
					} catch (ZException ex) {
						JOptionPane.showMessageDialog(zFrame, "Saving failed.");
						Logger.getLogger(View.class.getName()).log(Level.SEVERE, "Saving failed.", ex);
					}
				}
			}
		});
	}
	
	private void load() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				int returnVal = fileChooser.showOpenDialog(zFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					//TODO
				}
			}
		});
	}
	
	public boolean myKeyPressed(KeyEvent ke) {
		switch (state) {
			case NORMAL:
				if (ke.getKeyCode() == KeyEvent.VK_F2) {
					save();
					return true;
				}
				if (ke.getKeyCode() == KeyEvent.VK_F3) {
					load();
					return true;
				}

		}
		return false;
	}

	public Point getPointAt(double x, double y) {
		for (Point p : world.points) {
			if (Math.abs(p.x - x) < POINT_DISPLAY_SIZE / 2 && Math.abs(p.y - y) < POINT_DISPLAY_SIZE / 2) {
				return p;
			}
		}
		return null;
	}

	public void delSelectedPoints() {
		for (Point point : selectedPoints) {
			world.removePoint(point);
		}
		selectedPoints.clear();
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
//JOptionPane.showMessageDialog(zFrame, "123");//Žluťoučký kůň úpěl ďábelské ódy.");
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
