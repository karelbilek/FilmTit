package cz.filmtit.client;

import java.util.List;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.SubmitButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import cz.filmtit.share.MediaSource;


/**
 * Created with IntelliJ IDEA.
 * User: omikronn
 * Date: 19.6.12
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */
public class MediaSelector extends Composite {
    private List<MediaSource> suggestions;

    private static MediaSelectorUiBinder uiBinder = GWT.create(MediaSelectorUiBinder.class);

    interface MediaSelectorUiBinder extends UiBinder<Widget, MediaSelector> {
    }

    public MediaSelector(List<MediaSource> suggestions) {
        initWidget(uiBinder.createAndBindUi(this));

        this.suggestions = suggestions;

        for (MediaSource suggestion : suggestions) {
            String suggestString = suggestion.getTitle() + " (" + suggestion.getYear() + ")";
            listbox.addItem(suggestString);
        }

    }

    @UiField
    ListBox listbox;

    @UiField
    SubmitButton submitButton;

    public MediaSource getSelected() {
        return suggestions.get( listbox.getSelectedIndex() );
    }
}