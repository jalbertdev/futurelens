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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;

import edu.utk.cs.futurelens.data.Loader;
import edu.utk.cs.futurelens.ui.Callback;
import edu.utk.cs.futurelens.ui.Consts;
import edu.utk.cs.futurelens.ui.FLInterface;

/**
 * @author greg
 *
 */
public class WinProgress implements IWindow
{
	private Shell shell;
	
	private Shell parentShell = null;
	private Display parentDisplay = null;
		
	private Loader loader;
	
	// controls used in the window
	private ProgressBar progressBar;
	private Button btnCancel;
	private Label lblOperation;
	
	public WinProgress(Shell parent)
	{
		parentShell = parent;
	}
	
	public WinProgress(Display parent)
	{
		parentDisplay = parent;
	}
	
	public void setProgress(int progress)
	{
		this.updateProgressFromThread(100, false);
	}
	
	public void destroyWindow() {
		shell.dispose();
	}

	public Shell getShell() {
		return shell;
	}

	public void loadWindow() 
	{		
		if(parentShell != null)
			this.shell = new Shell(parentShell, SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL);
		else
			this.shell = new Shell(parentDisplay, SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL);
	
		shell.setSize(Consts.WINDOW_PROGRESS_WIDTH, Consts.WINDOW_PROGRESS_HEIGHT);

		// center the window in the parent window
		FLInterface.centerWindow(shell);
		
		// set the title on non osx
		if(! FLInterface.isMac())
			shell.setText("Loading...");
		
		// create all the controls
		createControls();
		
		// setup the callbacks
		Callback.connect(shell, SWT.Close, this, "onCancel");
		Callback.connect(shell, SWT.KeyDown, this, "onKeyEsc");
		Callback.connect(btnCancel, SWT.Selection, this, "onCancel");
		
		shell.open();
	}

	public void monitorDatasetProgress(Loader dl, final String progressLabel)
	{
		loader = dl;
	
		// set the text
		FLInterface.getDisplay().syncExec(new Runnable() {
			public void run()
			{
				lblOperation.setText(progressLabel);
			}
		});
		
		// create the monitoring thread
		Thread thread = new Thread( new Runnable() 
		{
			public void run()
			{
				while(loader.isOperationInProgress())
				{
					// update the progress bar
					updateProgressFromThread(loader.getPercentComplete(), true);
					
					// sleep
					try {
						Thread.sleep(120);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		thread.start();
		
		return;
	}
	
	private void createControls()
	{
		GridLayout layout = new GridLayout(1, true);
		GridData data;
		
		shell.setLayout(layout);
		
		// create the label
		data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		lblOperation = new Label(shell, SWT.CENTER);
		lblOperation.setLayoutData(data);
		lblOperation.setText("Loading data...");
		
		// and the progress bar
		data = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		data.widthHint = shell.getSize().x - 50;
		progressBar = new ProgressBar(shell, SWT.SMOOTH | SWT.HORIZONTAL);
		progressBar.setLayoutData(data);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		
		// and the cancel button
		data = new GridData(SWT.CENTER, SWT.BOTTOM, true, true);
		btnCancel = new Button(shell, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLayoutData(data);
	}
	
	private void updateProgressFromThread(final int value, final boolean async)
	{
		Runnable code = new Runnable()
		{
			public void run()
			{
				if(! progressBar.isDisposed())
					progressBar.setSelection(value);
			}
		};
		
		if(async)
			FLInterface.getDisplay().asyncExec(code);
		else
			FLInterface.getDisplay().syncExec(code);
	}
	
	public void onCancel()
	{
		if(loader.isOperationInProgress())
			loader.cancelOperation();
		
		destroyWindow();
	}
	
	public void onKeyEsc(Event e)
	{
		if(e.keyCode == SWT.ESC)
			onCancel();
	}
}
