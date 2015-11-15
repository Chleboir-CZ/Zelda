package net.trdlo.zelda.guan;

import java.util.regex.Pattern;

public class Line {

	public static final Pattern PAT_LINE = Pattern.compile("^\\s*Line\\s+(\\d+)\\s+(\\d+)\\s*\\z", Pattern.CASE_INSENSITIVE);

	public static Line constructFromTwoPoints(Point A, Point B) {
		Line l = new Line();
		l.A = A;
		l.B = B;
		l.refreshCoefs();
		return l;
	}

	public static Line constructFromPointAndNormal(Point A, double a, double b) {
		Line l = new Line();
		l.a = a;
		l.b = b;
		l.A = A;
		l.B = new Point(A.x - b, A.y + a);
		l.c = -a * A.x - b * A.y;
		return l;
	}

	public static Line constructFromPointAndVector(Point A, double a, double b) {
		Line l = new Line();
		l.a = -b;
		l.b = a;
		l.A = A;
		l.B = new Point(A.x + a, A.y + b);
		l.c = b * A.x - a * A.y;
		return l;
	}

	protected Point A, B;
	protected double a, b, c;

	/**
	 * Prázdný konstruktor používaný jen místními statickými továrními metodami
	 */
	protected Line() {
	}

	public final void refreshCoefs() {
		a = A.y - B.y;
		b = B.x - A.x;
		c = -a * A.x - b * A.y;
	}

	public Point getA() {
		return A;
	}

	public Point getB() {
		return B;
	}

	public void setA(Point A) {
		this.A = A;
		refreshCoefs();
	}

	public void setB(Point B) {
		this.B = B;
		refreshCoefs();
	}

	/**
	 * Nalezne průsečík této a jiné přímky nebo této a jiné úsečky
	 *
	 * @param line	druhá přímka
	 * @param treatAsSegments	zda mají být přímky chápány jako úsečky
	 * @return	bod, kde se protnou, nebo null, pokud se neprotnou
	 */
	public Point getIntersection(Line line, boolean treatAsSegments) {
		double denominator = (a * line.b - line.a * b);
		if (denominator == 0) {
			return null;
		}
		return new Point((b * line.c - c * line.b) / denominator, -(a * line.c - line.a * c) / denominator);
	}
	
	public boolean contains(Point p) {
		return a * p.x + b * p.y + c == 0; //Math.abs(a * p.x + b * p.y + c) < World.MINIMAL_DETECTABLE_DISTANCE;
	}

	/**
	 *
	 * @param p
	 * @param treatAsSegment
	 * @return
	 */
	public double getDistance(Point p, boolean treatAsSegment) {
		
		return 0;
	}

	/**
	 *
	 * @param l
	 * @param treatAsSegments
	 * @return
	 */
	public double getDistance(Line l, boolean treatAsSegments) {

		return 0;
	}

	/**
	 * Vypočítá úhel, který svírají tato přímka s další
	 *
	 * @param line	druhá přímka
	 * @return	úhel v radiánech v protisměru hodinových ručiček, který tato přímka svírá s druhou dodanou
	 */
	public double getAngle(Line line) {
		return Math.acos((a * line.a + b * line.b) / (Math.sqrt(a * a + b * b) * Math.sqrt(line.a * line.a + line.b * line.b)));
	}

	/**
	 * Vytvoří zrcadlový obraz dodané úsečky podle sebe
	 *
	 * @param original	dodaná úsečka
	 * @return			úsečka symetrická přes tuto přímku
	 */
	public Line reflect(Line original) {
		Point SA = getIntersection(Line.constructFromPointAndVector(original.A, a, b), false);
		Point AR = new Point(2 * SA.x - original.A.x, 2 * SA.y - original.A.y);
		Point SB = getIntersection(Line.constructFromPointAndVector(original.B, a, b), false);
		Point BR = new Point(2 * SB.x - original.B.x, 2 * SB.y - original.B.y);
		return Line.constructFromTwoPoints(AR, BR);
	}

	/**
	 * Vytvoří obraz dodané úsečky přes kolmici na průnik se sebou
	 *
	 * @param original	dodaná úsečka
	 * @return			úsečka symetrická přes kolmici na tuto přímku bodem průniku s dodanou přímkou
	 */
	public Line bounceOff(Line original) {
		Point iP = getIntersection(original, false);
		Line mirror = Line.constructFromPointAndVector(iP, a, b);
		return mirror.reflect(original);
	}

	/**
	 * 
	 * @param original
	 * @return 
	 */
	public Line bounceOffRay(Line original) {
		Point iP = getIntersection(original, false);
		Point newB = new Point(iP.x + original.A.x - original.B.x, iP.y + original.A.y - original.B.y);
		Point S = constructFromPointAndNormal(newB, a, b).getIntersection(constructFromPointAndVector(iP, a, b), false);
		Point refB = new Point(2 * S.x - newB.x, 2 * S.y - newB.y);
		return Line.constructFromTwoPoints(iP, refB);
	}

	@Override
	public String toString() {
		return "Line " + A.toStringSimple() + " <-> " + B.toStringSimple();
	}

	public void disconnect() {

	}
}
