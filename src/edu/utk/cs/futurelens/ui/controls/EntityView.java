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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

import edu.utk.cs.futurelens.ui.events.EntityAddedListener;
import edu.utk.cs.futurelens.ui.events.EntityPageListener;

/**
 * @author greg
 *
 */
public interface EntityView
{	
	public void addEntityAddedListener(EntityAddedListener listener);
	public void addEntityPageListener(EntityPageListener listener);
	
	public void addHeader(String header);
	public void addHeader(String header, String display);
	public void addTerm(String header, String term, int count);
	public void addTerm(String header, String term, double score);
	public void addGroupTerm(String header, String term, double score);
	public void clear(String header);
	public void clearAll();
	public ArrayList<String> getHeaders();
	public boolean getExpanded(String header);
	public void setExpanded(String header, boolean expanded);
	
	public boolean getVisible();
	public void setVisible(boolean visible);
	public Object getLayoutData();
	
	public Point computeSize(int wHint, int hHint, boolean changed);
	public void dispose();
	public void setBackground(Color color);
	public void setLayoutData(Object layoutData);
	
	public Point getTermButtonXY(String header, int index);
	
	public void setShowPageButtons(boolean showPageButtons);
	public int getPageNumber(String header);
	public void setPageNumber(String header, int page);
}
