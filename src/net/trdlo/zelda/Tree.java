/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.trdlo.zelda.exceptions.MapLoadException;

/**
 *
 * @author chleboir
 */
public class Tree extends GameObject {
    
    BufferedImage img;

    public Tree() throws MapLoadException {
        super('T', 0.5f);
        
        try {
            img = ImageIO.read(new File("images/tree.png"));
        } catch (IOException ex) {
            throw new MapLoadException("Tree instantion did not load it's graphics correctly.", ex);
        }
    }

    @Override
    public void render(Graphics2D graphics, int x, int y) {
        graphics.drawImage(img, x, y, null);
    }

    @Override
    public GameObjectInstance getInstance(float x, float y, String args) {
        return new GameObjectInstance(this, x, y);
	}

}
