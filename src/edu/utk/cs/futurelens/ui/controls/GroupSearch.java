package edu.utk.cs.futurelens.ui.controls;
import java.util.Observable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import edu.utk.cs.futurelens.data.dictionary.SearchResults;
import edu.utk.cs.futurelens.data.DataSet.DateRange;
import edu.utk.cs.futurelens.ui.Callback;
import edu.utk.cs.futurelens.ui.windows.WinMain;

/**
 * @author greg
 *
 */
public class GroupSearch  {
	 private ArrayList<edu.utk.cs.futurelens.data.group.Group> groups = new ArrayList<edu.utk.cs.futurelens.data.group.Group>();
	 private ArrayList<Integer> resultIndex = new ArrayList<Integer>();
	 private ArrayList<Double> resultValue = new ArrayList<Double>();
	 public static CTabFolder tabs;
	 
	 
	 public GroupSearch() {
		 this.setGroups();
	 }
	  void setGroups() {
		 groups=WinMain.groups;
	 }
	
	 SearchResults search(String term) {
		 this.setGroups();
		 SearchResults results = new SearchResults();
		 //loop that searches groups and sees if the term is in the group
		 for(int i=0;i<groups.size();i++) {
			 if(groups.get(i).search(term)) {
				 results.add(term,i);
				 resultIndex.add(i+1);
				 resultValue.add(groups.get(i).getEntityScore(term)+groups.get(i).getTermScore(term));
			 }
		 }
		 return results;
	 }
	String getGroup(int i) {
		String result=WinMain.groupNames.get(i);
		 return result;
	 }
	double getValue(int i) {
		return resultValue.get(i);
	}
	int length(){
		return resultIndex.size();
	}
}