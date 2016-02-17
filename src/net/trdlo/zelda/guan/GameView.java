package net.trdlo.zelda.guan;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import net.trdlo.zelda.Console;
import net.trdlo.zelda.FrameFactory;
import net.trdlo.zelda.XY;
import net.trdlo.zelda.ZeldaCursor;

class GameView extends AbstractView {

	private static final Color DEFAULT_TORCH_FILL_COLOR = Color.WHITE;

	private static final ZeldaCursor DEFAULT_CURSOR = null;//ZeldaCursor

	public GameView(World world) {
		super(world);
	}

	protected void readAsynchronoutInput() {
		Player p = world.getTestPlayer();
		if (FrameFactory.getInstance().isPressed(KeyEvent.VK_LEFT)) {
			p.orientation -= 0.3;
		}
		if (FrameFactory.getInstance().isPressed(KeyEvent.VK_RIGHT)) {
			p.orientation += 0.31;
		}
		if (FrameFactory.getInstance().isPressed(KeyEvent.VK_UP)) {
			p.x += Math.cos(p.orientation) * p.speed;
			p.y += Math.sin(p.orientation) * p.speed;
		}
		if (FrameFactory.getInstance().isPressed(KeyEvent.VK_DOWN)) {
			p.x -= Math.cos(p.orientation) * p.speed;
			p.y -= Math.sin(p.orientation) * p.speed;
		}
	}

	@Override
	public void update() {
		readAsynchronoutInput();

	}

	@Override
	public void render(Graphics2D graphics, float renderFraction) {
		Player player = world.getTestPlayer();

		graphics.setColor(DEFAULT_TORCH_FILL_COLOR);
		graphics.fillPolygon(convertPointListToPoly(TorchLight.getTorchLightPolygon(world.lines, player)));

		XY p = worldToView(player);

		graphics.setColor(Color.ORANGE);
		int pWidth = (int) (16 * zoomCoef());
		graphics.fillArc(p.x - pWidth / 2, p.y - pWidth / 2, pWidth, pWidth, 0, 360);

		if ((System.nanoTime() / 50000000L % 40) == 0) {
			graphics.setStroke(Line.DEFAULT_STROKE);
			graphics.setColor(Line.DEFAULT_COLOR);
			for (Line line : world.lines) {
				graphics.drawLine(worldToViewX(line.A.x), worldToViewY(line.A.y), worldToViewX(line.B.x), worldToViewY(line.B.y));
			}
		}
	}

	@Override
	public boolean keyTyped(KeyEvent e) {
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent e) {
		return false;
	}

	@Override
	public boolean keyReleased(KeyEvent e) {
		return false;
	}

	@Override
	public ZeldaCursor getCursor(CursorType cursor) {
		return DEFAULT_CURSOR;
	}

	@Override
	public boolean mouseClicked(MouseEvent e) {
		return false;
	}

	@Override
	public boolean mousePressed(MouseEvent e) {
		return super.mousePressed(e);
	}

	@Override
	public boolean mouseReleased(MouseEvent e) {
		return super.mouseReleased(e);
	}

	@Override
	public boolean mouseEntered(MouseEvent e) {
		return false;
	}

	@Override
	public boolean mouseExited(MouseEvent e) {
		return false;
	}

	@Override
	public boolean mouseDragged(MouseEvent e) {
		return super.mouseDragged(e);
	}

	@Override
	public boolean mouseMoved(MouseEvent e) {
		return false;
	}

	@Override
	public boolean mouseWheelMoved(MouseWheelEvent e) {
		return super.mouseWheelMoved(e);
	}

	@Override
	public boolean executeCommand(String command, Console console) {
		return false;
	}

}
