package net.trdlo.zelda.notiles;

public class WorldLine extends Line {

	/**
	 * Prázdný konstruktor používaný jen místními statickými továrními metodami
	 */
	private WorldLine() {
	}

	public static WorldLine constructFromTwoPoints(Point A, Point B) {
		WorldLine l = new WorldLine();
		l.A = A;
		l.B = B;
		l.refreshCoefs();
		l.A.addChangeListener(l);
		l.B.addChangeListener(l);
		return l;
	}

	public static WorldLine constructFromPointAndNormal(Point A, double a, double b) {
		WorldLine l = new WorldLine();
		l.a = a;
		l.b = b;
		l.A = A;
		l.B = new Point(A.x - b, A.y + a);
		l.c = -a * A.x - b * A.y;
		l.A.addChangeListener(l);
		l.B.addChangeListener(l);
		return l;
	}

	public static WorldLine constructFromPointAndVector(Point A, double a, double b) {
		WorldLine l = new WorldLine();
		l.a = -b;
		l.b = a;
		l.A = A;
		l.B = new Point(A.x + a, A.y + b);
		l.c = b * A.x - a * A.y;
		l.A.addChangeListener(l);
		l.B.addChangeListener(l);
		return l;
	}

	@Override
	public void setA(Point A) {
		if (this.A != null) {
			this.A.removeChangeListener(this);
		}
		this.A = A;
		A.addChangeListener(this);
		refreshCoefs();
	}

	@Override
	public void setB(Point B) {
		if (this.B != null) {
			this.B.removeChangeListener(this);
		}
		this.B = B;
		B.addChangeListener(this);
		refreshCoefs();
	}


	/**
	 * Odregistruje se z role listenera u svývh koncových bodů
	 */
	public void unregister() {
		A.removeChangeListener(this);
		B.removeChangeListener(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WorldLine");
		sb.append(this.A.toString());
		sb.append(";");
		sb.append(B.toString());
		sb.append("\n");
		return sb.toString();
	}
}



