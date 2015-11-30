package net.trdlo.zelda.notiles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import net.trdlo.zelda.Console;
import net.trdlo.zelda.GameInterface;
import net.trdlo.zelda.InputListener;
import net.trdlo.zelda.ZeldaFrame;

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

public class NoTilesGame implements GameInterface, InputListener {

	public static final int POINT_DISPLAY_SIZE = 32;

	private final World world;
	private ZeldaFrame zFrame;

	private ViewState state = ViewState.NORMAL;
	private final List<String> console;

	Set<Point> selectedPoints;

	private WorldLine dragLine;
	private Point dragStart;
	private Point dragEnd;

	private String tmpDescription;
	private Point movingPoint;
	private final Map<Point, Point> movingPointMap;

	private Stroke defaultStroke;
	private final Stroke dashedStroke;
	private final Stroke selectionStroke;
	private Font defaultFont;

	private double x; //zamířeno na střed
	private double y;

	public int viewSizeX;
	public int viewSizeY;

	double sight;
	double orientation;
	double fOV;

	List<Line> viewPolygon;

	private double zoom;

	private double offsetX;
	private double offsetY;

	JFileChooser fileChooser;

	public NoTilesGame(World world) {
		this.world = world;

		selectedPoints = new HashSet<>();
		movingPointMap = new HashMap<>();

		dashedStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
		selectionStroke = new BasicStroke(2);

		console = new ArrayList<>();
		x = 0;
		y = 0;
		zoom = 1.5;
		sight = 500;
		fOV = Math.PI / 2;
		orientation = Math.PI * 1.5;

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

	public NoTilesGame() {
		this(World.createTestWorld());
	}

	@Override
	public void setZeldaFrame(ZeldaFrame frame) {
		this.zFrame = frame;
	}

	@Override
	public void update() {
//		getBoundsofView(world.points, world.hero, orientation, sight, fOV);
//		ViewUtils.evaluatePointsByAngle(world.points, world.hero, orientation, fOV);
//		viewPolygon = ViewUtils.getViewPolygon(fOV, orientation, sight, world.hero, world.lines, world.points);
	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		for (WorldLine line : world.lines) {
			graphics.drawLine(worldToViewXr(line.A.x), worldToViewYr(line.A.y), worldToViewXr(line.B.x), worldToViewYr(line.B.y));
		}

		defaultStroke = graphics.getStroke();
		defaultFont = graphics.getFont();
		graphics.setStroke(dashedStroke);
		for (Line line : GeometryUtils.constructRayPath(world.ray, world.lines)) {
			graphics.drawLine(worldToViewXr(line.A.x), worldToViewYr(line.A.y), worldToViewXr(line.B.x), worldToViewYr(line.B.y));
		}
		if (dragStart != null && dragEnd != null) {
			Rectangle rect = new Rectangle(new java.awt.Point(worldToViewXr(dragStart.x), worldToViewYr(dragStart.y)));
			rect.add(new java.awt.Point(worldToViewXr(dragEnd.x), worldToViewYr(dragEnd.y)));

			graphics.drawRect(rect.x, rect.y, rect.width, rect.height);
		}

//		graphics.setStroke(dashedStroke);
//		for (Line l : viewPolygon) {
//			graphics.drawLine(worldToViewXr(l.A.x), worldToViewYr(l.A.y), worldToViewXr(l.B.x), worldToViewYr(l.B.y));
//		}
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

			graphics.drawRect(worldToViewXr(p.x) - POINT_DISPLAY_SIZE / 2, worldToViewYr(p.y) - POINT_DISPLAY_SIZE / 2, POINT_DISPLAY_SIZE, POINT_DISPLAY_SIZE);

			if (textToDraw != null) {
				graphics.drawString(textToDraw, worldToViewXr(p.x) + POINT_DISPLAY_SIZE, worldToViewYr(p.y) + POINT_DISPLAY_SIZE);
			}
		}

		graphics.setStroke(defaultStroke);
		graphics.setColor(Color.WHITE);

		if (dragLine != null) {
			graphics.setStroke(dashedStroke);
			graphics.drawLine(worldToViewXr(dragLine.A.x), worldToViewYr(dragLine.A.y), worldToViewXr(dragLine.B.x), worldToViewYr(dragLine.B.y));
			graphics.setStroke(defaultStroke);
		}

		//graphics.drawString("Paint thread-ID: " + Thread.currentThread().getId() + "\n", 10, 20);
//		int outY = 0;
//		for (String s : console) {
//			graphics.drawString(s, 10, (outY += 16));
//		}
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
			Logger.getLogger(NoTilesGame.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void save() {
		/*SwingUtilities.invokeLater(new Runnable() {
		 @Override
		 public void run() {
		 int returnVal = fileChooser.showSaveDialog(zFrame);
		 if (returnVal == JFileChooser.APPROVE_OPTION) {
		 File file = fileChooser.getSelectedFile();
		 try {
		 world.saveToFile(file, false);
		 } catch (ZException ex) {
		 JOptionPane.showMessageDialog(zFrame, "Saving failed.");
		 Logger.getLogger(GameInterface.class.getName()).log(Level.SEVERE, "Saving failed.", ex);
		 }
		 }
		 }
		 });*/
	}

	private void load() {
		/*SwingUtilities.invokeLater(new Runnable() {
		 @Override
		 public void run() {
		 int returnVal = fileChooser.showOpenDialog(zFrame);
		 if (returnVal == JFileChooser.APPROVE_OPTION) {
		 File file = fileChooser.getSelectedFile();
		 //					try {
		 //					 world = World.loadFromFile(file, false);
		 //					 } catch (ZException ex) {
		 //					 JOptionPane.showMessageDialog(zFrame, "Saving failed.");
		 //					 Logger.getLogger(GameInterface.class.getName()).log(Level.SEVERE, "Saving failed.", ex);
		 //					 }
		 }
		 }
		 });*/
	}

	public Point getPointAt(double x, double y) {
		for (Point p : world.points) {
			if (Math.abs((p.x - viewToWorldX(x))) < POINT_DISPLAY_SIZE / 2 && Math.abs((p.y - viewToWorldY(y))) < POINT_DISPLAY_SIZE / 2) {
				return p;
			}
		}
		return null;
	}

	public Collection<Point> getPointsInPoly(List<Line> polyList) {
		Collection<Point> pointsInPoly = new ArrayList<>();
		for (Point p : world.points) {
			int intersectCount = 0;
			Line l = Line.constructFromTwoPoints(p, new Point(p.x - 100, p.y));
			for (Line n : polyList) {
				Point iP = l.intersectPoint(n);
				if (iP != null) {
					if (iP.x <= p.x) {
						double vx = l.B.x - l.A.x;
						double nx = n.B.x - n.A.x;
						double ny = n.B.y - n.A.y;
						if (Math.abs(nx) > Math.abs(ny)) {
							double distToInterFromNx = (iP.x - n.A.x) / nx;
							if (distToInterFromNx >= 0 && distToInterFromNx < 1) {
								intersectCount++;
							}
						} else {
							double distToInterFromNy = (iP.y - n.A.y) / ny;
							if (distToInterFromNy >= 0 && distToInterFromNy < 1) {
								intersectCount++;
							}
						}
					}
				}
			}
			if (intersectCount % 2 == 1) {
				pointsInPoly.add(p);
			}
		}
		return pointsInPoly;
	}

	public Point createPointAt(double x, double y, String description) {
		return new Point(viewToWorldX(x), viewToWorldY(y), description);
	}

	public double worldToViewX(double x) {
		double tempX = x - this.x;
		return tempX * zoom;
	}

	public double worldToViewY(double y) {
		double tempY = y - this.y;
		return tempY * zoom;
	}

	public double viewToWorldX(double x) {
		return ((x - zFrame.getBounds().width / 2) / zoom) + this.x;
	}

	public double viewToWorldY(double y) {
		return ((y - zFrame.getBounds().height / 2) / zoom) + this.y;
	}

	public int worldToViewXr(double x) {
		return (int) (worldToViewX(x) + zFrame.getBounds().width / 2);
	}

	public int worldToViewYr(double y) {
		return (int) (worldToViewY(y) + zFrame.getBounds().height / 2);
	}

	public void delSelectedPoints() {
		for (Point point : selectedPoints) {
			world.removePoint(point);
		}
		selectedPoints.clear();
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public void keyTyped(KeyEvent ke) {
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
				} else if (c == KeyEvent.VK_SPACE) {
					Point clickedPoint = world.getPointAt((int) dragLine.B.getX(), (int) dragLine.B.getY());
					if (clickedPoint != null) {
						dragLine.setB(clickedPoint);
						world.points.add(dragLine.B);
						world.lines.add(dragLine);
						state = ViewState.NORMAL;
					} else {
						world.lines.add(dragLine);
						world.points.add(dragLine.B);
						dragLine = WorldLine.constructFromTwoPoints(dragLine.B, new Point(dragLine.B.x + 1, dragLine.B.y + 1));
					}
				}
				break;
			case TYPING:
				if (defaultFont.canDisplay(c) && c != KeyEvent.VK_ENTER) {
					tmpDescription += Character.toString(c);
				} else if (c == KeyEvent.VK_BACK_SPACE) {
					if (tmpDescription.length() != 0) {
						tmpDescription = tmpDescription.substring(0, tmpDescription.length() - 1);
					}
				} else if (c == KeyEvent.VK_ESCAPE) {
					tmpDescription = null;
					state = ViewState.NORMAL;
				} else if (c == KeyEvent.VK_ENTER) {
					for (Point iP : world.points) {
						if (selectedPoints.contains(iP)) {
							iP.setDescription(tmpDescription);
						}
					}
					state = ViewState.NORMAL;
					tmpDescription = null;
				}
				break;

			case NORMAL:
				if (Character.toUpperCase(c) == 'T') {
					state = ViewState.TYPING;
					tmpDescription = "";
				}
				if (c == KeyEvent.VK_DELETE) {
					delSelectedPoints();
				}
				if (c == KeyEvent.VK_SPACE) {
					Point mp = new Point(viewToWorldX(zFrame.getMousePosition().getX()), viewToWorldY(zFrame.getMousePosition().getY()));
					if (this.getPointAt(mp.x, mp.y) == null) {
						world.points.add(new Point(mp.x, mp.y));
					}
				}
				if (c == KeyEvent.VK_ADD) {
					zoom *= 1.25;
				}
				if (c == KeyEvent.VK_MINUS) {
					zoom /= 1.25;
				}
				if (Character.toUpperCase(c) == 'P') {
					List<Point> polygon;
					polygon = new ArrayList<>();
					polygon.add(new Point(500, 800));
					polygon.add(new Point(200, 200));
					polygon.add(new Point(800, 200));

//					selectedPoints.addAll(getPointsInPoly(polygon));
				}
				break;
			default:

				break;
		}
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		switch (state) {
			case NORMAL:
				switch (ke.getKeyCode()) {
					case KeyEvent.VK_ESCAPE:
						zFrame.terminate();
						break;
					case KeyEvent.VK_F2:
						save();
						break;
					case KeyEvent.VK_F3:
						load();
						break;
					case KeyEvent.VK_LEFT:
						this.x -= 1;
						break;
					case KeyEvent.VK_RIGHT:
						this.x += 1;
						break;
					case KeyEvent.VK_UP:
						this.y -= 1;
						break;
					case KeyEvent.VK_DOWN:
						this.y += 1;
						break;
				}
		}
	}

	@Override
	public void keyReleased(KeyEvent ke) {

	}

	@Override
	public void mouseClicked(MouseEvent me) {
		Point clickedPoint = this.getPointAt(me.getX(), me.getY());

//		console.add("Event! Count = " + me.getClickCount() + "; Thread-ID: " + Thread.currentThread().getId() + "\n");
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
							dragLine = WorldLine.constructFromTwoPoints(clickedPoint, new Point(me.getX(), me.getY()));
						} else {
							Point newPoint = new Point(viewToWorldX(me.getX()), viewToWorldY(me.getY()));
							world.points.add(newPoint);
							dragLine = WorldLine.constructFromTwoPoints(newPoint, new Point(viewToWorldX(me.getX()), viewToWorldY(me.getY())));
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
					if (me.getButton() == MouseEvent.BUTTON3 && clickedPoint != null) {
						selectedPoints.add(clickedPoint);
					} else {
						selectedPoints.clear();
					}
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent me) {
		Point clickedPoint = this.getPointAt(me.getX(), me.getY());

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
						dragLine = WorldLine.constructFromTwoPoints(clickedPoint, new Point(clickedPoint.x + 1, clickedPoint.y + 1));
						state = ViewState.DRAG_LINE;
					} else {
						dragStart = new Point(viewToWorldX(me.getX()), viewToWorldY(me.getY()));
						dragEnd = new Point(viewToWorldX(me.getX()), viewToWorldY(me.getY()));
						state = ViewState.DRAG_RECT;
					}
				} else if (me.getButton() == MouseEvent.BUTTON3 && clickedPoint != null) {
					movingPoint = clickedPoint;
					offsetX = (clickedPoint.getX() - viewToWorldX(me.getX()));
					offsetY = (clickedPoint.getY() - viewToWorldY(me.getY()));
					for (Point p : selectedPoints) {
						if (p != movingPoint) {
							assert p != null;
							assert me != null;

							movingPointMap.put(p, new Point(p.getX() - viewToWorldX(me.getX()), p.getY() - viewToWorldY(me.getY())));
						}
					}
				}
				break;
			case MOVING_POINTS:

				break;
			default:
				//tak nedelej nic...
				break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		Point clickedPoint = this.getPointAt(me.getX(), me.getY());

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
				if (!movingPointMap.isEmpty()) {
					movingPointMap.clear();
				}

				break;
			default:
				//tak nedelej nic...
				break;
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
		Point clickedPoint = this.getPointAt(me.getX(), me.getY());

		switch (state) {
			case DRAG_LINE:
				dragLine.setB(new Point(viewToWorldX(me.getX()), viewToWorldY(me.getY())));
				break;
			case DRAG_RECT:
				dragEnd.setX(viewToWorldX(me.getX()));
				dragEnd.setY(viewToWorldY(me.getY()));
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
					movingPoint.setXY(viewToWorldX(me.getX()) + offsetX, viewToWorldY(me.getY()) + offsetY);
					for (Point p : selectedPoints) {
						if (p == movingPoint) {
							continue;
						}
						p.setXY(viewToWorldX(me.getX()) + movingPointMap.get(p).x, viewToWorldY(me.getY()) + movingPointMap.get(p).y);
					}
				}
				break;
			case MOVING_POINTS:
				break;
			default:
				//tak nedelej nic...
				break;
		}
	}

	@Override
	public void mouseMoved(MouseEvent me) {

		switch (state) {
			case DRAG_LINE:

				break;
			case DRAG_RECT:

				break;
			case POLY_LINE:
				dragLine.setB(new Point(viewToWorldX(me.getX()), viewToWorldY(me.getY())));
				break;
			case TYPING:

				break;
			case NORMAL:

				break;
			default:
				//tak nedelej nic...
				break;
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mwe) {

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
				int scroll = mwe.getWheelRotation();
				zoom *= Math.pow(1.25, -scroll);
				break;
			default:
				//tak nedelej nic...
				break;
		}
	}

	@Override
	public boolean executeCommand(String command, Console console) {
		return false;
	}

	@Override
	public String getWindowCaption() {
		return "Some freakin' title here!";
	}

	public static void main(String[] args) {
		try {
			ZeldaFrame.buildInstance(new NoTilesGame()).run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
