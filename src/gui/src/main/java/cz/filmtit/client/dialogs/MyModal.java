package cz.filmtit.client.dialogs;

import com.github.gwtbootstrap.client.ui.Modal;
import com.google.gwt.user.client.Window;

/**
 * Adjustment of gwt-bootstrap's Modal component
 * - disabled animation for Opera browser
 */
public class MyModal extends Modal {
    public MyModal() {
        super();
        if (Window.Navigator.getUserAgent().matches(".*Opera.*")) {
            this.setAnimation(false);
        }
        else {
            this.setAnimation(true);
        }
    }
}
