package net.trdlo.zelda.tiled;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import javax.imageio.ImageIO;

public class World {

	public static final int GRID_SIZE = 32;

	private Set<Tile> tiles = new HashSet<>();
	private Tile defaultTile;
	private Set<GameObject> objects = new HashSet<>();
	List<GameObjectInstance> objectInstances = new ArrayList<>();

	TileInstance map[];
	public int mapWidth;
	public int mapHeight;

	private World() throws Exception {
		try {
			defaultTile = new Tile(true, ImageIO.read(new File("images/grass.png")), '.');
			tiles.add(defaultTile);
			tiles.add(new Tile(false, ImageIO.read(new File("images/stone.png")), '@'));
			tiles.add(new Tile(false, ImageIO.read(new File("images/water.png")), '~'));
			tiles.add(new Tile(false, ImageIO.read(new File("images/bush.png")), '#'));

			objects.add(new Tree());
			objects.add(new Bird(this));
		} catch (IOException ex) {
			throw new Exception("Map could not be loaded: an I/O Exception occurred", ex);
		}
	}
	
	private Map<Character, Identifiable> buildIdentifiableDictionary() throws Exception {
		Map<Character, Identifiable> dictionary = new HashMap<>();
		for (Identifiable idable : tiles) {
			if (dictionary.put(idable.getIdentifier(), idable) != null) {
				throw new Exception("Duplicate key usage: " + idable.getIdentifier());
			}
		}
		for (Identifiable idable : objects) {
			if (dictionary.put(idable.getIdentifier(), idable) != null) {
				throw new Exception("Duplicate key usage: " + idable.getIdentifier());
			}
		}
		return dictionary;
	}

	public void update() {
		for (GameObjectInstance obj : objectInstances) {
			obj.update();
		}
	}

	protected void saveToWriter(BufferedWriter writer) throws Exception {
		try {
			writer.write(String.valueOf(mapWidth) + "\n" + String.valueOf(mapHeight) + "\n");
			for (int y = 0; y < mapHeight; y++) {
				for (int x = 0; x < mapWidth; x++) {
					writer.write(map[x + y * mapWidth].getTile().getIdentifier());
				}
				writer.write("\n");
			}
		} catch (IOException ex) {
			throw new Exception("Could not save, I/O error occurred.", ex);
		}
	}

	protected static BufferedReader getReader(File file, boolean compress) throws Exception {
		BufferedReader reader;
		if (compress) {
			try {
				reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
			} catch (FileNotFoundException ex) {
				throw new Exception("Could not load world: file not found", ex);
			} catch (IOException ex) {
				throw new Exception("Could not load world: I/O exception", ex);
			}
		} else {
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			} catch (FileNotFoundException ex) {
				throw new Exception("Could not load world: file not found", ex);
			}
		}
		return reader;
	}
	
	public static World loadFromFile(File file, boolean compress) throws Exception {
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
					throw new Exception("Map has more rows than declared.");
				}

				if (line.length() != world.mapWidth) {
					throw new Exception("Line length incorrect (" + line.length() + ") at row " + row);
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
							throw new Exception("No known object or tile defined for '" + tileChar + "' found");
						}

					}

					world.map[i + row * world.mapWidth] = new TileInstance(t, 0);
				}
				row++;
			}

			if (row < world.mapHeight - 1) {
				throw new Exception("Map has less rows than declared.");
			}

			Collections.sort(world.objectInstances, GameObjectInstance.zIndexComparator);

		} catch (IOException ex) {
			throw new Exception("Some I/O error occured.", ex);
		} catch (Exception ex) {
			throw new Exception("Could not bulid dictionaty.", ex);
		}
		
		return world;
	}
}
