package net.trdlo.zelda.guan;

import java.awt.event.MouseEvent;

/**
 * The simplest of all classes, a int-int tuple
 */
class XY {

	public int x, y;

	public XY(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public XY(MouseEvent e) {
		this.x = e.getX();
		this.y = e.getY();
	}

	public XY diff(XY to) {
		return new XY(x - to.x, y - to.y);
	}

	@Override
	public String toString() {
		return "[" + x + ";" + y + "]";
	}
}
