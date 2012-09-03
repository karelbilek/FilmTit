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

package cz.filmtit.share;
import java.io.Serializable;

/**
 * Type representing a source of translation pair, i.e. which method has been used to retrieve it.
 *
 * @author Joachim Daiber
 */
public enum TranslationSource implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {

    //Internal:
    INTERNAL_EXACT ("Exact TM match"),
    INTERNAL_NE    ("NE based TM match"),
    INTERNAL_FUZZY ("Fuzzy TM match"),

    //External:
    EXTERNAL_TM    ("External TM match"),
    EXTERNAL_MT    ("External MT"),
    MOSES    ("Moses"),

    //Multiple
    MULTIPLE       ("Multiple sources"),

    //Unknown:
    UNKNOWN        ("Unkown source");

    private String description;

    TranslationSource(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "TranslationSource[" + description + "]";
    }
    
    public String getDescription() {
    	return description;
    }
}
