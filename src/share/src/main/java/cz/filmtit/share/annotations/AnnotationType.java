package cz.filmtit.share.annotations;

import java.io.Serializable;

//There is only one class for all types of annotations
//basically because Java doesn't have enum inheritance
//And I thought using enums is easier for everyone
//
//downside: less general
//upside: all annotations are defined on one place
public enum AnnotationType implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {

    PLACE("Place"),
    ORGANIZATION("Organization"),
    PERSON("Person");

    private String description;

    AnnotationType() {
    }

    AnnotationType(String description) {
        this.description=description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static AnnotationType fromDescription(String description) {
        if (description.equals("Place")) {
            return PLACE;
        } else if (description.equals("Organization")) {
            return ORGANIZATION;
        } else if (description.equals("Person")) {
            return PERSON;
        }
        return null;
    }

}
