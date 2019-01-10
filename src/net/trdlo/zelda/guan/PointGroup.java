package net.trdlo.zelda.guan;

class PointGroup {

	final Point points[];
	Point anchor;

	public PointGroup(Point points[]) {
		this.points = points;
	}

	void setAnchor(Point anchor) {
		this.anchor = anchor;
	}

	void rotate(double fi) {
		if (anchor == null) {
			return;
		}

	}

}
