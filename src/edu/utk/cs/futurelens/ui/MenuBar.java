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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import edu.utk.cs.futurelens.ui.windows.WinAbout;
import edu.utk.cs.futurelens.ui.windows.WinMain;

/**
 * @author Greg
 * @author Andrey
 *
 */
public class MenuBar 
{
	// all available menu items
	public MenuItem fileCloseWindow;
	public MenuItem fileExit;
	public MenuItem fileLoadData;
	public MenuItem fileLoadDictionary;
	public MenuItem fileLoadGroup;
	public MenuItem createUserDictionary;
	public MenuItem resetDictionary;
	public MenuItem toolsPreferences;
	public MenuItem helpAbout;
	public MenuItem helpHelp;
	public MenuItem helpDemo;
	
	// additions for emotion tracking
	public MenuItem fileLoadCategories;
	public MenuItem fileLoadWeightedCategories;
	
	// callbacks for special case items
	private static Method 	toolsPreferencesMethod	= null;
	private static Object	toolsPreferencesTarget	= null;
	private static Method	helpAboutMethod			= null;
	private static Object	helpAboutTarget			= null;
	
	private static boolean osxAppMenuSetup = false;	
		
	public MenuBar(Shell shell) 
	{
		Menu menuBar;
		Menu fileMenu;
		Menu helpMenu;
		Menu optionsMenu = null;
		Menu toolsMenu;
		MenuItem fileMenuHeader;
		MenuItem toolsMenuHeader;
		MenuItem optionsMenuHeader;
		MenuItem helpMenuHeader;
		
		String aboutName = "About " + Consts.PROJECT_NAME;
		
		// set up the menu bar
		menuBar = new Menu(shell, SWT.BAR);
		
		// add the file menu
		fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuHeader.setText(FLInterface.isMac() ? "File" : "&File");
		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);
		
		// add the tools menu
		//	if(FLInterface.isMac())
		//{
			toolsMenuHeader = new MenuItem(menuBar, SWT.CASCADE);	
			toolsMenuHeader.setText("&Tools");
			toolsMenu = new Menu(shell, SWT.DROP_DOWN);
			toolsMenuHeader.setMenu(toolsMenu);
		//}
			if(!FLInterface.isMac()){
			optionsMenuHeader = new MenuItem(menuBar,SWT.CASCADE);
			optionsMenuHeader.setText("&Options");
			optionsMenu = new Menu(shell,SWT.DROP_DOWN);
			optionsMenuHeader.setMenu(optionsMenu);
			
			}
			
		// add the help menu
		helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
		helpMenuHeader.setText(FLInterface.isMac() ? "Help" : "&Help");
		helpMenu = new Menu(shell, SWT.DROP_DOWN);
		helpMenuHeader.setMenu(helpMenu);
		
		// add in file->load data
		fileLoadData = new MenuItem(fileMenu, SWT.PUSH);
		fileLoadData.setAccelerator( (FLInterface.isMac() ? SWT.COMMAND : SWT.CTRL) + 'O');
		fileLoadData.setText( (FLInterface.isMac() ? "Load Data..." : "Load Data...\tCtrl+O"));
		fileLoadData.setEnabled(false);
		
		// add in file->load categories
		fileLoadCategories = new MenuItem(fileMenu, SWT.PUSH);
		fileLoadCategories.setAccelerator( (FLInterface.isMac() ? SWT.COMMAND : SWT.CTRL) + 'U');
		fileLoadCategories.setText( (FLInterface.isMac() ? "Load Categories..." : "Load Categories...\tCtrl+U"));
		fileLoadCategories.setEnabled(true);
		
		// add in file->load categories
		/*fileLoadWeightedCategories = new MenuItem(fileMenu, SWT.PUSH);
		fileLoadWeightedCategories.setAccelerator( (FLInterface.isMac() ? SWT.COMMAND : SWT.CTRL) + 'W');
		fileLoadWeightedCategories.setText( (FLInterface.isMac() ? "Load Weighted Categories..." : "Load Weighted Categories...\tCtrl+W"));
		fileLoadWeightedCategories.setEnabled(true);*/
		
		// file->load dictionary
		fileLoadDictionary = new MenuItem(fileMenu, SWT.PUSH);
		fileLoadDictionary.setAccelerator(SWT.SHIFT + (FLInterface.isMac() ? SWT.COMMAND : SWT.CTRL) + 'O');
		fileLoadDictionary.setText( (FLInterface.isMac() ? "Load Dictionary..." : "Load Dictionary...\tShift+Ctrl+O"));
		fileLoadDictionary.setEnabled(false);
		
		// file->load tensor input
		fileLoadGroup = new MenuItem(fileMenu, SWT.PUSH);
		fileLoadGroup.setAccelerator( (FLInterface.isMac() ? SWT.COMMAND : SWT.CTRL) + 'L');
		fileLoadGroup.setText( (FLInterface.isMac() ? "Load Group File..." : "Load Group File...\tCtrl+L"));
		fileLoadGroup.setEnabled(false);
		
		createUserDictionary = new MenuItem(toolsMenu, SWT.PUSH);
		createUserDictionary.setAccelerator( (FLInterface.isMac() ? SWT.COMMAND : SWT.CTRL) + 'D');
		createUserDictionary.setText( (FLInterface.isMac() ? "Create Dictionary..." : "Create Dictionary...\tCtrl+D"));
		createUserDictionary.setEnabled(false);
		
		resetDictionary = new MenuItem(toolsMenu, SWT.PUSH);
		resetDictionary.setAccelerator( (FLInterface.isMac() ? SWT.COMMAND : SWT.CTRL) + 'R');
		resetDictionary.setText( (FLInterface.isMac() ? "Reset Dictionary..." : "Reset Dictionary...\tCtrl+R"));
		resetDictionary.setEnabled(false);
		
		
		// separator
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		// add in file->close window...only on macs
		if(FLInterface.isMac())
		{
			fileCloseWindow = new MenuItem(fileMenu, SWT.PUSH);
			fileCloseWindow.setAccelerator( SWT.COMMAND + 'W');
			fileCloseWindow.setText("Close Window");
			fileCloseWindow.setEnabled(false);
			
			
		}
		// add help->futurelens help
		helpHelp = new MenuItem(helpMenu, SWT.PUSH);
		helpHelp.setText(Consts.PROJECT_NAME + " Help");
		helpHelp.setEnabled(false);
		
		// sep
		new MenuItem(helpMenu, SWT.SEPARATOR);
		
		// add help->demo
		helpDemo = new MenuItem(helpMenu, SWT.PUSH);
		helpDemo.setText("Show Demo");
		helpDemo.setEnabled(false);
		
		// add in menus for OSX
		//Modified from http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet354.java
		if(FLInterface.isMac())
		{ /*
			String osVersion = System.getProperty("os.version");
		    String[] fragments = osVersion.split("\\.");
		    if((Integer.parseInt(fragments[0]) >= 10) && (Integer.parseInt(fragments[1]) >= 6)){
		    	Menu systemMenu = FLInterface.getDisplay().getSystemMenu();
		    	ArmListener armListener = new ArmListener() {
				public void widgetArmed(ArmEvent e) {
				}
			};
			if (systemMenu != null) {
				MenuItem sysItem;
				
				sysItem = getItem(systemMenu, SWT.ID_PREFERENCES);
				sysItem.addArmListener(armListener);
				sysItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						onFilePreferences();
					};
				});
				sysItem = getItem(systemMenu, SWT.ID_ABOUT);
				sysItem.addArmListener(armListener);
				sysItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						onHelpAbout();
					};
				});
			}
		    }
			// create unused menu items for preferences/about
			this.toolsPreferences = new MenuItem(new Menu(shell), SWT.NONE);
			this.helpAbout = new MenuItem(new Menu(shell), SWT.NONE);
			*/
		}
		else
		{
			// add in menu items for non macs
			
			// tools->options (non mac)
			toolsPreferences = new MenuItem(optionsMenu, SWT.PUSH);
			toolsPreferences.setText("Options");
			
			// add in the file->exit item
			fileExit = new MenuItem(fileMenu, SWT.PUSH);
			fileExit.setText("E&xit");
			fileExit.setEnabled(false);
			
			// add in a separator before help->about
			new MenuItem(helpMenu, SWT.SEPARATOR);
			
			// add in the help->about item
			helpAbout = new MenuItem(helpMenu, SWT.PUSH);
			helpAbout.setText(aboutName);
			helpAbout.setEnabled(false);
		}
		
		// assign the menu bar
		shell.setMenuBar(menuBar);
		
	}
	//Modified from http://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet354.java
	static MenuItem getItem(Menu menu, int id) {
			MenuItem[] items = menu.getItems();
			for (int i = 0; i < items.length; i++) {
				if (items[i].getID() == id) return items[i];
			}
			return null;
		}
	
	@SuppressWarnings("unchecked")
	public void connect(MenuItem menu, Object object, String function)
	{
		if(menu == null)
			return;
		
		if(FLInterface.isMac() && (menu == this.toolsPreferences || menu == this.helpAbout))
		{
			Class targetClass = object.getClass();
			
			if(menu == this.toolsPreferences)
				toolsPreferencesTarget = object;
			else
				helpAboutTarget = object;
						
			// special cases for macs
			while(targetClass != null)
			{
				for(Method method : targetClass.getDeclaredMethods())
				{
					if(method.getName().equals(function))
					{
						if(menu == this.toolsPreferences)
							toolsPreferencesMethod = method;
						else
							helpAboutMethod = method;
						
						menu.setEnabled(true);
						
						return;
					}
				}
				
				// get the parent class
				targetClass = targetClass.getSuperclass();
			}
		}
		else
		{
			if(object == null || function == null)
			{
				menu.setEnabled(false);
				return;
			}
			
			Callback.connect(menu, SWT.Selection, object, function);
			menu.setEnabled(true);
		}
	}
	
	public void disconnect(MenuItem item)
	{
		connect(item, null, null);
	}
	
	public static void onHelpAbout()
	{
		if(helpAboutTarget != null && helpAboutMethod != null)
		{
			try {
				helpAboutMethod.invoke(helpAboutTarget, new Object[0]);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void onFilePreferences()
	{
		if(toolsPreferencesTarget != null && toolsPreferencesMethod != null)
		{
			try {
				toolsPreferencesMethod.invoke(toolsPreferencesTarget, new Object[0]);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
