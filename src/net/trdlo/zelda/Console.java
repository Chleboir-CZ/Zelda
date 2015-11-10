package net.trdlo.zelda;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
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

	private static final long MOTION_LENGTH = 750;
	private static final int CONSOLE_WIDTH = 480, CONSOLE_HEIGHT = 480, PADDING = 10;

	private static final Font FEED_FONT = new Font("Monospaced", Font.BOLD, 12);
	private static final Font UI_FONT = new Font("Monospaced", Font.BOLD, 12);
	final Stroke defaultStroke = new BasicStroke(1);

	private final List<Message> messages = new LinkedList<>();
	private StringBuilder currentCommand = new StringBuilder("Test");

	private long motionStart;
	private boolean visible = false;

	private int currentHeight;
	private boolean mouseCapture = false;

	public void render(Graphics2D graphics, float renderFraction) {
		long time = getTime();
		double showing = Math.min(1, (time - motionStart) / (double) MOTION_LENGTH);
		showing = (Math.sin(-Math.PI / 2 + showing * Math.PI) + 1) / 2;

		if (!visible) {
			showing = 1 - showing;
		}

		if (showing == 0) {
			return;
		}

		int width = CONSOLE_WIDTH;
		currentHeight = (int) (CONSOLE_HEIGHT * showing);

		//graphics.setBackground(Color.MAGENTA);
		graphics.setStroke(defaultStroke);
		graphics.setColor(Color.WHITE);
		graphics.clearRect(0, 0, width, currentHeight);
		graphics.drawRect(3, 3, width - 7, currentHeight - 7);
		graphics.setFont(UI_FONT);

		String str = "[Console]";
		FontMetrics fm = graphics.getFontMetrics();
		Rectangle2D rect = fm.getStringBounds(str, graphics);

		graphics.clearRect(width - 10 - (int) rect.getWidth() + 2, currentHeight - 10, (int) rect.getWidth() - 6, 10);

		graphics.setColor(Color.WHITE);
		graphics.drawString(str, width - 10 - (int) rect.getWidth(), currentHeight - 2);

		graphics.setFont(FEED_FONT);
		graphics.setClip(3, 3, width - 6, currentHeight - 6);

		int fontLineHeight = graphics.getFontMetrics().getHeight();
		int y = currentHeight - PADDING;

		String currentCommandStr = ">" + currentCommand.toString() + ((time / 500 & 1) == 1 ? "_" : " ");
		graphics.drawString(currentCommandStr, PADDING, y);

		y -= fontLineHeight;
		int visibleMsgCount = (currentHeight - (2 * PADDING)) / fontLineHeight;
		int i = 0;
		while (i < messages.size()) {
			Message msg = messages.get(i);
			if (msg.removeTime != 0 && msg.removeTime < time) {
				messages.remove(i);
				continue;
			}

			if (i < visibleMsgCount) {
				graphics.setColor(new Color(255, 255, 255, msg.getFadeAlpha()));
				graphics.drawString(msg.toString(), PADDING, y);
				y -= fontLineHeight;
			}
			i++;
		}
		graphics.setClip(null);
	}

	public void update() {

	}

	private static long getTime() {
		return System.nanoTime() / 1000000L;
	}

	public void echo(int lifeTime, String text) {
		Message msg = new Message(text, lifeTime);
		messages.add(0, msg);
		setVisible(true);
	}

	public void echo(int lifeTime, String format, Object... params) {
		Message msg = new Message(format, lifeTime, params);
		messages.add(0, msg);
		setVisible(true);
	}

	public void echo(String text) {
		echo(Message.DEFAULT_LIFETIME, text);
	}

	public void echo(String format, Object... params) {
		echo(Message.DEFAULT_LIFETIME, format, params);
	}

	public void clear() {
		messages.clear();
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

	public boolean keyPressed(KeyEvent e) {
		if (visible && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setVisible(false);
			return true;
		}

		return visible;
	}

	private boolean isPrintableChar(char c) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return (FEED_FONT.canDisplay(c)
				&& !Character.isISOControl(c))
				&& c != KeyEvent.CHAR_UNDEFINED
				&& block != null && block != Character.UnicodeBlock.SPECIALS;
	}

	public boolean keyTyped(KeyEvent e) {
		if (e.getKeyChar() == ';') {
			setVisible(!visible);
			return true;
		} else if (!visible) {
			return false;
		}

		char typed = e.getKeyChar();
		if (typed == '\n' && currentCommand.length() > 0) {
			echo(3000, currentCommand.toString());
			currentCommand.setLength(0);
			//TODO execute command
		}
		if (typed == '\b' && currentCommand.length() > 0) {
			currentCommand.deleteCharAt(currentCommand.length() - 1);
		} else if (isPrintableChar(typed)) {
			currentCommand.append(typed);
		}

		return true;
	}

	public boolean keyReleased(KeyEvent e) {
		return visible;
	}

	public boolean mousePressed(MouseEvent e) {
		if (visible && e.getX() <= CONSOLE_WIDTH && e.getY() <= currentHeight) {
			mouseCapture = true;
			return true;
		}

		return false;
	}

	public boolean mouseClicked(MouseEvent e) {
		return mouseCapture;
	}

	public boolean mouseReleased(MouseEvent e) {
		boolean retVal = mouseCapture;
		mouseCapture = false;
		return retVal;
	}

}
