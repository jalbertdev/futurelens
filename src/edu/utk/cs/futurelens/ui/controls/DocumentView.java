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

package edu.utk.cs.futurelens.ui.controls;

//import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.graphics.Font;

import edu.utk.cs.futurelens.data.DataElement;
import edu.utk.cs.futurelens.data.FileLoader;
import edu.utk.cs.futurelens.data.parser.Handler;

public class DocumentView extends Composite {
	// controls used
	private StyledText txtMain;
	private Composite cmpMetaData;
	private Label lblFileName;
	// private Label lblScore;
	private Label lblDate;

	Pattern pattern = Pattern.compile("<(?:TIMEX\\s+TYPE|ENAMEX\\s+TYPE)=.+?>(.+?)</(?:TIMEX|ENAMEX)>", Pattern.DOTALL);

	private final String defaultFileName = "No document selected";

	// other stuff
	private Hashtable<String, Color> highlightTerms;

	@Override
	public int hashCode() {
		final int prime = 92821;
		int result = 1;
		result = prime * result
				+ ((highlightTerms == null) ? 0 : highlightTerms.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentView other = (DocumentView) obj;
		if (highlightTerms == null) {
			if (other.highlightTerms != null)
				return false;
		} else if (!highlightTerms.equals(other.highlightTerms))
			return false;
		return true;
	}

	public DocumentView(Composite parent, int style) {
		super(parent, style);

		// create a new list of highlighted terms
		highlightTerms = new Hashtable<String, Color>();

		// create the controls
		GridLayout layout = new GridLayout(1, true);
		layout.horizontalSpacing = layout.verticalSpacing = 4;
		layout.marginHeight = layout.marginWidth = 0;
		this.setLayout(layout);

		// document info
		cmpMetaData = new Composite(this, SWT.NONE);
		cmpMetaData.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		cmpMetaData.setLayout(new GridLayout(2, false));

		Label label = new Label(cmpMetaData, SWT.NONE);
		label.setText("Filename:");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		lblFileName = new Label(cmpMetaData, SWT.LEFT | SWT.WRAP);
		lblFileName.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		lblFileName.setText(defaultFileName);

		// date
		label = new Label(cmpMetaData, SWT.NONE);
		label.setText("Date:");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		lblDate = new Label(cmpMetaData, SWT.LEFT);
		lblDate.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// score
		// label = new Label(composite, SWT.NONE);
		// label.setText("Score:");
		// label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		//
		// lblScore = new Label(composite, SWT.NONE);
		// lblScore.setText("0.410");
		// lblScore.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		//
		// // hide the score stuff for now
		// label.setVisible(false);
		// lblScore.setVisible(false);

		// main text box
		txtMain = new StyledText(this, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY
				| SWT.WRAP | SWT.V_SCROLL);
		txtMain.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		txtMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		// attach some listeners
		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				DocumentView.this.onResize(e);
			}
		});
	}

	public void showDocument(DataElement doc) {
		if (doc == null) {
			// clear everything
			txtMain.setText("");
			lblFileName.setText(defaultFileName);
			lblDate.setText("");
			this.layout(true);
			return;
		}

		String formattedDate;
		if (doc.getDate() != null) {
			SimpleDateFormat df = new SimpleDateFormat("MMMMM d, yyyy");
			formattedDate = df.format(doc.getDate());
		} else {
			formattedDate = "Unknown";
		}

		StringBuilder fileData = new StringBuilder(2048);
		try {
			String path = FileLoader.getPath() + File.separator
					+ doc.getFileName();
			BufferedReader reader = new BufferedReader(new FileReader(path));
			char buf[] = new char[2048];
			int bytesRead;

			while ((bytesRead = reader.read(buf)) != -1)
				fileData.append(buf, 0, bytesRead);
			reader.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		txtMain.setText(fileData.toString());
		highlightTags();

		//System.out.println(txtMain.getLineCount());
		// txtMain.setText(doc.getRawData());
		lblFileName.setText(doc.getFileName());
		lblDate.setText(formattedDate);
		this.layout(true);

		// update the styles if there are any
		if (highlightTerms.size() > 0)
			updateStyles();
	}

	public void highlightTerm(String[] terms, Color color) {
		for (String t : terms)
			highlightTerms.put(t.toLowerCase(), color);

		updateStyles();
	}

	public void highlightTerm(ArrayList<String> terms, Color color) {
		for (String t : terms)
			highlightTerms.put(t.toLowerCase(), color);

		updateStyles();
	}

	public void highlightTerm(String term, Color color) {
		highlightTerms.put(term.toLowerCase(), color);
		updateStyles();
	}

	public void removeTerm(String[] terms) {
		String lTerm;

		for (String t : terms) {
			if (t != null) {
				lTerm = t.toLowerCase();
				// System.out.println("full terms list: " + lTerm);
				// remove the whole phrase (if chained a phrase)
				// this will also clear out non-chained terms, or chained
				// non-phrases
				if (highlightTerms.containsKey(lTerm))
					highlightTerms.remove(lTerm);

				// remove the individual components of phrase (if phrase)
				StringTokenizer st = new StringTokenizer(lTerm);
				while (st.hasMoreTokens()) {
					String elem = st.nextToken();

					if (highlightTerms.containsKey(elem)) {
						highlightTerms.remove(elem);
						// System.out.println("Just removed: " + elem);
					}
				}
			}
		}
		// System.out.println("highlight terms now are: " + highlightTerms);
		updateStyles();
	}

	public void removeTerm(String term) {
		String lTerm = term.toLowerCase();

		if (!highlightTerms.containsKey(lTerm))
			return;

		highlightTerms.remove(lTerm);

		// update all the styles
		updateStyles();
	}

	public Point computeSize(int wHint, int hHint, boolean changed) {
		return (txtMain.computeSize(wHint, hHint, true));
	}

	private void removeStyles() {
		StyleRange clearRange = new StyleRange(0, txtMain.getText().length(),
				null, null);
		txtMain.setStyleRange(clearRange);
	}

	private void updateStyles() {
		String text = txtMain.getText();
		// remove the current styles
		removeStyles();
		highlightTags();
		for (String term : highlightTerms.keySet()) {
			// compile a regex to search for the term
			Pattern pTerm = Pattern.compile("\\b" + term + "\\b",
					Pattern.CASE_INSENSITIVE);
			Matcher mTerm = pTerm.matcher(text);

			// find the terms
			while (mTerm.find()) {
				StyleRange style = new StyleRange(mTerm.start(), term.length(),
						null, highlightTerms.get(term));
		
				txtMain.setStyleRange(style);
				
			}
			
		}
	}

	private void onResize(ControlEvent event) {
		Rectangle parentExtent = this.getClientArea();

		txtMain.setBounds(0, 0, parentExtent.width, parentExtent.height);
	}

private void highlightTags(){
	int previous_index = 0;
	StyleRange sr = new StyleRange();	

	String s = txtMain.getText();
	Matcher matcher = pattern.matcher(s);

	while (matcher.find()) {
		previous_index = s.indexOf(matcher.group(1), previous_index);
		//System.out.println("WORD:" + matcher.group(1) + " Start:" + previous_index + " Length:" + matcher.group(1).length());
		sr.start = previous_index;
		sr.length = matcher.group(1).length();

		sr.fontStyle = SWT.BOLD;
		  sr.foreground = this.getDisplay().getSystemColor(SWT.COLOR_BLUE);

		txtMain.setStyleRange(sr);
		}
	}
}
