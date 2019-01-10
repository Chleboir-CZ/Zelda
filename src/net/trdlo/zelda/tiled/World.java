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
import net.trdlo.zelda.XY;

public class World {
	public static final int GRID_SIZE = 32;
	
	public static final int NEIGHBOUR_NORTH = 0;
	public static final int NEIGHBOUR_EAST = 1;
	public static final int NEIGHBOUR_SOUTH = 2;
	public static final int NEIGHBOUR_WEST = 3;
	public static final int NEIGHBOUR_NORTH_EAST = 4;
	public static final int NEIGHBOUR_SOUTH_EAST = 5;
	public static final int NEIGHBOUR_SOUTH_WEST = 6;
	public static final int NEIGHBOUR_NORTH_WEST = 7;
	public static final int NEIGHBOUR_COUNT = 8;
	public static final int FOUR_DIR_NEIGHBOUR_COUNT = 4;
	

	public static final XY[] NEIGHBOUR_COORDINATES = {
		new XY(0, -1),
		new XY(1, 0),
		new XY(0, 1),
		new XY(-1, 0),
		new XY(1, -1),
		new XY(1, 1),
		new XY(-1, 1),
		new XY(-1, -1)
	};
	
	
	public final Tile defaultTile;
	public final Tile waterTile;
	private Set<Tile> tileLibrary = new HashSet<>();

	private Set<GameObject> gameObjectLibrary = new HashSet<>();

	TileInstance map[];
	public int mapWidth;
	public int mapHeight;
	List<GameObjectInstance> objectInstances = new ArrayList<>();

	private World() throws Exception {
		try {
			tileLibrary.add(defaultTile = new Grass());
			tileLibrary.add(waterTile = new Water());
			tileLibrary.add(new Stone());
			tileLibrary.add(new Bush());

			gameObjectLibrary.add(new Tree());
			gameObjectLibrary.add(new Bird(this));
		} catch (IOException ex) {
			throw new Exception("Map could not be loaded: an I/O Exception occurred", ex);
		}
	}

	private Map<Character, Identifiable> buildIdentifiableDictionary() throws Exception {
		Map<Character, Identifiable> dictionary = new HashMap<>();
		for (Identifiable idable : tileLibrary) {
			if (dictionary.put(idable.getIdentifier(), idable) != null) {
				throw new Exception("Duplicate key usage: " + idable.getIdentifier());
			}
		}
		for (Identifiable idable : gameObjectLibrary) {
			if (dictionary.put(idable.getIdentifier(), idable) != null) {
				throw new Exception("Duplicate key usage: " + idable.getIdentifier());
			}
		}
		return dictionary;
	}

	public void update(long time) {
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

	public TileInstance getTileInstance(int x, int y) {
		if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) {
			return null;
		} else {
			return map[x + y * mapWidth];
		}
	}

	private void setTileInstance(int x, int y, TileInstance tileInstance) {
		if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) {
			throw new IndexOutOfBoundsException();
		} else {
			map[x + y * mapWidth] = tileInstance;
		}
	}
	
	private void updateTileNeighbours(int x, int y) {
		TileInstance tileInstance = getTileInstance(x, y);
		for (int direction = 0; direction < NEIGHBOUR_COUNT; direction++) {
			XY coord = NEIGHBOUR_COORDINATES[direction];
			TileInstance neighbour = getTileInstance(x + coord.x, y + coord.y);
			tileInstance.setNeighbour(direction, neighbour);
		}
	}
	
	private void updateTilesNeighbours() {
		for (int y = 0; y < mapHeight; y++) {
			for (int x = 0; x < mapWidth; x++) {
				updateTileNeighbours(x, y);
			}
		}
	}

	public void setTile(int x, int y, TileInstance tileInstance) {
		setTileInstance(x, y, tileInstance);
		updateTileNeighbours(x, y);
		for (int direction = 0; direction < NEIGHBOUR_COUNT; direction++) {
			XY coord = NEIGHBOUR_COORDINATES[direction];
			if (x + coord.x >= 0 && x + coord.x < mapWidth && y + coord.y >= 0 && y + coord.y < mapHeight) {
				updateTileNeighbours(x + coord.x, y + coord.y);
			}
		}
	}

	public static World loadFromFile(File file, boolean compress) throws Exception {
		BufferedReader reader = getReader(file, compress);
		World world = new World();

		try {
			String line;
			int y = 0;
			int maxRowLength = 0;
			List<List<TileInstance>> rows = new ArrayList<>();

			Map<Character, Identifiable> dictionary = world.buildIdentifiableDictionary();

			while ((line = reader.readLine()) != null) {
				List<TileInstance> row = new ArrayList<>(line.length());

				for (int x = 0; x < line.length(); x++) {
					char tileChar = line.charAt(x);
					Identifiable identifiable = dictionary.get(tileChar);

					Tile tile;
					if (identifiable instanceof Tile) {
						tile = (Tile) identifiable;
					} else {
						tile = world.defaultTile;

						if (identifiable instanceof GameObject) {
							GameObject gObject = (GameObject) identifiable;
							GameObjectInstance goInstance = gObject.getInstance(x, y, null);
							world.objectInstances.add(goInstance);
						} else {
							//throw new Exception("No known object or tile defined for '" + tileChar + "' found");
						}

					}
					row.add(tile.createInstance(x, y));
					//world.map[x + rowNumber * world.mapWidth] = new TileInstance(t, 0);
				}
				maxRowLength = Math.max(maxRowLength, row.size());
				rows.add(row);
				y++;
			}
			if (maxRowLength < 8 || rows.size() < 8) {
				throw new Exception("Minimal map size is 8 x 8 tiles, here we have " + maxRowLength + " x " + rows.size());
			}

			Collections.sort(world.objectInstances, GameObjectInstance.zIndexComparator);

			world.mapWidth = maxRowLength;
			world.mapHeight = rows.size();
			world.map = new TileInstance[world.mapWidth * world.mapHeight];

			for (y = 0; y < world.mapHeight; y++) {
				List<TileInstance> row = rows.get(y);
				for (int x = 0; x < world.mapWidth; x++) {
					TileInstance tileInstance = (x < row.size() ? row.get(x) : world.defaultTile.createInstance(x, y));
					world.setTileInstance(x, y, tileInstance);
				}
			}
			world.updateTilesNeighbours();
		} catch (IOException ex) {
			throw new Exception("Some I/O error occured.", ex);
		} catch (Exception ex) {
			throw new Exception("Could not load map.", ex);
		}

		return world;
	}
}
