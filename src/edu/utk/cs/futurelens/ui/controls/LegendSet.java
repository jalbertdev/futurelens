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
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import edu.utk.cs.futurelens.ui.events.LegendDropEvent;
import edu.utk.cs.futurelens.ui.events.LegendDropListener;
import edu.utk.cs.futurelens.ui.events.LegendRemovedEvent;
import edu.utk.cs.futurelens.ui.events.LegendRemovedListener;

public class LegendSet extends Composite 
{
	// non adjustable settings
	private Color[] colors = new Color[] { 
			new Color(getDisplay(), 18, 129, 229), 	// blue
			new Color(getDisplay(), 63, 211, 0), 	// green
			new Color(getDisplay(), 234, 39, 0), 	// red
			new Color(getDisplay(), 246, 206, 29), 	// yellow
			new Color(getDisplay(), 229, 125, 18), 	// orange
			new Color(getDisplay(), 34, 85, 221),	// dark blue
			new Color(getDisplay(), 255,20,147),
			new Color(getDisplay(), 148,0,211)
			//new Color(getDisplay(), 100, 100, 100)  //black-ish
		};
	
	// controls
	private ScrolledComposite scmpMain;
	private Composite cmpMain;
		
	// other stuff
	private int curColor = 0;
	private Hashtable<String, Legend>legends;
	private ArrayList<LegendDropListener> dropListeners;
	private ArrayList<LegendRemovedListener> removeListeners;
	
	public LegendSet(Composite parent, int style) 
	{
		super(parent, style);

		// create all the controls	
		final ScrolledComposite scmpMain = new ScrolledComposite(this, SWT.V_SCROLL);
		final Composite cmpMain = new Composite(scmpMain, SWT.NONE);
		
		cmpMain.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		scmpMain.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				Control control = scmpMain.getContent();
				Point ssize = scmpMain.getSize();
				Point csize = control.computeSize(ssize.x, SWT.DEFAULT);
				control.setSize(ssize.x, csize.y);
			}
		});
		
		scmpMain.setContent(cmpMain);
		this.cmpMain = cmpMain;
		this.scmpMain = scmpMain;
		
		// create the list of legends
		legends = new Hashtable<String, Legend>();
		dropListeners = new ArrayList<LegendDropListener>();
		removeListeners = new ArrayList<LegendRemovedListener>();
		
		// add listeners
		addControlListener( new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				LegendSet.this.onResize(e);
			}
		});
	}

	public void add(String term)
	{
		Legend legend = new Legend(cmpMain, SWT.NONE);
		
		// set the color
		if(curColor >= colors.length)
			curColor = 0;
		legend.setColor(colors[curColor++]);
		
		legend.addTerm(term);
		cmpMain.layout(true);
		cmpMain.setSize(cmpMain.computeSize(cmpMain.getSize().x, SWT.DEFAULT));
		
		// connect the drop listener
		legend.addDropListener(new LegendDropListener() {
			public void legendDropped(LegendDropEvent e) {
				// propagate
				onDrop(e);
			}
		});
		
		legend.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				onLegendDispose(e);
			}
		});
		
		// add the new legend to the list
		legends.put(term, legend);
	}
	
	private void onLegendDispose(DisposeEvent e)
	{
		ArrayList<String> terms = ((Legend)e.widget).getTerms();
		String[] termArray = new String[terms.size()];
		
		// remove this legend from the list
		if(legends.values().contains(e.widget))
		{
			// find the widget to remove it
			for(String key : legends.keySet())
			{
				if(legends.get(key) == e.widget)
				{
					legends.remove(key);
					break;
				}
			}
			
			// resize the composite
			cmpMain.setSize(cmpMain.computeSize(cmpMain.getSize().x, SWT.DEFAULT));
		}
		
		for(int i = 0; i < termArray.length; i++)
			termArray[i] = terms.get(i);
		
		LegendRemovedEvent event = new LegendRemovedEvent(e.widget, (Legend)e.widget, termArray);
		
		for(LegendRemovedListener l : removeListeners)
			l.legendRemoved(event);
	}
	
	private void onDrop(LegendDropEvent e)
	{
		for(LegendDropListener l : dropListeners)
			l.legendDropped(e);
	}
	
	public void addDropListener(LegendDropListener listener)
	{
		dropListeners.add(listener);
	}
	
	public void addRemoveListener(LegendRemovedListener listener)
	{
		removeListeners.add(listener);
	}
	
	public Legend get(String term)
	{
		for(Legend l : legends.values())
			if(l.contains(term))
				return(l);
		
		return(null);
	}
	
	public Legend[] getLegends()
	{
		Legend[] legendArray = new Legend[legends.size()];
		
		int i = 0;
		for(Legend l : legends.values())
			legendArray[i++] = l;
		
		return(legendArray);
	}
	
	public boolean remove(String term)
	{
		// find the corresponding legend
		Legend legend = get(term);
		
		if(legend != null)
		{
			legend.removeTerm(term);
			
			if(legend.getTerms().size() <= 0)
			{
				legend.dispose();
				cmpMain.layout(true);
				return(true);
			}
		}
		
		return(false);
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		return(scmpMain.computeSize(wHint, hHint, true));
	}

	private void onResize(ControlEvent e)
	{
		Rectangle parentExtent = this.getClientArea();
		
		scmpMain.setBounds(0, 0, parentExtent.width, parentExtent.height);
	}
}
