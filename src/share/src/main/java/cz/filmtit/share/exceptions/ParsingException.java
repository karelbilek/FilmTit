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
