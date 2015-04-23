
package net.trdlo.zelda.notiles;


public class IndependentPoint extends Point {
	private boolean selected;
	private String description; 
	
	public IndependentPoint(double x, double y) {
		super(x, y);
		this.selected = false;
	}

	public String getDescription() {
		return description;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isSelected() {
		return selected;
	}	
	
}
