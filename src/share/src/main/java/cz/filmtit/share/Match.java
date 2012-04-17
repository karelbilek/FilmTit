package cz.filmtit.share;

import java.util.ArrayList;
import java.util.List;

public class Match {
    public Match() {
        translations = new ArrayList<Translation>();
    }

    public String text;
    public List<Translation> translations;
}
