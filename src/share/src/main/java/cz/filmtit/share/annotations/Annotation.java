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

package cz.filmtit.share.annotations;

import java.io.Serializable;

/**
 *  Class represents a named entity annotation. String, which are not send to
 *  TM for querying.
 */

public class Annotation implements com.google.gwt.user.client.rpc.IsSerializable, Serializable, Comparable<Annotation> {

    /**
     * Type of name entity
     */
    private AnnotationType type;
    /**
     * Position of the first character of the annotated substring
     *  Zero-based - i.e. "Peter lives at home" would have substring Peter annotated as (AnnotationType.PERSON, 0, 5)
     */
    private int begin;
    /**
     * Position of the first character AFTER the annotated substring
     * Zero-based - i.e. "Peter lives at home" would have substring Peter annotated as (AnnotationType.PERSON, 0, 5)
     */
    //
    private int end;
    //

    /**
     * Default constructor for GWT.
     */
    public Annotation() {
    	// do nothing;
    }

    /**
     * Create new instance of given type, start and end.
     * @param type  Type of annotation
     * @param begin  Start position
     * @param end    End position
     */
    public Annotation(AnnotationType type, int begin, int end) {
        
        this.type = type;
        this.begin = begin;
        this.end = end;

    }

    /**
     * Gets the type of annotation
     * @return  Annotation type
     */
    public AnnotationType getType() {
        return type;
    }

    /**
     * Gets start position of the annotation
     * @return   Start position
     */
    public int getBegin(){
        return begin;
    }

    /**
     * Gets end position of the annotation
     * @return  End position
     */
    public int getEnd() {
        return end;
    }

    /**
     * Compares start position of annotation
     * @param other annotation
     * @return  0 if the same start position, 0 > if the other starts earlier, 0 < if the other starts later
     */
    public int compareTo(Annotation other) {
        return (begin - other.begin);
    }

}
