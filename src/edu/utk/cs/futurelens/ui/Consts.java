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

import org.eclipse.swt.graphics.RGB;

/**
 * @author greg
 *
 */
public final class Consts {
	
	// used for the main window
	public static final String PROJECT_NAME = "FutureLens";
	
	// entity header on linux/windows
	public static final int ENTITY_MARGIN_WIDTH = 10;
	
	// left pane color for osx
	public static final RGB WINDOW_LEFTPANE_BACKGROUND = new RGB(212, 221, 229);
	
	// legend set height
	public static final int WINDOW_LEGENDSET_HEIGHT = 80;
	
	// terms header text...this should probably be put somewhere else
	public static final String TERMS_HEADER_TEXT = "Terms";
	
	// entities header text
	public static final String ENTITIES_HEADER_TEXT = "Entities";
	
	// search results header text
	public static final String SEARCH_RESULTS_HEADER_TEXT = "Search results";
	
	// default search box text
	public static final String SEARCH_BOX_TEXT = "Search";
	
	// progress window
	public static final int WINDOW_PROGRESS_WIDTH = 320;
	public static final int WINDOW_PROGRESS_HEIGHT = 110;

	// preference window
	public static final int PREFERENCE_WIDTH = 500;
	public static final int PREFERENCE_HEIGHT = 320;
	
	public static final int PREFERENCE_TAB_MARGIN_WIDTH = 12;
	public static final int PREFERENCE_TAB_MARGIN_HEIGHT = 12;
	
	// about window
	public static final int ABOUT_WIDTH = 400;
	public static final int ABOUT_HEIGHT = 200;
}
