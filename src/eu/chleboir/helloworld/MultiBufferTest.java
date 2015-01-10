/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.chleboir.helloworld;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class MultiBufferTest implements MouseListener, KeyListener {

    private static final DisplayMode[] BEST_DISPLAY_MODES = new DisplayMode[]{
        //new DisplayMode(1920, 1080, 32, 0),
        new DisplayMode(640, 480, 32, 0)
    };    

    private Frame mainFrame;
    private volatile boolean running = true;
    private volatile int nicMocX = 10, nicMocY = 10;
    
    World world;
    
    public final int KEY_UP = 0;
    public final int KEY_DOWN = 1;
    public final int KEY_LEFT = 2;
    public final int KEY_RIGHT = 3;
    public final int KEY_SHOOT = 4;
    public final int KEY_LMOUSE = 5;
    private volatile boolean keyMap[] = new boolean[5];
    private int maxX;
    private int maxY;
    
    public MultiBufferTest(int numBuffers, GraphicsDevice device) {
        try {
            world = new World("maps/default.txt");
            
            GraphicsConfiguration gc = device.getDefaultConfiguration();
            mainFrame = new Frame(gc);
            mainFrame.setUndecorated(true);
            mainFrame.setIgnoreRepaint(true);
            mainFrame.addMouseListener(this);
            mainFrame.addKeyListener(this);
            device.setFullScreenWindow(mainFrame);
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            maxX = gd.getDisplayMode().getWidth();
            maxY = gd.getDisplayMode().getHeight();
            
            /*try {
                images[4] = ImageIO.read(new File("images/ball.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            if (device.isDisplayChangeSupported()) {
                chooseBestDisplayMode(device);
            }
            Rectangle bounds = mainFrame.getBounds();
            mainFrame.createBufferStrategy(numBuffers);
            BufferStrategy bufferStrategy = mainFrame.getBufferStrategy();
            Random r = new Random();
            while (running) {
                //compute
                //vyhodnotit vstupy
                if (keyMap[KEY_UP]) {
                    nicMocY -= 10;
                    if (nicMocY < 0) nicMocY = 0;
                }
                if (keyMap[KEY_DOWN]) {
                    nicMocY += 10;
                    if (nicMocY > maxY) nicMocY = maxY;
                }
                if (keyMap[KEY_LEFT]) {
                    nicMocX -= 10;
                    if (nicMocX < 0) nicMocX = 0;
                }
                if (keyMap[KEY_RIGHT]) {
                    nicMocX += 10;
                    if (nicMocX > maxX) nicMocX = maxX;
                }
                
                
                
                //zobrazit
                Graphics g = bufferStrategy.getDrawGraphics();
                if (!bufferStrategy.contentsLost()) {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, bounds.width, bounds.height);

                    for(int y = 0; y < world.mapHeight && y * 32 < bounds.height; y++) {
                        for(int x = 0; x < world.mapWidth && x * 32 < bounds.width; x++) {
                            g.drawImage(world.map[x + y * world.mapWidth].getTile().getImg(), 32*x, 32*y, null);
                        }
                    }
                    
                    //g.drawImage(images[4], nicMocX, nicMocY, null);

                    bufferStrategy.show();
                    g.dispose();
                }

                //čekat
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
        } catch (MapLoadException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            device.setFullScreenWindow(null);
        }
    }

    
    
    private static DisplayMode getBestDisplayMode(GraphicsDevice device) {
        for (int x = 0; x < BEST_DISPLAY_MODES.length; x++) {
            DisplayMode[] modes = device.getDisplayModes();
            for (int i = 0; i < modes.length; i++) {
                if (modes[i].getWidth() == BEST_DISPLAY_MODES[x].getWidth()
                        && modes[i].getHeight() == BEST_DISPLAY_MODES[x].getHeight()
                        && modes[i].getBitDepth() == BEST_DISPLAY_MODES[x].getBitDepth()) {
                    return BEST_DISPLAY_MODES[x];
                }
            }
        }
        return null;
    }

    public static void chooseBestDisplayMode(GraphicsDevice device) {
        DisplayMode best = getBestDisplayMode(device);
        if (best != null) {
            device.setDisplayMode(best);
        }
    }

    public static void main(String[] args) {
        try {
            int numBuffers = 2;
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = env.getDefaultScreenDevice();
            MultiBufferTest test = new MultiBufferTest(numBuffers, device);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    @Override
    public void keyTyped(KeyEvent ke) {
        running = false;
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                keyMap[KEY_LEFT] = true;
                break;
            case KeyEvent.VK_RIGHT:
                keyMap[KEY_RIGHT] = true;
                break;
            case KeyEvent.VK_UP:
                keyMap[KEY_UP] = true;
                break;
            case KeyEvent.VK_DOWN:
                keyMap[KEY_DOWN] = true;
                break;
            case KeyEvent.VK_CONTROL:
                keyMap[KEY_SHOOT] = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                keyMap[KEY_LEFT] = false;
                break;
            case KeyEvent.VK_RIGHT:
                keyMap[KEY_RIGHT] = false;
                break;
            case KeyEvent.VK_UP:
                keyMap[KEY_UP] = false;
                break;
            case KeyEvent.VK_DOWN:
                keyMap[KEY_DOWN] = false;
                break;
            case KeyEvent.VK_CONTROL:
                keyMap[KEY_SHOOT] = false;
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (me.getButton() == MouseEvent.BUTTON1)
            keyMap[KEY_LMOUSE] = true;
        
        Point p = me.getPoint();
        nicMocX = p.x;
        nicMocY = p.y;        
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        if (me.getButton() == MouseEvent.BUTTON1)
            keyMap[KEY_LMOUSE] = false;
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }
}


