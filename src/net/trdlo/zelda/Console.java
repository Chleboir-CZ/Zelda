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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Console implements CommandExecuter {

	private static class Message {

		private static final int DEFAULT_LIFETIME = 1000 * 60 * 5;
		private static final int FADE_LENGTH = 1000;

		private final String format;
		private final Object[] params;
		private final long removeTime;

		public Message(String format, long lifeTime, Object... params) {
			this.format = format;
			this.params = params;
			this.removeTime = (lifeTime != 0) ? (getTime() + lifeTime) : 0;
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
	private static final int CONSOLE_MIN_WIDTH = 480, CONSOLE_HEIGHT = 480, PADDING = 10;

	private static final Font FEED_FONT = new Font("Monospaced", Font.BOLD, 12);
	private static final Font UI_FONT = new Font("Monospaced", Font.BOLD, 12);
	private static final Stroke DEFAULT_STROKE = new BasicStroke(1);

	private final List<Message> messages = new LinkedList<>();
	private final StringBuilder currentCommand = new StringBuilder();
	private final List<CommandExecuter> executers = new ArrayList<>();

	private long motionStart;
	private boolean visible = false;

	private int currentHeight;
	private boolean mouseCapture = false;
	private boolean mouseCaptureClick = false;

	private int currentWidth = CONSOLE_MIN_WIDTH;
	private int targetWidth = CONSOLE_MIN_WIDTH;
	
	private static Console instance;

	private final List<String> commandHistory = new LinkedList<>();
	private int historyLookupIndex = -1;
	private String unfinishedCommand;

	private int cursorPosition = 0;

	public static Console getInstance() {
		if (instance == null) {
			instance = new Console();
			instance.addCommandExecuter(instance);
		}
		return instance;
	}

	private Console() {

	}

	public void render(Graphics2D graphics, float renderFraction) {
		long time = getTime();
		double fractionShowing = Math.min(1, (time - motionStart) / (double) MOTION_LENGTH);
		fractionShowing = (Math.sin(-Math.PI / 2 + fractionShowing * Math.PI) + 1) / 2;

		if (!visible) {
			fractionShowing = 1 - fractionShowing;
		}

		if (fractionShowing == 0) {
			return;
		}

		currentWidth = targetWidth; //TODO animate!
		
		currentHeight = (int) (CONSOLE_HEIGHT * fractionShowing);

		graphics.setStroke(DEFAULT_STROKE);
		graphics.setColor(Color.WHITE);
		graphics.clearRect(0, 0, currentWidth, currentHeight);
		graphics.drawRect(3, 3, currentWidth - 7, currentHeight - 7);
		graphics.setFont(UI_FONT);

		String str = "[Console]";
		FontMetrics fontMetrics = graphics.getFontMetrics();
		Rectangle2D rect = fontMetrics.getStringBounds(str, graphics);

		graphics.clearRect(currentWidth - 10 - (int) rect.getWidth() + 2, currentHeight - 10, (int) rect.getWidth() - 6, 10);
		graphics.setColor(Color.WHITE);
		graphics.drawString(str, currentWidth - 10 - (int) rect.getWidth(), currentHeight - 2);

		graphics.setFont(FEED_FONT);
		graphics.setClip(3, 3, currentWidth - 6, currentHeight - 6);

		int fontLineHeight = graphics.getFontMetrics().getHeight();
		int y = currentHeight - PADDING;

		String currentCommandStr = ">" + currentCommand.toString();
		String stringBeforeCursor = currentCommandStr.substring(0, cursorPosition + 1);
		graphics.drawString(currentCommandStr, PADDING, y);
		graphics.drawString("_", PADDING + fontMetrics.stringWidth(stringBeforeCursor), y);

		y -= fontLineHeight;
		int remainingVisibleLines = (currentHeight - (2 * PADDING)) / fontLineHeight;
		List<String> lines = new ArrayList<>();
		targetWidth = CONSOLE_MIN_WIDTH - 2 * PADDING;

		MSG_CYCLE:
		for (Message msg : messages) {
			try (Scanner scanner = new Scanner(msg.toString())) {
				while (scanner.hasNextLine()) {
					lines.add(scanner.nextLine());
				}
			}
			for (int i = lines.size() - 1; i >= 0; i--) {
				graphics.setColor(new Color(255, 255, 255, msg.getFadeAlpha()));
				String line = lines.get(i);
				graphics.drawString(line, PADDING, y);
				targetWidth = Math.max(targetWidth, fontMetrics.stringWidth(line));
				y -= fontLineHeight;
				if (--remainingVisibleLines == 0) {
					break MSG_CYCLE;
				}
			}
			lines.clear();
		}
		graphics.setClip(null);
		targetWidth += 2 * PADDING;
	}

	public void update() {
		long time = getTime();
		messages.removeIf(msg -> msg.removeTime != 0 && msg.removeTime < time);
	}

	private static long getTime() {
		return System.nanoTime() / 1000000L;
	}

	public void echo(int lifeTime, String format, Object... params) {
		Message msg = new Message(format, lifeTime, params);
		messages.add(0, msg);
		setVisible(true);
	}

	public void echo(int lifeTime, String text) {
		echo(lifeTime, text, (Object[]) null);
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

	private void setVisible(boolean value) {
		if (visible != value) {
			visible = value;
			motionStart = getTime() - Math.max(0, motionStart + MOTION_LENGTH - getTime());
			if (value) {
				ZeldaFrame.getInstance().clearPressedKeys();
			}
		}
	}

	private boolean isIncidentalWithConsole(int x, int y) {
		return visible && x <= CONSOLE_MIN_WIDTH && y <= currentHeight;
	}

	private static final Pattern PAT_CLEAR = Pattern.compile("^\\s*clear\\s*\\z", Pattern.CASE_INSENSITIVE);
	private static final Pattern PAT_HELP = Pattern.compile("^\\s*help\\s*(.*)\\s*\\z", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean executeCommand(String command, Console console) {
		Matcher m;
		if (PAT_CLEAR.matcher(command).matches()) {
			messages.clear();
		} else if ((m = PAT_HELP.matcher(command)).matches()) {
			for (CommandExecuter executer : executers) {
				executer.listCommands(m.group(1), console);
			}
		} else {
			return false;
		}

		return true;
	}

	@Override
	public void listCommands(String command, Console console) {
		if (command.isEmpty()) {
			console.echo("== console commands ==");
			console.echo("clear");
			console.echo("help");
		} else if (PAT_CLEAR.matcher(command).matches()) {
			console.echo("clear clears the console");
		} else if (PAT_HELP.matcher(command).matches()) {
			console.echo("help displays the list of known commands. If a command parameter is specified, details of it are displayed.");
		}
	}

	private static boolean isPrintableChar(char c) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return (FEED_FONT.canDisplay(c)
				&& !Character.isISOControl(c))
				&& c != KeyEvent.CHAR_UNDEFINED
				&& block != null && block != Character.UnicodeBlock.SPECIALS;
	}

	private void executeCommand(String command) {
		commandHistory.add(0, command);
		for (CommandExecuter executer : executers) {
			if (executer.executeCommand(command, this)) {
				return;
			}
		}
		echo(5000, "Unknown or malformed command \"" + command + "\"");
	}

	public void addCommandExecuter(CommandExecuter executer) {
		executers.add(executer);
	}

	public boolean keyTyped(KeyEvent e) {
		char typed = e.getKeyChar();

		if (typed == ';') {
			setVisible(!visible);
			return true;
		} else if (!visible) {
			return false;
		}

		if (typed == '\n' && currentCommand.length() > 0) {
			String command = currentCommand.toString();
			echo(command);
			executeCommand(command);
			currentCommand.setLength(0);
			historyLookupIndex = -1;
			cursorPosition = 0; //currentCommand + znak na začátku řádku
		} else if (typed == '\b' && currentCommand.length() > 0 && cursorPosition > 0) {
			currentCommand.deleteCharAt(cursorPosition - 1);
			cursorPosition--;
		} else if (typed == 127 /* DEL */ && currentCommand.length() > 0 && cursorPosition < currentCommand.length()) {
			currentCommand.deleteCharAt(cursorPosition);
		} else if (isPrintableChar(typed)) {
			currentCommand.insert(cursorPosition, typed);
			cursorPosition++;
		}

		return true;
	}

	public boolean keyPressed(KeyEvent e) {
		if (visible) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_ESCAPE) {
				setVisible(false);
				return true;
			} else if (code == KeyEvent.VK_UP) {
				if (historyLookupIndex < commandHistory.size() - 1) {
					if (historyLookupIndex == -1) {
						unfinishedCommand = currentCommand.toString();
					}
					historyLookupIndex++;
					currentCommand.setLength(0);
					currentCommand.append(commandHistory.get(historyLookupIndex));
					cursorPosition = currentCommand.length();
				}
			} else if (code == KeyEvent.VK_DOWN) {
				if (historyLookupIndex > -1) {
					historyLookupIndex--;
					currentCommand.setLength(0);
					if (historyLookupIndex == -1) {
						currentCommand.append(unfinishedCommand);
					} else {
						currentCommand.append(commandHistory.get(historyLookupIndex));
					}
					cursorPosition = currentCommand.length();
				}
			} else if (code == KeyEvent.VK_LEFT && cursorPosition > 0) {
				cursorPosition--;
			} else if (code == KeyEvent.VK_RIGHT && cursorPosition < currentCommand.length()) {
				cursorPosition++;
			} else if (code == KeyEvent.VK_HOME) {
				cursorPosition = 0;
			} else if (code == KeyEvent.VK_END) {
				cursorPosition = currentCommand.length();
			}
		}
		return visible;
	}

	public boolean keyReleased(KeyEvent e) {
		return visible;
	}

	public boolean mousePressed(MouseEvent me) {
		mouseCapture = isIncidentalWithConsole(me.getX(), me.getY());
		//echo("mousePressed at [%d; %d], %s", me.getX(), me.getY(), mouseCapture ? "capturing" : "ignoring" );
		return mouseCapture;
	}

	public boolean mouseReleased(MouseEvent me) {
		//echo("mouseReleased at [%d; %d], %s", me.getX(), me.getY(), mouseCapture ? "capturing" : "ignoring" );
		mouseCaptureClick = mouseCapture;
		boolean retVal = mouseCapture;
		mouseCapture = false;
		return retVal;
	}

	public boolean mouseClicked(MouseEvent me) {
		//echo("mouseClicked at [%d; %d], %s", me.getX(), me.getY(), mouseCaptureClick ? "capturing" : "ignoring");
		boolean retVal = mouseCaptureClick;
		mouseCaptureClick = false;
		return retVal;
	}

	public boolean mouseDragged(MouseEvent me) {
		//echo(1000, "mouseDragged at [%d; %d], %s", me.getX(), me.getY(), mouseCapture ? "capturing" : "ignoring" );
		return mouseCapture;
	}

}
