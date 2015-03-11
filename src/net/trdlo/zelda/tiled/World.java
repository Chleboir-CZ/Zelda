package net.trdlo.zelda.tiled;

import net.trdlo.zelda.exceptions.MapLoadException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import net.trdlo.zelda.GameObject;
import net.trdlo.zelda.GameObjectInstance;
import net.trdlo.zelda.Identifiable;
import net.trdlo.zelda.Tree;
import net.trdlo.zelda.ZWorld;


public class World extends ZWorld {

	public static final int GRID_SIZE = 32;

	private Set<Tile> tiles = new HashSet<>();
	private Tile defaultTile;
	private Set<GameObject> objects = new HashSet<>();
	List<GameObjectInstance> objectInstances = new ArrayList<>();

	TileInstance map[];
	public int mapWidth;
	public int mapHeight;

	public World(String fileName) throws MapLoadException {
		try {
			defaultTile = new Tile(true, ImageIO.read(new File("images/grass.png")), '.');
			tiles.add(defaultTile);
			tiles.add(new Tile(false, ImageIO.read(new File("images/stone.png")), '@'));
			tiles.add(new Tile(false, ImageIO.read(new File("images/water.png")), '~'));
			tiles.add(new Tile(false, ImageIO.read(new File("images/bush.png")), '#'));

			objects.add(new Tree());
			objects.add(new Bird(this));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		BufferedReader r;
		try {
			r = new BufferedReader(new FileReader(fileName));

			String line;

			line = r.readLine();
			mapWidth = Integer.parseInt(line);

			line = r.readLine();
			mapHeight = Integer.parseInt(line);

			map = new TileInstance[mapWidth * mapHeight];
			int row = 0;

			Map<Character, Identifiable> dictionary = new HashMap<>();
			for (Identifiable idable : tiles) {
				if (dictionary.put(idable.getIdentifier(), idable) != null) {
					throw new MapLoadException("Duplicate key usage: " + idable.getIdentifier());
				}
			}
			for (Identifiable idable : objects) {
				if (dictionary.put(idable.getIdentifier(), idable) != null) {
					throw new MapLoadException("Duplicate key usage: " + idable.getIdentifier());
				}
			}

			while ((line = r.readLine()) != null) {
				if (row == mapHeight) {
					throw new MapLoadException("Map has more rows than declared.");
				}

				if (line.length() != mapWidth) {
					throw new MapLoadException("Line length incorrect (" + line.length() + ") at row " + row);
				}

				for (int i = 0; i < mapWidth; i++) {
					char tileChar = line.charAt(i);
					Identifiable idable = dictionary.get(tileChar);

					Tile t;
					if (idable instanceof Tile) {
						t = (Tile) idable;
					} else {
						t = defaultTile;

						if (idable instanceof GameObject) {
							GameObject gObject = (GameObject) idable;
							GameObjectInstance goInstance = gObject.getInstance(i, row, null);
							objectInstances.add(goInstance);
						} else if (idable == null) {
							System.err.println("No object or tile defined for '" + tileChar + "'!");
						}

					}

					map[i + row * mapWidth] = new TileInstance(t, 0);
				}
				row++;
			}

			if (row < mapHeight - 1) {
				throw new MapLoadException("Map has less rows than declared.");
			}

			Collections.sort(objectInstances, GameObjectInstance.zIndexComparator);

		} catch (FileNotFoundException ex) {
			throw new MapLoadException("Map file not found (" + fileName + ")", ex);
		} catch (IOException ex) {
			throw new MapLoadException("Some IO error occured...", ex);
		}
	}

	@Override
	public void update() {
		for (GameObjectInstance obj : objectInstances) {
			obj.update();
		}
	}
}
