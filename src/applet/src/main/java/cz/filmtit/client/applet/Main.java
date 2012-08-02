package cz.filmtit.client.applet;

import java.io.File;
import javax.swing.JApplet;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import netscape.javascript.JSObject;

public class Main extends JApplet {

    @Override
    public void init() {
        setSize(1,1);
        try {
            final Main m = this;
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {

                    
                    JFrame frame = new JFrame();

                    JFileChooser fc = new JFileChooser();
                    fc.showOpenDialog(frame);
                    File selFile = fc.getSelectedFile();

                    JSObject o = JSObject.getWindow(m);

                    if (o != null) {
                        try {
                            o.call("setFileAddress", new String[]{selFile.getAbsolutePath()});
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

