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

package cz.filmtit.share.exceptions;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;


/**
 * Exception occurring during the parsing of a subtitle file contents.
 */
public class ParsingException extends Exception implements Serializable, IsSerializable {
    String message;
    int lineNumber;
    boolean maybeEarlier;

    ParsingException() {}

    /**
     * Creates a new ParsingException.
     * @param message - what went wrong
     * @param lineNumber - on what line it was recognized
     * @param maybeEarlier - possibility that the actual error is located earlier that on the given line
     */
    public ParsingException(String message, int lineNumber, boolean maybeEarlier) {
        super(message);
        this.message = message;
        this.lineNumber = lineNumber;
        this.maybeEarlier = maybeEarlier;
    }

    @Override
    public String getMessage() {
        String returnMessage = message
                + " - on line: "
                + lineNumber
                + (maybeEarlier ? " (or above)." : ".");
        if (lineNumber == 1) {
            returnMessage += " (Check your file format and encoding.)";
        }
        return returnMessage;
    }
}
