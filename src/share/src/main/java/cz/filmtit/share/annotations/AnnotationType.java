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
