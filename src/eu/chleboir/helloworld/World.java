/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.chleboir.helloworld;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;




public class World {
    
    private Set<Tile> tiles = new HashSet<>(); 
    private Tile defaultTile;
    /*private Set<Item> items = new HashSet<>();
    private Set<ItemInstance> ItemInstances = new HashSet<>();*/
    
    
    public TileInstance map[];
    public int mapWidth;
    public int mapHeight;
    
    
    public World(String fileName) throws MapLoadException {
        try{
            defaultTile = new Tile(true, ImageIO.read(new File("images/grass.png")), '.');
            tiles.add(defaultTile);
            tiles.add(new Tile(false, ImageIO.read(new File("images/stone.png")), '@'));
            tiles.add(new Tile(false, ImageIO.read(new File("images/water.png")), '~'));
            tiles.add(new Tile(false, ImageIO.read(new File("images/bush.png")), '#'));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(fileName));

            String line;
            
            line = r.readLine();
            mapWidth = Integer.parseInt(line);
            
            line = r.readLine();
            mapHeight = Integer.parseInt(line);

            map = new TileInstance[mapWidth * mapHeight];
            int row = 0;

            Map<Character,Tile> dict = new HashMap<>();
            for(Tile tile : tiles) {
                dict.put(tile.getIdentifier(), tile);
            }
            
            while ((line = r.readLine()) != null) {
                if (row == mapHeight) {
                    throw new MapLoadException("Map has more rows than declared.");
                }
                
                if (line.length() != mapWidth) {
                    throw new MapLoadException("Line length incorrect (" + line.length() + ") at row " + row);
                } 
                
                for (int i = 0; i < mapWidth; i++) {
                    Tile t = dict.get(line.charAt(i));
                    if (t == null)
                        t = defaultTile;
                    map[i + row * mapWidth] = new TileInstance(t, 0);
                }
                row++;
            }
            
            if (row < mapHeight -1) {
                throw new MapLoadException("Map has less rows than declared.");
            }
            
        } catch (FileNotFoundException ex) {
            throw new MapLoadException("Map file not found ("+ fileName +")", ex);
        } catch (IOException ex) {
            throw new MapLoadException("Some IO error occured...", ex);
        }
    }    
}
