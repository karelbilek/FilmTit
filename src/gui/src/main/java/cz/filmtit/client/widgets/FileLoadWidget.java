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

package cz.filmtit.client.widgets;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;

import cz.filmtit.client.pages.DocumentCreator;

import com.google.gwt.user.client.Window;

/**
 * Widget that calls the applet and prints the file address to DocumentCreator GUI.
 * The applet is called as soon as the widget is created.
 *
 * @author Karel Bílek
 */
public class FileLoadWidget extends HTML {

    // creator that will display the file address
    static private DocumentCreator creator;

    /**
     * Because the applet has to know what exactly to call, a global procedure
     * is exported. (Unlike java, javascript is functional)
     * @param flw
     */
    private static native void exportStaticLoadFile(FileLoadWidget flw) /*-{
        $wnd.setFileAddress = $entry(flw.@cz.filmtit.client.widgets.FileLoadWidget::setFileAddress(Ljava/lang/String;));

    }-*/;

    //deexported what is in the previous one
    private static native void removeStaticLoadFile() /*-{
        $wnd.setLoadedFile = 0;
    }-*/;

    private static String htmlCode =
            "<applet code=\"cz.filmtit.client.applet.Main.class\" archive=\"applet.jar\">";

    /**
     *  Creates a loadwidget.
     * @param dc "Father" - a DocumentCreator where the address will be written.
     */
    public FileLoadWidget(DocumentCreator dc) {
        super(htmlCode);
        creator = dc;
        exportStaticLoadFile(this); 
    }

    private String address = "";

    /**
     * Procedure, called by the applet after file is chosen.
     * (IDEs will maybe show that it's not used, but it's used by JNI code.)
     * It calls the creator.addressSet.
     * @param s The address.
     */
    public void setFileAddress(String s) {
        address = s;
        creator.addressSet(this, s);
    }

    /**
     * Returns the address.
     * @return The address of the file.
     */
    public String getAddress() {
        return address;
    }


    /**
     * Removes the global exported function.
     */
    @Override
    public void onUnload() {
        removeStaticLoadFile();
    }

     


}
