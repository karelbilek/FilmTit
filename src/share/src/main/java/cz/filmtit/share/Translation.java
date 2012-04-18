package cz.filmtit.share;

public class Translation {
	public String text = null;
	public double score = Double.MIN_VALUE;

	public Translation() {
		// nothing to initialize
	}
	
	public Translation(String text) {
		this.text = text;
	}
	
	public Translation (String text, double score) {
        this.text = text;
        this.score = score;
    }
}
