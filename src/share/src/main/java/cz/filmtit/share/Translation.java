package cz.filmtit.share;

public class Translation {
    public String text = null;
    public double score = Double.MIN_VALUE;

    public Translation (String text, double score) {
        this.text = text;
        this.score = score;
    }

    public Translation() {}
}
