package net.trdlo.zelda;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.trdlo.zelda.exceptions.MapLoadException;


public class Tree extends GameObject {
    
    BufferedImage img;

    public Tree() throws MapLoadException {
        super('T', 0.5f, Z_OVERHEAD);
        
        try {
            img = ImageIO.read(new File("images/tree.png"));
        } catch (IOException ex) {
            throw new MapLoadException("Tree instantion did not load it's graphics correctly.", ex);
        }
    }

    @Override
    public GameObjectInstance getInstance(float x, float y, String args) {
        return new TreeInstance(this, x, y);
	}

}
