package cz.filmtit.client;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import cz.filmtit.share.MediaSource;

import java.util.List;



public class MediaSelector extends Composite {
    private List<MediaSource> suggestions;
    private MediaSource selected;


    private static MediaSelectorUiBinder uiBinder = GWT.create(MediaSelectorUiBinder.class);

    interface MediaSelectorUiBinder extends UiBinder<Widget, MediaSelector> {
    }


    static class MediaCell extends AbstractCell<MediaSource> {

        @Override
        public void render(Cell.Context context, MediaSource value, SafeHtmlBuilder sb) {
            // Value can be null, so do a null check..
            if (value == null) {
                return;
            }

            sb.appendHtmlConstant("<table>");

            // Add the contact image.
            sb.appendHtmlConstant("<tr><td rowspan='3' class='mediasource_thumb'>");
            sb.appendHtmlConstant("<img src='" + value.getThumbnailURL() + "' />");
            sb.appendHtmlConstant("</td>");

            // Add the name and address.
            sb.appendHtmlConstant("<td style='font-size:95%;'>");
            sb.appendEscaped(value.getTitle());
            sb.appendHtmlConstant("</td></tr><tr><td>");
            sb.appendEscaped(value.getYear());
            sb.appendHtmlConstant("</td></tr></table>");
        }
    }

    public MediaSelector(List<MediaSource> suggestions) {
        listbox = new CellList<MediaSource>(new MediaCell());

        initWidget(uiBinder.createAndBindUi(this));

        listbox.addStyleName("mediasource_selector");
        listbox.setRowData(0, suggestions);
        listbox.setRowCount(suggestions.size());

        final SingleSelectionModel<MediaSource> selectionModel = new SingleSelectionModel<MediaSource>();
        listbox.setSelectionModel(selectionModel);

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                selected = selectionModel.getSelectedObject();
            }
        });




        this.suggestions = suggestions;



    }

    public void setSelected(MediaSource mediaSource) {
        this.selected = mediaSource;
    }

    @UiField(provided = true)
    CellList<MediaSource> listbox;

    @UiField
    //SubmitButton submitButton;
    Button submitButton;

    public MediaSource getSelected() {
        return selected;
    }
}