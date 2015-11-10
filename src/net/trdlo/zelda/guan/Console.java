package net.trdlo.zelda.guan;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.regex.Pattern;

public class Console {

	private static class Message {

		private static final int DEFAULT_LIFETIME = 0;
		private static final int FADE_LENGTH = 1000;

		private final String format;
		private final Object[] params;
		private final long removeTime;

		public Message(String format, long lifeTime, Object... params) {
			this.format = format;
			this.params = params;
			this.removeTime = (lifeTime != 0) ? (getTime() + lifeTime) : 0;
		}

		public Message(String text, long lifeTime) {
			this(text, lifeTime, (Object[]) null);
		}

		public long getRemoveTime() {
			return removeTime;
		}

		static int ensureRange(int value, int min, int max) {
			return Math.min(Math.max(value, min), max);
		}

		public int getFadeAlpha() {
			return removeTime != 0 ? ensureRange(((int) (removeTime - getTime())) * 255 / FADE_LENGTH, 0, 255) : 255;
		}

		private static final Pattern PAT_TOKEN = Pattern.compile("\\$(\\d+)");

		@Override
		public String toString() {
			if (params == null) {
				return format;
			} else {
				return String.format(format, params);
			}
		}
	}

	private static final long MOTION_LENGTH = 1000;
	private static final int CONSOLE_WIDTH = 480, CONSOLE_HEIGHT = 480, PADDING = 10;

	private static final Font FEED_FONT = new Font("Monospaced", Font.BOLD, 12);
	private static final Font UI_FONT = new Font("Monospaced", Font.BOLD, 12);

	private final List<Message> lines = new LinkedList<>();
	private final Queue<Message> removeOrder = new PriorityQueue<>(10, new Comparator<Message>() {
		@Override
		public int compare(Message m1, Message m2) {
			return (int) (m1.getRemoveTime() - m2.getRemoveTime());
		}
	});

	private long motionStart;
	private boolean visible = false;

	final Stroke defaultStroke = new BasicStroke(1);

	public void render(Graphics2D graphics, float renderFraction) {
		double showing = Math.min(1, (getTime() - motionStart) / (double) MOTION_LENGTH);
		showing = (Math.sin(-Math.PI / 2 + showing * Math.PI) + 1) / 2;

		if (!visible) {
			showing = 1 - showing;
		}

		if (showing == 0) {
			return;
		}

		int width = CONSOLE_WIDTH;
		int height = (int) (CONSOLE_HEIGHT * showing);

		//graphics.setBackground(Color.MAGENTA);
		graphics.setStroke(defaultStroke);
		graphics.setColor(Color.WHITE);
		graphics.clearRect(0, 0, width, height);
		graphics.drawRect(3, 3, width - 7, height - 7);
		graphics.setFont(UI_FONT);

		String str = "[Console]";
		FontMetrics fm = graphics.getFontMetrics();
		Rectangle2D rect = fm.getStringBounds(str, graphics);

		graphics.clearRect(width - 10 - (int) rect.getWidth() + 2, height - 10, (int) rect.getWidth() - 6, 10);

		graphics.setColor(Color.WHITE);
		graphics.drawString(str, width - 10 - (int) rect.getWidth(), height - 2);

		graphics.setFont(FEED_FONT);

		int y = height - PADDING;
		int fontLineHeight = graphics.getFontMetrics().getHeight();
		graphics.setClip(3, 3, width - 6, height - 6);
		for (int i = lines.size() - 1; i >= Math.max(0, lines.size() - ((height - 2 * PADDING) / fontLineHeight) - 1); i--) {
			Message line = lines.get(i);
			graphics.setColor(new Color(255, 255, 255, line.getFadeAlpha()));
			graphics.drawString(line.toString(), PADDING, y);
			y -= fontLineHeight;
		}
		graphics.setClip(null);
	}

	public void update() {
		long time = getTime();
		Message m;
		while ((m = removeOrder.peek()) != null && m.getRemoveTime() < time) {
			removeOrder.remove();
			lines.remove(m);
		}
	}

	private static long getTime() {
		return System.nanoTime() / 1000000L;
	}

	public void echo(int lifeTime, String text) {
		Message msg = new Message(text, lifeTime);
		lines.add(msg);
		if (lifeTime != 0) {
			removeOrder.add(msg);
		}
		setVisible(true);
	}

	public void echo(int lifeTime, String format, Object... params) {
		Message msg = new Message(format, lifeTime, params);
		lines.add(msg);
		if (lifeTime != 0) {
			removeOrder.add(msg);
		}
		setVisible(true);
	}

	public void echo(String text) {
		echo(Message.DEFAULT_LIFETIME, text);
	}

	public void echo(String format, Object... params) {
		echo(Message.DEFAULT_LIFETIME, format, params);
	}

	public void clear() {
		lines.clear();
		removeOrder.clear();
	}

	public void setVisible(boolean value) {
		if (visible != value) {
			visible = value;
			motionStart = getTime() - Math.max(0, motionStart + MOTION_LENGTH - getTime());
		}
	}

	public boolean isVisible() {
		return visible;
	}
}
