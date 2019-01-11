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
import java.util.List;
import java.util.Map;
import net.trdlo.zelda.XY;

public class World {

	public static final int GRID_SIZE = 64;

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

	public static Tile defaultTile, grassTile, waterTile, roadTile;
	private static Map<Character, Identifiable> serializationDictionary = new HashMap<>();

	int mapWidth;
	int mapHeight;
	TileInstance[] tileMap;
	List<GameObjectInstance> objectInstances = new ArrayList<>();

	public World() throws IOException {
		if (defaultTile == null) {
			defaultTile = grassTile = new Grass();
			serializationDictionary.put(grassTile.getIdentifier(), grassTile);
			waterTile = new Water();
			serializationDictionary.put(waterTile.getIdentifier(), waterTile);
			roadTile = new Road();
			serializationDictionary.put(roadTile.getIdentifier(), roadTile);
			Stone stone = new Stone();
			serializationDictionary.put(stone.getIdentifier(), stone);
			Bush bush = new Bush();
			serializationDictionary.put(bush.getIdentifier(), bush);

			GameObject tree = new Tree();
			serializationDictionary.put(tree.getIdentifier(), tree);
			GameObject bird = new Bird(this);
			serializationDictionary.put(bird.getIdentifier(), bird);
		}
	}

	public void update(long time) {
		for (GameObjectInstance obj : objectInstances) {
			obj.update();
		}
	}

	public TileInstance getTileInstance(int x, int y) {
		if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) {
			return null;
		} else {
			return tileMap[x + y * mapWidth];
		}
	}

	private void setTileInstance(int x, int y, TileInstance tileInstance) {
		if (x < 0 || x >= mapWidth || y < 0 || y >= mapHeight) {
			throw new IndexOutOfBoundsException();
		} else {
			tileMap[x + y * mapWidth] = tileInstance;
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

	protected void saveToWriter(BufferedWriter writer) throws Exception {
		try {
			writer.write(String.valueOf(mapWidth) + "\n" + String.valueOf(mapHeight) + "\n");
			for (int y = 0; y < mapHeight; y++) {
				for (int x = 0; x < mapWidth; x++) {
					writer.write(tileMap[x + y * mapWidth].getTile().getIdentifier());
				}
				writer.write("\n");
			}
		} catch (IOException ex) {
			throw new Exception("Could not save, I/O error occurred.", ex);
		}
	}

	private void load(BufferedReader reader) throws IOException, MapLoadException {
		String line;
		int y = 0;
		int maxRowLength = 0;
		List<List<TileInstance>> rows = new ArrayList<>();

		while ((line = reader.readLine()) != null) {
			if (line.isEmpty()) {
				continue;
			}
			
			List<TileInstance> row = new ArrayList<>(line.length());

			for (int x = 0; x < line.length(); x++) {
				char tileChar = line.charAt(x);
				Identifiable identifiable = serializationDictionary.get(tileChar);

				Tile tile;
				if (identifiable instanceof Tile) {
					tile = (Tile) identifiable;
				} else {
					tile = defaultTile;

					if (identifiable instanceof GameObject) {
						GameObject gObject = (GameObject) identifiable;
						GameObjectInstance goInstance = gObject.getInstance(x, y, null);
						objectInstances.add(goInstance);
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
			throw new MapLoadException(String.format("Minimal map size is 8 x 8 tiles, here we have %d x %d", maxRowLength, rows.size()));
		}

		Collections.sort(objectInstances, GameObjectInstance.zIndexComparator);

		mapWidth = maxRowLength;
		mapHeight = rows.size();
		tileMap = new TileInstance[mapWidth * mapHeight];

		for (y = 0; y < mapHeight; y++) {
			List<TileInstance> row = rows.get(y);
			for (int x = 0; x < mapWidth; x++) {
				TileInstance tileInstance = (x < row.size() ? row.get(x) : defaultTile.createInstance(x, y));
				tileMap[x + y * mapWidth] = tileInstance;
			}
		}
		updateTilesNeighbours();
	}

	public void loadFromFile(File file) throws MapLoadException {
		try {
			load(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
		} catch (FileNotFoundException ex) {
			throw new MapLoadException(String.format("Map file %s not found!", file.getAbsolutePath()), ex);
		} catch (IOException ex) {
			throw new MapLoadException(String.format("IOException while loading %s!", file.getAbsolutePath()), ex);
		}
	}
}

class MapLoadException extends Exception {

	public MapLoadException(String message) {
		super(message);
	}

	public MapLoadException(String message, Throwable cause) {
		super(message, cause);
	}
}
