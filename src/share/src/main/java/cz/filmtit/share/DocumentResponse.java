package cz.filmtit.share;
import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

import java.util.List;

/**
 * @author Jindřich Libovický
 */
public class DocumentResponse implements Serializable, IsSerializable {
    public DocumentResponse(Document document, List<MediaSource> mediaSourceSuggestions) {
        this.document = document;
        this.mediaSourceSuggestions = mediaSourceSuggestions;
    }

    public DocumentResponse(){}

    public Document document;
    public List<MediaSource> mediaSourceSuggestions;
}
