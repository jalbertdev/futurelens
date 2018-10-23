/******************************************************************************

                        FutureLens 

Copyright 2008 G. Shutt, A. Puretskiy, M.W. Berry 
Licensed under the Apache License, Version 2.0 (the "License"); you may not 
use this file except in compliance with the License. You may obtain a copy 
of the License at

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable
law or agreed to in writing, software distributed under the License is
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.

            Interested parties may
            send electronic mail to berry@eecs.utk.edu for
            more information.  Written requests for software
            distribution or use may be sent to:

             Michael W. Berry
             Department of Electrical Engineering and Computer Science
             203 Claxton Complex
             1122 Volunteer Boulevard
             University of Tennessee
             Knoxville, TN 37996-3450

 ******************************************************************************/

package edu.utk.cs.futurelens.ui.windows;

import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import java.text.*;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import org.jdatepicker.*;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import edu.utk.cs.futurelens.FutureLens;
import edu.utk.cs.futurelens.data.Stoplist;
import edu.utk.cs.futurelens.data.DateRange;
import edu.utk.cs.futurelens.ui.Callback;
import edu.utk.cs.futurelens.ui.Consts;
import edu.utk.cs.futurelens.ui.FLInterface;
import edu.utk.cs.futurelens.ui.MenuBar;
import edu.utk.cs.futurelens.ui.Prefs;

public class WinPreferences implements IWindow {

	private static Shell shell;

	private Shell parentShell = null;
	private Display parentDisplay = null;

	
	
	
	// controls used
	Text txtExtension;
	Text txtCommonWords;

	Button btnOk, btnCancel, btnApply;

	private static boolean isOpen;

	public WinPreferences(Shell parent) {
		parentShell = parent;
	}

	public WinPreferences(Display parent) {
		parentDisplay = parent;
	}

	public void destroyWindow() {
		// macs have no ok/cancel buttons or tab
		// if(FLInterface.isMac())
		// saveSettings();

		isOpen = false;
		shell.dispose();
	}

	public Shell getShell() {
		return shell;
	}

	public void loadWindow() {
		// only open one window
		if (isOpen) {
			shell.forceActive();
			return;
		}

		if (parentShell != null)
			shell = new Shell(parentShell, SWT.DIALOG_TRIM);
		else
			shell = new Shell(parentDisplay, SWT.DIALOG_TRIM);

		shell.setText(Consts.PROJECT_NAME + " Preferences");

		// set the size
		shell.setSize(Consts.PREFERENCE_WIDTH, Consts.PREFERENCE_HEIGHT);

		// create all the controls
		createControls();

		// setup the menubar
		if (FLInterface.isMac()) {
			MenuBar mb = new MenuBar(shell);
			mb.connect(mb.fileCloseWindow, this, "destroyWindow");
		}

		// connect some callbacks
		Callback.connect(shell, SWT.Close, this, "destroyWindow");

		// if( FLInterface.isMac())
		// {
		Callback.connect(btnOk, SWT.Selection, this, "onOkClick");
		Callback.connect(btnApply, SWT.Selection, this, "onApplyClick");
		Callback.connect(btnCancel, SWT.Selection, this, "onCancelClick");
		// }

		// center the window in the parent
		FLInterface.centerWindow(shell);

		shell.open();

		isOpen = true;
	}

	@SuppressWarnings("unused")
	private void onOkClick() {

		saveSettings();
		shell.close();
	}

	@SuppressWarnings("unused")
	private void onCancelClick() {
		shell.close();
	}

	@SuppressWarnings("unused")
	private void onApplyClick() {
		saveSettings();
	}

	private void saveSettings() {
		Prefs.set(Prefs.DATASET_EXT, txtExtension.getText());
		Stoplist.WriteStopList(txtCommonWords.getText());
		// Prefs.set(Prefs.DATASET_IGNORE, txtCommonWords.getText());
	}

	private void createControls() {
		
		GridLayout layout = new GridLayout(1, false);
		GridData data;
		TabFolder tf = new TabFolder(shell, SWT.NONE);
		Composite comp = new Composite(tf, SWT.NONE);

		Label label;
		TabItem item;

		layout.marginHeight = Consts.PREFERENCE_TAB_MARGIN_HEIGHT;
		layout.marginWidth = Consts.PREFERENCE_TAB_MARGIN_WIDTH;
		shell.setLayout(layout);

		// set up the layout for the tab folder
		tf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// add the general tab
		item = new TabItem(tf, SWT.NONE);
		item.setText("Dataset");
		item.setControl(comp);
		comp.setLayout(new GridLayout(3, true));

		// add in ok/cancel buttons for non mac
		// if( FLInterface.isMac())
		// {
		Composite innercomp;
		GridLayout innerlayout = new GridLayout(3, true);

		innerlayout.marginHeight = 0;
		innerlayout.marginWidth = 0;

		// i should probably change the 75 to some constant
		data = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		data.widthHint = 75;

		innercomp = new Composite(shell, SWT.NONE);
		innercomp.setLayout(innerlayout);
		innercomp.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false));

		btnOk = new Button(innercomp, SWT.PUSH);
		btnOk.setText("OK");
		btnOk.setLayoutData(data);

		btnCancel = new Button(innercomp, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLayoutData(data);

		btnApply = new Button(innercomp, SWT.PUSH);
		btnApply.setText("Apply");
		btnApply.setLayoutData(data);
		// }

		// add in the preference items
		// dataset extension
		label = new Label(comp, SWT.NONE);
		label.setText("Dataset extension:");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1,
				1));

		txtExtension = new Text(comp, SWT.BORDER);
		txtExtension.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));
		txtExtension.setText(Prefs.get(Prefs.DATASET_EXT,
				Prefs.DATASET_EXT_DEFAULT));

		// common words
		label = new Label(comp, SWT.NONE);
		label.setText("Ignore common words:");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false, 1, 1));

		txtCommonWords = new Text(comp, SWT.MULTI | SWT.BORDER | SWT.WRAP
				| SWT.V_SCROLL);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		data.heightHint = 100;
		txtCommonWords.setLayoutData(data);
		txtCommonWords.setText(Stoplist.FormatReadList());
		
		//Datelabel
		label = new Label(comp, SWT.NONE);
		label.setText("Date Range(From/To)");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false, 1, 1));
		
		//DateDropdown
		final DateTime dateFrom = new DateTime(comp, SWT.BORDER | SWT.DATE | SWT.DROP_DOWN);
		final DateTime dateTo = new DateTime(comp, SWT.BORDER | SWT.DATE | SWT.DROP_DOWN);
		
		Button button = new Button(shell, SWT.NONE);
	    button.setText("Click To Set Date Range");
	    button.addListener(SWT.Selection, new Listener() {
	      public void handleEvent(Event e) {
	        switch (e.type) {
	        case SWT.Selection:
	        	this.writeDates(dateFrom,dateTo);
	          break;
	          
	        }
	      }
	      //method activated when button is pressed to trim dates and write them to a file
		private void writeDates(DateTime dateFrom, DateTime dateTo) {
			//trimming strings
			String dateFromString = dateFrom.toString().substring(dateFrom.toString().indexOf("{")+1,dateFrom.toString().indexOf("}"));
	        String dateToString = dateTo.toString().substring(dateTo.toString().indexOf("{")+1,dateTo.toString().indexOf("}"));
	        //creating path
	        String path = FutureLens.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	        String winLoc="";
	        try {
				winLoc = URLDecoder.decode(path, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	        String name = winLoc + "Dates";
			File dir = new File(name);
			if (!dir.exists()) {
				dir.mkdir();
			}
			name = winLoc + "Dates/Dates.txt";
			if (!(new File(name).exists())) {
				name = winLoc + "Dates/Dates.txt";
			}
			
			//writing string to file
			FileWriter fstream;
			try {
				fstream = new FileWriter(name);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(dateFromString+"|"+dateToString);
				out.close();
			} catch (Exception e) {// Catch exception if any
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace();
			}
			
		}
	    });
	}
	
}
