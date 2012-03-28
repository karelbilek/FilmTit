package cz.filmtit.client;

public class SubtitleChunk {
	public String source;
	public String match;
	public String translation;

	public SubtitleChunk(String source, String match, String translation) {
		this.source = source;
		this.match = match;
		this.translation = translation;
	}
	
	public static SubtitleChunk NoNextSubtitle = new SubtitleChunk("< no next subtitle >", "< no next subtitle >", "< no next subtitle >");
}
