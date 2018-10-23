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

package edu.utk.cs.futurelens.data;

import java.util.Comparator;

public class DataElementCmp implements Comparator<DataElement>
{
	public int compare(DataElement o1, DataElement o2) 
	{
		if(o1.getDate() == null && o2.getDate() == null)
			return(0);
		
		if(o1.getDate() == null)
			return(-1);
		
		if(o2.getDate() == null)
			return(1);
		
		return o1.getDate().compareTo(o2.getDate());
	}
}
