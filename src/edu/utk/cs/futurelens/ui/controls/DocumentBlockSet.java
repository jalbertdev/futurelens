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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

import edu.utk.cs.futurelens.FutureLens;
import edu.utk.cs.futurelens.data.DataElement;
import edu.utk.cs.futurelens.data.DataSet;
import edu.utk.cs.futurelens.data.DocumentSet;
import edu.utk.cs.futurelens.data.DataSet.DateRange;
import edu.utk.cs.futurelens.ui.Callback;

/**
 * @author greg
 *
 */
public class DocumentBlockSet extends Composite 
{
	private DataSet dataSet = null;
	
	// controls used
	private Composite cmpMain;
	private Group grpMain;
	private Vector<DocumentBlock> documentBlocks;
	
	// other stuff
	private ArrayList<Listener> selectionListeners;
	private int selectedDoc;
	
	public DocumentBlockSet(Composite parent, int style) 
	{
		super(parent, style);
		
		// create all the controls
		grpMain = new Group(this, SWT.NONE);
		grpMain.setLayout(new FillLayout());
		
		// scrolled composite
		final ScrolledComposite sCenterGroup = new ScrolledComposite(grpMain, SWT.V_SCROLL);
		final Composite centerComp = new Composite(sCenterGroup, SWT.NONE);
		
		centerComp.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		sCenterGroup.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				Control control = sCenterGroup.getContent();
				Point ssize = sCenterGroup.getSize();
				Point csize = control.computeSize(ssize.x, SWT.DEFAULT);
				control.setSize(ssize.x, csize.y);
			}
		});
		
		sCenterGroup.setContent(centerComp);
		this.cmpMain = centerComp;
		
		// create the list of document blocks
		documentBlocks = new Vector<DocumentBlock>();
		
		// and the list of selection listeners
		selectionListeners = new ArrayList<Listener>();
		
		// add listeners
		addControlListener( new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				DocumentBlockSet.this.onResize(e);
			}
		});
	}
	
	public DocumentBlock getBlock(int index)
	{
		return(documentBlocks.get(index));
	}
	
	public int getSelectedDoc()
	{
		return(selectedDoc);
	}
	
	public void addListener(int eventType, Listener listener)
	{
		switch(eventType)
		{
		// intercept selection listeners
		case SWT.Selection:
			selectionListeners.add(listener);
			break;
		default:
			super.addListener(eventType, listener);
			break;
		}
	}
	
	public void addSelectionListener(final SelectionListener listener)
	{
		Listener l = new Listener() {
			public void handleEvent(Event event) {
				SelectionEvent e = new SelectionEvent(event);
				listener.widgetSelected(e);				
			}
		};
		
		// throw the listener on the list
		selectionListeners.add(l);
	}
	
	public void setDataSet(DataSet ds)
	{
		this.dataSet = ds;
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
		File dateFile=new File(name);
		if(dateFile.exists()){
		String dateString="";
		//read the Date File
		try (Scanner s = new Scanner(dateFile).useDelimiter("\\Z")) {
			   dateString = s.next();
			   s.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//Split the string
			String dateFromString=dateString.substring(0,dateString.indexOf("|"));
			String dateToString=dateString.substring(dateString.indexOf("|")+1,dateString.length());
		//Format into Date variable
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
			Date dateFrom, dateTo;
			System.out.println(dateToString);
			try {
				dateFrom = format.parse(dateFromString);
				dateTo= format.parse(dateToString);
				this.dataSet.trim(dateFrom, dateTo);
				System.out.println("Removing Dates Outside of The Range:");
				System.out.println(dateFrom);
				System.out.println(dateTo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		else {
			System.out.println("Date File not Found!");
		}
		
	}
	
	public void setDateRange(DateRange dr)
	{
		ArrayList<DocumentSet> documentSets = dataSet.getDocumentsByDateRange(dr);
		int startIndex = 0;
		for(DocumentSet ds : documentSets)
		{
			// create a new document block
			DocumentBlock db = new DocumentBlock(cmpMain, SWT.NONE);
			db.setTitle(ds.getTitle());
			db.setNumberOfDocuments(ds.getDocuments().size());
			
			Callback.connect(db, SWT.Selection, this, "onDocumentSelect");
						
			// add this new db to the list
			documentBlocks.add(db);			
			db.setData(startIndex);
			startIndex += ds.getSize();
		}
		
		// force the layout
		cmpMain.layout(true);
		cmpMain.setSize(cmpMain.computeSize(cmpMain.getSize().x, SWT.DEFAULT));
	}
	
	public void addLegend(Legend legend)
	{
		// add the legend to all the document blocks
		for(DocumentBlock db : documentBlocks)
			db.addLegend(legend);
	}
	
	public void updateLegend(Legend legend)
	{
		int max = 0, min = 0;

		if(legend == null)
			return;

		// reset the legend
		for(DocumentBlock db : documentBlocks)
			db.resetLegend(legend);
		
		ArrayList<DataElement> allDocs = new ArrayList<DataElement>();
		//added this code (below) to handle phrases:
		ArrayList<String> temp = legend.getTerms();
		if(temp.size()==1){
			//need to make this a call to a function that will
			//do adjacency (phrase) searching:
			allDocs = dataSet.getDocumentsWithPhrase(temp.get(0));
		}
		else{
		allDocs = dataSet.getDocumentsWithMultipleTerms(legend.getTerms());
		}
		
		if(allDocs == null)
			return;
		
		for(DataElement doc : allDocs)
		{
			// find the documentblock and highlight the term
			DocumentBlock db = documentBlocks.get(doc.getDocumentRangeIndex());
			
			if(legend.getTerms().size() == 1)
			{
				String term = legend.getTerms().get(0);
				
				if(doc.getTerm(term) > max)
					max = doc.getTerm(term);
				
				if(doc.getTerm(term) < min || min == 0)
					min = doc.getTerm(term);
				
				db.highlightDocument(legend, doc.getDocumentIndex(), doc.getTerm(term));
			}
			else
			{
				// multiple term documents can only be 0 or 1
				db.highlightDocument(legend, doc.getDocumentIndex(), 1);
			}
		}
		
		// tell the legend the max
		if((min == 0 && max > 0) || (min == max && max > 0))
			min = 1;
		
		legend.setMin(min);
		legend.setMax(max);
		
		// force the redraw
		for(DocumentBlock db : documentBlocks)
		{
			// set the same max for all document blocks
			db.setDocMax(legend, max);
			
			// force the redraw of the control
			db.repaint();
		}
	}
	
	public void removeLegend(Legend legend)
	{
		for(DocumentBlock db : documentBlocks)
		{
			// remove the term
			db.removeLegend(legend);
			
			if(db.isDisposed() != true)
				db.redraw(0, 0, db.getSize().x, db.getSize().y, true);
		}
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		return(grpMain.computeSize(wHint, hHint, true));
	}
	
	@SuppressWarnings("unused")
	private void onDocumentSelect(Event e)
	{
		DataElement doc;
		int docIndex;
		
		for(DocumentBlock db : documentBlocks)
		{
			if(! e.widget.equals(db))
				db.clearSelection();
		}
		
		// build the full index of the document
		docIndex = (Integer)e.widget.getData();
		docIndex += ((DocumentBlock)e.widget).getSelection();
		selectedDoc = docIndex;
		
		// alert the listeners
		for(Listener l : selectionListeners)
		{
			Event event = new Event();
			event.widget = this;
			l.handleEvent(event);
		}
	}
	
	private void onResize(ControlEvent e)
	{
		Rectangle parentExtent = getClientArea();
		
		// resize the group
		grpMain.setBounds(parentExtent);
	}

}
