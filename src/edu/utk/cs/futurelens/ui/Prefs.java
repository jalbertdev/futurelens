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

import java.util.prefs.*;

import edu.utk.cs.futurelens.FutureLens;

/**
 * @author greg
 *
 */
public class Prefs 
{
	// preference keys and defaults
	//  window stuff
	public static final String 	WINDOW_HEIGHT 	= "window/height";
	public static final int 	WINDOW_HEIGHT_DEFAULT = 600;
	
	public static final String	WINDOW_WIDTH 	= "window/width";
	public static final int		WINDOW_WIDTH_DEFAULT = 800;
	
	public static final String 	WINDOW_X		= "window/x";
	public static final int		WINDOW_X_DEFAULT = -1;
	
	public static final String	WINDOW_Y		= "window/y";
	public static final int 	WINDOW_Y_DEFAULT = -1;
	
	public static final String 	WINDOW_IS_MAXIMIZED = "window/isMaximized";
	public static final boolean WINDOW_IS_MAXIMIZED_DEFAULT = false;
	
	//  paths
	public static final String	DATASET_PATH	= "dataset/path";
	public static final String	DATASET_PATH_DEFAULT = null;
	
	public static final String	DATASET_EXT		= "dataset/ext";
	public static final String	DATASET_EXT_DEFAULT = "";
	
	public static final String	DATASET_IGNORE	= "dataset/ignore";
	public static final String	DATASET_IGNORE_DEFAULT = "The, Of, And, To, A, In, That, For, Is, On, Are, " +
															"With, As, It, From, At, Be, Was, Have, By, They, Has, " + 
															"But, Not, Or, An, 00:00:00, Mon, Tue, Wed, Thu, Fri, Sat, Sun, " +
															"Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec";
	public static final String	DICTIONARY_PATH	= "dictionary/path";
	public static final String	DICTIONARY_PATH_DEFAULT = null;
	
	public static final String	GROUP_PATH		= "group/path";
	public static final String 	GROUP_PATH_DEFAULT = null;
	
	// interface stuff
	public static final String	TERMS_PER_HEADER = "ui/termsPerHeader";
	public static final int		TERMS_PER_HEADER_DEFAULT = 30;
	
	// sort method
	public static final String	SORT_METHOD = "ui/sortMethod";
	public static final String	SORT_METHOD_DEFAULT = "alphabetical";
	
	// preference store
	private static final Preferences prefs = Preferences.userNodeForPackage(FutureLens.class);
	
	// force non-instantiability
	private Prefs()
	{
	}
	
	public static String get(String key, String defaultVal)
	{	
		return(prefs.get(key, defaultVal));
	}
	public static String getDefaultIgnoredDataSet()
	{	
		return DATASET_IGNORE_DEFAULT;
	}
	
	public static int get(String key, int defaultVal)
	{
		return(prefs.getInt(key, defaultVal));
	}
	
	public static boolean get(String key, boolean defaultVal)
	{
		return(prefs.getBoolean(key, defaultVal));
	}
	
	public static void set(String key, String value)
	{
		prefs.put(key, value);
	}
	
	public static void set(String key, int value)
	{
		prefs.putInt(key, value);
	}
	
	public static void set(String key, boolean value)
	{
		prefs.putBoolean(key, value);
	}
}
