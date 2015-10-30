package net.trdlo.zelda.guan;

public class SmartLine extends Line implements Selectable {

	public static SmartLine constructFromTwoPoints(Point A, Point B) {
		SmartLine l = new SmartLine();
		l.A = A;
		l.B = B;
		l.refreshCoefs();
		A.addConnectedLine(l);
		B.addConnectedLine(l);
		return l;
	}

	public static SmartLine constructFromPointAndNormal(Point A, double a, double b) {
		SmartLine l = new SmartLine();
		l.a = a;
		l.b = b;
		l.A = A;
		l.B = new Point(A.x - b, A.y + a);
		l.c = -a * A.x - b * A.y;
		l.A.addConnectedLine(l);
		l.B.addConnectedLine(l);
		return l;
	}

	public static SmartLine constructFromPointAndVector(Point A, double a, double b) {
		SmartLine l = new SmartLine();
		l.a = -b;
		l.b = a;
		l.A = A;
		l.B = new Point(A.x + a, A.y + b);
		l.c = b * A.x - a * A.y;
		l.A.addConnectedLine(l);
		l.B.addConnectedLine(l);
		return l;
	}

	public static SmartLine constructFromLine(Line original) {
		SmartLine sl = new SmartLine();
		sl.A = original.A;
		sl.B = original.B;
		sl.a = original.a;
		sl.b = original.b;
		sl.c = original.c;
		sl.A.addConnectedLine(sl);
		sl.B.addConnectedLine(sl);
		return sl;
	}

	protected SmartLine() {
	}

	@Override
	public void setA(Point A) {
		if (this.A != null) {
			this.A.removeConnectedLine(this);
		}
		this.A = A;
		A.addConnectedLine(this);
		refreshCoefs();
	}

	@Override
	public void setB(Point B) {
		if (this.B != null) {
			this.B.removeConnectedLine(this);
		}
		this.B = B;
		B.addConnectedLine(this);
		refreshCoefs();
	}

	/**
	 * Odregistruje se z role listenera u svývh koncových bodů
	 */
	@Override
	public void disconnect() {
		A.removeConnectedLine(this);
		B.removeConnectedLine(this);
	}
}
