
package wtf;

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
        try {
            final Main m = this;
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    JLabel lbl = new JLabel("Vyberte soubor");
                    add(lbl);

                    
                    JFrame frame = new JFrame();

                    JFileChooser fc = new JFileChooser();
                    fc.showOpenDialog(frame);
                    File selFile = fc.getSelectedFile();

                    JSObject o = JSObject.getWindow(m);

                    if (o != null) {
                        try {
                            o.call("loadFile", new String[]{selFile.getAbsolutePath()});
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't complete successfully");
        }
    }
}

