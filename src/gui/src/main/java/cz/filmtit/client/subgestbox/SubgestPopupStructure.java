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

package cz.filmtit.client.subgestbox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.github.gwtbootstrap.client.ui.Paragraph;

import cz.filmtit.share.TranslationPair;

/**
 * Displays translation suggestions for a {@link SubgestBox}.
 * @author honza
 *
 */
public class SubgestPopupStructure extends Composite {

	private static SubgestPopupStructureUiBinder uiBinder = GWT.create(SubgestPopupStructureUiBinder.class);

	interface SubgestPopupStructureUiBinder extends UiBinder<Widget, SubgestPopupStructure> {
	}

    /**
     * Primary constructor, initializing the values according
     * to the given TranslationPair.
     * @param value
     */
	public SubgestPopupStructure(TranslationPair value) {
		initWidget(uiBinder.createAndBindUi(this));
		
		suggestionItemText.setText(value.getStringL2());
		suggestionItemMatch.setText(value.getStringL1());
		if (value.getScore() != null) {
            suggestionItemScore.setStyleName("score score_" + Math.round(value.getScore() * 10));
			suggestionItemScore.setText( NumberFormat.getPercentFormat().format(value.getScore()) );
		}
		else {
			suggestionItemScore.setText("Score unknown");
		}
		suggestionItemSource.setText( "Source: " + value.getSource().getDescription() );
	}
	
	@UiField
	Paragraph suggestionItemText;
	@UiField
	Paragraph suggestionItemMatch;
	@UiField
	Paragraph suggestionItemScore;
	@UiField
	Paragraph suggestionItemSource;

}
