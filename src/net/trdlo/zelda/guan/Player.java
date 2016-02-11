package net.trdlo.zelda.guan;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.trdlo.zelda.NU;

public class Player {

	public static final Pattern PAT_PLAYER = Pattern.compile("^\\s*Player\\s*\\[\\s*([-+]?\\d*\\.?\\d+)\\s*;\\s*([-+]?\\d*\\.?\\d+)\\s*\\]\\s*([-+]?\\d*\\.?\\d+)(?:\\s*(\\d*\\.?\\d+))?", Pattern.CASE_INSENSITIVE);
	public double x, y;
	public int speed;
	public double vx, vy;
	public double orientation;
	public double fov, vDist, vDistSqr;

	private static final double DEFAULT_FOV = NU.degToRad(120);

	public Player() {
		fov = DEFAULT_FOV;
		vDist = 321;
		vDistSqr = NU.sqr(vDist);
		speed = 5;
	}

	public Player(double x, double y, double orientation, double fov) {
		this();
		this.x = x;
		this.y = y;
		this.orientation = orientation;
		this.fov = fov;
	}

	static boolean lineMatchesPattern(String line) {
		return PAT_PLAYER.matcher(line).matches();
	}

	static Player loadFromString(String line) {
		Matcher m = PAT_PLAYER.matcher(line);
		if (m.matches()) {
			String fovStr = m.group(4);
			return new Player(Double.valueOf(m.group(1)), Double.valueOf(m.group(2)), Double.valueOf(m.group(3)), fovStr != null ? NU.degToRad(Integer.valueOf(fovStr)) : DEFAULT_FOV);
		} else {
			throw new IllegalArgumentException("Supplied line does not match pattern.");
		}
	}

	String saveToString() {
		return "Player [" + String.format(Locale.ENGLISH, "%f", x) + ";" + String.format(Locale.ENGLISH, "%f", y) + "] " + orientation + " " + NU.radToDeg(fov);
	}
}
