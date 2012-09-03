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

package cz.filmtit.client.dialogs;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.SubmitButton;
import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.Range;

import cz.filmtit.client.Gui;
import cz.filmtit.client.callables.SetChunkTimes;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.share.SrtTime;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.exceptions.InvalidValueException;

/**
 * Enables the user to edit timing of a subtitle item.
 * @author rur
 *
 */
public class TimeEditDialog extends Composite {
	
	private static TimeEditDialogUiBinder uiBinder = GWT
	.create(TimeEditDialogUiBinder.class);

	interface TimeEditDialogUiBinder extends UiBinder<Widget, TimeEditDialog> {
	}


	private List<TimedChunk> chunks;

	private TranslationWorkspace workspace;
	
	private SrtTime startTimeOrig;
	private SrtTime endTimeOrig;
	
	private SrtTime startTimeWorking;
	private SrtTime endTimeWorking;
	
	/**
	 * Shows the dialog.
	 * @param chunks Chunks with identical id, and thus with identical times
	 * which are about to be changed.
	 * The chunks are directly modified.
	 * @param translationWorkspace The workspace where these chunks are diplayed at the moment.
	 */
	public TimeEditDialog(List<TimedChunk> chunks, TranslationWorkspace translationWorkspace) {
		initWidget(uiBinder.createAndBindUi(this));
		
		if (chunks == null || chunks.isEmpty()) {
			return;
		}
		
		this.chunks = chunks;
		this.workspace = translationWorkspace;
		
		try {
			initTimes();
		} catch (InvalidValueException e) {
			Gui.exceptionCatcher(e, false);
			Window.alert("There is an error in the original file: " + e.getLocalizedMessage());
			return;
		}
		
		initTable();
		
		StringBuilder sb = new StringBuilder();
		for (TimedChunk timedChunk : chunks) {
			sb.append(timedChunk.getSurfaceForm());
			sb.append(' ');
		}
		chunkText.setText(sb.toString());
		
		showDialog();
	}

	/**
	 * correctly set the *Time* fields
	 * @param chunks
	 * @throws InvalidValueException if there is an error in the format of the time in the chunk
	 */
	private void initTimes() throws InvalidValueException {
		
		startTimeOrig = new SrtTime(chunks.get(0).getStartTime());
		endTimeOrig = new SrtTime(chunks.get(0).getEndTime());
		
		startTimeWorking = startTimeOrig.clone();
		endTimeWorking = endTimeOrig.clone();
		
	}

	/**
	 * create the table
	 */
	private void initTable() {
		
		// create columns
		
		Column<SrtTime, String> hColumn = new Column<SrtTime, String>(new TextInputCell()) {
			@Override
			public String getValue(SrtTime time) {
				return time.getStringH();
			}
		};
		Column<SrtTime, String> mColumn = new Column<SrtTime, String>(new TextInputCell()) {
			@Override
			public String getValue(SrtTime time) {
				return time.getStringM();
			}
		};
		Column<SrtTime, String> sColumn = new Column<SrtTime, String>(new TextInputCell()) {
			@Override
			public String getValue(SrtTime time) {
				return time.getStringS();
			}
		};
		Column<SrtTime, String> tColumn = new Column<SrtTime, String>(new TextInputCell()) {
			@Override
			public String getValue(SrtTime time) {
				return time.getStringT();
			}
		};
		
		// add column styles
		hColumn.setCellStyleNames("numerical2digits");
		mColumn.setCellStyleNames("numerical2digits");
		sColumn.setCellStyleNames("numerical2digits");
		tColumn.setCellStyleNames("numerical3digits");
		
		// add column update handlers
		hColumn.setFieldUpdater(new FieldUpdater<SrtTime, String>() {
			@Override
			public void update(int index, SrtTime time, String value) {
				try {
					time.setH(value);
				} catch (InvalidValueException e) {
					Window.alert(e.getLocalizedMessage());
				}
			}
		});
		mColumn.setFieldUpdater(new FieldUpdater<SrtTime, String>() {
			@Override
			public void update(int index, SrtTime time, String value) {
				try {
					time.setM(value);
				} catch (InvalidValueException e) {
					Window.alert(e.getLocalizedMessage());
				}
			}
		});
		sColumn.setFieldUpdater(new FieldUpdater<SrtTime, String>() {
			@Override
			public void update(int index, SrtTime time, String value) {
				try {
					time.setS(value);
				} catch (InvalidValueException e) {
					Window.alert(e.getLocalizedMessage());
				}
			}
		});
		tColumn.setFieldUpdater(new FieldUpdater<SrtTime, String>() {
			@Override
			public void update(int index, SrtTime time, String value) {
				try {
					time.setT(value);
				} catch (InvalidValueException e) {
					Window.alert(e.getLocalizedMessage());
				}
			}
		});
		
		// add columns to table
		//timesTable = new CellTable<SrtTime>();
		timesTable.addColumn(hColumn, "hour");
		timesTable.addColumn(mColumn, "minute");
		timesTable.addColumn(sColumn, "second");
		timesTable.addColumn(tColumn, "milisecond");
		
		// add the data
		ArrayList<SrtTime> rowData = new ArrayList<SrtTime>(2);
		rowData.add(startTimeWorking);
		rowData.add(endTimeWorking);
//		timesTable.setRowData(rowData);
//		timesTable.setRowCount(2, true);
		//timesTable.setVisibleRange(new Range(0, 2));
		timesTable.setRowData(0, rowData);
		
		// show the table
		timesTable.redraw();
	}
	
	/**
	 * show the dialog
	 */
	private void showDialog() {
		dialogBox = new DialogBox();
		dialogBox.setAnimationEnabled(true);
		dialogBox.setGlassEnabled(true);
		dialogBox.addStyleName("timeEditDialog");
		dialogBox.setHTML("<h3>Change chunk timing</h3>");
		dialogBox.setWidget(this);
		dialogBox.center();
	}
	
	
    DialogBox dialogBox;
    
    @UiField
    Form timesForm;
    
    @UiField
    Paragraph chunkText;
    
	@UiField
	CellTable<SrtTime> timesTable;
	
    @UiField
    Paragraph timeValue;
    
	@UiField
	Button cancelButton;
	
	@UiHandler("cancelButton")
	void cancel(ClickEvent e) {
		dialogBox.hide();
	}
	
	@UiField
	SubmitButton submitButton;
	
	@UiHandler("timesForm")
	void submit(Form.SubmitEvent e) {
		
		deactivate();
		
		// if times changed
		if (!startTimeOrig.equals(startTimeWorking) ||
				!endTimeOrig.equals(endTimeWorking)) {
			
			if (checkTimes()) {
				// set new times in chunks
				for (TimedChunk chunk : chunks) {
					chunk.setStartTime(startTimeWorking.toString());
					chunk.setEndTime(endTimeWorking.toString());
				}
				
				// set new times in workspace
				workspace.changeTimeLabels(chunks);
				
				// set new times in DB
				for (TimedChunk chunk : chunks) {
					new SetChunkTimes(chunk);
				}
				
				dialogBox.hide();
			}
			else {
				reactivate();
			}
		}
	}
	
	/**
	 * Check whether the timing is OK.
	 * @return
	 */
	private boolean checkTimes() {
		if (startTimeWorking.compareTo(endTimeWorking) == -1) {
			return true;
		}
		else {
			Window.alert("The beginning time must be earlier than the end time!");
			return false;
		}
	}

	private void deactivate() {
		submitButton.setEnabled(false);
	}
 
	private void reactivate() {
		submitButton.setEnabled(true);
	}
 
}
