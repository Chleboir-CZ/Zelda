package net.trdlo.zelda.guan;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import net.trdlo.zelda.Console;
import net.trdlo.zelda.NU;
import net.trdlo.zelda.XY;
import net.trdlo.zelda.ZeldaFrame;

class GameView extends AbstractView {

	private static final java.awt.Cursor DEFAULT_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new java.awt.Point(0, 0), "blank");

	private BufferedImage gradientImage = null;

	public GameView(World world) {
		super(world);
	}

	protected void readAsynchronoutInput() {
		Player p = world.getTestPlayer();
		if (ZeldaFrame.getInstance().isPressed(KeyEvent.VK_LEFT)) {
			p.orientation -= 0.3;
		}
		if (ZeldaFrame.getInstance().isPressed(KeyEvent.VK_RIGHT)) {
			p.orientation += 0.31;
		}
		if (ZeldaFrame.getInstance().isPressed(KeyEvent.VK_UP)) {
			p.x += Math.cos(p.orientation) * p.speed;
			p.y += Math.sin(p.orientation) * p.speed;
		}
		if (ZeldaFrame.getInstance().isPressed(KeyEvent.VK_DOWN)) {
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

		//TODO: bude se renderovat jen do čtverce s baterkou
		for (Texture t : world.textures) {
			XY vPos = worldToView(t.getPosition());
			Image img = t.getImage();
			graphics.drawImage(img, vPos.x, vPos.y, (int) (img.getWidth(null) * zoomCoef()), (int) (img.getHeight(null) * zoomCoef()), null);
		}

		int imgHalfSize = (int) (Math.ceil(player.vDist) * zoomCoef());
		int imgSize = imgHalfSize * 2;

		BufferedImage torchImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
		Graphics2D tig = (Graphics2D) torchImage.getGraphics();

		tig.setColor(Color.BLACK);
		tig.fillRect(0, 0, imgSize, imgSize);

		tig.setComposite(AlphaComposite.Src);
		tig.setColor(new Color(0, 0, 0, 0));
		List<Point> pointList = TorchLight.getPointList(world.lines, player);
		tig.fillPolygon(convertPointListToImagePoly(pointList, player));
		tig.setComposite(AlphaComposite.SrcOver);

		if (gradientImage == null || gradientImage.getWidth() != imgSize) {
			gradientImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
			for (int pixY = 0; pixY < imgSize; pixY++) {
				for (int pixX = 0; pixX < imgSize; pixX++) {
					int alpha = (int) (Math.max(Math.sqrt(NU.sqr(pixX - imgHalfSize) + NU.sqr(pixY - imgHalfSize)) / imgHalfSize, 0) * 255);
					int color = alpha << 24;
					gradientImage.setRGB(pixX, pixY, color);
				}
			}
		}
		tig.drawImage(gradientImage, null, 0, 0);
		tig.dispose();

		XY p = worldToView(player);
		graphics.drawImage(torchImage, p.x - imgHalfSize, p.y - imgHalfSize, null);

		graphics.setColor(Color.ORANGE);
		int pWidth = (int) (16 * zoomCoef());
		graphics.fillArc(p.x - pWidth / 2, p.y - pWidth / 2, pWidth, pWidth, 0, 360);

		/*if ((System.nanoTime() / 50000000L % 40) == 0) {
			graphics.setStroke(Line.DEFAULT_STROKE);
			graphics.setColor(Line.DEFAULT_COLOR);
			for (Line line : world.lines) {
				graphics.drawLine(worldToViewX(line.A.x), worldToViewY(line.A.y), worldToViewX(line.B.x), worldToViewY(line.B.y));
			}
		}*/
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
	public java.awt.Cursor getCursor(Cursor cursor) {
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
