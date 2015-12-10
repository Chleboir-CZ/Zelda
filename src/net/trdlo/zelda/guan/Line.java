package net.trdlo.zelda.guan;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.trdlo.zelda.NU;

public final class Line implements Selectable {

	public static final double MINIMAL_DENOMINOATOR = 1e-5;

	public static final Pattern PAT_LINE = Pattern.compile("^\\s*Line\\s+(\\d+)\\s+(\\d+)\\s*\\z", Pattern.CASE_INSENSITIVE);
	public static final double SELECTION_MAX_DISTANCE = 3;
	public static final double HIGHLIGHT_MAX_DISTANCE = 64;

	public static final Stroke DEFAULT_STROKE = new BasicStroke(1);
	public static final Color DEFAULT_COLOR = Color.LIGHT_GRAY;
	public static final Stroke SELECTION_STROKE = new BasicStroke(2);
	public static final Color SELECTION_COLOR = Color.YELLOW;

	public static Line constructFromTwoPoints(Point A, Point B) {
		assert A != null;
		assert B != null;
		assert A != B;

		Line l = new Line();
		l.A = A;
		l.B = B;
		l.refreshCoefs();
		return l;
	}

	public static Line constructFromPointAndNormal(Point A, double a, double b) {
		assert A != null;
		assert a != 0 || b != 0;

		Line l = new Line();
		l.a = a;
		l.b = b;
		l.A = A;
		l.B = new Point(A.x - b, A.y + a);
		l.c = -a * A.x - b * A.y;
		return l;
	}

	public static Line constructFromPointAndVector(Point A, double a, double b) {
		assert A != null;
		assert a != 0 || b != 0;

		Line l = new Line();
		l.a = -b;
		l.b = a;
		l.A = A;
		l.B = new Point(A.x + a, A.y + b);
		l.c = b * A.x - a * A.y;
		return l;
	}

	static LoadedLine loadFromString(String line) {
		Matcher m = PAT_LINE.matcher(line);
		if (m.matches()) {
			m.group(1);
			return new LoadedLine(Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)));
		} else {
			return null;
		}
	}

	protected Point A, B;

	private double a, b, c;
	private boolean autoUpdate = false;

	/**
	 * Prázdný konstruktor používaný jen místními statickými továrními metodami
	 */
	protected Line() {
	}

	public void refreshCoefs() {
		a = A.y - B.y;
		b = B.x - A.x;
		c = -a * A.x - b * A.y;
	}

	public boolean isValid() {
		return (a != 0) || (b != 0);
	}

	public Point getA() {
		return A;
	}

	public Point getB() {
		return B;
	}

	public void setA(Point A) {
		assert A != null;
		assert A != this.B;

		if (autoUpdate) {
			this.A.removeConnectedLine(this);
		}
		this.A = A;
		if (autoUpdate) {
			A.addConnectedLine(this);
		}
		refreshCoefs();
	}

	public void setB(Point B) {
		assert B != null;
		assert B != this.A;

		if (autoUpdate) {
			this.B.removeConnectedLine(this);
		}
		this.B = B;
		if (autoUpdate) {
			B.addConnectedLine(this);
		}
		refreshCoefs();
	}

	public void changePoint(Point old, Point nu) {
		if (old == A) {
			setA(nu);
		} else if (old == B) {
			setB(nu);
		} else {
			assert false;
		}
	}

	public void connect() {
		if (!autoUpdate) {
			autoUpdate = true;
			A.addConnectedLine(this);
			B.addConnectedLine(this);
			refreshCoefs();
		}
	}

	public void disconnect() {
		if (autoUpdate) {
			autoUpdate = false;
			A.removeConnectedLine(this);
			B.removeConnectedLine(this);
		}
	}

	/**
	 * Test, zda přímka obsahuje bod [x; y]
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contains(double x, double y) {
		assert isValid();
		
		return Math.abs(a * x + b * y + c) < World.MINIMAL_DETECTABLE_DISTANCE;
	}

	/**
	 * Test, zda přímka obsahuje bod p
	 *
	 * @param p
	 * @return
	 */
	public boolean contains(Point p) {
		assert isValid();
		
		return Math.abs(a * p.x + b * p.y + c) < World.MINIMAL_DETECTABLE_DISTANCE;
	}

	/**
	 * Vrací pozici bodu [x; y] na přímce (danou body A, B) jako násobek vektoru A->B. Pokud je výsledek v intervalu
	 * <0; 1>, pak leží i na úsečce AB.
	 *
	 * @param x
	 * @param y
	 * @return	pozice daného bodu v násobku vektoru A->B
	 */
	public double getPosition(double x, double y) {
		assert contains(x, y);
		assert isValid();

		double vx = B.x - A.x, vy = B.y - A.y;

		if (Math.abs(vx) > Math.abs(vy)) {
			return (x - A.x) / vx;
		} else {
			return (y - A.y) / vy;
		}
	}

	/**
	 * Vrací pozici bodu p na přímce (danou body A, B) jako násobek vektoru A->B. Pokud je výsledek v intervalu <0; 1>,
	 * pak leží i na úsečce AB.
	 *
	 * @param p
	 * @return	pozice daného bodu v násobku vektoru A->B
	 */
	public double getPosition(Point p) {
		assert contains(p);
		assert isValid();

		double vx = B.x - A.x, vy = B.y - A.y;

		if (Math.abs(vx) > Math.abs(vy)) {
			return (p.x - A.x) / vx;
		} else {
			return (p.y - A.y) / vy;
		}
	}

	/**
	 * Test, zda úsečka obsahuje bod [x; y]
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean segmentContains(double x, double y) {
		if (!contains(x, y)) {
			return false;
		}
		return NU.inRange(0, getPosition(x, y), 1);
	}

	/**
	 * Test, zda úsečka obsahuje bod p
	 *
	 * @param p
	 * @return
	 */
	public boolean segmentContains(Point p) {
		if (!contains(p)) {
			return false;
		}
		return NU.inRange(0, getPosition(p), 1);
	}

	/**
	 * Test rovnoběžnosti s přímkou l
	 *
	 * @param l
	 * @return
	 */
	public boolean isParallel(Line l) {
		assert isValid();
		assert l.isValid();

		return a * l.b - b * l.a == 0; //tolerance?
	}

	/**
	 * Vytvoří rovnoběžku skrz dodaný bod P
	 *
	 * @param p
	 * @return
	 */
	public Line getParallel(Point p) {
		return constructFromPointAndNormal(p, a, b);
	}

	/**
	 * Test kolmosti na přímku l
	 *
	 * @param l
	 * @return
	 */
	public boolean isPerpendicular(Line l) {
		assert isValid();
		assert l.isValid();

		return a * l.a + b * l.b == 0; //tolerance?
	}

	/**
	 * Vytvoří kolmici skrz dodaný bod P
	 *
	 * @param p
	 * @return
	 */
	public Line getPerpendicular(Point p) {
		return constructFromPointAndVector(p, a, b);
	}

	/**
	 * Vypočítá úhel, který svírají tato přímka s další
	 *
	 * @param l	druhá přímka
	 * @return	úhel v radiánech v protisměru hodinových ručiček, který tato přímka svírá s druhou dodanou
	 */
	public double getAngle(Line l) {
		assert isValid();
		assert l.isValid();

		return Math.acos((a * l.a + b * l.b) / (Math.sqrt(a * a + b * b) * Math.sqrt(l.a * l.a + l.b * l.b)));
	}

	/**
	 *
	 * @param l
	 * @return
	 */
	public boolean intersects(Line l) {
		return !isPerpendicular(l);
	}

	/**
	 *
	 * @param segment
	 * @return
	 */
	public boolean lineIntersectsSegment(Line segment) {
		double denominator = (a * segment.b - segment.a * b);
		if (Math.abs(denominator) >= MINIMAL_DENOMINOATOR) {
			double ix = (b * segment.c - c * segment.b) / denominator;
			double iy = (segment.a * c - a * segment.c) / denominator;
			return NU.inRange(0, segment.getPosition(ix, iy), 1);
		} else {
			return contains(segment.A);
		}
	}

	/**
	 *
	 * @param segment
	 * @return
	 */
	public boolean segmentIntersectsSegment(Line segment) {
		double denominator = (a * segment.b - segment.a * b);
		if (Math.abs(denominator) >= MINIMAL_DENOMINOATOR) {
			double ix = (b * segment.c - c * segment.b) / denominator;
			double iy = (segment.a * c - a * segment.c) / denominator;
			return NU.inRange(0, segment.getPosition(ix, iy), 1) && NU.inRange(0, getPosition(ix, iy), 1);
		} else {
			return contains(segment.A);
		}
	}

	/**
	 * Nalezne průsečík této a jiné přímky
	 *
	 * @param line	druhá přímka
	 * @return	bod, kde se protnou, nebo null, pokud se neprotnou
	 */
	public Point getIntersection(Line line) {
		double denominator = (a * line.b - line.a * b);
		if (Math.abs(denominator) >= MINIMAL_DENOMINOATOR) {
			return new Point((b * line.c - c * line.b) / denominator, (line.a * c - a * line.c) / denominator);
		} else {
			return null;
		}
	}

	/**
	 * Nalezne průsečík této přímky a úsečky
	 *
	 * @param segment	druhá přímka
	 * @return	bod, kde se protnou, nebo null, pokud se neprotnou
	 */
	public Point getLineSegmentIntersection(Line segment) {
		double denominator = (a * segment.b - segment.a * b);
		if (Math.abs(denominator) >= MINIMAL_DENOMINOATOR) {
			double ix = (b * segment.c - c * segment.b) / denominator;
			double iy = (segment.a * c - a * segment.c) / denominator;
			if (NU.inRange(0, segment.getPosition(ix, iy), 1)) {
				return new Point(ix, iy);
			}
		}
		return null;
	}

	/**
	 * Nalezne průsečík této a jiné úsečky
	 *
	 * @param segment	druhá přímka
	 * @return	bod, kde se protnou, nebo null, pokud se neprotnou
	 */
	public Point getSegmentSegmentIntersection(Line segment) {
		double denominator = (a * segment.b - segment.a * b);
		if (Math.abs(denominator) >= MINIMAL_DENOMINOATOR) {
			double ix = (b * segment.c - c * segment.b) / denominator;
			double iy = (segment.a * c - a * segment.c) / denominator;
			if (NU.inRange(0, segment.getPosition(ix, iy), 1) && NU.inRange(0, getPosition(ix, iy), 1)) {
				return new Point(ix, iy);
			}
		}
		return null;
	}

	/**
	 * Druhá mocnina vzdálenosti bodu a přímky
	 *
	 * @param p
	 * @return
	 */
	public double getDistanceSquare(Point p) {
		//TODO zvážit, zda jde provést bez výroby objektů na haldě
		Point iP = getIntersection(Line.constructFromPointAndVector(p, a, b));
		return NU.sqr(iP.x - p.x) + NU.sqr(iP.y - p.y);
	}

	/**
	 * Vzdálenost bodu a přímky
	 *
	 * @param p
	 * @return
	 */
	public double getDistance(Point p) {
		return Math.sqrt(getDistanceSquare(p));
	}

	/**
	 * Druhá mocnina vzdálenosti bodu [x; y] a úsečky
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public double getSegmentDistanceSquare(double x, double y) {
		assert isValid();

		double denominator = (a * a + b * b);
		double cn = (b * x - a * y);
		double iPx = (b * cn - c * a) / denominator;
		double iPy = (-b * c - a * cn) / denominator;

		double vx = B.x - A.x, vy = B.y - A.y, dl;
		if (Math.abs(vx) > Math.abs(vy)) {
			dl = (iPx - A.x) / vx;
		} else {
			dl = (iPy - A.y) / vy;
		}

		if (dl < 0) {
			return NU.sqr(x - A.x) + NU.sqr(y - A.y);
		} else if (dl > 1) {
			return NU.sqr(x - B.x) + NU.sqr(y - B.y);
		} else {
			return NU.sqr(iPx - x) + NU.sqr(iPy - y);
		}
	}

	/**
	 * Vzdálenost bodu [x; y]a úsečky
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public double getSegmentDistance(double x, double y) {
		return Math.sqrt(getSegmentDistanceSquare(x, y));
	}

	/**
	 * Druhá mocnina vzdálenosti bodu a úsečky
	 *
	 * @param p
	 * @return
	 */
	public double getSegmentDistanceSquare(Point p) {
		//TODO zvážit, zda jde provést bez výroby objektů na haldě
		Point iP = getIntersection(Line.constructFromPointAndVector(p, a, b));

		double vx = B.x - A.x, vy = B.y - A.y, dl;
		if (Math.abs(vx) > Math.abs(vy)) {
			dl = (iP.x - A.x) / vx;
		} else {
			dl = (iP.y - A.y) / vy;
		}

		if (dl < 0) {
			return NU.sqr(p.x - A.x) + NU.sqr(p.y - A.y);
		} else if (dl > 1) {
			return NU.sqr(p.x - B.x) + NU.sqr(p.y - B.y);
		} else {
			return NU.sqr(iP.x - p.x) + NU.sqr(iP.y - p.y);
		}
	}

	/**
	 * Vzdálenost bodu a úsečky
	 *
	 * @param p
	 * @return
	 */
	public double getSegmentDistance(Point p) {
		return Math.sqrt(getSegmentDistanceSquare(p));
	}

	/**
	 * Druhá mocnina vzdálenosti dvou přímek (má větší smysl jen u rovnoběžek)
	 *
	 * @param line
	 * @return
	 */
	public double getDistanceSquare(Line line) {
		//TODO zvážit, zda jde provést bez výroby objektů na haldě
		if (isParallel(line)) {
			Line perpendicularLine = Line.constructFromPointAndVector(A, a, b);
			Point p = getIntersection(perpendicularLine);
			Point lp = line.getIntersection(perpendicularLine);
			return NU.sqr(lp.x - p.x) + NU.sqr(lp.y - p.y);
		} else {
			//různoběžky mají vzdálenost 0
			return 0;
		}
	}

	/**
	 * Vzdálenost dvou přímek (má větší smysl jen u rovnoběžek)
	 *
	 * @param line
	 * @return
	 */
	public double getDistance(Line line) {
		return Math.sqrt(Line.this.getDistanceSquare(line));
	}

	/**
	 * Druhá mocnina vzdálenosti přímky a úsečky
	 *
	 * @param segment
	 * @return
	 */
	public double getLineToSegmentDistanceSquare(Line segment) {
		double denominator = (a * segment.b - segment.a * b);
		if (Math.abs(denominator) < MINIMAL_DENOMINOATOR) {
			//rovnobezne => vzdalenost jednoho z bodu
			return getDistanceSquare(segment.A);
		}
		double ix = (b * segment.c - c * segment.b) / denominator;
		double iy = (segment.a * c - a * segment.c) / denominator;
		double pos = segment.getPosition(ix, iy);

		if (NU.inRange(0, pos, 1)) {
			//usecka se krizi s primkou
			return 0;
		} else if (pos < 0) {
			return getDistanceSquare(segment.A);
		} else {
			return getDistanceSquare(segment.B);
		}
	}

	/**
	 * Vzdálenost přímky a úsečky
	 *
	 * @param segment
	 * @return
	 */
	public double getLineToSegmentDistance(Line segment) {
		return Math.sqrt(getLineToSegmentDistanceSquare(segment));
	}

	/**
	 * Druhá mocnina vzdálenosti dvou úseček
	 *
	 * @param segment
	 * @return
	 */
	public double getSegmentToSegmentDistanceSquare(Line segment) {
		double denominator = (a * segment.b - segment.a * b);
		if (Math.abs(denominator) >= MINIMAL_DENOMINOATOR) {
			double ix = (b * segment.c - c * segment.b) / denominator;
			double iy = (segment.a * c - a * segment.c) / denominator;
			double pos1 = getPosition(ix, iy);
			double pos2 = segment.getPosition(ix, iy);
			if (NU.inRange(0, pos1, 1) && NU.inRange(0, pos2, 1)) {
				//usecka se krizi s useckou
				return 0;
			}
		}

		double minSqrDist = getSegmentDistanceSquare(segment.A);
		minSqrDist = Math.min(minSqrDist, getSegmentDistanceSquare(segment.B));
		minSqrDist = Math.min(minSqrDist, segment.getSegmentDistanceSquare(A));
		minSqrDist = Math.min(minSqrDist, segment.getSegmentDistanceSquare(B));

		return minSqrDist;
	}

	/**
	 * Vzdálenost dvou úseček
	 *
	 * @param segment
	 * @return
	 */
	public double getSegmentToSegmentDistance(Line segment) {
		return Math.sqrt(getSegmentToSegmentDistanceSquare(segment));
	}

	/**
	 * Vytvoří zrcadlový obraz dodané úsečky podle sebe
	 *
	 * @param original	dodaná úsečka
	 * @return	úsečka symetrická přes tuto přímku
	 */
	public Line reflect(Line original) {
		assert isValid();
		assert original.isValid();

		//TODO zvážit, zda jde provést bez výroby objektů na haldě
		Point SA = getIntersection(Line.constructFromPointAndVector(original.A, a, b));
		Point AR = new Point(2 * SA.x - original.A.x, 2 * SA.y - original.A.y);
		Point SB = getIntersection(Line.constructFromPointAndVector(original.B, a, b));
		Point BR = new Point(2 * SB.x - original.B.x, 2 * SB.y - original.B.y);
		return Line.constructFromTwoPoints(AR, BR);
	}

	/**
	 * Vytvoří obraz dodané úsečky přes kolmici na průnik se sebou (pokud existuje)
	 *
	 * @param original	dodaná úsečka
	 * @return	úsečka symetrická přes kolmici na tuto přímku bodem průniku s dodanou přímkou nebo null
	 */
	public Line bounceOff(Line original) {
		assert isValid();
		assert original.isValid();

		//TODO zvážit, zda jde provést bez výroby objektů na haldě
		Point iP = getIntersection(original);
		if (iP != null) {
			Line mirror = Line.constructFromPointAndVector(iP, a, b);
			return mirror.reflect(original);
		} else {
			return null;
		}
	}

	/**
	 * Vytvoří obraz dodané úsečky přes kolmici na průnik se sebou posunutou na počátek odrazu (pokud existuje)
	 *
	 * @param original	dodaná úsečka
	 * @return	úsečka symetrická přes kolmici na tuto přímku bodem průniku s dodanou přímkou nebo null
	 */
	public Line bounceOffRay(Line original) {
		assert isValid();
		assert original.isValid();

		//TODO zvážit, zda jde provést bez výroby objektů na haldě		
		//najít průnik originálu s touto přímkou
		Point iP = getIntersection(original);
		if (iP != null) {
			//bod newB je na originálu tak, že délka |iP newB| je stejná jako původní |A B|
			Point newB = new Point(iP.x + original.A.x - original.B.x, iP.y + original.A.y - original.B.y);
			//bod S je křížením kolmice z bodu iP a rovnoběžkou skrz new B, je tak středem, přes který se promítne refB (odraz newB)
			Point S = constructFromPointAndNormal(newB, a, b).getIntersection(constructFromPointAndVector(iP, a, b));
			Point refB = new Point(2 * S.x - newB.x, 2 * S.y - newB.y);
			return Line.constructFromTwoPoints(iP, refB);
		} else {
			return null;
		}
	}

	/**
	 * Vrací nejbližší bod na úsečce nebo null pokud by šlo o koncový bod
	 *
	 * @param p
	 * @return
	 */
	public Point getNearestPointInSegment(Point p) {
		assert isValid();

		double denominator = (a * a + b * b);
		double cn = (b * p.x - a * p.y);
		double iPx = (b * cn - c * a) / denominator;
		double iPy = (-b * c - a * cn) / denominator;

		double vx = B.x - A.x, vy = B.y - A.y, dl;
		if (Math.abs(vx) > Math.abs(vy)) {
			dl = (iPx - A.x) / vx;
		} else {
			dl = (iPy - A.y) / vy;
		}

		if (dl <= 0 || dl >= 1) {
			return null;
		} else {
			return new Point(iPx, iPy);
		}
	}

	@Override
	public String toString() {
		return "Line " + A.toStringSimple() + " <-> " + B.toStringSimple();
	}

	String saveToString(int idA, int idB) {
		return "Line " + idA + " " + idB;
	}
}

class LoadedLine {

	public final int idA, idB;

	public LoadedLine(int idA, int idB) {
		this.idA = idA;
		this.idB = idB;
	}
}
