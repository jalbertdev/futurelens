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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JFrame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import edu.utk.cs.futurelens.FutureLens;
import edu.utk.cs.futurelens.data.CreateUserDict;
import edu.utk.cs.futurelens.data.DataLoader;
import edu.utk.cs.futurelens.data.FileLoader;
import edu.utk.cs.futurelens.data.DataSet;
import edu.utk.cs.futurelens.data.ResourceLoader;
import edu.utk.cs.futurelens.data.dictionary.Dictionary;
import edu.utk.cs.futurelens.data.dictionary.DictionaryLoader;
import edu.utk.cs.futurelens.data.group.Group;
import edu.utk.cs.futurelens.data.group.GroupLoader;
import edu.utk.cs.futurelens.data.parser.ParseException;
import edu.utk.cs.futurelens.ui.Callback;
import edu.utk.cs.futurelens.ui.Consts;
import edu.utk.cs.futurelens.ui.Demo;
import edu.utk.cs.futurelens.ui.FLInterface;
import edu.utk.cs.futurelens.ui.MenuBar;
import edu.utk.cs.futurelens.ui.Prefs;
import edu.utk.cs.futurelens.ui.controls.EntityView;
import edu.utk.cs.futurelens.ui.controls.GroupView;

/**
 * @author Greg
 * @author Andrey
 * 
 */
public class WinMain implements IWindow {
	private JFrame frame;
	private Shell shell;
	private Shell parentShell = null;
	private Display parentDisplay = null;
	private DataLoader dataLoader;
	private DataSet dataSet;
	private Boolean flag = false;
	private static Dictionary oldDict;
	//new stuff for date restrictions
	private DataSet restrictedDataSet;

	private volatile boolean isDialogOpen = false;
	private volatile boolean isDataSetLoaded = false;

	// controls used in the window
	private CTabFolder tfGroups;
	private MenuBar menuBar;
	private GroupView gvOverview;
	public ArrayList allCategories = new ArrayList();

	public WinMain(Shell shell) {
		parentShell = shell;
	}

	public WinMain(Display display) {
		parentDisplay = display;
	}

	public GroupView getOverview() {
		return (gvOverview);
	}

	public MenuBar getMenuBar() {
		return (menuBar);
	}

	public boolean isDialogOpen() {
		return (isDialogOpen);
	}

	public void loadWindow() {
		Rectangle position = new Rectangle(0, 0, 0, 0);
		Rectangle screenSize;

		if (parentShell != null)
			shell = new Shell(parentShell);
		else
			shell = new Shell(parentDisplay);

		// need this to map the "x"-shaped button and the "Close" menu choice to
		// the same event handling code
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				/*
				 * if (frame!=null) frame.dispose(); shell.dispose();
				 */
				System.exit(0);
			}
		});

		// restore the size/position of the window
		position.x = Prefs.get(Prefs.WINDOW_X, Prefs.WINDOW_X_DEFAULT);
		position.y = Prefs.get(Prefs.WINDOW_Y, Prefs.WINDOW_Y_DEFAULT);
		position.width = Prefs.get(Prefs.WINDOW_WIDTH,
				Prefs.WINDOW_WIDTH_DEFAULT);
		position.height = Prefs.get(Prefs.WINDOW_HEIGHT,
				Prefs.WINDOW_HEIGHT_DEFAULT);

		screenSize = shell.getDisplay().getPrimaryMonitor().getClientArea();

		// adjust the width and height so the window is on screen
		if (position.width > screenSize.width) {
			position.width = screenSize.width;
			position.x = -1;
		}

		if (position.height > screenSize.height) {
			position.height = screenSize.height;
			position.y = -1;
		}

		if (position.x < screenSize.x)
			// center on screen
			position.x = (screenSize.width - position.width) / 2;

		if (position.y < screenSize.y)
			position.y = (screenSize.height - position.height) / 2;

		if (position.x >= screenSize.width)
			position.x = (screenSize.width - position.width) / 2;

		if (position.y >= screenSize.height)
			position.y = (screenSize.height - position.height) / 2;

		// move the window on screen
		if (position.x + position.width > screenSize.width)
			position.width = screenSize.width - position.x;

		if (position.y + position.height > screenSize.height)
			position.height = screenSize.height - position.y;

		shell.setLocation(position.x, position.y);
		shell.setSize(position.width, position.height);

		// restore maximize
		if (Prefs.get(Prefs.WINDOW_IS_MAXIMIZED,
				Prefs.WINDOW_IS_MAXIMIZED_DEFAULT) == true)
			shell.setMaximized(true);

		// set up the menubar
		menuBar = new MenuBar(shell);
		menuBar.connect(menuBar.fileCloseWindow, this, "onFileCloseWindow");
		menuBar.connect(menuBar.fileExit, this, "destroyWindow");
		menuBar.connect(menuBar.fileLoadData, this, "onFileLoadData");
		menuBar.connect(menuBar.toolsPreferences, this, "onToolsPreferences");
		menuBar.connect(menuBar.helpAbout, this, "onHelpAbout");
		menuBar.connect(menuBar.helpDemo, this, "onHelpDemo");
		menuBar.connect(menuBar.fileLoadCategories, this,
				"onFileLoadCategories");
		menuBar.connect(menuBar.fileLoadWeightedCategories, this,
				"onFileLoadWeightedCategories");

		// set up listeners
		Callback.connect(shell, SWT.Resize, this, "onResize");
		Callback.connect(shell, SWT.Move, this, "onMove");

		// set up the tab bar
		setupTabs();
	}

	public void destroyWindow() {
		if (frame != null)
			frame.dispose();

		if (shell != null) {
			shell.close();
		}
	}

	public Shell getShell() {
		return shell;
	}

	public boolean isDataSetLoaded() {
		return isDataSetLoaded;
	}

	public void onResize() {
		if (!shell.getMaximized()) {
			Prefs.set(Prefs.WINDOW_WIDTH, shell.getSize().x);
			Prefs.set(Prefs.WINDOW_HEIGHT, shell.getSize().y);
		}

		Prefs.set(Prefs.WINDOW_IS_MAXIMIZED, shell.getMaximized());
	}

	public void onMove() {
		Prefs.set(Prefs.WINDOW_X, shell.getLocation().x);
		Prefs.set(Prefs.WINDOW_Y, shell.getLocation().y);
	}

	public void onFileCloseWindow() {
		destroyWindow();
	}

	public void onFileLoadData() {
		DirectoryDialog dd = new DirectoryDialog(shell, SWT.OPEN);
		String path;

		dd.setText("Open Folder");
		dd.setFilterPath(Prefs.get(Prefs.DATASET_PATH,
				Prefs.DATASET_PATH_DEFAULT));

		isDialogOpen = true;
		path = dd.open();

		if (path != null) {
			// save this location
			Prefs.set(Prefs.DATASET_PATH, path);

			// load the data set
			dataLoader = new FileLoader(Prefs.get(Prefs.DATASET_EXT,
					Prefs.DATASET_EXT_DEFAULT));
			dataLoader.setSourcePath(path);
			startLoad(dataLoader);
		}
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public void onFileLoadCategories() throws IOException {
		// DirectoryDialog dd = new DirectoryDialog(shell, SWT.OPEN);
		String path;
		String[] sourceFiles;
		FileDialog fd = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
		path = fd.open();

		if (path != null) {
			path = path.substring(0, path.lastIndexOf(File.separator));

			// dd.setText("Open Folder");
			// dd.setFilterPath(Prefs.get(Prefs.DATASET_PATH,
			// Prefs.DATASET_PATH_DEFAULT));

			isDialogOpen = true;
			// path = dd.open();
			// File dir = new File(path);
			// sourceFiles = dir.listFiles();

			sourceFiles = fd.getFileNames();

			if (sourceFiles.length < 2) {
				JOptionPane.showMessageDialog(null,
						"At least 2 files must be selected", "Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				allCategories = new ArrayList();

				for (String f : sourceFiles) {
					// System.out.println(f.getName());
					FileInputStream fin = new FileInputStream(path
							+ File.separator + f);
					DataInputStream reader = new DataInputStream(fin);
					String line;
					ArrayList temp = new ArrayList();
					while ((line = reader.readLine()) != null) {
						temp.add((line.trim()).toLowerCase());
					}
					allCategories.add(temp);
				}
			}

			// create a legend box for categories
			Display display = FLInterface.getDisplay();
			org.eclipse.swt.graphics.Image image = new org.eclipse.swt.graphics.Image(
					display, 20, 20);
			GC gc = new GC(image);
			gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
			gc.fillRectangle(0, 0, 20, 20);
			gc.dispose();

			frame = new JFrame("Category Legend");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setLayout(new GridLayout(allCategories.size() + 1, 2));

			// add the first row (the white box and the label of N/A)
			JLabel tempLabel = new JLabel("Category N/A");
			tempLabel.setForeground(java.awt.Color.WHITE);
			frame.getContentPane().add(tempLabel);
			frame.getContentPane().setBackground(java.awt.Color.BLACK);
			// frame.getContentPane().add(new JLabel(image));
			int colorIndex = 1;
			for (int i = 0; i < allCategories.size(); i++) {
				// add the text label and color-code
				ArrayList temp = (ArrayList) allCategories.get(i);
				String title = (String) temp.get(0);
				tempLabel = new JLabel(title);
				tempLabel.setForeground(convertColor(i + 3));
				frame.getContentPane().add(tempLabel);
			}
			frame.pack();
			frame.setVisible(true);
		}

	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public void onFileLoadWeightedCategories() throws IOException {
		// DirectoryDialog dd = new DirectoryDialog(shell, SWT.OPEN);
		String path;
		String[] sourceFiles;
		FileDialog fd = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
		path = fd.open();
		path = path.substring(0, path.lastIndexOf(File.separator));

		// dd.setText("Open Folder");
		// dd.setFilterPath(Prefs.get(Prefs.DATASET_PATH,
		// Prefs.DATASET_PATH_DEFAULT));

		isDialogOpen = true;
		// path = dd.open();
		// File dir = new File(path);
		// sourceFiles = dir.listFiles();

		sourceFiles = fd.getFileNames();

		if (sourceFiles.length < 2) {
			JOptionPane.showMessageDialog(null,
					"At least 2 files must be selected", "Error",
					JOptionPane.ERROR_MESSAGE);
		} else {
			allCategories = new ArrayList();

			for (String f : sourceFiles) {
				// System.out.println(f.getName());
				FileInputStream fin = new FileInputStream(path + File.separator
						+ f);
				DataInputStream reader = new DataInputStream(fin);
				String line;
				ArrayList temp = new ArrayList();
				while ((line = reader.readLine()) != null) {
					temp.add((line.trim()).toLowerCase());
				}
				allCategories.add(temp);
			}
		}

		// create a legend box for categories
		Display display = FLInterface.getDisplay();
		org.eclipse.swt.graphics.Image image = new org.eclipse.swt.graphics.Image(
				display, 20, 20);
		GC gc = new GC(image);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(0, 0, 20, 20);
		gc.dispose();

		frame = new JFrame("Category Legend");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new GridLayout(allCategories.size() + 1, 2));

		// add the first row (the white box and the label of N/A)
		JLabel tempLabel = new JLabel("Category N/A");
		tempLabel.setForeground(java.awt.Color.WHITE);
		frame.getContentPane().add(tempLabel);
		frame.getContentPane().setBackground(java.awt.Color.BLACK);
		// frame.getContentPane().add(new JLabel(image));
		int colorIndex = 1;
		for (int i = 0; i < allCategories.size(); i++) {
			// add the text label and color-code
			ArrayList temp = (ArrayList) allCategories.get(i);
			String title = (String) temp.get(0);
			tempLabel = new JLabel(title);
			tempLabel.setForeground(convertColor(i + 3));
			frame.getContentPane().add(tempLabel);
		}
		frame.pack();
		frame.setVisible(true);

	}

	/*
	 * 1: white 2: black 3: red 4: dark red 5: green 6: dark green 7: yellow 8:
	 * dark yellow 9: blue 10: dark blue 11: magenta 12: dark magenta 13: cyan
	 * 14: dark cyan
	 */
	private java.awt.Color convertColor(int swtColorIndex) {
		java.awt.Color awtColor;
		switch (swtColorIndex) {
		case 1:
			awtColor = java.awt.Color.WHITE;
			return awtColor;
		case 2:
			awtColor = java.awt.Color.BLACK;
			return awtColor;
		case 3:
			awtColor = java.awt.Color.RED;
			return awtColor;
		case 4:
			awtColor = (java.awt.Color.RED).darker();
			return awtColor;
		case 5:
			awtColor = java.awt.Color.GREEN;
			return awtColor;
		case 6:
			awtColor = (java.awt.Color.GREEN).darker();
			return awtColor;
		case 7:
			awtColor = java.awt.Color.yellow;
			return awtColor;
		case 8:
			awtColor = (java.awt.Color.yellow).darker();
			return awtColor;
		case 9:
			awtColor = java.awt.Color.BLUE;
			return awtColor;
		case 10:
			awtColor = (java.awt.Color.BLUE).darker();
			return awtColor;
		case 11:
			awtColor = java.awt.Color.MAGENTA;
			return awtColor;
		case 12:
			awtColor = (java.awt.Color.MAGENTA).darker();
			return awtColor;
		case 13:
			awtColor = java.awt.Color.CYAN;
			return awtColor;
		case 14:
			awtColor = (java.awt.Color.CYAN).darker();
			return awtColor;
		default:
			awtColor = java.awt.Color.white;
			return awtColor;
		}

	}

	public void loadDataFromResource(String path) {
		dataLoader = new ResourceLoader();
		dataLoader.setSourcePath(path);
		startLoad(dataLoader);
	}

	public void onFileLoadDictionary() {
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		String path;

		fd.setText("Open Dictionary");
		// restore the path
		fd.setFilterPath(Prefs.get(Prefs.DICTIONARY_PATH,
				Prefs.DICTIONARY_PATH_DEFAULT));
		this.datasetTrim();
		path = fd.open();
		if (path != null) {
			// save this location
			Prefs.set(Prefs.DICTIONARY_PATH, path);
			
			try {
				new DictionaryLoader(dataSet).load(path);
				// FIXME: clear the entity list here
				int count = 0;
				for (String term : dataSet.getGlobalDict().getSorted(
						Dictionary.SortMethod.FREQUENCY)) {
					if (count++ >= Prefs.get(Prefs.TERMS_PER_HEADER,
							Prefs.TERMS_PER_HEADER_DEFAULT))
						break;

					// add the term to the list
					gvOverview.getEntityView().addTerm(
							Consts.TERMS_HEADER_TEXT, term,
							dataSet.getGlobalDict().getTerm(term));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void onFileLoadGroup() {
		FileDialog fd = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
		String path;

		fd.setText("Open Group");

		// restore the path
		fd.setFilterPath(Prefs.get(Prefs.GROUP_PATH, Prefs.GROUP_PATH_DEFAULT));
		this.datasetTrim();
		path = fd.open();
		if (path != null) {
			// save this location
			Prefs.set(Prefs.GROUP_PATH, path);

			// chop off the filename from the path
			path = path.substring(0, path.lastIndexOf(File.separator));

			// go through all the files
			for (String filename : fd.getFileNames()) {
				try {
					GroupLoader gl = new GroupLoader();
					gl.load(path + File.separator + filename);

					// add the new tab
					GroupView gv = new GroupView(tfGroups, SWT.NONE);
					CTabItem item = new CTabItem(tfGroups, SWT.CLOSE);

					item.setText(gl.getGroup().getBaseName());
					item.setControl(gv);

					// do stuff with the group
					Group group = gl.getGroup();
					gv.setDataSet(dataSet);
					gv.setGroup(group);
					/*
					 * Color x = new Color(parentDisplay, 100, 0, 0); Color y =
					 * new Color(parentDisplay, 0,100,0); gv.setBackground(x);
					 * gv.setForeground(y); Display display =
					 * FLInterface.getDisplay();
					 * gv.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
					 * item.setControl(gv);
					 */
					Display display = FLInterface.getDisplay();
					org.eclipse.swt.graphics.Image image = new org.eclipse.swt.graphics.Image(
							display, 20, 20);
					GC gc = new GC(image);
					// gc.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
					// gc.fillRectangle(0, 0, 50, 50);
					// gc.dispose();
					// org.eclipse.swt.graphics.Image image = new
					// org.eclipse.swt.graphics.Image(display,
					// "/Users/andrey/Desktop/me.jpg");
					// gv.setBackgroundImage(image);

					// Find out which category group belongs to
					String dfl = "Category: N/A";
					int colorIndex = 1; // 1 is SWT.COLOR_WHITE (white for
										// unlabeled groups)
					if (!allCategories.isEmpty()) {
						int count = 0;
						int prev_count = 0;
						ArrayList local_terms = group.getTerms();
						ArrayList local_ents = group.getEntities();
						for (int i = 0; i < allCategories.size(); i++) {
							ArrayList temp = (ArrayList) allCategories.get(i);
							// title will be the first term in the file
							String title = (String) temp.get(0);

							for (int j = 0; j < temp.size(); j++) {
								if (local_terms.contains(temp.get(j))
										|| local_ents.contains(temp.get(j)))
									// count += weights.get(j);
									count++;
							}
							if (count > prev_count) {
								prev_count = count;
								dfl = title;
								colorIndex = i + 3;
							}
						}
					}

					// labels the groups with colors and tooltip (title of
					// category file)
					item.setToolTipText(dfl);
					gc.setBackground(display.getSystemColor(colorIndex));
					gc.fillRectangle(0, 0, 20, 20);
					gc.dispose();
					item.setImage(image);

					// show the new tab
					tfGroups.setSelection(item);
				} catch (Exception e) {
					MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING
							| SWT.OK);

					if (FLInterface.isMac()) {
						mb.setText("The group file \"" + filename
								+ "\" could not be loaded");
						mb.setMessage(e.getMessage());
					} else {
						mb.setText(Consts.PROJECT_NAME);
						mb.setMessage("The group file \"" + filename
								+ "\" could not be loaded: " + e.getMessage());
					}

					mb.open();
				}
			}
		}
	}

	public void onLoadComplete() {
		// store the data set
		dataSet = dataLoader.getDataSet();
		this.datasetTrim();
		
		// set up the overview
		gvOverview.setEnabled(true);
		gvOverview.setDataSet(dataSet);
		gvOverview.makeOverview();

		// enable the file->load dictionary menu
		menuBar.connect(menuBar.fileLoadDictionary, this,
				"onFileLoadDictionary");

		// enable the file->load group menu item
		menuBar.connect(menuBar.fileLoadGroup, this, "onFileLoadGroup");

		// kill the load data set menu option...loading another dataset would
		// get messy
		menuBar.disconnect(menuBar.fileLoadData);

		// enable the tools->create dictionary menu item
		menuBar.connect(menuBar.createUserDictionary, this,
				"onCreateUserDictionary");

		menuBar.connect(menuBar.resetDictionary, this, "onResetDictionary");
		
		oldDict = dataSet.getGlobalDict();
		
		isDataSetLoaded = true;

	}

	private void showUserDictionary(){
		EntityView ev = gvOverview.getEntityView();
		ev.clearAll();
		gvOverview.populateEV();
		for(String header : ev.getHeaders())
		{
			if(ev.getExpanded(header) == true)
			{
				ev.setExpanded(header, false);
				ev.setExpanded(header, true);
			}
		}
	}
	
	
	public void onResetDictionary() {
		this.datasetTrim();
		dataSet.setGlobalDict(oldDict);
		//menuBar.connect(menuBar.createUserDictionary, this, "onCreateUserDictionary");
		//menuBar.disconnect(menuBar.resetDictionary);	
		//flag = false;
		showUserDictionary();
	}

	public void onCreateUserDictionary() {
		//if(!flag){
		this.datasetTrim();
		CreateUserDict.createUserDictionary(dataSet, parentDisplay);
		showUserDictionary();
	}

	public void onToolsPreferences() {
		WinPreferences wp;

		// osx wants the preferences window separate from the main window...
		if (FLInterface.isMac())
			wp = new WinPreferences(this.parentDisplay);
		else
			wp = new WinPreferences(this.shell);

		wp.loadWindow();
	}

	public void onHelpAbout() {
		WinAbout wa;

		// make the window separate from the main window on osx
		if (FLInterface.isMac())
			wa = new WinAbout(this.parentDisplay);
		else
			wa = new WinAbout(this.shell);

		wa.loadWindow();
	}

	public void onHelpDemo() {
		// make the overview active
		tfGroups.setSelection(0);

		// do demo stuff
		Demo.start(this);
	}
	
	
	private void setupTabs() {
		Display display = FLInterface.getDisplay();

		// set up the shell
		shell.setLayout(new FillLayout());

		// set up the tab folder
		tfGroups = new CTabFolder(shell, SWT.NONE);
		CTabItem item = new CTabItem(tfGroups, SWT.NONE);
		tfGroups.setSimple(false);
		tfGroups.setMinimumCharacters(8);

		// show the close buttons all the time
		tfGroups.setUnselectedCloseVisible(true);

		item.setText("Overview");

		// set up the gradient
		tfGroups.setSelectionBackground(
				new Color[] { display.getSystemColor(SWT.COLOR_WHITE),
						display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND) },
				new int[] { 90 }, true);
		if (FLInterface.isMac())
			tfGroups.setBackground(display
					.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		else
			tfGroups.setBackground(display
					.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		// set the font
		FontData font[] = display.getSystemFont().getFontData();
		Font face = new Font(display, font[0]);
		tfGroups.setFont(face);

		// set the tab height...mac needs slightly smaller tab
		if (FLInterface.isMac())
			tfGroups.setTabHeight(font[0].getHeight() * 2);
		else
			tfGroups.setTabHeight(font[0].getHeight() * 3);

		gvOverview = new GroupView(tfGroups, SWT.NONE);

		// disable all the controls
		gvOverview.setEnabled(false);

		// add the form to the tab item
		item.setControl(gvOverview);
	}

	/**
	 * @param dl
	 */
	private void startLoad(final DataLoader dl) {
		final WinProgress wp = new WinProgress(shell);

		// load the window
		FLInterface.getDisplay().syncExec(new Runnable() {
			public void run() {
				wp.loadWindow();
			}
		});

		Thread thread = new Thread(new Runnable() {
			public void run() {
				// load the data
				try {
					// launch the monitoring thread
					dl.startOperation();
					wp.monitorDatasetProgress(dl, "Loading and Parsing data...");
					dl.load(dl.getSourcePath());
				} catch (Exception e) {
					dl.cancelOperation();

					if (FLInterface.isMac())
						messageBox(
								SWT.ICON_WARNING | SWT.OK,
								"The dataset in directory \""
										+ dl.getSourcePath()
										+ "\" could not be loaded",
								e.getMessage());
					else
						messageBox(
								SWT.ICON_WARNING | SWT.OK,
								Consts.PROJECT_NAME,
								"The dataset in directory \""
										+ dl.getSourcePath()
										+ "\" could not be loaded: "
										+ e.getMessage());
				}

				if (dl.isParsed() && dl.isLoaded()) {
					// kill the progress window
					wp.setProgress(100);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					FLInterface.getDisplay().syncExec(new Runnable() {
						public void run() {
							wp.destroyWindow();

							// return control to the main ui thread
							if (dl.isLoaded())
								onLoadComplete();
						}
					});
				}
			}

		});

		// kick off the load thread
		thread.start();
	}

	private void messageBox(final int style, final String title,
			final String text) {
		FLInterface.getDisplay().syncExec(new Runnable() {

			public void run() {
				MessageBox mb = new MessageBox(shell, style);

				mb.setText(title);
				mb.setMessage(text);

				mb.open();
			}
		});
	}
	//method to change Date Range
	public void datasetTrim()   {
			//Find location of Date file
		
		String path = FutureLens.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String winLoc="";
        try {
			winLoc = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        String name = winLoc + "Dates";
		File dir = new File(name);
		if (!dir.exists()) {
			dir.mkdir();
		}
		name = winLoc + "Dates/Dates.txt";
		if (!(new File(name).exists())) {
			name = winLoc + "Dates/Dates.txt";
		}
		File dateFile=new File(name);
		if(dateFile.exists()){
		String dateString="";
		//read the Date File
		try (Scanner s = new Scanner(dateFile).useDelimiter("\\Z")) {
			   dateString = s.next();
			   s.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//Split the string
			String dateFromString=dateString.substring(0,dateString.indexOf("|"));
			String dateToString=dateString.substring(dateString.indexOf("|")+1,dateString.length());
		//Format into Date variable
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
			Date dateFrom, dateTo;
			
			try {
				dateFrom = format.parse(dateFromString);
				dateTo= format.parse(dateToString);
				this.dataSet.trim(dateFrom, dateTo);
				System.out.println("Removing Dates Outside of The Range:");
				System.out.println(dateFrom);
				System.out.println(dateTo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		else {
			System.out.println("Date File not Found!");
		}
		
		
		return;
	}
}
