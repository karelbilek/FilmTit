package cz.filmtit.client.widgets;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;

import cz.filmtit.client.pages.DocumentCreator;

import com.google.gwt.user.client.Window;


public class FileLoadWidget extends HTML {

    //this is ugly but can't really be helped due to the order the javascripts are called
    static DocumentCreator creator;
    public static void setDocumentCreator(DocumentCreator dc) {
        creator = dc;
    }

    public static native void exportStaticLoadFile(FileLoadWidget flw) /*-{
        $wnd.setFileAddress = $entry(flw.@cz.filmtit.client.widgets.FileLoadWidget::setFileAddress(Ljava/lang/String;));
        

    }-*/;

    public static native void removeStaticLoadFile() /*-{
        $wnd.setLoadedFile = 0;
    }-*/;

    static String htmlCode = "<applet code=\"cz.filmtit.client.applet.Main.class\" archive=\"applet.jar\">";

    public FileLoadWidget() {
        super(htmlCode);        
        exportStaticLoadFile(this); 
    }

    private String address = "";
    
    public void setFileAddress(String s) {
        address = s;
        creator.addressSet(this, s);
    }

    public String getAddress() {
        return address;
    }

    @Override
    public void onLoad() {
        //exportStaticLoadFile(this);
    }

    @Override
    public void onUnload() {
        removeStaticLoadFile();
    }

     


}
