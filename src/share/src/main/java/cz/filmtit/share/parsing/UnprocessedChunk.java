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

package cz.filmtit.share.parsing;

/**
 * "Raw" chunk read from subtitle file.
 * It is not a Chunk yet, it is just a time info and a string
 *
 * @author Honza Václ, Karel Bílek
 *
 */
public class UnprocessedChunk {

    /**
     * Start time of the chunk.
     */
    private String startTime;
    /**
     * End time of the chunk.
     */
    private String endTime;
    /**
     * Text of the chunk. Can have more sentences. Has "|" as newline.
     */
    private String text;

    /**
     * Constructs chunk from start, end and text.
     * @param startTime start time of chunk
     * @param endTime end time of chunk
     * @param text text of chunk. Can have more sentences, etc. Has "|" as newline.
     */
    public UnprocessedChunk(String startTime, String endTime, String text) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.text=text;
    }
        
    /**
     * Returns end time of the chunk.
     * @return end time of the chunk.
     */
    public String getEndTime() {
        return endTime;
    }
    
    /**
     * Returns start time of the chunk.
     * @return start time of the chunk.
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Returns text.
     * @return text.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns textual information about the chunk.
     * Used mainly for debugging purposes.
     * @return Textual information about the chunk.
     */
    public String toString() {
        return startTime + " - "+endTime+" : "+text;
    }

    /**
     * Check for equality with another chunk.
     * Chunks are equal when they have the same start time, end time and text.
     * @return true iff equal.
     */
    public boolean equalsTo(UnprocessedChunk other) {
        return (startTime.equals(other.startTime) && endTime.equals(other.endTime) && text.equals(other.text));
    }

}

