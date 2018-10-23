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
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.utk.cs.futurelens.data.DataSet;
import edu.utk.cs.futurelens.data.dictionary.SearchResults;
import edu.utk.cs.futurelens.data.dictionary.Dictionary.SortMethod;
import edu.utk.cs.futurelens.data.group.Group;
import edu.utk.cs.futurelens.ui.Callback;
import edu.utk.cs.futurelens.ui.Consts;
import edu.utk.cs.futurelens.ui.FLInterface;
import edu.utk.cs.futurelens.ui.Prefs;
import edu.utk.cs.futurelens.ui.events.EntityAddedEvent;
import edu.utk.cs.futurelens.ui.events.EntityAddedListener;
import edu.utk.cs.futurelens.ui.events.EntityPageEvent;
import edu.utk.cs.futurelens.ui.events.EntityPageListener;
import edu.utk.cs.futurelens.ui.events.LegendDropEvent;
import edu.utk.cs.futurelens.ui.events.LegendDropListener;
import edu.utk.cs.futurelens.ui.events.LegendRemovedEvent;
import edu.utk.cs.futurelens.ui.events.LegendRemovedListener;
/**
 * @author Greg
 * @author Andrey
 *
 */
public class GroupView extends Composite
{	
	private DataSet dataSet;
	private Group group;
	private SearchResults searchResults;
	private SortMethod sortMethod;
	
	// controls used in the window
	private SashForm frmMain;
	private Composite leftPane, rightPane;
	private SashForm centerPane;
	private final Color colorLeftPane;
	private DocumentBlockSet blockSet;
	private LegendSet legendSet;
	
	private DocumentView docView;
	private EntityView entityView;
	private EntityView searchView;
	private Text txtSearch;
	private Combo cmbSort;
	private ExpandBar ebFilter;
	private Composite cmpFilter;
	private Button btnSearch, btnClear;
	private GraphView graphView;
	
	private boolean isOverview = false;
	private boolean isSearchVisible = false;
	
	private final String sortAlpha = "alphabetical";
	private final String sortFreq = "frequency";
	private final String sortLength = "length";
	
	public GroupView(Composite parent, int style)
	{
		super(parent, style);
		colorLeftPane = new Color(FLInterface.getDisplay(), Consts.WINDOW_LEFTPANE_BACKGROUND);
		createControls();
		
		dataSet = null;
		
		// set the last used sort method
		setSortMethod(Prefs.get(Prefs.SORT_METHOD, Prefs.SORT_METHOD_DEFAULT));
		
		// add listeners
		addControlListener( new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				GroupView.this.onResize(e);
			}
		});
	}
	
	public EntityView getEntityView()
	{
		return entityView;
	}
	
	public Combo getCmbSort()
	{
		return cmbSort;
	}
	
	public DocumentBlockSet getBlockSet()
	{
		return blockSet;
	}
	
	public LegendSet getLegendSet()
	{
		return legendSet;
	}
	
	public DocumentView getDocumentView()
	{
		return docView;
	}
	
	public GraphView getGraphView()
	{
		return graphView;
	}
	
	public void setSortMethod(String method)
	{
		String lMethod = method.toLowerCase();
		
		if(lMethod.equals(sortAlpha))
			cmbSort.select(0);
		else if(lMethod.equals(sortFreq))
			cmbSort.select(1);
		else if(lMethod.equals(sortLength))
			cmbSort.select(2);
		
		onSort();
	}
	
	public void setDataSet(DataSet ds)
	{
		dataSet = ds;
		
		blockSet.setDataSet(ds);
		graphView.setDataSet(ds);
		
		// FIXME: fix this!
		blockSet.setDateRange(DataSet.DateRange.ONE_MONTH);
		
		graphView.setDateRange(DataSet.DateRange.ONE_MONTH);
	}
	
	public void makeOverview()
	{
		if(this.dataSet == null)
			return;
		
		this.isOverview = true;
		
		entityView.setShowPageButtons(true);
		
		// add the headers
		entityView.addHeader(Consts.TERMS_HEADER_TEXT);
		
		for(String entity : dataSet.getSortedEntities())
			entityView.addHeader(entity, capitalize(entity));
		
		populateEV();
	}
	
	public void setGroup(Group group)
	{
		this.group = group;
		this.isOverview = false;
		
		// rip out the search stuff
		if(FLInterface.isLinux() || FLInterface.isWindows())
		{
			ebFilter.dispose();
		}
		else if(FLInterface.isMac())
		{
			txtSearch.dispose();
			cmpFilter.dispose();
		}
		
		entityView.addHeader(Consts.ENTITIES_HEADER_TEXT);
		entityView.addHeader(Consts.TERMS_HEADER_TEXT);
		
		populateEV();
	}
	
	public void populateEV()
	{
		if(this.isOverview)
		{
			int count;
			
			// populate the terms item
			count = 0;
			for(String term : dataSet.getGlobalDict().getSorted(sortMethod))
			{
				if(count++ >= Prefs.get(Prefs.TERMS_PER_HEADER, Prefs.TERMS_PER_HEADER_DEFAULT))
					break;
				
				// add the term to the list			
				entityView.addTerm(Consts.TERMS_HEADER_TEXT, term, dataSet.getGlobalDict().getTerm(term));
			}
			
			for(String entity : dataSet.getSortedEntities())
			{
				count = 0;
				// get some terms
				for(String term : dataSet.getEntityDict(entity).getSorted(sortMethod))
				{
					if(count++ >= Prefs.get(Prefs.TERMS_PER_HEADER, Prefs.TERMS_PER_HEADER_DEFAULT))
						break;
					
					// add in the term
					entityView.addTerm(entity, term, dataSet.getEntityDict(entity).getTerm(term));
				}
			}
		}
		else
		{
			for(String entity : group.getSortedEntities())
				entityView.addTerm(Consts.ENTITIES_HEADER_TEXT, capitalize(entity), group.getEntityScore(entity));
			
			for(String term : group.getSortedTerms())
				entityView.addTerm(Consts.TERMS_HEADER_TEXT, capitalize(term), group.getTermScore(term));
		}
	}
	
	public void populateSV()
	{
		// update the search
		if(isSearchVisible)
		{
			ArrayList<String>results = searchResults.sort(sortMethod);
			
			for(String s : results)
				searchView.addTerm(Consts.SEARCH_RESULTS_HEADER_TEXT, s, searchResults.get(s));
		}
	}
	
	public void onDocumentSelect(Event e)
	{

		docView.showDocument(dataSet.getDocumentByIndex(blockSet.getSelectedDoc()));
	}
	
	private void onEntityAdded(EntityAddedEvent e)
	{
		// do stuff
		legendSet.add(e.term);
		blockSet.addLegend(legendSet.get(e.term));
		blockSet.updateLegend(legendSet.get(e.term));
		graphView.addLegend(legendSet.get(e.term));
		docView.highlightTerm(e.term, legendSet.get(e.term).getColor(0));
	}
	
	public void onLegendRemoved(LegendRemovedEvent e)
	{
		Legend legend = e.legend;
		
		blockSet.removeLegend(legend);
		graphView.removeLegend(legend);
		
		// added the code below to handle phrases
		String[] temp = new String[100];
		int i=0;
		for(String st : (legend.getTerms()))
		{
			temp[i]=st;
			i++;
		}

		if(temp!=null)
			docView.removeTerm(temp);
		
		// the code below will remove substrings that are created
		// in the course of making a multi-element chained phrase
		if(temp[1]==null)
		{
			StringTokenizer st = new StringTokenizer(temp[0]);
			String[] elems = new String[100];
			i=0;
			while (st.hasMoreTokens()) 
			{
				elems[i] = st.nextToken();
				i++;
			}
			String substr="";
			for(i=0; i<elems.length-1;i++)
			{
				if(elems[i+1]==null)
					break;
				substr= substr + " " + elems[i];
				String a = substr.trim();
				docView.removeTerm(a);
			}
			substr=substr.trim();
			//System.out.println("attempt removing: " + substr);
			docView.removeTerm(substr);
		}
		
		//docView.removeTerm(e.terms);
		
		// update the document view
		for(Legend l : legendSet.getLegends())
			docView.highlightTerm(l.getTerms(), l.getColor(0));
	}
	
	public void onLegendDrop(LegendDropEvent e)
	{	
		// tell the document set to update
		
		// added the code below to handle phrases
		String[] temp = new String[100];
		int i=0;
		for(String st : ((e.newLegend).getTerms()))
		{
			temp[i]=st;
			i++;
		}
		
		if(temp!=null)
			docView.removeTerm(temp);
		
		// the code below will remove substrings that are created
		// in the course of making a multi-element chained phrase
		if(temp[1]==null)
		{
			StringTokenizer st = new StringTokenizer(temp[0]);
			String[] elems = new String[100];
			i=0;
			while (st.hasMoreTokens()) 
			{
				elems[i] = st.nextToken();
				i++;
			}
			String substr="";
			for(i=0; i<elems.length-1;i++)
			{
				if(elems[i+1]==null)
					break;
				substr= substr + " " + elems[i];
				String a = substr.trim();
				docView.removeTerm(a);
			}
			substr=substr.trim();
			//System.out.println("attempt removing: " + substr);
			docView.removeTerm(substr);
		}
		
		//docView.removeTerm(((e.newLegend).getText()));
		blockSet.updateLegend(e.newLegend);
		graphView.updateLegend();
	}
	
	public void onFilterEnter(Event e)
	{
		if(e.detail == SWT.CANCEL)
			onSearchCancel();
		else
			onSearch();
	}
	
	private void onSearchCancel()
	{
		// restore the text
		txtSearch.setText(Consts.SEARCH_BOX_TEXT);
		
		if(this.isSearchVisible)
		{
			searchView.setVisible(false);
			entityView.setVisible(true);
			
			entityView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			searchView.setLayoutData(null);
			
			leftPane.layout(true);
			
			this.isSearchVisible = false;
		}
	}
	
	private void onSearch()
	{
		if(txtSearch.getText().length() == 0)
			return;
		
		searchResults = dataSet.getGlobalDict().searchTerms(txtSearch.getText());
		
		searchView.clearAll();
		
		// restore visibility
		this.isSearchVisible = true;
		searchView.setVisible(true);
		entityView.setVisible(false);
	
		searchView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		entityView.setLayoutData(null);
		
		populateSV();
		
		leftPane.layout(true);
		
		searchView.setExpanded(Consts.SEARCH_RESULTS_HEADER_TEXT, true);
	}
	
	private void onSort()
	{
		String sortValue;
		
		// figure out the sort type
		switch(cmbSort.getSelectionIndex())
		{
		case 0:
			sortMethod = SortMethod.ALPHABETICAL;
			sortValue = sortAlpha;
			break;
		case 1:
			sortMethod = SortMethod.FREQUENCY;
			sortValue = sortFreq;
			break;
		case 2:
			sortMethod = SortMethod.LENGTH;
			sortValue = sortLength;
			break;
		default:
			return;
		}
		
		if(dataSet == null)
			return;
		
		// save the sort method
		Prefs.set(Prefs.SORT_METHOD, sortValue);
		
		// clear all the headers
		entityView.clearAll();
		
		populateEV();
		
		// re-expand everything
		// this forces a redisplay of the terms
		for(String header : entityView.getHeaders())
		{
			if(entityView.getExpanded(header) == true)
			{
				entityView.setExpanded(header, false);
				entityView.setExpanded(header, true);
			}
		}
		
		if(isSearchVisible == true)
		{
			searchView.clearAll();
			
			populateSV();

			searchView.setExpanded(Consts.SEARCH_RESULTS_HEADER_TEXT, false);
			searchView.setExpanded(Consts.SEARCH_RESULTS_HEADER_TEXT, true);
		}
	}
	
	@SuppressWarnings("unused")
	private void onFilterExpand()
	{
		ExpandItem item = ebFilter.getItem(0);
		Composite cmpItem = (Composite)item.getControl();
		
		item.setHeight(cmpItem.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	}
	
	@SuppressWarnings("unused")
	private void onFilterCollapse()
	{
		ExpandItem item = ebFilter.getItem(0);
		
		item.setHeight(0);
	}
	
	public void onSearchFocus()
	{
		// clear the box
		if(txtSearch.getText().equals(Consts.SEARCH_BOX_TEXT))
			txtSearch.setText("");
	}
	
	public void onSearchBlur()
	{
		if(txtSearch.getText().length() == 0)
			txtSearch.setText(Consts.SEARCH_BOX_TEXT);
	}
	
	private void onPageChange(EntityPageEvent e)
	{
		int maxPage;
		int pageSize = Prefs.get(Prefs.TERMS_PER_HEADER, Prefs.TERMS_PER_HEADER_DEFAULT);
		int pageNumber = entityView.getPageNumber(e.header);
		boolean isGlobal;
		ArrayList<String> dataSource;
		
		if(! this.isOverview)
			return;
		
		// figure out if it was an entity or term page
		if(e.header.equals(Consts.TERMS_HEADER_TEXT))
		{
			dataSource = dataSet.getGlobalDict().getSorted(sortMethod);
			isGlobal = true;
		}
		else
		{
			dataSource = dataSet.getEntityDict(e.header).getSorted(sortMethod);
			isGlobal = false;
		}
		
		maxPage = (int)Math.ceil((double)dataSource.size() / (double)pageSize);
		
		if(entityView.getPageNumber(e.header) >= maxPage)
		{
			entityView.setPageNumber(e.header, maxPage - 1);
			return;
		}
		
		// clear the list and repopulate
		entityView.clear(e.header);
		
		for(int i = 0; i < pageSize; i++)
		{
			// make sure there are terms left
			if(pageNumber * pageSize + i >= dataSource.size())
				break;
			
			String term = dataSource.get(pageNumber * pageSize + i);
			
			// add the term to the list	
			if(isGlobal)
				entityView.addTerm(e.header, term, dataSet.getGlobalDict().getTerm(term));
			else
				entityView.addTerm(e.header, term, dataSet.getEntityDict(e.header).getTerm(term));
		}
		
		// refresh
		entityView.setExpanded(e.header, false);
		entityView.setExpanded(e.header, true);
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		return(frmMain.computeSize(wHint, hHint, changed));
	}
	
	private void onResize(ControlEvent e)
	{
		Rectangle parentExtents = this.getClientArea();
		frmMain.setBounds(0, 0, parentExtents.width, parentExtents.height);
	}
	
	private void createControls()
	{
		GridLayout layout;
		Label label;
		
		// set up the main container form
		frmMain = new SashForm(this, SWT.HORIZONTAL);
		frmMain.setLayout(new FillLayout());
		
		// set up the left pane
		leftPane = new Composite(frmMain, SWT.BORDER);
		layout = new GridLayout(1, false);
		layout.horizontalSpacing = layout.verticalSpacing = 0;
		layout.marginHeight = layout.marginWidth = 0;
		leftPane.setLayout(layout);

		// set the color on the mac
		if(FLInterface.isMac())
			leftPane.setBackground(colorLeftPane);
		
		// add in the expand bar on windows and linux
		if(FLInterface.isLinux() || FLInterface.isWindows())
		{
			ExpandItem eitem;
			
			// set up the expand bar for terms
			ebFilter = new ExpandBar(leftPane, SWT.NONE);		
			ebFilter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			eitem = new ExpandItem(ebFilter, SWT.NONE, ebFilter.getItemCount());
			Composite cmpFilter = new Composite(ebFilter, SWT.NONE);
			GridLayout filterLayout = new GridLayout(1, true);
			cmpFilter.setLayout(filterLayout);
			
			// add in the filtering controls
			txtSearch = new Text(cmpFilter, SWT.SINGLE | SWT.BORDER);
			txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			Composite cmpButtons = new Composite(cmpFilter, SWT.NONE);
			layout = new GridLayout(2, true);
			cmpButtons.setLayout(layout);
			cmpButtons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			btnSearch = new Button(cmpButtons, SWT.PUSH);
			btnSearch.setText("Search");
			btnSearch.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			btnClear = new Button(cmpButtons, SWT.PUSH);
			btnClear.setText("Clear");
			btnClear.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			
			Composite filter = new Composite(cmpFilter, SWT.NONE);
			RowLayout rl = new RowLayout(SWT.HORIZONTAL);
			rl.center = true;
			filter.setLayout(rl);
			filter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

			label = new Label(filter, SWT.NONE);
			label.setText("Sort terms:");
			
			cmbSort = new Combo(filter, SWT.READ_ONLY | SWT.DROP_DOWN);
			cmbSort.add("alphabetically");
			cmbSort.add("by frequency");
			cmbSort.add("by length");
			cmbSort.select(0);
			
			eitem.setText("Search");
			eitem.setControl(cmpFilter);
			eitem.setHeight(cmpFilter.computeSize(SWT.DEFAULT, SWT.DEFAULT).y  + 
				cmbSort.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			eitem.setExpanded(true);
			
			entityView = new EntityViewWinLinux(leftPane, SWT.NONE);
			entityView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			searchView = new EntityViewWinLinux(leftPane, SWT.NONE);
			searchView.setShowPageButtons(false);
			searchView.addHeader(Consts.SEARCH_RESULTS_HEADER_TEXT);
			searchView.setLayoutData(new GridData(SWT.BOTTOM, SWT.RIGHT, false, false));
			searchView.setVisible(false);
			
			// add the callbacks
			Callback.connect(btnSearch, SWT.Selection, this, "onSearch");
			Callback.connect(btnClear, SWT.Selection, this, "onSearchCancel");
		}
		else if(FLInterface.isMac())
		{
			// set up the search/sort box			
			txtSearch = new Text(leftPane, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.CANCEL);
			txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			txtSearch.setText(Consts.SEARCH_BOX_TEXT);
			
			cmpFilter = new Composite(leftPane, SWT.NONE);
			RowLayout rl = new RowLayout(SWT.HORIZONTAL);
			rl.center = true;
			cmpFilter.setLayout(rl);
			cmpFilter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			cmpFilter.setBackground(colorLeftPane);
			
			label = new Label(cmpFilter, SWT.NONE);
			label.setText("Sort terms:");
			label.setBackground(colorLeftPane);
			
			cmbSort = new Combo(cmpFilter, SWT.READ_ONLY | SWT.DROP_DOWN);
			cmbSort.add("alphabetically");
			cmbSort.add("by frequency");
			cmbSort.add("by length");
			cmbSort.select(0);
			
			entityView = new EntityViewOSX(leftPane, SWT.NONE);
			entityView.setBackground(colorLeftPane);
			entityView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			searchView = new EntityViewOSX(leftPane, SWT.NONE);
			searchView.setShowPageButtons(false);
			searchView.setBackground(colorLeftPane);
			searchView.addHeader(Consts.SEARCH_RESULTS_HEADER_TEXT);
			searchView.setVisible(false);
		}
		
		// disable page buttons by default
		entityView.setShowPageButtons(false);
		
		// set up the center pane
		centerPane = new SashForm(frmMain, SWT.VERTICAL);
		
		// set up the top part of the center pane
		graphView = new GraphView(centerPane, SWT.NONE);
		graphView.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		// set up the middle document set
		blockSet = new DocumentBlockSet(centerPane, SWT.NONE);
		
		// and the legend set
		legendSet = new LegendSet(centerPane, SWT.NONE);
		
		// set the weights for the center form
		centerPane.setWeights(new int[] {1, 5, 1});
		
		// set up the right pane
		rightPane = new Composite(frmMain, SWT.NONE);
		rightPane.setLayout(new FillLayout());
		docView = new DocumentView(rightPane, SWT.NONE);
	
		// set the form widths
		frmMain.setWeights(new int[] {3, 7, 4});

		// setup all the callbacks
		Callback.connect(blockSet, SWT.Selection, this, "onDocumentSelect");
		Callback.connect(txtSearch, SWT.DefaultSelection, this, "onFilterEnter");
		Callback.connect(txtSearch, SWT.FocusIn, this, "onSearchFocus");
		Callback.connect(txtSearch, SWT.FocusOut, this, "onSearchBlur");
		Callback.connect(cmbSort, SWT.Selection, this, "onSort");
		
		searchView.addEntityAddedListener(new EntityAddedListener() {
			public void entityAdded(EntityAddedEvent event) {
				onEntityAdded(event);				
			}
		});
		
		entityView.addEntityAddedListener(new EntityAddedListener() {
			public void entityAdded(EntityAddedEvent event) {
				onEntityAdded(event);				
			}
		});
		
		entityView.addEntityPageListener(new EntityPageListener() {
			public void entityPageChange(EntityPageEvent event) {
				onPageChange(event);
			}
		});
		
		legendSet.addDropListener(new LegendDropListener() {
			public void legendDropped(LegendDropEvent e) {
				onLegendDrop(e);				
			}
		});
		
		legendSet.addRemoveListener(new LegendRemovedListener() {
			public void legendRemoved(LegendRemovedEvent e) {
				onLegendRemoved(e);
			}
		});
	}
	
	public void setEnabled(boolean enabled)
	{
		txtSearch.setEnabled(enabled);
		cmbSort.setEnabled(enabled);
		
		if(FLInterface.isLinux() || FLInterface.isWindows())
		{
			ebFilter.setEnabled(enabled);
			btnSearch.setEnabled(enabled);
			btnClear.setEnabled(enabled);
		}
	}
	
	private String capitalize(String word)
	{
		String[] words = word.split("\\s+");
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < words.length; i++)
		{
			sb.append(words[i].substring(0, 1).toUpperCase());
			sb.append(words[i].substring(1).toLowerCase());
			
			if(i < words.length - 1)
				sb.append(' ');
		}
		
		return(sb.toString());
	}
}
