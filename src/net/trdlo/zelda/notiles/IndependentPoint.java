
package net.trdlo.zelda.notiles;


public class IndependentPoint extends Point {
	public boolean selected;
	
	public IndependentPoint(double x, double y, boolean selected) {
		super(x, y);
		this.selected = selected;
	}
	
}
