package net.trdlo.zelda.guan;

import net.trdlo.zelda.NU;

public class Player {

	public Player() {
		x = -419.52863948359635;
		y = -81.44728225361818;
		orientation = -0.7;
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
