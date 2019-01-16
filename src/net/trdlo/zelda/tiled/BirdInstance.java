package net.trdlo.zelda.tiled;

import java.awt.Graphics2D;
import java.util.Random;
import java.util.Scanner;
import net.trdlo.zelda.ZeldaFrame;

public class BirdInstance extends GameObjectInstance {

	private final Bird bird;
	private final World world;

	private float dx, dy;
	private float flatterSpeed;

	private int updateCounter = 0;
	private final Random r;

	public BirdInstance(Bird bird, float posX, float posY, World world) {
		super(bird, posX, posY);
		this.bird = bird;
		this.world = world;
		r = new Random();
		randomSpeedBird();
	}

	private void randomPlaceBird() {
//		boolean isVert = r.nextBoolean();
//		boolean isMax = r.nextBoolean();
//		TODO: pták může začít kdekoli po obvodu

		x = -bird.size; // mělo být /2, ale při kreslení neadresujeme střed, nýbrž levý horní roh
		y = 0 + r.nextInt(world.height - 1);
	}

	public void randomSpeedBird() {
		flatterSpeed = ZeldaFrame.getUpdateCountFromHz(2.0f + r.nextFloat() * 3.0f); //udává, jakou frekvencí se přepínají snímky, ne jakou se opakuje celý cyklus
		float velocity = ZeldaFrame.getFrameStepFromTps(0.5f + r.nextFloat() * 1.5f);
		//směr se dává do celého kruhu, tedy i ven, namísto dovnitř mapy, pokud startuje mimo ni
		//několikanásobné přegenerování není za tak hrozné, za tu logiku to teď nestojí
		float angle = r.nextFloat() * 2 * (float) Math.PI;
		dx = velocity * (float) (Math.cos(angle));
		dy = velocity * (float) (Math.sin(angle));
	}

	private void checkMapBorders() {
		if (x < -bird.size || y < -bird.size || x > world.width || y > world.height) {
			//adresujeme levý horní roh objektu a ne jeho střed, zatím
//		if(posX < -gameObject.size / 2 || posY < -gameObject.size / 2 || posX > world.mapWidth + gameObject.size / 2 || posY > world.mapHeight + gameObject.size / 2) {
			randomSpeedBird();
			randomPlaceBird();
		}
	}

	@Override
	public void update() {
		updateCounter++;
		x += dx;
		y += dy;

		checkMapBorders();
	}

	@Override
	public float getDX() {
		return dx;
	}

	@Override
	public float getDY() {
		return dy;
	}

	@Override
	public void render(Graphics2D graphics, float x, float y, float renderFraction) {
		bird.renderFrame(graphics, x, y, (int) (updateCounter / flatterSpeed) % bird.FRAME_COUNT);
	}

	@Override
	public void stateFromString(String args) {
		try (Scanner scanner = new Scanner(args)) {
			dx = Float.intBitsToFloat(scanner.nextInt());
			dy = Float.intBitsToFloat(scanner.nextInt());
			flatterSpeed = Float.intBitsToFloat(scanner.nextInt());
		}
	}

	@Override
	public String stateToString() {
		return String.format("%d %d %d", Float.floatToIntBits(dx), Float.floatToIntBits(dy), Float.floatToIntBits(flatterSpeed));
	}
}
