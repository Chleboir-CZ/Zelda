package net.trdlo.zelda;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import net.trdlo.zelda.exceptions.ZException;


public abstract class ZFrame extends JFrame implements WindowListener, KeyListener {

	protected boolean terminate = false;
	private long runStartTime = 0;

	private long updateFrame = 0;
	public static final int UPDATES_FREQ = 20;
	public static final long UPDATE_PERIOD = 1000L / UPDATES_FREQ;
	private static final int MAX_FRAME_DROPS = UPDATES_FREQ;

	private long renderFrame = 0;
	private long totalRenderLength = 0;
	private int lastRenderCount;

	GraphicsDevice gDevice;
	BufferStrategy bufferStrategy;

	protected Font defaultFont;

	protected ZWorld world;
	protected ZView mainView;

	public ZFrame(String frameCaption) throws ZException {
		super(frameCaption);
		initFrame();
		defaultFont = new Font("Monospaced", Font.BOLD, 12);
	}

	private void initFrame() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		setIgnoreRepaint(true);
		setUndecorated(true);

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		//GraphicsDevice[] screenDevices = ge.getScreenDevices(); //u mě dostanu 2 zařízení - levý a pravý monitor
		gDevice = ge.getDefaultScreenDevice();

		gDevice.setFullScreenWindow(this);

		//if (gDevice.isDisplayChangeSupported()) {
		//	gDevice.setDisplayMode(new DisplayMode(1680, 1050, 32, DisplayMode.REFRESH_RATE_UNKNOWN));
		//}
		createBufferStrategy(2);
		bufferStrategy = getBufferStrategy();
	}

	protected void render(float renderFraction) {
		long renderStart = getTime();

		Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();

		g.setBackground(Color.BLACK);
		Rectangle bounds = g.getDeviceConfiguration().getBounds();
		g.clearRect(0, 0, bounds.width, bounds.height);

		g.setFont(defaultFont);
		g.setColor(Color.WHITE);

		mainView.render(g, renderFraction);

		g.setColor(Color.GREEN);

		g.fillArc(20, 20, 20, 20, 0, (int) (360 * renderFraction));

		if (!bufferStrategy.contentsLost()) {
			bufferStrategy.show();
		}

		setRenderLength(getTime() - renderStart);
	}

	protected void update() {
		world.update();

		updateFrame++;
	}

	protected void run() {
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
				int approxRenderCount = renderCounter + (int) (remainingToUpdate / getAvgRenderLenth());
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
	}

	private long getTime() {
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

	protected void doneFrame() {
		gDevice.setFullScreenWindow(null);
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
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

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			terminate = true;
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
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

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			terminate = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
}
