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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class EntityViewOSXButton extends Composite 
{
	private final int defaultHeight = 14;
	private final int defaultWidth = 14;
	
	private final Color buttonColor = new Color(getDisplay(), 120, 120, 120);
	
	private Image imgCache = null;
	private boolean cacheDirty = true;
	
	private ArrayList<Listener> selectionListeners;
	
	public EntityViewOSXButton(Composite parent, int style) 
	{
		super(parent, style);
	
		setBackground(parent.getBackground());
		
		selectionListeners = new ArrayList<Listener>();
		
		addMouseTrackListener(new MouseTrackListener() {
			public void mouseEnter(MouseEvent e) {
				onMouseOver(e);
			}

			public void mouseExit(MouseEvent e) {
				onMouseOut(e);
			}

			public void mouseHover(MouseEvent e) {
								
			}
		});
		
		addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				
			}

			public void mouseDown(MouseEvent e) {
				
			}

			public void mouseUp(MouseEvent e) {
				EntityViewOSXButton.this.onMouseUp(e);
			}
		});
		
		addPaintListener( new PaintListener() {
			public void paintControl(PaintEvent e) {
				EntityViewOSXButton.this.onCanvasPaint(e);
			}
		});
	}
	
	private void onMouseOver(MouseEvent e)
	{
		
	}
	
	private void onMouseOut(MouseEvent e)
	{
		
	}
	
	private void onMouseUp(MouseEvent e)
	{
		// alert the listeners
		for(Listener l : selectionListeners)
		{
			Event event = new Event();
			event.widget = this;
			l.handleEvent(event);
		}
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
	
	private void onCanvasPaint(PaintEvent e)
	{
		GC gc = e.gc;
		
		if(cacheDirty || imgCache == null)
		{
			Rectangle canvasSize = this.getClientArea();
			
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
			
			// blank the canvas
			canvas.setBackground(this.getBackground());
			canvas.fillRectangle(0, 0, canvasSize.width, canvasSize.height);
			
			int x = 3;
			int y = 3;
			int width = canvasSize.width - x * 2;
			int height = canvasSize.height - y * 2;
			
			canvas.setLineJoin(SWT.JOIN_ROUND);
			canvas.setLineWidth(2);
			canvas.setAlpha(255);
			canvas.setBackground(buttonColor);
			canvas.fillOval(0, 0, canvasSize.width, canvasSize.height);
			canvas.setAlpha(220); 
			canvas.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			canvas.drawLine(x + width / 2, y, x  + width / 2, y + height);
			canvas.drawLine(x, y + height / 2, x + width, y + height / 2);
			
			canvas.dispose();
		}
		
		// copy
		gc.drawImage(imgCache, e.x, e.y, e.width, e.height,
				e.x, e.y, e.width, e.height);
	}
}
