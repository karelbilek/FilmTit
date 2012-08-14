package cz.filmtit.client.dialogs;

import java.util.ArrayList;
import java.util.List;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.SubmitButton;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
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
 * Common ancestor to dialogs in FilmTit.
 * Each dialog should be extended from this class.
 * All dialogs should be accessed only through this class methods.
 * Must be placed in the dialogs subpackage because of visibility of dialogBox to GuiBinder.
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
	 * 
	 * @param chunks Chunks with identical id, and thus with identical times
	 * which are about to be changed.
	 * The chunks are directly modified.
	 * @param translationWorkspace
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
		// TODO: define the styles
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
	CellTable<SrtTime> timesTable;
	
	@UiField
	SubmitButton submitButton;
	
	@UiHandler("timesForm")
	void submit(Form.SubmitEvent e) {
		
		// if times changed
		if (!startTimeOrig.equals(startTimeWorking) ||
				!endTimeOrig.equals(endTimeWorking)) {
			
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
		}
		
		dialogBox.hide();
	}
	
// unused methods copied from Dialog; use them or delete them
//
//    @UiHandler("dialogBox")
//    final void dialogClosed(HiddenEvent e) {
//    	if (!closedByClose) {
//    		onHide();
//    	}    	
//    }
//
//    /**
//     * set to true right before the close() method closes the dialog
//     */
//    boolean closedByClose = false;
//    
//    /**
//     * Called when the dialog is closed directly by dialog.hide(), i.e. NOT with the close() method.
//     * Intended to handle the user clicking on the close cross or pressing Esc.
//     * @see Dialog.onClosing()
//     */
//    protected void onHide() {
//    	// nothing to do by default
//    }
//	
//	/**
//	 * Temporarily prevent the user from using the dialog,
//	 * but do not hide it.
//	 * Used to block the user from doing anything
//	 * e.g. while waiting for an RPC call to complete.
//	 */
//	public void deactivate() {
//		dialogBox.setVisible(false);
//	}
//	
//	/**
//	 * Activate the dialog again
//	 * @param message
//	 */
//	public void reactivate() {
//		dialogBox.setVisible(true);
//	}
//	
//	/**
//	 * Show an error message to the user.
//	 * @param message
//	 */
//	public void showErrorMessage(String message) {
//		Window.alert(message);		
//	}
//
//	/**
//	 * Show an info message to the user.
//	 * @param message
//	 */
//	public void showInfoMessage(String message) {
//		Window.alert(message);		
//	}

//	/**
//	 * Activate the dialog again, showing an error message to the user.
//	 * @param message
//	 */
//	final public void reactivateWithErrorMessage(String message) {
//		reactivate();
//		showErrorMessage(message);		
//	}
//	
//	/**
//	 * Activate the dialog again, showing an info message to the user.
//	 * @param message
//	 */
//	final public void reactivateWithInfoMessage(String message) {
//		reactivate();
//		showInfoMessage(message);		
//	}
//	
//	/**
//	 * Hide the dialog, if not prevented by onClosing().
//	 */
//	final public void close() {
//		if (onClosing()) {
//			closedByClose = true;
//			dialogBox.hide();
//		}		
//	}
//	
//    /**
//     * Called when the dialog is closed with the close() method, i.e. NOT when the user presses Esc etc.
//     * @see Dialog.onHide()
//     * @return true if the closing should continue, false to stop it
//     */
//    protected boolean onClosing() {
//    	// nothing to do by default
//    	return true;
//    }
    
}
