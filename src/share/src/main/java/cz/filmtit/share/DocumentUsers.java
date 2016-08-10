package cz.filmtit.share;

import java.io.Serializable;

/**
 *
 * @author Matus Namesny
 */
public class DocumentUsers implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {

    private volatile Long userId;
    private volatile Long documentId;



    /**
     * @return the documentId
     */
    public Long getDocumentId() {
        return documentId;
    }

    /**
     * @param documentId the documentId to set
     */
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public DocumentUsers() {
    }

    /**
     * @return the userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
