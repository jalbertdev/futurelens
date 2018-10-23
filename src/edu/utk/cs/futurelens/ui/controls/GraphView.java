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

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;

import edu.utk.cs.futurelens.data.DataElement;
import edu.utk.cs.futurelens.data.DataSet;
import edu.utk.cs.futurelens.data.DocumentSet;
import edu.utk.cs.futurelens.data.DataSet.DateRange;

public class GraphView extends Composite 
{
	// associated dataset
	private DataSet dataSet = null;
	
	// document set
	private ArrayList<DocumentSet> documentSets = null;
	
	// all the legends
	private ArrayList<Legend> legends = null;
	
	// non changeable stuff
	private final int defaultWidth = 125;
	private final int defaultHeight = 50;
	private final int bottomLabelSpacing = 10;
	private final int bottomLabelPadding = 4;
	private final int sideLabelPadding = bottomLabelPadding;
	private final int margin = 5;
	private final int lineWeight = 2;
	private final Color gridLineColor = new Color(getDisplay(), 230, 230, 230);
	private final Color graphBgColor1 = getDisplay().getSystemColor(SWT.COLOR_WHITE);
	private final Color graphBgColor2 = new Color(getDisplay(), 230, 230, 230);
	private final Color graphBorderColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
	private final String yAxisLabel = "% docs";
	
	// cache stuff
	private Image imgCache = null;
	private boolean cacheDirty = true;
	private double max = 0;
	
	public GraphView(Composite parent, int style) 
	{
		super(parent, style);
		
		legends = new ArrayList<Legend>();
		
		addPaintListener( new PaintListener() {
			public void paintControl(PaintEvent e) {
				GraphView.this.onPaint(e);
			}
		});
		
		addControlListener( new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				GraphView.this.onResize(e);
			}
		});
	}

	public void addLegend(Legend legend)
	{
		legends.add(legend);
		updateMax();
		repaint();
	}
	
	public void removeLegend(Legend legend)
	{
		legends.remove(legend);
		updateMax();
		repaint();
	}
	
	public void updateLegend()
	{
		updateMax();
		repaint();
	}
	
	private void updateMax()
	{
		// calculate the maximum percentage
		max = 0;
	
		if(legends.size() == 0)
			return;
		
		for(Legend l : legends)
		{
			double[] points = calculatePoints(l);
		
			if(points == null)
				break;
			
			for(double d : points)
				if(d > max)
					max = d;
		}
	}
	
	public void setDataSet(DataSet dataSet)
	{
		this.dataSet = dataSet;
	}
	
	public void setDateRange(DateRange dr)
	{
		documentSets = dataSet.getDocumentsByDateRange(dr);
		
		repaint();
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		int width, height;
		
		if(wHint != SWT.DEFAULT)
			width = wHint;
		else
			width = defaultWidth;
		
		if(hHint != SWT.DEFAULT)
			height = hHint;
		else
			height = defaultHeight;
		
		return(new Point(width, height));
	}
	
	private void onResize(ControlEvent e)
	{
		cacheDirty = true;
	}
	
	private void onPaint(PaintEvent e)
	{
		GC gc = e.gc;
		
		if(cacheDirty || imgCache == null)
		{
			Rectangle canvasSize = this.getClientArea();
			int x, y;
			
			// need to repaint
			if(imgCache == null)
				imgCache = new Image(getDisplay(), canvasSize.width, canvasSize.height);
			
			// resize the cache if necessary
			if(imgCache.getBounds().height != canvasSize.height ||
					imgCache.getBounds().width != canvasSize.width)
			{
				imgCache.dispose();
				imgCache = new Image(getDisplay(), canvasSize.width, canvasSize.height);
			}
			
			GC canvas = new GC(imgCache);
			
			// blank the background
			canvas.setBackground(getBackground());
			canvas.fillRectangle(canvasSize);
			
			if(documentSets != null)			
			{
				// make sure there is at least one known date
				int startIndex = getFirstKnownDate();
				int numKnown = startIndex < 0 ? 0 : documentSets.size() - startIndex;
				
				// figure out where to put the graph
				Point pBottomText;
				if(numKnown > 0)
					pBottomText = canvas.stringExtent(documentSets.get(startIndex).getTitle());
				else
					pBottomText = canvas.stringExtent("######");
				
				// this should probably be fixed at some point
				Point pSideText = canvas.stringExtent("####");
				
				Point pSideLabel = canvas.stringExtent(yAxisLabel);
			
				Color oldFg = canvas.getForeground();
				
				// draw the surrounding rectangle
				x = margin + pSideText.x + pSideLabel.y + sideLabelPadding;
				y = margin;
				
				int width = canvasSize.width - margin - x;
				int height = canvasSize.height - pBottomText.y - y - margin - bottomLabelPadding;
				
				Pattern p1 = new Pattern(this.getDisplay(), 0, 0, 0, canvasSize.height, graphBgColor1, graphBgColor2);
				canvas.setBackgroundPattern(p1);
				
				canvas.fillRectangle(x, y, width, height);
				
				canvas.setBackgroundPattern(null);
				p1.dispose();
				
				canvas.setForeground(graphBorderColor);
				
				// draw the borders
				y = canvasSize.height - pBottomText.y - margin - bottomLabelPadding;
				canvas.drawLine(x, margin, x, y);
				canvas.drawLine(x, y, canvasSize.width - margin, y);
				canvas.setForeground(oldFg);
				
				y = canvasSize.height - pBottomText.y - margin;
				
				int docSet = startIndex;
				int xDelta = numKnown > 1 ? width / (numKnown - 1) : width;
				
				canvas.setForeground(oldFg);
				
				// draw all the x labels
				int xStart = x;
				
				if(numKnown > 0)
				{
					while(x < canvasSize.width - pBottomText.x)
					{
						// draw the label
						canvas.drawText(documentSets.get(docSet).getTitle(), x, y, true);
						
						// move to the next x position
						x += pBottomText.x + bottomLabelSpacing;
						
						// adjust so it's a multiple of dx
						x += xDelta - x % xDelta;
						
						// figure out which label to draw
						docSet = startIndex + (x - xStart) / xDelta + 1;
						
						// check for out of bounds
						if(docSet >= documentSets.size())
							break;
					}
				}
				
				// draw all the grid lines
				x = margin + pSideText.x + pSideLabel.y + sideLabelPadding;
				canvas.setForeground(gridLineColor);
				for(int i = 1; i < numKnown; i++)
					canvas.drawLine(x + xDelta * i, 0, x + xDelta * i, y - bottomLabelPadding - 1);
				
				// label the y axis
				canvas.setForeground(oldFg);
				
				// i don't know why this works but it does
				x = (canvasSize.height + pSideLabel.x) / -2;
				y = margin;
				
				Transform transform = new Transform(getDisplay());
				transform.rotate(-90);
				canvas.setTransform(transform);
				canvas.drawText(yAxisLabel, x, y, true);
				canvas.setTransform(null);
				transform.dispose();
				
				// draw the side labels
				x = margin + pSideLabel.y + pSideText.x - canvas.stringExtent("0").x;
				y = canvasSize.height - pBottomText.y - margin - bottomLabelPadding - pSideText.y / 2;
				
				canvas.drawText("0", x, y, true);
				
				x = margin + pSideText.x + pSideLabel.y;
				y = margin - pSideText.y / 2;
				
				if(max > 0 && startIndex >= 0)
				{
					DecimalFormat df = new DecimalFormat("###");
					x -= canvas.stringExtent(df.format(max * 100)).x; 
					canvas.drawText(df.format(max * 100), x, y, true);
				}
				else
				{
					x -= canvas.stringExtent("100").x;
					canvas.drawText("100", x, y, true);
				}
				
				// draw the lines
				if(legends.size() > 0)
				{
					// set the line weight
					canvas.setLineWidth(lineWeight);
					
					// turn anti aliasing on
					canvas.setAntialias(SWT.ON);
					
					for(Legend l : legends)
					{
						double[] points = calculatePoints(l);
						
						// check if there is something to draw
						if(points == null)
							break;
						
						// move to the lower left corner
						x = margin + pSideText.x + pSideLabel.y + sideLabelPadding + 1;
						y = canvasSize.height - pBottomText.y - margin - bottomLabelPadding - 1;
						
						// set the line color
						canvas.setForeground(l.getColor(-1));
						
						// draw the line
						for(int i = 0; i < points.length - 1; i++)
						{
							canvas.drawLine(x, y - pointToPixel(points[i], height), x + xDelta, y - pointToPixel(points[i + 1], height));
							
							// move over
							x += xDelta;
						}
					}
				}
			}
			
			canvas.dispose();
			
			cacheDirty = false;
		}
		
		// copy
		gc.drawImage(imgCache, e.x, e.y, e.width, e.height,
				e.x, e.y, e.width, e.height);
	}
	
	private double[] calculatePoints(Legend legend)
	{
		int startIndex = getFirstKnownDate();
		int numKnown = documentSets.size() - startIndex;
		
		if(startIndex < 0)
			return(null);
		
		double[] points = new double[numKnown];
		
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
		
		// clear out all the points
		for(int i = 0; i < points.length; i++)
			points[i] = 0;
		
		// calculate the number of documents in this date range
		if(allDocs!=null){
		for(DataElement doc : allDocs)
			if(doc.getDocumentRangeIndex() >= startIndex)
				points[doc.getDocumentRangeIndex() - startIndex]++;
		}
		
		// normalize
		int numDocs;
		for(int i = 0; i < numKnown; i++)
		{
			numDocs = documentSets.get(startIndex + i).getDocuments().size();
			
			if(numDocs > 0)
				points[i] /= numDocs;
		}
		
		return(points);
	}
	
	private int pointToPixel(double point, int height)
	{
		return((int)(point / max * (height - 2)));
	}
	
	private int getFirstKnownDate()
	{
		int index;
		
		if(documentSets == null)
			return(-1);
		
		for(index = 0; index < documentSets.size(); index++)
			if(! documentSets.get(index).isDateUnknown())
				break;
		
		if(index >= documentSets.size())
			return(-1);
		
		return(index);
	}
	
	private void repaint()
	{
		if(this.isDisposed())
			return;
		
		cacheDirty = true;
		redraw(0, 0, getSize().x, getSize().y, true);
	}
}
