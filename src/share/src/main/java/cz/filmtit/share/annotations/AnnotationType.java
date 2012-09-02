package cz.filmtit.share.annotations;

import java.io.Serializable;

/**
 *  Definition of annotation types.
 *
 */

public enum AnnotationType implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {

    PLACE("Place"),
    ORGANIZATION("Organization"),
    PERSON("Person"),
    LINEBREAK("LineBreak"),
    DIALOGUE("Dialogue");

    /**
     * Text description of type
     */
    private String description;

    AnnotationType() {
    }

    /**
     * Creates object with description
     * @param description item enum like string
     */
    AnnotationType(String description) {
        this.description=description;
    }

    /**
     * Gets the text description
     * @return The text description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets text description
     * @param description The text description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Creates annotation from text description
     * @param description  - text description
     * @return  AnnotationType item
     */
    public static AnnotationType fromDescription(String description) {
        if (description.equals("Place")) {
            return PLACE;
        } else if (description.equals("Organization")) {
            return ORGANIZATION;
        } else if (description.equals("Person")) {
            return PERSON;
        } else if (description.equals("LineBreak")) {
            return LINEBREAK;
        } else if (description.equals("Dialogue")) {
            return DIALOGUE;
        }

        return null;
    }

}
