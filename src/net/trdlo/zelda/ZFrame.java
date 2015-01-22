package net.trdlo.zelda;

import net.trdlo.zelda.exceptions.MapLoadException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import net.trdlo.zelda.exceptions.ZException;

public class ZFrame extends JFrame implements WindowListener, KeyListener, MouseListener {

    private boolean terminate = false;

    private long runStartTime = 0;
    private long updateFrame = 0;
    private long renderFrame = 0;

    private static final int UPDATES_FREQ = 1;
    private static final long UPDATE_PERIOD = 1000L / UPDATES_FREQ;
    private static final int MAX_FRAME_DROPS = UPDATES_FREQ;

    private static final double RENDER_AVG_SUSTAIN = 0.9;
    private double renderAverage = 1.0;

    GraphicsDevice gDevice;
    BufferStrategy bufferStrategy;

    Random rand;
    World world;
    WorldView mainView;

//	int minUpdPenting = Integer.MAX_VALUE, maxUpdPenting = 1;
//	long minRenderLength = Long.MAX_VALUE, maxRenderLength = 0;
//	int maxRendersPerUpdate = 0;
    public ZFrame() throws ZException {
        super("ZeldaFrame app");

        addWindowListener(this);
        addKeyListener(this);
        addMouseListener(this);

        initFrame();

        rand = new Random();
        try {
            world = new World("maps/small.txt");
        } catch (MapLoadException ex) {
            throw new ZException("Game can't begin, map did no load well.", ex);
        }

        mainView = new WorldView(world);
    }

    private void initFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setIgnoreRepaint(true);
        setUndecorated(true);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        //GraphicsDevice[] screenDevices = ge.getScreenDevices(); //u mě dostanu 2 zařízení - levý a pravý monitor
        gDevice = ge.getDefaultScreenDevice();

        gDevice.setFullScreenWindow(this);

        /*if (gDevice.isDisplayChangeSupported()) {
         gDevice.setDisplayMode(new DisplayMode(1680, 1050, 32, DisplayMode.REFRESH_RATE_UNKNOWN));
         }*/
        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();
    }

    private void render(float renderFraction) {
        Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();

        g.setBackground(Color.BLACK);
        Rectangle bounds = g.getDeviceConfiguration().getBounds();
        g.clearRect(0, 0, bounds.width, bounds.height);

        mainView.render(g, renderFraction);

        g.setColor(Color.GREEN);

        g.fillArc(20, 20, 20, 20, 0, (int) (360 * renderFraction));

        if (!bufferStrategy.contentsLost()) {
            bufferStrategy.show();
        }

    

        renderFrame++;
    }

    private void update() {
		world.update();

        updateFrame++;
    }

    private void run() {
        while (!terminate) {
//			long whileStartTime = getTime();
//			long frameIndexByTime = (whileStartTime / UPDATE_PERIOD);						
//			long updatesPending = frameIndexByTime - updateFrame +1;
            long updatesPending = (getTime() / UPDATE_PERIOD) - updateFrame + 1;

//			System.err.println("Time: " + whileStartTime);
//			System.err.println("Frame: " + (updateFrame+1));
//			System.err.println("Shlould be: " + (frameIndexByTime+1));
//			minUpdPenting = Math.min(minUpdPenting, (int)updatesPending);
//			maxUpdPenting = Math.max(maxUpdPenting, (int)updatesPending);
//			System.err.println("UpdatesPending: " + updatesPending);
            if (updatesPending > MAX_FRAME_DROPS) {
                updatesPending = MAX_FRAME_DROPS;
            }

            while (updatesPending-- > 0) {
                update();
            }

            long renderStart = getTime();
//			System.err.println("Render start: " + renderStart);
            render(0);
            long renderEnd = getTime();
//			System.err.println("Render end: " + renderEnd);
            //long renderLength = renderEnd - renderStart;
            serRenderLength(renderEnd - renderStart);

//			minRenderLength = Math.min(minRenderLength, (int)renderLength);
//			maxRenderLength = Math.max(maxRenderLength, (int)renderLength);
//			System.err.println("Render length: " + renderLength);
            long nextUpdateTime = updateFrame * UPDATE_PERIOD;
//			System.err.println("Next time: " + nextUpdateTime);
            long remainingToUpdate = nextUpdateTime - renderEnd;
//			System.err.println("Remaining to update: " + remainingToUpdate);
            int renderCounter = 1;
            int rendersPerUpdate = 1;

            if (remainingToUpdate > avgRenderLenth()) {
                rendersPerUpdate += (int) (remainingToUpdate / avgRenderLenth());
            }

//			maxRendersPerUpdate = Math.max(maxRendersPerUpdate, rendersPerUpdate);
//			System.err.println("RPU: " + rendersPerUpdate);
            while (renderCounter < rendersPerUpdate && remainingToUpdate > avgRenderLenth()) {
                renderStart = getTime();
                render(renderCounter++ / (float) rendersPerUpdate);
                renderEnd = getTime();
                serRenderLength(renderEnd - renderStart);

                remainingToUpdate = nextUpdateTime - getTime();
            }

            //long endTime = getTime();
//			System.err.println("End at: " + endTime);
//			System.err.println("Sleep for: " + remainingToUpdate);
//			System.err.println("");
            if (remainingToUpdate > 0) {
                try {
                    Thread.sleep((int) remainingToUpdate);
                } catch (InterruptedException ex) {
                }
            }
        }

//		System.err.println("UP: (" + minUpdPenting + "; " + maxUpdPenting + ")");
//		System.err.println("RL: (" + minRenderLength + "; " + maxRenderLength + ")");
//		System.err.println("RPU: (1; " + maxRendersPerUpdate + ")");
    }

    private long getTime() {
        //return testTimes[i++];

        if (runStartTime == 0) {
            runStartTime = System.nanoTime();
        }
        return ((System.nanoTime() - runStartTime) / 1000000L);
    }

    private void serRenderLength(long length) {
        renderAverage = renderAverage * RENDER_AVG_SUSTAIN + length * (1.0 - RENDER_AVG_SUSTAIN);
    }

    private long avgRenderLenth() {
        return (long) renderAverage;
    }

    private void doneFrame() {
        gDevice.setFullScreenWindow(null);
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    public static void main(String[] args) {
        try {
            ZFrame zFrame = new ZFrame();
            zFrame.run();
            zFrame.doneFrame();
        } catch (ZException ex) {
            ex.printStackTrace();
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
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            terminate = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
