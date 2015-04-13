
package net.trdlo.zelda.notiles;


public class IndependentPoint extends Point {
	private boolean selected;
	
	public IndependentPoint(double x, double y) {
		super(x, y);
		this.selected = false;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}	
	
}
