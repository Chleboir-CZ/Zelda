package net.trdlo.zelda.tiled;

import java.awt.Graphics2D;
import java.util.Random;
import net.trdlo.zelda.ZeldaFrame;

public class BirdInstance extends GameObjectInstance {

	
	private final Bird gameObject;
	private float moveX, moveY;
	private final World world;

	private float flightSpeed;
	private float flatterSpeed;
	private int updateCounter = 0;

	private final Random r;

	public BirdInstance(Bird gameObject, float posX, float posY, World world) {
		super(posX, posY);
		this.gameObject = gameObject;
		this.world = world;
		r = new Random();

		randomSpeedBird();
	}

	private void randomPlaceBird() {
//		boolean isVert = r.nextBoolean();
//		boolean isMax = r.nextBoolean();
//		TODO: pták může začít kdekoli po obvodu

		posX = -gameObject.size; // mělo být /2, ale při kreslení neadresujeme střed, nýbrž levý horní roh
		posY = 0 + r.nextInt(world.mapHeight - 1);
	}

	private void randomSpeedBird() {
		flatterSpeed = ZeldaFrame.getUpdateCountFromHz(2.0f + r.nextFloat() * 3.0f); //udává, jakou frekvencí se přepínají snímky, ne jakou se opakuje celý cyklus
		flightSpeed = ZeldaFrame.getFrameStepFromTps(0.5f + r.nextFloat() * 1.5f);
		//směr se dává do celého kruhu, tedy i ven, namísto dovnitř mapy, pokud startuje mimo ni
		//několikanásobné přegenerování není za tak hrozné, za tu logiku to teď nestojí
		float angle = r.nextFloat() * 2 * (float) Math.PI;
		moveX = flightSpeed * (float) (Math.cos(angle));
		moveY = flightSpeed * (float) (Math.sin(angle));
	}

	private void checkMapBorders() {
		if (posX < -gameObject.size || posY < -gameObject.size || posX > world.mapWidth || posY > world.mapHeight) {
			//adresujeme levý horní roh objektu a ne jeho střed, zatím
//		if(posX < -gameObject.size / 2 || posY < -gameObject.size / 2 || posX > world.mapWidth + gameObject.size / 2 || posY > world.mapHeight + gameObject.size / 2) {
			randomSpeedBird();
			randomPlaceBird();
		}
	}

	@Override
	public void update() {
		updateCounter++;
		posX += moveX;
		posY += moveY;

		checkMapBorders();
	}

	@Override
	public float getMoveX() {
		return moveX;
	}

	@Override
	public float getMoveY() {
		return moveY;
	}

	@Override
	public void render(Graphics2D graphics, float x, float y, float renderFraction) {
		gameObject.renderFrame(graphics, x, y, (int) (updateCounter / flatterSpeed) % gameObject.FRAME_COUNT);
	}

	@Override
	public int getZIndex() {
		return gameObject.zIndex;
	}
}
