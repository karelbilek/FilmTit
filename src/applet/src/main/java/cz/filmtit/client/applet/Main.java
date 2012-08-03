package cz.filmtit.client.applet;

import java.io.File;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;
/*import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;*/

import java.awt.*;

import netscape.javascript.JSObject;

public class Main extends JApplet {

    @Override
    public void init() {
        setSize(1,1);
        try {
            final Main m = this;
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    Frame[] frames = Frame.getFrames();
                    
                    FileDialog fd = new FileDialog(frames[0], "Select a file",    FileDialog.LOAD);  
                  
                    fd.show();

                    String f = fd.getDirectory()+fd.getFile();

                    JSObject o = JSObject.getWindow(m);

                    if (f!=null && o != null) {
                        try {
                            o.call("setFileAddress", new String[]{f});
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    stop();
                }
            });
            //GOTTA CATCH EM ALL!
        } catch (Exception e) {
            System.err.println("createGUI didn't complete successfully");
        }
    }
}

