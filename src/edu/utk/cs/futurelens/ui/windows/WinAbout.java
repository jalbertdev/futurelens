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

package edu.utk.cs.futurelens.ui.windows;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import edu.utk.cs.futurelens.ui.Callback;
import edu.utk.cs.futurelens.ui.Consts;
import edu.utk.cs.futurelens.ui.FLInterface;
import edu.utk.cs.futurelens.ui.MenuBar;

public class WinAbout implements IWindow 
{
	private static Shell shell;
	
	private Display parentDisplay = null;
	private Shell parentShell = null;
	
	private static boolean isOpen;
	
	public WinAbout(Shell parent)
	{
		parentShell = parent;
	}
	
	public WinAbout(Display parent)
	{
		parentDisplay = parent;
	}

	public void destroyWindow() 
	{
		
	}

	public Shell getShell() 
	{
		
		return null;
	}

	public void loadWindow() 
	{
		// only open one window
		if(isOpen)
		{
			shell.forceActive();
			return;
		}

		if(parentShell != null)
			shell = new Shell(parentShell, SWT.DIALOG_TRIM);
		else
			shell = new Shell(parentDisplay, SWT.DIALOG_TRIM);
		
		shell.setText("About " + Consts.PROJECT_NAME);
		
		// set the size
		shell.setSize(Consts.ABOUT_WIDTH, Consts.ABOUT_HEIGHT);
		
		// create all the controls
		createControls();
		
		// setup the menubar
		if(FLInterface.isMac())
		{
			MenuBar mb = new MenuBar(shell);
			mb.connect(mb.fileCloseWindow, this, "destroyWindow");
		}
		
		// connect the close callback
		Callback.connect(shell, SWT.Close, this, "destroyWindow");
		
		// center the window in the parent
		FLInterface.centerWindow(shell);
		
		shell.open();
		
		isOpen = true;
	}
	
	private void createControls()
	{
		shell.setLayout(new GridLayout(2, true));
		
		Composite cmpLeft = new Composite(shell, SWT.NONE);
		cmpLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		cmpLeft.setLayout(new FillLayout());
		
		Label logo = new Label(cmpLeft, SWT.NONE);
		logo.setImage(loadImage());
		logo.setAlignment(SWT.CENTER);
		
		Composite cmpRight = new Composite(shell, SWT.NONE);
		cmpRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cmpRight.setLayout(new FillLayout());
		
		Label label = new Label(cmpRight, SWT.NONE);
		label.setText(	"FutureLens\n\n" +
						"(C) Copyright 2008\n\n" +
						"Gregory Shutt\n" +
						"Andrey Puretskiy\n" +
						"Michael W. Berry\n\n" +
						"All Rights Reserved");
		label.setAlignment(SWT.CENTER);
	}
	
	private Image loadImage()
	{
		InputStream is = getClass().getResourceAsStream("/edu/utk/cs/futurelens/resource/fl-icon-128.png");
		
		if(is == null)
			return(null);
		
		Image image = new Image(shell.getDisplay(), is);
		
		try {
			is.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return(image);
	}
}
