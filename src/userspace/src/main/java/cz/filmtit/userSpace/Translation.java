package cz.filmtit.userSpace;

import java.*;
import java.util.*;

/**
 * Represents a single translation returned by the TM. Belongs to exactly one match.
 *
 * @author Jindřich Libovický
*/

public class Translation {
    /**
     * Creates the translation object of given rank and text.
     * @param text Text of the translation.
     * @param rank Rank of the translation from TM.
     */
    public Translation(String text, double rank) {
        this.text = text;
        this.rank = rank;
    }

    /**
     * Default constructor. (Hibernate requires it.)
     */
    public Translation() {}

    private String text;

    private double rank;

    private int databaseId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(Float rank) {
        this.rank = rank;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }
}
