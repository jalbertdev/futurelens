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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class DocumentBlock extends Composite 
{
	private Label lblHeader;
	private Canvas cDocuments;
	
	// adjustable settings
	private int numDocuments;
	
	// non adjustable settings
	private final int defaultWidth = 120;
	private final int canvasMargin = 2;
	private final int labelCanvasSpace = 5;
	private final int selectBandWidth = 5;
	private final int selectBandHeight = 12;
	private final int selectBandGap = 1;
	private final double selectBandOpacity = 0.40;
	private final int documentHeight = 5;
	private final int documentMarginBottom = 3;
	private final int documentMarginTop = 3;
	private final Color bgColor1 = getDisplay().getSystemColor(SWT.COLOR_WHITE);
	private final Color bgColor2 = new Color(getDisplay(), 240, 240, 240);
	private final Color lineColor = new Color(getDisplay(), 220, 220, 220);
	
	// other stuff
	private boolean isSelected;
	private int selectedDoc;
	private int numLegends;
	private int lastPosition = 0;
	private ArrayList<Listener> selectionListeners;
	private Hashtable<Legend, DocumentSet> legends;
	
	// cached version of the drawing
	private Image imgCache;
	
	// class to store a set of documents for a term
	private class DocumentSet
	{
		public int max;
		public int position;
		public ArrayList<Document> documents;
		
		DocumentSet()
		{
			documents = new ArrayList<Document>();
			max = 0;
		}
	}
	
	// class to store a single document for a set
	private class Document
	{
		public int documentIndex;
		public int value;
	}
	
	// class that compares two documentsets by position
	private class DocumentSetCmp implements Comparator<Map.Entry<Legend, DocumentSet>>
	{
		public int compare(Entry<Legend, DocumentSet> o1, Entry<Legend, DocumentSet> o2) {
			return o1.getValue().position - o2.getValue().position;
		}
	}
	
	public DocumentBlock(Composite parent, int style) 
	{
		super(parent, style | SWT.NONE);
		
		// create the children widgets
		lblHeader = new Label(this, SWT.CENTER);
		lblHeader.setText("Documents");
		
		cDocuments = new Canvas(this, SWT.NONE);
		
		// zero everything out
		numDocuments = 0;
		isSelected = false;
		selectedDoc = 0;
		imgCache = null;
		numLegends = 0;
		
		selectionListeners = new ArrayList<Listener>();
		legends = new Hashtable<Legend, DocumentSet>(); 
		
		// add some listeners
		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				DocumentBlock.this.onResize(e);
			}
		});
		
		cDocuments.addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				onKeyDown(e);
			}			
		});
		
		cDocuments.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				DocumentBlock.this.onMouseUp(e);
			}
		});
		
		cDocuments.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				DocumentBlock.this.onCanvasPaint(e);
			}
		});
	}

	public void setDocMax(Legend legend, int max)
	{
		legends.get(legend).max = max;
	}
	
	public void setNumberOfDocuments(int numDocuments)
	{
		this.numDocuments = numDocuments;
	}
	
	public void setTitle(String title)
	{
		if(title == null)
			throw new IllegalArgumentException("title cannot be null");
		
		lblHeader.setText(title);
	}
	
	public int getSelection()
	{
		return(selectedDoc);
	}
	
	public void clearSelection()
	{
		if(isSelected)
		{
			isSelected = false;
			invalidateCache();
			cDocuments.redraw();
		}
	}
	
	public void removeLegend(Legend legend)
	{
		if(legends.containsKey(legend))
		{
			legends.remove(legend);	
			numLegends--;
		
			// invalidate the cache
			invalidateCache();
		}
	}
	
	public void repaint()
	{
		invalidateCache();
		redraw(0, 0, getSize().x, getSize().y, true);
	}
	
	public void addLegend(Legend legend)
	{
		DocumentSet ds = new DocumentSet();
	
		ds.position = lastPosition++;
		
		// put the new list into the highlighted list
		legends.put(legend, ds);
		
		numLegends++;
	}
	
	public void resetLegend(Legend legend)
	{
		DocumentSet ds = legends.get(legend);
		
		ds.documents = new ArrayList<Document>();
		ds.max = 0;
	}
	
	/**
	 * @param term
	 * @param documentIndex absolute document index in the document block
	 * @param value
	 */
	public void highlightDocument(Legend legend, int documentIndex, int value)
	{
		Document doc = new Document();

		if(documentIndex > this.numDocuments)
			throw new IllegalArgumentException("documentIndex is out of range (requested: " + documentIndex + " max: " + this.numDocuments);
		
		doc.documentIndex = documentIndex;
		doc.value = value;
		
		// find the legend in the list
		DocumentSet ds = legends.get(legend);
		
		if(ds == null)
		{
			return;
		}
		
		if(value > ds.max)
			ds.max = value;
		
		ds.documents.add(doc);
		
		// invalidate the cache
		invalidateCache();
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
	
	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		Point headerExtent = lblHeader.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
		int width, height;
		
		width = defaultWidth;
		height = headerExtent.y + labelCanvasSpace + documentMarginTop + numDocuments * documentHeight 
					+ documentMarginBottom + canvasMargin;
		
		if(wHint != SWT.DEFAULT)
			width = wHint;
		
		if(hHint != SWT.DEFAULT)
			height = hHint;
		
		return(new Point(width, height));
	}
	
	public void setBackground(Color color)
	{
		super.setBackground(color);
		cDocuments.setBackground(color);
		System.out.println(color);
	}
	
	private void onKeyDown(KeyEvent e)
	{
		if(e.keyCode == SWT.ARROW_DOWN)
		{
			if(selectedDoc < numDocuments - 1)
			{
				selectedDoc++;
				invalidateCache();
				cDocuments.redraw();
				raiseSelection();
			}
		}
		else if(e.keyCode == SWT.ARROW_UP)
		{
			if(selectedDoc > 0)
			{
				selectedDoc--;
				invalidateCache();
				cDocuments.redraw();
				raiseSelection();
			}
		}
	}
	
	private void onMouseUp(MouseEvent e)
	{
		int docnumber;
		
		// figure out what document got selected, if any  
		docnumber = (e.y - documentMarginTop) / documentHeight;
		
		if(docnumber < 0)
			docnumber = 0;
		else if(docnumber >= numDocuments)
			docnumber = numDocuments - 1;
		
		// move the select band		
		invalidateCache();
		cDocuments.redraw();
		
		// only raise the event if the selection has changed
		if(selectedDoc != docnumber || isSelected == false)
		{
			selectedDoc = docnumber;
			
			raiseSelection();
		}
		
		isSelected = true;
	}
	
	private void raiseSelection()
	{
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
		Point headerExtent = lblHeader.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
		//Point canvasExtent = cDocuments.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
		Rectangle parentExtent = this.getClientArea();
		
		lblHeader.setBounds((parentExtent.width - headerExtent.x) / 2, 1, headerExtent.x, headerExtent.y);
		cDocuments.setBounds(1, headerExtent.y + labelCanvasSpace, parentExtent.width - canvasMargin, 
				parentExtent.height - headerExtent.y - (labelCanvasSpace + canvasMargin));
		
		// invalidate the cache
		invalidateCache();
	}
	
	private void invalidateCache()
	{
		if(imgCache != null)
			imgCache.dispose();
		
		imgCache = null;
	}
	
	private void onCanvasPaint(PaintEvent e)
	{
		GC gc = e.gc;
		Rectangle canvasSize = cDocuments.getClientArea();

		if(imgCache == null)
		{
			imgCache = new Image(this.getDisplay(), canvasSize.width, canvasSize.height);
			GC canvas = new GC(imgCache);
			
			// clear the background
			canvas.setBackground(this.getBackground());
			canvas.fillRectangle(canvasSize);
			
			// don't draw anything if there are no documents
			if(numDocuments > 0)
			{
				int blockWidth = canvasSize.width - (selectBandGap + selectBandWidth) * 2;
				int termX = canvasSize.x + selectBandWidth + selectBandGap;
				
				// create the gradient fill
				Pattern p1 = new Pattern(this.getDisplay(), 0, 0, 0, canvasSize.height, bgColor1, bgColor2);
				
				canvas.setBackgroundPattern(p1);
				canvas.fillRoundRectangle(canvasSize.x + selectBandWidth + selectBandGap, canvasSize.y, 
						blockWidth, canvasSize.height, 8, 8);
				canvas.setBackgroundPattern(null);
								
				// draw all the selections
				if(numLegends > 0)
				{
					int termWidth = blockWidth / numLegends;
					
					// get the key set organized by position
					ArrayList<Entry<Legend, DocumentSet>> termKeys = new ArrayList<Map.Entry<Legend, DocumentSet>>(legends.entrySet());
					Collections.sort(termKeys, new DocumentSetCmp());
					
					// who doesn't like transparency?
					canvas.setAlpha(150);
					
					// go through each legend
					for(Map.Entry<Legend, DocumentSet> keyEntry : termKeys)
					{
						Legend legend = keyEntry.getKey();
						DocumentSet ds = legends.get(legend);
						int max = ds.max;
							
						for(Document doc : ds.documents)
						{
							// figure out what color to use
							int index = (int)(((float)doc.value / max) * legend.getNumBlocks());
							
							// check if the term is at the max
							if(index >= legend.getNumBlocks())
								index = legend.getNumBlocks() - 1;
							
							if(legend.getColor(index) == null)
								continue;
							
							// set the color
							canvas.setBackground(legend.getColor(index));
							
							// check if this is the last block
							if(termX / termWidth == numLegends - 1)
							{
								int rightBound = blockWidth + selectBandGap + selectBandWidth;
								
								// fill in the whole block
								if((termX + termWidth) < rightBound)
									termWidth += rightBound - (termX + termWidth);
							}
							
							// draw the block
							canvas.fillRectangle(termX, doc.documentIndex * documentHeight + documentMarginTop, termWidth, documentHeight);
						}
						
						termX += termWidth;
					}
				}
				
				// draw all the lines
				canvas.setForeground(lineColor);
				canvas.setAlpha(96);
				termX = canvasSize.x + selectBandWidth + selectBandGap;
				for(int i = 1; i < numDocuments; i++)
					canvas.drawLine(termX, documentHeight * i + documentMarginTop, blockWidth + termX - 1, documentHeight * i + documentMarginTop);
				canvas.setAlpha(255);
				
				// draw the select bar if necessary
				if(isSelected)
				{
					int y = selectedDoc * documentHeight + documentMarginTop;
					
					canvas.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_BLUE));
					canvas.setAlpha((int)(255 * selectBandOpacity));
					canvas.fillRectangle(selectBandWidth, y, canvasSize.width - selectBandWidth * 2, documentHeight);
					
					// draw the side bands
					//canvas.setAlpha(190);
					canvas.fillRoundRectangle(0, y - selectBandHeight / 2, selectBandWidth, selectBandHeight + documentHeight, 2, 2);
					canvas.fillRoundRectangle(canvasSize.width - selectBandWidth, y - selectBandHeight / 2, 
							selectBandWidth, selectBandHeight + documentHeight, 3, 3);
				}
				
				// get rid of everything
				p1.dispose();
				canvas.dispose();
			}
		}
		
		// copy
		gc.drawImage(imgCache, e.x, e.y, e.width, e.height,
				e.x, e.y, e.width, e.height);
	}
}
