package cz.filmtit.share.exceptions;

/**
 * @author Jindřich Libovický
 */
public class InvalidChunkIdException extends Exception  {
    InvalidChunkIdException() {}

    public InvalidChunkIdException(String message) {
        super(message);
    }
}
