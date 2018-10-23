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
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

import edu.utk.cs.futurelens.ui.Callback;
import edu.utk.cs.futurelens.ui.Consts;
import edu.utk.cs.futurelens.ui.Prefs;
import edu.utk.cs.futurelens.ui.events.EntityAddedEvent;
import edu.utk.cs.futurelens.ui.events.EntityAddedListener;
import edu.utk.cs.futurelens.ui.events.EntityPageEvent;
import edu.utk.cs.futurelens.ui.events.EntityPageListener;

/**
 * @author greg
 *
 */
public class EntityViewWinLinux extends Composite implements EntityView 
{
	// controls used
	private ExpandBar ebMain;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result
				+ ((pageNumbers == null) ? 0 : pageNumbers.hashCode());
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
		EntityViewWinLinux other = (EntityViewWinLinux) obj;
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (pageNumbers == null) {
			if (other.pageNumbers != null)
				return false;
		} else if (!pageNumbers.equals(other.pageNumbers))
			return false;
		return true;
	}

	private Hashtable<String, ExpandItem> headers;
	private Hashtable<String, Integer> pageNumbers;
	
	// list of listeners
	private ArrayList<EntityAddedListener> addedListeners;
	private ArrayList<EntityPageListener> pageListeners;
	
	private boolean showPageButtons = true;
	
	public EntityViewWinLinux(Composite parent, int style) 
	{		
		super(parent, style);
		
		// create the controls
		ebMain = new ExpandBar(this, SWT.NONE);
		
		headers = new Hashtable<String, ExpandItem>();
		pageNumbers = new Hashtable<String, Integer>();
		
		addedListeners = new ArrayList<EntityAddedListener>();
		pageListeners = new ArrayList<EntityPageListener>();
		
		// add listeners
		addControlListener( new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				EntityViewWinLinux.this.onResize(e);
			}
		});
	}

	public void addEntityAddedListener(EntityAddedListener listener) 
	{
		addedListeners.add(listener);		
	}

	public void addEntityPageListener(EntityPageListener listener)
	{
		pageListeners.add(listener);
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		if(getVisible() == false)
			return(new Point(0, 0));
		
		return(ebMain.computeSize(wHint, hHint, changed));
	}
	
	public void addHeader(String header) 
	{
		addHeader(header, header);
	}

	public void addHeader(String header, String display) 
	{
		ExpandItem item;
		
		// recreate ebmain if this is the first header
		// this is due to the fact that an empty expandbar
		//  shows a scroll bar when V_SCROLL is set
		if(headers.size() == 0)
		{
			ebMain.dispose();
			ebMain = new ExpandBar(this, SWT.V_SCROLL);
			onResize(null);
			getParent().layout(true);
		}
			
		item = new ExpandItem(ebMain, SWT.NONE, ebMain.getItemCount());
		item.setText(display);
		
		Composite composite = new Composite(ebMain, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginLeft = Consts.ENTITY_MARGIN_WIDTH;
		composite.setLayout(layout);
		
		if(showPageButtons)
		{
			GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
			data.grabExcessHorizontalSpace = true;
			data.horizontalSpan = 2;
			
			Composite cmpPage = new Composite(composite, SWT.NONE);
			cmpPage.setLayoutData(data);
			cmpPage.setLayout(new GridLayout(2, false));
			cmpPage.setSize(cmpPage.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			cmpPage.setData(new Boolean(true));
			
			int numTerms = Prefs.get(Prefs.TERMS_PER_HEADER, Prefs.TERMS_PER_HEADER_DEFAULT);
			
			// add the previous button
			Button button = new Button(cmpPage, SWT.PUSH);
			button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			button.setText("Prev " + numTerms);
			
			// store the header
			button.setData(header);
			
			Callback.connect(button, SWT.Selection, this, "onPrevPage");
			
			// add the next button			
			button = new Button(cmpPage, SWT.PUSH);
			button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			button.setText("Next " + numTerms);
			
			// store the header
			button.setData(header);
			
			Callback.connect(button, SWT.Selection, this, "onNextPage");
		}
		
		item.setControl(composite);
		
		headers.put(header, item);
	}
	
	public void addTerm(String header, String term, int value)
	{
		addTerm(header, term, (Object)value);
	}
	
	public void addTerm(String header, String term, double value)
	{
		addTerm(header, term, (Object)value);
	}
	
	private void addTerm(String header, String term, Object value) 
	{
		ExpandItem parent = headers.get(header);
		
		if(header == null)
			return;
		
		Composite cmpTerms = (Composite)parent.getControl();
		
		Button button = new Button(cmpTerms, SWT.PUSH);
		button.setText("+");
		button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		Label label = new Label(cmpTerms, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		if((value instanceof Integer) && ((Integer)value > 0))
		{
			label.setText(term + " (" + value + ")");
		}
		else if((value instanceof Double) && ((Double)value > 0))
		{
			DecimalFormat df = new DecimalFormat("0.###");
			label.setText(term + " (" + df.format(value) + ")");
		}
		else
		{
			label.setText(term);
		}
		
		cmpTerms.layout(true);
		parent.setHeight(cmpTerms.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	
		button.setData("term", term);
		button.setData("header", header);
		
		label.setData("term", term);
		label.setData("header", header);
		
		Callback.connect(button, SWT.Selection, this, "onEntityAdd");
		Callback.connect(label, SWT.MouseUp, this, "onEntityAdd");
	}
	
	public Point getTermButtonXY(String header, int index)
	{
		if(headers.containsKey(header) != true)
			return null;
		
		ExpandItem item = headers.get(header);
		Control[] controls = ((Composite)item.getControl()).getChildren();
		
		if(index * 2 >= controls.length)
			return null;
		
		Point size = controls[index * 2].getSize();
		Point point = getDisplay().map(controls[index * 2], this, size.x / 2, size.y / 2);
		
		return(point);
	}
	
	public void clear(String header) 
	{
		ExpandItem parent = headers.get(header);
		
		if(parent == null)
			return;
		
		Composite cmpTerms = (Composite)parent.getControl();
		Widget[] children = cmpTerms.getChildren();
		
		for(Widget child : children)
			// don't remove page buttons
			if(child.getData() == null)
				child.dispose();
	}
	
	public void clearAll()
	{
		for(String header : headers.keySet())
			clear(header);
	}
	
	public ArrayList<String> getHeaders()
	{
		ArrayList<String> allHeaders = new ArrayList<String>();
		
		for(String s : headers.keySet())
			allHeaders.add(s);
		
		return(allHeaders);
	}
	
	public int getPageNumber(String header)
	{
		return(pageNumbers.get(header));
	}
	
	public void setPageNumber(String header, int page)
	{
		pageNumbers.put(header, page);
	}
	
	public boolean getExpanded(String header)
	{
		if(headers.get(header) == null)
			return(false);
		
		return(headers.get(header).getExpanded());
	}
	
	public void setExpanded(String header, boolean expanded)
	{
		if(headers.get(header) == null)
			return;
		
		headers.get(header).setExpanded(expanded);
	}
	
	public void setShowPageButtons(boolean showPageButtons)
	{
		this.showPageButtons = showPageButtons;
	}
	
	@SuppressWarnings("unused")
	private void onEntityAdd(Event e)
	{
		String term = (String)e.widget.getData("term");
		String header = (String)e.widget.getData("header");
		
		// checked
		EntityAddedEvent event = new EntityAddedEvent(this, header, term, true);
		
		// alert the listeners
		for(EntityAddedListener l : addedListeners)
			l.entityAdded(event);
	}
	
	@SuppressWarnings("unused")
	private void onPrevPage(Event e)
	{
		String header = (String)e.widget.getData();
		
		EntityPageEvent event = new EntityPageEvent(this, false, header);
		
		// ignore pages < 0
		if(pageNumbers.containsKey(header) && pageNumbers.get(header) <= 0)
			return;
		
		if(pageNumbers.containsKey(header))
			pageNumbers.put(header, pageNumbers.get(header) - 1);
		else
			pageNumbers.put(header, 0);
		
		for(EntityPageListener l : pageListeners)
			l.entityPageChange(event);
	}
	
	@SuppressWarnings("unused")
	private void onNextPage(Event e)
	{
		String header = (String)e.widget.getData();
		
		EntityPageEvent event = new EntityPageEvent(this, true, header);
		
		if(pageNumbers.containsKey(header))
			pageNumbers.put(header, pageNumbers.get(header) + 1);
		else
			pageNumbers.put(header, 1);
		
		for(EntityPageListener l : pageListeners)
			l.entityPageChange(event);
	}
	
	private void onResize(ControlEvent event)
	{
		Rectangle parentExtent = this.getClientArea();
		ebMain.setBounds(0, 0, parentExtent.width, parentExtent.height);
	}
	public void addGroupTerm(String header, String term, double score) {
		this.addGroupTerm(header, term,(Object) score);
	}
	private void addGroupTerm(String header, String term, Object value) 
	{
		ExpandItem parent = headers.get(header);
		
		if(header == null)
			return;
		
		Composite cmpTerms = (Composite)parent.getControl();
		
		Button button = new Button(cmpTerms, SWT.PUSH);
		button.setText("+");
		button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		Label label = new Label(cmpTerms, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		
		if((value instanceof Double) && ((Double)value > 0))
		{
			DecimalFormat df = new DecimalFormat("0.###");
			label.setText(term + " (" + df.format(value) + ")");
		}
		else
		{
			label.setText(term);
		}
		
		cmpTerms.layout(true);
		parent.setHeight(cmpTerms.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	
		button.setData("term", term);
		button.setData("header", header);
		
		label.setData("term", term);
		label.setData("header", header);
		
		Callback.connect(button, SWT.Selection, this, "onEntityAdd");
		Callback.connect(label, SWT.MouseUp, this, "onEntityAdd");
	}
}
