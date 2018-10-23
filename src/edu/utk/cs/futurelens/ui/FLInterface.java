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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

import edu.utk.cs.futurelens.ui.windows.WinMain;

/**
 * @author Greg
 *
 */
public class FLInterface 
{	
	// from http://developer.apple.com/technotes/tn2002/tn2110.html
	private static final boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
	private static final boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
	private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

	
	// various interface parts
	private static WinMain winMain;
	private static Display display;
	
	public static Display getDisplay() 
	{
		return display;
	}

	public static Monitor getMonitor()
	{
		return winMain.getShell().getMonitor();
	}
	
	public static final boolean isLinux() 
	{
		return isLinux;
	}
	
	public static final boolean isMac() 
	{
		return isMac;
	}
	
	public static final boolean isWindows() 
	{
		return isWindows;
	}

	public static WinMain getWinMain()
	{
		return winMain;
	}
	
	public void loadInterface()
	{
		Shell shell;
		
		if(isMac()){
		Display.setAppName(Consts.PROJECT_NAME);
		}
		
		display = new Display();
		
		// load the main window
		winMain = new WinMain(display);
		winMain.loadWindow();
		shell = winMain.getShell();

		// set the window title
		Display.setAppName(Consts.PROJECT_NAME);
		shell.setText(Consts.PROJECT_NAME);
		System.out.println(Consts.PROJECT_NAME);
		// load the icon
		if(FLInterface.isLinux || FLInterface.isWindows)
			loadIcon(shell);
		
		// show the window
		shell.open();
		
		// main event loop
		while(! shell.isDisposed())
		{
			if(! display.readAndDispatch())
			{
				display.sleep();
			}
		}
		
		display.dispose();
		
		return;
	}

	private void loadIcon(Shell shell)
	{
		InputStream is16 = getClass().getResourceAsStream("/edu/utk/cs/futurelens/resource/icon-16.png");
		InputStream is32 = getClass().getResourceAsStream("/edu/utk/cs/futurelens/resource/icon-32.png");
		InputStream is64 = getClass().getResourceAsStream("/edu/utk/cs/futurelens/resource/icon-64.png");
		
		if(is16 == null || is32 == null || is64 == null)
		{
			System.out.println("Warning: could not load icon");
			return;
		}
		
		// create the icons
		Image icn16 = new Image(display, is16);
		Image icn32 = new Image(display, is32);
		Image icn64 = new Image(display, is64);
		
		shell.setImages(new Image[] {icn16, icn32, icn64});
		
		try {
			is16.close();
			is32.close();
			is64.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void centerWindow(Shell window)
	{
		Composite parent;
		Point parentSize;
		Point location = new Point(0, 0);
		
		parent = window.getParent();
		
		if(parent != null)
		{
			parentSize = parent.getLocation();
			parentSize.x += parent.getSize().x / 2;
			parentSize.y += parent.getSize().y / 2;
		}
		else
		{
			parentSize = new Point(0, 0);
			parentSize.x = display.getPrimaryMonitor().getClientArea().width / 2;
			parentSize.y = display.getPrimaryMonitor().getClientArea().height / 2;
		}
		
		location.x = parentSize.x - window.getSize().x / 2;
		location.y = parentSize.y - window.getSize().y / 2;
		window.setLocation(location);
	}
	
}
