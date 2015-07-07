package net.trdlo.zelda.tiled;

import net.trdlo.zelda.exceptions.MapLoadException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import net.trdlo.zelda.ZWorld;
import net.trdlo.zelda.exceptions.ZException;

public class World extends ZWorld {

	public static final int GRID_SIZE = 32;

	private Set<Tile> tiles = new HashSet<>();
	private Tile defaultTile;
	private Set<GameObject> objects = new HashSet<>();
	List<GameObjectInstance> objectInstances = new ArrayList<>();

	TileInstance map[];
	public int mapWidth;
	public int mapHeight;

	private World() throws MapLoadException {
		try {
			defaultTile = new Tile(true, ImageIO.read(new File("images/grass.png")), '.');
			tiles.add(defaultTile);
			tiles.add(new Tile(false, ImageIO.read(new File("images/stone.png")), '@'));
			tiles.add(new Tile(false, ImageIO.read(new File("images/water.png")), '~'));
			tiles.add(new Tile(false, ImageIO.read(new File("images/bush.png")), '#'));

			objects.add(new Tree());
			objects.add(new Bird(this));
		} catch (IOException ex) {
			throw new MapLoadException("Map could not be loaded: an I/O Exception occurred", ex);
		}
	}
	
	private Map<Character, Identifiable> buildIdentifiableDictionary() throws ZException {
		Map<Character, Identifiable> dictionary = new HashMap<>();
		for (Identifiable idable : tiles) {
			if (dictionary.put(idable.getIdentifier(), idable) != null) {
				throw new ZException("Duplicate key usage: " + idable.getIdentifier());
			}
		}
		for (Identifiable idable : objects) {
			if (dictionary.put(idable.getIdentifier(), idable) != null) {
				throw new ZException("Duplicate key usage: " + idable.getIdentifier());
			}
		}
		return dictionary;
	}

	@Override
	public void update() {
		for (GameObjectInstance obj : objectInstances) {
			obj.update();
		}
	}

	@Override
	protected void saveToWriter(BufferedWriter writer) throws ZException {
		try {
			writer.write(String.valueOf(mapWidth) + "\n" + String.valueOf(mapHeight) + "\n");
			for (int y = 0; y < mapHeight; y++) {
				for (int x = 0; x < mapWidth; x++) {
					writer.write(map[x + y * mapWidth].getTile().getIdentifier());
				}
				writer.write("\n");
			}
		} catch (IOException ex) {
			throw new ZException("Could not save, I/O error occurred.", ex);
		}
	}
	
	public static World loadFromFile(File file, boolean compress) throws ZException {
		BufferedReader reader = getReader(file, compress);
		World world = new World();
		
		try {
			String line;

			line = reader.readLine();
			world.mapWidth = Integer.parseInt(line);

			line = reader.readLine();
			world.mapHeight = Integer.parseInt(line);

			world.map = new TileInstance[world.mapWidth * world.mapHeight];
			int row = 0;

			Map<Character, Identifiable> dictionary = world.buildIdentifiableDictionary();

			while ((line = reader.readLine()) != null) {
				if (row == world.mapHeight) {
					throw new MapLoadException("Map has more rows than declared.");
				}

				if (line.length() != world.mapWidth) {
					throw new MapLoadException("Line length incorrect (" + line.length() + ") at row " + row);
				}

				for (int i = 0; i < world.mapWidth; i++) {
					char tileChar = line.charAt(i);
					Identifiable idable = dictionary.get(tileChar);

					Tile t;
					if (idable instanceof Tile) {
						t = (Tile) idable;
					} else {
						t = world.defaultTile;

						if (idable instanceof GameObject) {
							GameObject gObject = (GameObject) idable;
							GameObjectInstance goInstance = gObject.getInstance(i, row, null);
							world.objectInstances.add(goInstance);
						} else if (idable == null) {
							throw new MapLoadException("No known object or tile defined for '" + tileChar + "' found");
						}

					}

					world.map[i + row * world.mapWidth] = new TileInstance(t, 0);
				}
				row++;
			}

			if (row < world.mapHeight - 1) {
				throw new MapLoadException("Map has less rows than declared.");
			}

			Collections.sort(world.objectInstances, GameObjectInstance.zIndexComparator);

		} catch (IOException ex) {
			throw new MapLoadException("Some I/O error occured.", ex);
		} catch (ZException ex) {
			throw new MapLoadException("Could not bulid dictionaty.", ex);
		}
		
		return world;
	}
}
