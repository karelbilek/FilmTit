/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/
package cz.filmtit.share;

import cz.filmtit.share.annotations.Annotation;
import cz.filmtit.share.annotations.AnnotationType;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Class representing the result sent from the core corresponding to the request
 * by a source-chunk, packaging the given source-chunk and the
 * translation-suggestions from the core. Additionally, the translation from the
 * user is present, for its storage and feedback.
 */
public class TranslationResult implements com.google.gwt.user.client.rpc.IsSerializable, Comparable<TranslationResult>, Serializable {

    /**
     * The timed chunk in the source language
     */
    private TimedChunk sourceChunk;
    /**
     * List of tranlsation suggestion
     */
    private volatile List<TranslationPair> tmSuggestions = new LinkedList<TranslationPair>();
    /**
     * Tranlsation provided by the user
     */
    private volatile String userTranslation;
    /**
     * ID of the translation suggestion selected by the user
     */
    private volatile long selectedTranslationPairID;
    /**
     * shared id
     */
    private volatile long id;

    /**
     * Default constructor for GWT.
     */
    public TranslationResult() {
        sourceChunk = new TimedChunk();
    }

    /**
     * Creates a translation result from the timed chunk in source language
     *
     * @param sourceChunk A timed chunk
     */
    public TranslationResult(TimedChunk sourceChunk) {
        this.sourceChunk = sourceChunk;
    }

    /**
     * Gets the ID of the subtitle item the is from
     *
     * @return ID of subtitle item
     */
    public int getChunkId() {
        return sourceChunk.getId();
    }

    /**
     * Sets the ID of subtitle item if it has not been set before, throws an
     * exception otherwise.
     *
     * @param id ID of the source language timed chunk
     */
    public void setChunkId(int id) {
        sourceChunk.setId(id);
    }

    /**
     * Gets ID of the document the tranlsation result belongs to.
     *
     * @return Document ID
     */
    public long getDocumentId() {
        return sourceChunk.getDocumentId();
    }

    /**
     * Sets ID of the document the translation result belongs to if it has no
     * been set before.
     *
     * @param documentId Document ID
     */
    public void setDocumentId(long documentId) {
        sourceChunk.setDocumentId(documentId);
    }

    /**
     * Gets the translation suggestion to the translation result.
     *
     * @return List of tranlsation suggestions
     */
    public List<TranslationPair> getTmSuggestions() {
        return tmSuggestions;
    }

    /**
     * Sets the list of tranlsation suggestions.
     *
     * @param tmSuggestions List of tranlsation suggestion
     */
    public void setTmSuggestions(List<TranslationPair> tmSuggestions) {
        this.tmSuggestions = tmSuggestions;
    }

    /**
     * Gets a timed chunk created from the users tranlsation
     *
     * @param isDialogue Flag if it is dialog (and therefore should be preceded
     * by a dash)
     * @return Timed chunk with users tranlsation
     */
    public TimedChunk getUserTranslationAsChunk(boolean isDialogue) {
        TimedChunk source = this.getSourceChunk();
        TimedChunk tc = new TimedChunk(this.getUserTranslation(), source.getStartTime(), source.getEndTime(), new LinkedList<Annotation>());
        if (isDialogue) {
            Annotation a = new Annotation(AnnotationType.DIALOGUE, 0, 0);
            tc.addAnnotation(a);
        }
        return tc;
    }

    /**
     * Gets the user translation
     *
     * @return The user translation
     */
    public String getUserTranslation() {
        return userTranslation;
    }

    /**
     * Sets the user translation
     *
     * @param userTranslation New user translation
     */
    public void setUserTranslation(String userTranslation) {
        this.userTranslation = userTranslation;
    }

    /**
     * Gets the ID translation pair selected by user.
     *
     * @return ID translation pair selected by user
     */
    public long getSelectedTranslationPairID() {
        return selectedTranslationPairID;
    }

    /**
     * Sets the ID translation pair selected by user.
     *
     * @param selectedTranslationPairID ID translation pair selected by user
     */
    public void setSelectedTranslationPairID(long selectedTranslationPairID) {
        this.selectedTranslationPairID = selectedTranslationPairID;
    }

    /**
     * Gets the source timed chunk
     *
     * @return The source timed chunk
     */
    public TimedChunk getSourceChunk() {
        return sourceChunk;
    }

    /**
     * Sets the source timed chunk
     *
     * @param sourceChunk The source timed chunk
     */
    public void setSourceChunk(TimedChunk sourceChunk) {
        this.sourceChunk = sourceChunk;
    }

    /**
     * Compares the two TranslationResults according to their source chunks.
     *
     * @param that Other translation result
     */
    public int compareTo(TranslationResult that) {
        return this.sourceChunk.compareTo(that.sourceChunk);
    }

    /**
     * If comparing two TranslationResults, they are equal iff their proper
     * compareTo returns 0.
     *
     * @param that Other translation result
     * @return Flag if they are equal
     */
    @Override
    public boolean equals(Object that) {
        if (that instanceof TranslationResult) {
            return (this.compareTo((TranslationResult) that) == 0) ? true : false;
        } else {
            return super.equals(that);
        }
    }

    /**
     * Prints out the source chunk surface form.
     */
    @Override
    public String toString() {
        return "TranslationResult[" + sourceChunk.toString() + "->" + userTranslation + "(" + selectedTranslationPairID + ")]";
    }

    public TranslationResult resultWithoutSuggestions() {
        if (tmSuggestions == null || tmSuggestions.size() == 0) {
            return this;
        }

        TranslationResult clone = new TranslationResult();
        clone.selectedTranslationPairID = selectedTranslationPairID;
        clone.sourceChunk = sourceChunk;
        clone.userTranslation = userTranslation;
        return clone;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }
}
