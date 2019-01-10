package net.trdlo.zelda;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public final class ZeldaFrame extends JFrame implements WindowListener, InputListener, CommandExecuter {

	private static ZeldaFrame instance = null;

	private volatile boolean terminate = false;
	private long runStartTime = 0;

	private long updateFrame = 0;
	public static final int UPDATES_FREQ = 20;
	public static final long UPDATE_PERIOD = 1000L / UPDATES_FREQ;
	private static final int MAX_FRAME_DROPS = UPDATES_FREQ;

	private long renderFrame = 0;
	private long totalRenderLength = 0;
	private int lastRenderCount;

	private GraphicsDevice gDevice;
	private BufferStrategy bufferStrategy;

	private final Font defaultFont;

	private final GameInterface gameInterface;

	private final Queue<KeyEvent> keyEventQueue;
	private final Queue<MouseEvent> mouseEventQueue;

	private final boolean keyMap[] = new boolean[256];
	private boolean keyInputDebug = false;
	private XY mouseXY = new XY(0, 0);

	private final Console console;

	public static ZeldaFrame buildInstance(GameInterface game) {
		assert instance == null;

		instance = new ZeldaFrame(game.getWindowCaption(), game);
		instance.setListeners();
		game.setZeldaFrame(instance);
		return instance;
	}

	public static ZeldaFrame getInstance() {
		assert instance != null;

		return instance;
	}

	private ZeldaFrame(String frameCaption, GameInterface gameInterface) {
		super(frameCaption);

		this.gameInterface = gameInterface;

		keyEventQueue = new ConcurrentLinkedQueue<>();
		mouseEventQueue = new ConcurrentLinkedQueue<>();

		defaultFont = new Font("Monospaced", Font.BOLD, 12);

		console = Console.getInstance();
	}

	private void setListeners() {
		addWindowListener(this);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);

		console.addCommandExecuter(this);
		console.addCommandExecuter(gameInterface);
	}

	private void render(float renderFraction) {
		long renderStart = getTime();

		Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();

		g.setBackground(Color.BLACK);
		Rectangle bounds = g.getDeviceConfiguration().getBounds();
		g.clearRect(0, 0, bounds.width, bounds.height);

		g.setFont(defaultFont);
		g.setColor(Color.WHITE);

		gameInterface.render(g, renderFraction);
		console.render(g, renderFraction);

		g.setColor(Color.GREEN);

		g.fillArc(bounds.width - 40, 20, 20, 20, 0, (int) (360 * renderFraction));

		if (!bufferStrategy.contentsLost()) {
			bufferStrategy.show();
		}

		setRenderLength(getTime() - renderStart);
	}

	public boolean isPressed(int keyCode) {
		return keyCode >= 0 && keyCode < 256 && keyMap[keyCode];
	}

	public void clearPressedKeys() {
		Arrays.fill(keyMap, false);
	}

	public XY getMouseXY() {
		return mouseXY;
	}

	private final Pattern PAT_GET_KEY_DEBUG = Pattern.compile("^\\s*key-debug\\s*$", Pattern.CASE_INSENSITIVE);
	private final Pattern PAT_SET_KEY_DEBUG = Pattern.compile("^\\s*key-debug\\s+([01])\\s*$", Pattern.CASE_INSENSITIVE);
	private final Pattern PAT_EXIT = Pattern.compile("^exit$", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean executeCommand(String command, Console console) {
		Matcher m;
		if (PAT_GET_KEY_DEBUG.matcher(command).matches()) {
			console.echo("key-debug " + (keyInputDebug ? "1" : "0"));
		} else if ((m = PAT_SET_KEY_DEBUG.matcher(command)).matches()) {
			keyInputDebug = "1".equals(m.group(1));
		} else if (PAT_EXIT.matcher(command).matches()) {
			terminate();
		} else {
			return false;
		}
		return true;
	}

	public void dispatchInput() {
		KeyEvent e;
		while ((e = keyEventQueue.poll()) != null) {
			int key;
			switch (e.getID()) {
				case KeyEvent.KEY_TYPED:
					if (keyInputDebug) {
						console.echo(3000, "Typed: '" + e.getKeyChar() + "'");
					}
					if (!console.keyTyped(e)) {
						gameInterface.keyTyped(e);
					}
					break;
				case KeyEvent.KEY_PRESSED:
					key = e.getKeyCode();
					if (keyInputDebug) {
						console.echo(3000, "Pressed: " + key + ", char: '" + e.getKeyChar() + "'");
					}
					if (!console.keyPressed(e)) {
						if (key < 256) {
							keyMap[key] = true;
						}
						gameInterface.keyPressed(e);
					}
					break;
				case KeyEvent.KEY_RELEASED:
					key = e.getKeyCode();
					if (keyInputDebug) {
						console.echo(3000, "Released: " + key + ", char: '" + e.getKeyChar() + "'");
					}
					if (key < 256) {
						keyMap[key] = false;
					}
					if (!console.keyReleased(e)) {
						gameInterface.keyReleased(e);
					}
					break;
			}
		}
		MouseEvent me;
		while ((me = mouseEventQueue.poll()) != null) {
			mouseXY = new XY(me);
			switch (me.getID()) {
				case MouseEvent.MOUSE_CLICKED:
					if (!console.mouseClicked(me)) {
						gameInterface.mouseClicked(me);
					}
					break;
				case MouseEvent.MOUSE_PRESSED:
					if (!console.mousePressed(me)) {
						gameInterface.mousePressed(me);
					}
					break;
				case MouseEvent.MOUSE_RELEASED:
					if (!console.mouseReleased(me)) {
						gameInterface.mouseReleased(me);
					}
					break;
				case MouseEvent.MOUSE_MOVED:
					gameInterface.mouseMoved(me);
					break;
				case MouseEvent.MOUSE_ENTERED:
					gameInterface.mouseEntered(me);
					break;
				case MouseEvent.MOUSE_EXITED:
					gameInterface.mouseExited(me);
					break;
				case MouseEvent.MOUSE_DRAGGED:
					if (!console.mouseDragged(me)) {
						gameInterface.mouseDragged(me);
					}					
					break;
				case MouseEvent.MOUSE_WHEEL:
					gameInterface.mouseWheelMoved((MouseWheelEvent) me);
					break;
			}
		}
	}

	private void update() {
		if (lastRenderCount > 0) {
			dispatchInput();
		}
		console.update();
		gameInterface.update(getTime());
		updateFrame++;
	}

	public void run() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setIgnoreRepaint(true);
		setUndecorated(true);

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		//GraphicsDevice[] screenDevices = ge.getScreenDevices(); //u mě dostanu 2 zařízení - levý a pravý monitor
		gDevice = ge.getDefaultScreenDevice();

		try {
			gDevice.setFullScreenWindow(this);

			//if (gDevice.isDisplayChangeSupported()) {
			//	gDevice.setDisplayMode(new DisplayMode(1680, 1050, 32, DisplayMode.REFRESH_RATE_UNKNOWN));
			//}
			createBufferStrategy(2);
			bufferStrategy = getBufferStrategy();

			while (!terminate) {
				long updatesPending = (getTime() / UPDATE_PERIOD) - updateFrame + 1;

				if (updatesPending < 1) {
					updatesPending = 1; //one update minimum!
				} else if (updatesPending > MAX_FRAME_DROPS) {
					updatesPending = MAX_FRAME_DROPS;
				}

				//System.err.print("pending: " + updatesPending + ",\t");
				while (updatesPending-- > 0) {
					update();
				}

				render(0);

				long nextUpdateTime = updateFrame * UPDATE_PERIOD;

				long remainingToUpdate = nextUpdateTime - getTime();

				int renderCounter = 1;

				while (remainingToUpdate > getAvgRenderLenth()) {
					long avgLen = getAvgRenderLenth();
					int approxRenderCount = avgLen > 0 ? (renderCounter + (int) (remainingToUpdate / avgLen)) : 0;
					float approxProgres = renderCounter / (float) approxRenderCount;

					int rendersPerFrame;
					if (lastRenderCount == 0) {
						rendersPerFrame = approxRenderCount;
					} else {
						rendersPerFrame = (int) (lastRenderCount * (1.0f - approxProgres) + approxRenderCount * approxProgres);
					}
					//logger.log(Level.SEVERE, updateFrame + ": " + renderCounter + "/" + rendersPerFrame);
					//System.err.print(updateFrame + ": " + renderCounter + "/" + rendersPerFrame + ",\t");

					float renderFraction = renderCounter / (float) rendersPerFrame;
					render(renderFraction);
					remainingToUpdate = nextUpdateTime - getTime();
					renderCounter++;
				}
				//System.err.println("total: " + renderCounter);

				lastRenderCount = renderCounter;

				if (remainingToUpdate > 0) {
					try {
						//System.err.println("Sleep for " + remainingToUpdate + " / " + getAvgRenderLenth());
						Thread.sleep((int) remainingToUpdate);
					} catch (InterruptedException ex) {
					}
				}
			}
		} finally {
			gDevice.setFullScreenWindow(null);
			//dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			dispose();
		}
	}

	public long getTime() {
		if (runStartTime == 0) {
			runStartTime = System.nanoTime();
			return 0;
		}
		return ((System.nanoTime() - runStartTime) / 1000000L);
	}

	private void setRenderLength(long length) {
		renderFrame++;
		totalRenderLength += length;
	}

	private long getAvgRenderLenth() {
		return totalRenderLength / renderFrame;
	}

	public static float getHzFromUpdateCount(float updateCount) {
		return UPDATES_FREQ / updateCount;
	}

	public static int getHzFromUpdateCount(int updateCount) {
		return UPDATES_FREQ / updateCount;
	}

	public static float getUpdateCountFromHz(float Hz) {
		return UPDATES_FREQ / Hz;
	}

	public static int getUpdateCountFromHz(int Hz) {
		return UPDATES_FREQ / Hz;
	}

	public static float getFrameStepFromTps(float tps) {
		return tps / UPDATES_FREQ;
	}

	public static float getTpsFrpmFrameStep(float frameStep) {
		return frameStep * UPDATES_FREQ;
	}

	public void terminate() {
		terminate = true;
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		terminate();
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	private void keyEvent(KeyEvent e) {
		keyEventQueue.add(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		keyEvent(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		keyEvent(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keyEvent(e);
	}

	private void mouseEvent(MouseEvent me) {
		mouseEventQueue.add(me);
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		mouseEvent(me);
	}

	@Override
	public void mousePressed(MouseEvent me) {
		mouseEvent(me);
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		mouseEvent(me);
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		mouseEvent(me);
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		mouseEvent(me);
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		mouseEvent(me);
	}

	@Override
	public void mouseExited(MouseEvent me) {
		mouseEvent(me);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mwe) {
		mouseEvent(mwe);
	}
}
