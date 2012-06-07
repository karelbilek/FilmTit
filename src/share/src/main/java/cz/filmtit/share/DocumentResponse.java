package cz.filmtit.share;

import java.util.List;

/**
 * @author Jindřich Libovický
 */
public class DocumentResponse {
    public DocumentResponse(Document document, List<MediaSource> mediaSourceSuggestions) {
        this.document = document;
        this.mediaSourceSuggestions = mediaSourceSuggestions;
    }

    public Document document;
    public List<MediaSource> mediaSourceSuggestions;
}
