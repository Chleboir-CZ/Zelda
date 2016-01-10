package net.trdlo.zelda.guan;

import net.trdlo.zelda.NU;

public class Player {

	public Player() {
		fov = NU.degToRad(120);
		vDist = 321;
	}

	public double x, y;
	public double vx, vy;
	public double orientation;
	public double fov, vDist;
}
