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

package edu.utk.cs.futurelens.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;

import edu.utk.cs.futurelens.ui.controls.DocumentBlockSet;
import edu.utk.cs.futurelens.ui.controls.DocumentView;
import edu.utk.cs.futurelens.ui.controls.EntityView;
import edu.utk.cs.futurelens.ui.controls.GraphView;
import edu.utk.cs.futurelens.ui.controls.GroupView;
import edu.utk.cs.futurelens.ui.controls.LegendSet;
import edu.utk.cs.futurelens.ui.windows.WinMain;

public class Demo 
{
	private final static Display display = FLInterface.getDisplay();
	private final static boolean showTips = true;
	private final static boolean presentationMode = false;
	
	// controls
	private static WinMain winMain;
	private static GroupView gvOverview;
	private static EntityView entityView;
	private static Shell parentShell;
	private static DocumentBlockSet blockSet;
	private static LegendSet legendSet;
	private static DocumentView docView;
	private static GraphView graphView;
	
	private static ToolTip toolTip;
	private static ToolTip balloonTip;
	
	private Demo()
	{
		// prevent instantiation
	}
	
	public static void start(WinMain winMain)
	{
		Demo.winMain = winMain;
		Demo.parentShell = winMain.getShell();
		
		toolTip = new ToolTip(parentShell, SWT.NONE);
		balloonTip = new ToolTip(parentShell, SWT.BALLOON);
		
		Thread thread = new Thread(new Runnable() {
			public void run()
			{
				runDemo();
			}
		});
		
		thread.start();
	}
	
	private static void runDemo()
	{
		// get all the controls
		getControls(winMain);
		
		// do demo stuff
		// open the data set
		if(! presentationMode)
		{
			showTip("Normally, a data set must be loaded by going to File->Load Data.", null);
			
			hideTip(2500, 0);
			
			showTip("However, for this demo an internal data set will automatically be loaded", null);
			
			hideTip(5000, 0);
		
			winMain.loadDataFromResource("/edu/utk/cs/futurelens/resource/data");
		}
		else
		{
			showTip("To load a data set, go to File->Load Data", null);
			
			pause(1500);
			
			openMenu();
			
			// press enter
			pause(2000);
			
			sendKey('\r', SWT.NONE);
			
			// wait for the dialog to appear and press enter
			pause(3000);
			
			sendKey('\r', SWT.NONE);
		}
		
		// wait to load the data set
		while(winMain.isDataSetLoaded() == false)
			pause(500);
		
		hideTip(0, 0);
		
		showTip("Each block represents a specific amount of time" , blockSet.getBlock(0));
		hideTip(4000, 0);
		
		showTip("The height of the block corresponds to the number of documents in that time period", blockSet.getBlock(0));
		hideTip(4000, 0);
		
		// change sort method to frequency
		setSortMethod("frequency");
		
		showTip("All the terms and entities in the documents are listed here", (Control)entityView);
		hideTip(5000, 0);
		
		// expand the terms header
		mouseMove((Control)entityView, 50, 15);
		setExpanded(Consts.TERMS_HEADER_TEXT, true);
		
		showTip("Click the + button to add a term", (Control)entityView, 24, -1);
		hideTip(5000, 0);
		
		// add a term or two
		addTerm(Consts.TERMS_HEADER_TEXT, 0);
		
		pause(1000);
		
		addTerm(Consts.TERMS_HEADER_TEXT, 1);
		
		showTip("Any document that contains the term is highlighted with its corresponding color", blockSet.getBlock(0));
		hideTip(5000, 0);
		
		showTip("Documents are listed by increasing date. Select a document by clicking it", blockSet.getBlock(0));
		hideTip(5000, 0);
		
		// click a doc block
		click(blockSet.getBlock(0), 30, 55);
		
		showTip("The document text appears here and all selected terms are highlighted", docView);
		hideTip(5000, 0);
		
		showTip("Terms can be combined by dragging and dropping", legendSet);
		hideTip(5000, 0);
		
		// drag a block
		//  linux *shockingly* has problems with this
		if(! FLInterface.isLinux())
			drag(legendSet.getLegends()[0], legendSet.getLegends()[1], -1, -1);
		else
			simulateDnd(0, 1);
		
		showTip("Any document containing both terms will be highlighted", blockSet.getBlock(0));
		hideTip(5000, 0);
		
		showTip("The percentage of documents containing the terms over time is shown", graphView);
		hideTip(5000, 0);
		
		mouseMove(legendSet.getLegends()[0], 130, 10);
		
		showTip("Finally, terms can be removed by clicking the X button", legendSet.getLegends()[0]);
		hideTip(5000, 0);
		
		removeLegend(0);
		
		// reset everything
		if(! FLInterface.isMac())
			setExpanded(Consts.TERMS_HEADER_TEXT, false);
		
		clearDocView();
	}
	
	private static void clearDocView()
	{
		display.syncExec(new Runnable() {
			public void run() {
				docView.showDocument(null);
			}
		});
	}
	
	private static void simulateDnd(final int index1, final int index2)
	{
		display.syncExec(new Runnable() {
			public void run() {
				legendSet.getLegends()[index1].addTerm(legendSet.getLegends()[index2].getText());
				legendSet.getLegends()[index2].dispose();
			}
		});
	}
	
	private static void removeLegend(final int index)
	{
		display.syncExec(new Runnable() {
			public void run() {
				legendSet.getLegends()[index].dispose();
			}
		});
	}
	
	private static void addTerm(final String header, final int index)
	{
		display.syncExec(new Runnable() {
			public void run() {
				Point p = entityView.getTermButtonXY(header, index);
				click((Control)entityView, p.x, p.y);
			}
		});
	}
	
	private static void setSortMethod(final String method)
	{
		display.syncExec(new Runnable() {
			public void run() {
				gvOverview.setSortMethod(method);
			}
		});
	}
	
	private static void setExpanded(final String header, final boolean expanded)
	{
		display.syncExec(new Runnable() {
			public void run() {
				entityView.setExpanded(header, expanded);
			}
		});
	}
	
	private static void openMenu()
	{
		if(FLInterface.isWindows())
		{
			// this is terrible but is about the only way to do this
			//  until swt supports finding the location of a menubar item
			sendKey('F', SWT.ALT);
			sendKey('\r', SWT.NONE);
		}
		else if(FLInterface.isMac())
		{
			// highlight the menubar
			sendKey(SWT.F2, SWT.CTRL);
			
			// move over to file
			sendKey(SWT.ARROW_RIGHT, SWT.NONE);
			sendKey(SWT.ARROW_RIGHT, SWT.NONE);
			
			// open the file menu and go to open data
			sendKey(SWT.ARROW_DOWN, SWT.NONE);
			sendKey(SWT.ARROW_DOWN, SWT.NONE);
		}
	}
	
	private static void sendKey(char keyCode, int modifier)
	{
		sendKey(0, keyCode, modifier);
	}
	
	private static void sendKey(int keyCode, int modifier)
	{
		sendKey(keyCode, (char)0, modifier);
	}
	
	private static void sendKey(int keyCode, char key, int modifier)
	{
		final Event event = new Event();
		
		event.type = SWT.KeyDown;
		
		// press the modifier key
		if(modifier != 0)
		{
			event.keyCode = modifier;
			display.post(event);
		}

		// send the key
		if(keyCode == 0)
			event.character = key;
		else
			event.keyCode = keyCode;
		
		display.post(event);
		
		// wait
		pause(100);
		
		// release
		event.type = SWT.KeyUp;
		display.post(event);
		
		if(modifier != 0)
		{
			pause(100);
			event.keyCode = modifier;
			event.character = 0;
			display.post(event);
		}
	}
	
	private static void showTip(final String text, final Control control)
	{
		showTip(text, control, -1, -1);
	}
	
	private static void showTip(final String text, final Control control, final int x, final int y)
	{
		if(showTips == false)
			return;
		
		display.syncExec(new Runnable() {
			public void run() {
				Point size, location;
				ToolTip tip;
				int adjX = x, adjY = y;
				
				if(control == null)
				{
					size = parentShell.getSize();
					
					if(adjX == -1)
						adjX = 200;
					
					if(adjY == -1)
						adjY = (int)(size.y * 0.75);
					
					location = display.map(parentShell, null, adjX, adjY);
					tip = toolTip;
				}
				else
				{
					size = control.getSize();
					
					if(adjX == -1)
						adjX = size.x / 2;
					
					if(adjY == -1)
						adjY = size.y / 2;
					
					location = display.map(control, null, adjX, adjY);
					tip = balloonTip;
				}
			
				tip.setText(text);
				tip.setLocation(location);
				tip.setAutoHide(false);
				tip.setVisible(true);
			}
		});
	}
	
	private static void hideTip(int waitToolTip, int waitNonToolTip)
	{
		if(showTips == false)
		{
			pause(waitNonToolTip);
			return;
		}
		
		pause(waitToolTip);
		
		display.syncExec(new Runnable() {
			public void run() {
				toolTip.setVisible(false);
				balloonTip.setVisible(false);
			}
		});
	}
	
	private static void click(final Control control, final int x, final int y)
	{
		Event event;

		event = new Event();
		
		// move the cursor to the control				
		mouseMove(control, x, y);
		
		pause(100);
		
		// click
		event.type = SWT.MouseDown;
		event.button = 1;
		display.post(event);
		
		pause(100);
		
		// release
		event.type = SWT.MouseUp;
		display.post(event);
	}
	
	private static void drag(final Control control, final Control target, final int x, final int y)
	{
		Event event;
					
		event = new Event();
			
		// move the mouse to the control
		mouseMove(control, x, y);
		
		pause(100);
		
		// click
		event.type = SWT.MouseDown;
		event.button = 1;
		display.post(event);
		
		pause(100);
		
		// move to the target
		event.button = 0;
		
		mouseMove(target, x, y);
		
		// release
		event.type = SWT.MouseUp;
		event.button = 1;
		display.post(event);	
	}
	
	private static void mouseMove(final Control control, final int x, final int y)
	{
		int jump = 5;
		final Point startPoint = new Point(0, 0);
		final Point endPoint = new Point(0, 0);
		
		display.syncExec(new Runnable() {
			public void run() {
				int adjX = x;
				int adjY = y;
				
				// map the mouse coords
				if(x == -1)
					// move to the center of the control
					adjX = control.getSize().x / 2;
				
				if(y == -1)
					adjY = control.getSize().y / 2;
				
				Point point = display.getCursorLocation();
				
				startPoint.x = point.x;
				startPoint.y = point.y;
				
				point = display.map(control, null, adjX, adjY);
				
				endPoint.x = point.x;
				endPoint.y = point.y;
			}
		});
				
		Event event = new Event();
		
		event.x = startPoint.x;
		event.y = startPoint.y;
		event.type = SWT.MouseMove;
		while(Math.abs(event.x - endPoint.x) > jump || Math.abs(event.y - endPoint.y) > jump)
		{
			if(Math.abs(event.x - endPoint.x) > jump)
				event.x += event.x < endPoint.x ? jump : -jump;
			
			if(Math.abs(event.y - endPoint.y) > 3)
				event.y += event.y < endPoint.y ? jump : -jump;
			
			pause(30);
			
			display.post(event);
		}
	}
	
	private static void pause(int length)
	{
		try {
			Thread.sleep(length);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void getControls(WinMain winMain)
	{
		gvOverview = winMain.getOverview();
		entityView = gvOverview.getEntityView();
		blockSet = gvOverview.getBlockSet();
		legendSet = gvOverview.getLegendSet();
		docView = gvOverview.getDocumentView();
		graphView = gvOverview.getGraphView();
	}
}
