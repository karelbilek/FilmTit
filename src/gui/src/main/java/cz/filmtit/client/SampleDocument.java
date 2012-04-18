package cz.filmtit.client;

import cz.filmtit.share.*;

public final class SampleDocument extends Document {

	// filling a sample Document/SubtitleList by hand:
	public SampleDocument() {
		
		Chunk chunk1 = new Chunk("Hi, Bob!");
		Match match1_1 = new Match("Hi, Bob!");
		match1_1.translations.add( new Translation("Ahoj, Bobe!") );
		match1_1.translations.add( new Translation("ahoj, bobe!") );
		match1_1.translations.add( new Translation("nazdar, bobe!") );
		match1_1.translations.add( new Translation("Čau, Roberte!") );
		chunk1.matches.add(match1_1);
		Match match1_2 = new Match("Hi, Bob.");
		match1_2.translations.add( new Translation("ahoj, bobe.") );
		match1_2.translations.add( new Translation("ahoj, bobe") );
		chunk1.matches.add(match1_2);
		this.chunks.add(chunk1);
		
		Chunk chunk2 = new Chunk("Hi, Tom!");
		Match match2_1 = new Match("Hi Tom!");
		match2_1.translations.add( new Translation("Ahoj Tome!") );
		match2_1.translations.add( new Translation("ahoj, tome!") );
		chunk2.matches.add(match2_1);
		this.chunks.add(chunk2);
		
		Chunk chunk3 = new Chunk("And he hath spoken...");
		//Match match3_1 = new Match("", new ArrayList<Translation>());
		//chunk3.matches.add(match3_1);
		this.chunks.add(chunk3);
		
		Chunk chunk4 = new Chunk("Run, you fools!");
		Match match4_1 = new Match("Run, you fools!");
		match4_1.translations.add( new Translation("Utíkejte, hlupáci!") );
		match4_1.translations.add( new Translation("Utíkejte, blbci!") );
		chunk4.matches.add(match4_1);
		Match match4_2 = new Match("Run, you bastards!");
		//match4_2.translations.add( new Translation("Zdrhejte, hovada!") );
		//match4_2.translations.add( new Translation("utíkejte, plantážníci!") );
		chunk4.matches.add(match4_2);
		Match match4_3 = new Match("Run, run, run!");
		match4_3.translations.add( new Translation("Běžte, běžte, běžte!") );
		match4_3.translations.add( new Translation("Makáme, makáme!") );
		match4_3.translations.add( new Translation("Utíkejte!!!") );
		chunk4.matches.add(match4_3);
		this.chunks.add(chunk4);
		
	}
	
}
