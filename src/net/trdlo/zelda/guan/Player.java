package net.trdlo.zelda.guan;

import net.trdlo.zelda.NU;

public class Player {

	public Player() {
		fov = NU.degToRad(120);
		vDist = 321;
		vDistSqr = NU.sqr(vDist);
	}

	public double x, y;
	public static final int speed = 5;
	public double vx, vy;
	public double orientation;
	public double fov, vDist, vDistSqr;
}
