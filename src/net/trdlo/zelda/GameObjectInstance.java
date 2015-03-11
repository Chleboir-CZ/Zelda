package net.trdlo.zelda;

import java.awt.Graphics2D;
import java.util.Comparator;


public abstract class GameObjectInstance {
	protected float posX, posY;

	public GameObjectInstance(float posX, float posY) {
		this.posX = posX;
		this.posY = posY;
	}
	
	public void update() {
		
	}

	public float getPosX() {
		return posX;
	}

	public float getPosY() {
		return posY;
	}

	public float getMoveX() {
		return 0;
	}

	public float getMoveY() {
		return 0;
	}

	public abstract void render(Graphics2D graphics, float x, float y, float renderFraction);
	
	public abstract int getZIndex();
	
	public static Comparator<GameObjectInstance> zIndexComparator = new Comparator<GameObjectInstance>() {
		@Override
	    public int compare(GameObjectInstance go1, GameObjectInstance go2) {
			return go1.getZIndex() - go2.getZIndex();
	    }
	};
}
