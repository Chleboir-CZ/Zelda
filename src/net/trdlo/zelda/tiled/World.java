package net.trdlo.zelda.tiled;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import net.trdlo.zelda.XY;

/**
 * The World stores the level data, both static (tiles, staticObjects) and dynamic (gameObjects).
 *
 * @author bayer
 */
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

	public static final int getOppositeDirection(int direction) {
		return direction ^ 2; //XOR magic: inverting the 2nd bit reverses the direction :-)
	}

	private File currentFile;
	
	public static Tile defaultTile, grassTile, waterTile, roadTile;
	private static Map<Character, Tile> tileDictionary = new HashMap<>();

	int width;
	int height;
	TileInstance[] tileInstances;

	private static Map<Character, GameObject> gameObjectDictionary = new HashMap<>();
	List<GameObjectInstance> objectInstances = new ArrayList<>();

	public World() throws IOException {
		if (defaultTile == null) {
			defaultTile = grassTile = new Grass();
			tileDictionary.put(grassTile.identifier, grassTile);
			waterTile = new Water();
			tileDictionary.put(waterTile.identifier, waterTile);
			roadTile = new Road();
			tileDictionary.put(roadTile.identifier, roadTile);
			Stone stone = new Stone();
			tileDictionary.put(stone.identifier, stone);
			Bush bush = new Bush();
			tileDictionary.put(bush.identifier, bush);

			GameObject tree = new Tree();
			gameObjectDictionary.put(tree.identifier, tree);
			GameObject bird = new Bird(this);
			gameObjectDictionary.put(bird.identifier, bird);
		}
	}

	public void update(long time) {
		for (GameObjectInstance obj : objectInstances) {
			obj.update();
		}
	}

	public TileInstance getTileInstance(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return null;
		} else {
			return tileInstances[x + y * width];
		}
	}

	private void setTileInstance(int x, int y, TileInstance tileInstance) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			throw new IndexOutOfBoundsException();
		} else {
			tileInstances[x + y * width] = tileInstance;
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

	private void updateTileNeighboursBiDirectional(int x, int y) {
		TileInstance tileInstance = getTileInstance(x, y);
		for (int direction = 0; direction < NEIGHBOUR_COUNT; direction++) {
			XY coord = NEIGHBOUR_COORDINATES[direction];
			TileInstance neighbour = getTileInstance(x + coord.x, y + coord.y);
			tileInstance.setNeighbour(direction, neighbour);
			if (neighbour != null) {
				neighbour.setNeighbour(getOppositeDirection(direction), tileInstance);
			}
		}
	}

	private void updateAllTilesNeighbours() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				updateTileNeighbours(x, y);
			}
		}
	}

	public void setTileInstanceUpdateNeighbours(int x, int y, TileInstance tileInstance) {
		setTileInstance(x, y, tileInstance);
		updateTileNeighboursBiDirectional(x, y);
	}

	protected void saveToWriter(BufferedWriter writer) throws Exception {
		try {
			writer.write(String.valueOf(width) + "\n" + String.valueOf(height) + "\n");
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					writer.write(tileInstances[x + y * width].getTile().identifier);
				}
				writer.write("\n");
			}
		} catch (IOException ex) {
			throw new Exception("Could not save, I/O error occurred.", ex);
		}
	}

	/**
	 * Loads the map from a string up to the first empty line. If lines differ in length, they will be padded with default tiles. The map has to be larger than
	 * 8 x 8 tiles (would probably indicate a format error)
	 *
	 * @param data
	 * @throws MapLoadException on unknown tile identifier or insufficient map size
	 */
	private void mapFromString(String data) throws MapLoadException {
		int y = 0;
		int maxRowLength = 0;
		List<List<TileInstance>> rows = new ArrayList<>();

		try (Scanner scanner = new Scanner(data)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (line.isEmpty()) {
					break;
				}

				List<TileInstance> row = new ArrayList<>(line.length());

				for (int x = 0; x < line.length(); x++) {
					char tileIdentifier = line.charAt(x);
					Tile tile = tileDictionary.get(tileIdentifier);
					if (tile == null) {
						tile = defaultTile;
						//throw new MapLoadException("No known Tile defined for '" + tileIdentifier + "'");
						GameObject gameObject = gameObjectDictionary.get(tileIdentifier);
						if (gameObject != null) {
							objectInstances.add(gameObject.getInstanceWithDefaults(x, y));
						} else {
							//throw new MapLoadException("No known Tile or GameObject defined for '" + tileIdentifier + "'");
						}

					}
					row.add(tile.createInstance(x, y));
				}
				maxRowLength = Math.max(maxRowLength, row.size());
				rows.add(row);
				y++;
			}
		}

		if (maxRowLength < 8 || rows.size() < 8) {
			throw new MapLoadException(String.format("Minimal map size is 8 x 8 tiles, here we have %d x %d", maxRowLength, rows.size()));
		}

		width = maxRowLength;
		height = rows.size();
		tileInstances = new TileInstance[width * height];

		for (y = 0; y < height; y++) {
			List<TileInstance> row = rows.get(y);
			for (int x = 0; x < width; x++) {
				TileInstance tileInstance = (x < row.size() ? row.get(x) : defaultTile.createInstance(x, y));
				tileInstances[x + y * width] = tileInstance;
			}
		}
		updateAllTilesNeighbours();
	}

	private void loadMap(BufferedReader reader) throws IOException, MapLoadException {
		StringBuilder sb = new StringBuilder();
		char[] arr = new char[8 * 1024];
		int numCharsRead;
		while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
			sb.append(arr, 0, numCharsRead);
		}
		mapFromString(sb.toString());
	}

	public void loadMapFromFile(File file) throws MapLoadException {
		try {
			loadMap(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
		} catch (FileNotFoundException ex) {
			throw new MapLoadException(String.format("Map file %s not found!", file.getAbsolutePath()), ex);
		} catch (IOException ex) {
			throw new MapLoadException(String.format("IOException while loading %s!", file.getAbsolutePath()), ex);
		}
		currentFile = file;
	}

	/**
	 *
	 * @param data
	 * @throws MapLoadException
	 */
	private void stateFromString(String data) throws MapLoadException {
		objectInstances.clear();
		try (Scanner scanner = new Scanner(data)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (line.isEmpty()) {
					break;
				}
				String[] lineParts = line.split("[^\\w]", 2);
				String gameObjectIdentifier = lineParts[0];
				String args = lineParts[1].trim();
				GameObject gameObject = gameObjectDictionary.get(gameObjectIdentifier);
				if (gameObject == null) {
					throw new MapLoadException("No known GameObject defined for '" + gameObjectIdentifier + "'");
				}

				try {
					objectInstances.add(gameObject.getInstanceFromString(args));
				} catch (IllegalArgumentException ex) {
					throw new MapLoadException("Could not load state of " + gameObjectIdentifier + " because parameter is malformed: " + args, ex);
				}
			}
		}
		Collections.sort(objectInstances, GameObjectInstance.zIndexComparator);
	}

	public String mapToString() {
		StringBuilder sb = new StringBuilder();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				sb.append(tileInstances[x + y * width].getTile().identifier);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private void saveMap(Writer writer) throws IOException {
		writer.write(mapToString());
	}

	public void saveMapToFile(File file) throws FileNotFoundException, IOException {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
			saveMap(writer);
		}
		currentFile = file;
	}

	public String stateToString() {
		StringBuilder sb = new StringBuilder();
		for (GameObjectInstance objectInstance : objectInstances) {
			sb.append(objectInstance.gameObject.identifier);
			sb.append(' ');
			sb.append(Float.floatToIntBits(objectInstance.getX()));
			sb.append(' ');
			sb.append(Float.floatToIntBits(objectInstance.getY()));
			sb.append(' ');
			sb.append(objectInstance.stateToString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public File getCurrentFile() {
		return currentFile;
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
