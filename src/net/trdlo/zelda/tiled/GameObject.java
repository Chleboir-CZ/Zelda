package net.trdlo.zelda.tiled;

import java.util.NoSuchElementException;
import java.util.Scanner;

public abstract class GameObject {

	public final char identifier;
	public final float size;
	public final int defaultZIndex;

	public static final int Z_GROUND = 0;
	public static final int Z_OVERHEAD = 1;
	public static final int Z_SPITHEIGHT = 2;
	public static final int Z_BIRDSPACE = 3;

	public GameObject(char identifier, float size, int defaultZIndex) {
		this.identifier = identifier;
		this.size = size;
		this.defaultZIndex = defaultZIndex;
	}

	public boolean isColliding() {
		return false;
	}

	public abstract GameObjectInstance getInstance(float x, float y, String args);

	public abstract GameObjectInstance getInstanceWithDefaults(float x, float y);

	public GameObjectInstance getInstanceFromString(String args) throws IllegalArgumentException {
		float x, y;
		String otherArgs;

		try (Scanner scanner = new Scanner(args)) {
			x = Float.intBitsToFloat(scanner.nextInt());
			y = Float.intBitsToFloat(scanner.nextInt());

			scanner.useDelimiter("\n");
			otherArgs = scanner.next();
		} catch (NoSuchElementException | IllegalStateException ex) {
			throw new IllegalArgumentException(ex);
		}

		return getInstance(x, y, otherArgs);
	}
}
