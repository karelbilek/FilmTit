package cz.filmtit.share;

public class Translation {
	public String text = null;
	public double score = Double.MIN_VALUE;

	public Translation(String text) {
		this.text = text;
	}
	
	public Translation() {
		// nothing to initialize
	}
}
