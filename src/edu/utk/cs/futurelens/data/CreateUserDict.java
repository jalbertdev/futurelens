package edu.utk.cs.futurelens.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import edu.utk.cs.futurelens.data.dictionary.Dictionary;
import edu.utk.cs.futurelens.data.dictionary.FrequencyCmp;
import edu.utk.cs.futurelens.ui.FLInterface;

/**
 * @author Joshua
 */

public class CreateUserDict {

	static Display display;
	static Shell shell;
	static int values[] = { 0, 0 };
	static DataSet ds;
	static Dictionary oldDict;

	public static Dictionary getOldDict() {
		return oldDict;
	}

	private static int[] openInputWindow() {
		shell = new Shell(display, SWT.TITLE | SWT.BORDER
				| SWT.APPLICATION_MODAL);
		shell.setText("Frequency of Terms");

		shell.setLayout(new GridLayout(2, true));

		Label instLabel = new Label(shell, SWT.NONE);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		instLabel
				.setText("If you only want to use one frequency enter 0 into the one you do not want to use.");
		instLabel.setLayoutData(data);

		Label eLabel = new Label(shell, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		eLabel.setLayoutData(data);

		Label maxLabel = new Label(shell, SWT.NONE);
		maxLabel.setText("Please enter a maximum frequency:");
		final Text maxText = new Text(shell, SWT.SINGLE | SWT.BORDER);

		Label minLabel = new Label(shell, SWT.NONE);
		minLabel.setText("Please enter a minimum frequency:");
		final Text minText = new Text(shell, SWT.SINGLE | SWT.BORDER);

		final Button btnOk = new Button(shell, SWT.PUSH);
		btnOk.setText("Ok");
		btnOk.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		btnOk.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				shell.close();
			}
		});

		Button btnCancel = new Button(shell, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				values[0] = 0;
				values[1] = 0;
				shell.close();
			}
		});

		maxText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent Event) {
				try {
					values[0] = new Integer(maxText.getText());
					btnOk.setEnabled(true);
					try {
						values[1] = new Integer(minText.getText());
						btnOk.setEnabled(true);
					} catch (Exception e1) {
						btnOk.setEnabled(false);
					}
				} catch (Exception e) {
					btnOk.setEnabled(false);
				}
			}
		});

		minText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent Event) {
				try {
					values[1] = new Integer(minText.getText());
					btnOk.setEnabled(true);
					try {
						values[0] = new Integer(maxText.getText());
						btnOk.setEnabled(true);
					} catch (Exception e1) {
						btnOk.setEnabled(false);
					}
				} catch (Exception e) {
					btnOk.setEnabled(false);
				}
			}
		});

		minText.setText("");
		maxText.setText("");
		shell.pack();
		FLInterface.centerWindow(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return values;
	}
	

	public static void createUserDictionary(DataSet ds, Display parentDisplay) {
		display = parentDisplay;
		shell = new Shell(display);
		
		
		int freqs[] = openInputWindow();
		int max = freqs[0];
		int min = freqs[1];

		while (min > max && max != 0) {
			shell = new Shell(display);
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR
					| SWT.OK);
			messageBox.setText("Error");
			messageBox
					.setMessage("The minimum value can not be greater than the maximum value.\nPlease enter a valid range.");
			messageBox.open();
			freqs = openInputWindow();
			max = freqs[0];
			min = freqs[1];
		}

		Dictionary dict = ds.getGlobalDict();
		Dictionary newDict = new Dictionary();
		ArrayList<Entry<String, Integer>> hashentries = new ArrayList<Map.Entry<String, Integer>>(
				dict.getSet());
		Iterator<Entry<String, Integer>> it = hashentries.iterator();	
		while (it.hasNext()) {
			Entry<String, Integer> s = it.next();

			int value = s.getValue();
			String term = s.getKey();

			if (value >= min && min != 0 && max == 0) {

				newDict.addTerm(term, value);

			} else if (value <= max && max != 0 && min == 0) {

				newDict.addTerm(term, value);

			} else if (value >= min && value <= max) {

				newDict.addTerm(term, value);

			}

		}
		ds.setGlobalDict(newDict);
		if (!shell.isDisposed())
			shell.close();
	}
}
