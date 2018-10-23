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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import edu.utk.cs.futurelens.ui.Callback;
import edu.utk.cs.futurelens.ui.Prefs;
import edu.utk.cs.futurelens.ui.events.EntityAddedEvent;
import edu.utk.cs.futurelens.ui.events.EntityAddedListener;
import edu.utk.cs.futurelens.ui.events.EntityPageEvent;
import edu.utk.cs.futurelens.ui.events.EntityPageListener;

/**
 * @author greg
 *
 */
public class EntityViewOSX extends Composite implements EntityView 
{
	// controls used
	private ScrolledComposite scmpMain;
	private Composite cmpMain;

	// other stuff
	private Hashtable<String, Composite> headers;
	private Hashtable<String, TreeItem> treeHeaders;
	private Hashtable<String, Integer> pageNumbers;
	
	// list of listeners
	private ArrayList<EntityAddedListener> addedListeners;
	private ArrayList<EntityPageListener> pageListeners;
	
	private boolean showPageButtons = true;
	
	public EntityViewOSX(Composite parent, int style) 
	{
		super(parent, style);
		
		// create the outer scrolled composite
		scmpMain = new ScrolledComposite(this, SWT.V_SCROLL | SWT.H_SCROLL);
		
		// create the inner composite
		cmpMain = new Composite(scmpMain, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.horizontalSpacing = layout.verticalSpacing = 0;
		layout.marginHeight = layout.marginWidth = 0;
		cmpMain.setLayout(layout);
		cmpMain.setSize(cmpMain.computeSize(100, 100));

		scmpMain.setContent(cmpMain);
		
		headers = new Hashtable<String, Composite>();
		treeHeaders = new Hashtable<String, TreeItem>();
		pageNumbers = new Hashtable<String, Integer>();
		
		addedListeners = new ArrayList<EntityAddedListener>();
		pageListeners = new ArrayList<EntityPageListener>();
		
		// add listeners
		addControlListener( new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				EntityViewOSX.this.onResize(e);
			}
		});
	}
	
	public void setBackground(Color color)
	{
		super.setBackground(color);
		scmpMain.setBackground(color);
		cmpMain.setBackground(color);
	}

	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		if(getVisible() == false)
			return(new Point(0, 0));
		
		return(scmpMain.computeSize(wHint, hHint, changed));
	}
	
	public void addEntityAddedListener(EntityAddedListener listener) 
	{
		addedListeners.add(listener);		
	}
	
	public void addEntityPageListener(EntityPageListener listener)
	{
		pageListeners.add(listener);
	}
	
	public void addHeader(String header)
	{
		addHeader(header, header);
	}
	
	public void setExpanded(String header, boolean expanded)
	{
		if(treeHeaders.get(header) == null)
			return;
		
		treeHeaders.get(header).setExpanded(expanded);
		onExpand(headers.get(header));
	}
	
	public ArrayList<String> getHeaders()
	{
		ArrayList<String> allHeaders = new ArrayList<String>();
		
		for(String s : headers.keySet())
			allHeaders.add(s);
		
		return(allHeaders);
	}
	
	public boolean getExpanded(String header)
	{
		if(treeHeaders.get(header) == null)
			return(false);
		
		return(treeHeaders.get(header).getExpanded());
	}
	
	public Point getTermButtonXY(String header, int index)
	{
		if(headers.containsKey(header) != true)
			return null;
		
		if(showPageButtons == true)
			index++;
		
		Composite terms = headers.get(header);
		Control[] controls = terms.getChildren();
		
		if(index * 2 >= controls.length)
			return null;
		
		Point size = controls[index * 2].getSize();
		Point point = getDisplay().map(controls[index * 2], this, size.x / 2, size.y / 2);
		
		return(point);
	}
	
	public void addHeader(String header, String display) 
	{
		final Composite parent = cmpMain;
		Tree treeTerms;
		treeTerms = new Tree(parent, SWT.NO_SCROLL);
		treeTerms.setBackground(this.getBackground());
		
		TreeItem item = new TreeItem(treeTerms, SWT.NONE);
		item.setText(display);
		
		// store the tree item for collapsing later
		treeHeaders.put(header, item);
		
		Point size = treeTerms.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		treeTerms.setLayoutData(new GridData(size.x, size.y - 2));
		
		// add a dummy second item
		new TreeItem(item, SWT.NONE);
		
		// add the composite below
		final Composite cmpTerms = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginLeft = 20;
		layout.horizontalSpacing = layout.verticalSpacing = 2;

		cmpTerms.setLayout(layout);
		cmpTerms.setBackground(this.getBackground());
		
		GridData data;
		
		if(showPageButtons)
		{
			data = new GridData(SWT.FILL, SWT.TOP, true, false);
			data.grabExcessHorizontalSpace = true;
			data.horizontalSpan = 2;
			
			Composite cmpPage = new Composite(cmpTerms, SWT.NONE);
			cmpPage.setLayoutData(data);
			cmpPage.setBackground(this.getBackground());
			cmpPage.setLayout(new GridLayout(4, false));
			cmpPage.setSize(cmpPage.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			cmpPage.setData(new Boolean(true));
			
			// add the previous button
			Button button = new Button(cmpPage, SWT.LEFT | SWT.ARROW);
			button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			
			int numTerms = Prefs.get(Prefs.TERMS_PER_HEADER, Prefs.TERMS_PER_HEADER_DEFAULT);
			
			Label label = new Label(cmpPage, SWT.NONE);
			label.setText("Prev " + numTerms);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			label.setBackground(this.getBackground());
			
			// store the header
			button.setData(header);
			label.setData(header);
			
			Callback.connect(button, SWT.Selection, this, "onPrevPage");
			Callback.connect(label, SWT.MouseUp, this, "onPrevPage");
			
			// add the next button
			label = new Label(cmpPage, SWT.NONE);
			label.setText("Next " + numTerms);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			label.setBackground(this.getBackground());
			
			button = new Button(cmpPage, SWT.RIGHT | SWT.ARROW);
			button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			
			// store the header
			button.setData(header);
			label.setData(header);
			
			Callback.connect(button, SWT.Selection, this, "onNextPage");
			Callback.connect(label, SWT.MouseUp, this, "onNextPage");
		}
		
		// make the composite as wide as the pane
		data = new GridData(0, 0);
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		cmpTerms.setLayoutData(data);

		// resize the parent
		parent.setSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		treeTerms.addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event event) {
				onExpand(cmpTerms);
			}
		});
		
		treeTerms.addListener(SWT.Collapse, new Listener() {
			public void handleEvent(Event event) {
				// on collapse shrink the composite to 0 and alert its parent
				GridData data = (GridData)cmpTerms.getLayoutData();
				data.heightHint = 0;
				data.widthHint = 0;
				
				parent.setSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				
				cmpTerms.getParent().layout(true);
			}
		});
		
		treeTerms.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// no point in selecting a tree item
				Tree tree = (Tree)event.widget;
				tree.deselectAll();
			}
		});
	
		// force a layout update
		cmpTerms.getParent().layout();
		
		// store the composite that holds the children
		headers.put(header, cmpTerms);
	}
	
	private void onExpand(Composite cmpTerms)
	{
		// on expand, size the composite to its normal size
		GridData data = (GridData)cmpTerms.getLayoutData();
		data.heightHint = cmpTerms.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		data.widthHint = cmpTerms.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		
		// resize the parent composite
		cmpMain.setSize(cmpMain.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		cmpTerms.layout(true);
		cmpTerms.getParent().layout(true);
	}
	
	public void addTerm(String header, String term, double score)
	{
		addTerm(header, term, (Object)score);
	}
	
	public void addTerm(String header, String term, int count) 
	{
		addTerm(header, term, (Object)count);
	}
	
	private void addTerm(String header, String term, Object value)
	{
		Composite parent = headers.get(header);
		
		if(parent == null)
			return;
		
		EntityViewOSXButton button = new EntityViewOSXButton(parent, SWT.NONE);
		button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		Label label = new Label(parent, SWT.NONE);
		
		if(value instanceof Integer && (Integer)value > 0)
		{
			label.setText(term + " (" + value + ")");
		}
		else if(value instanceof Double && (Double)value > 0)
		{
			DecimalFormat df = new DecimalFormat("0.###");
			label.setText(term + " (" + df.format(value) + ")");
		}
		else
		{
			label.setText(term);
		}
		
		label.setBackground(getBackground());
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		button.setData("term", term);
		button.setData("header", header);
		
		label.setData("term", term);
		label.setData("header", header);
		
		Callback.connect(button, SWT.Selection, this, "onEntityAdd");
		Callback.connect(label, SWT.MouseUp, this, "onEntityAdd");
	}
	
	public void clear(String header) 
	{
		Composite cmpTerms = headers.get(header);
		
		if(cmpTerms == null)
			return;
		
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
	
	public void setVisible(boolean visible)
	{
		if(visible == false)
			this.setSize(0, 0);
		
		super.setVisible(visible);
	}
	
	public void setShowPageButtons(boolean showPageButtons)
	{
		this.showPageButtons = showPageButtons;
	}
	
	public int getPageNumber(String header)
	{
		return(pageNumbers.get(header));
	}
	
	public void setPageNumber(String header, int page)
	{
		pageNumbers.put(header, page);
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
		
		scmpMain.setBounds(0, 0, parentExtent.width, parentExtent.height);
	}
	
}
