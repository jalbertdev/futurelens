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

package edu.utk.cs.futurelens.data.dictionary;

import java.util.Comparator;
import java.util.Map;

public class LengthCmp implements Comparator<Map.Entry<String, Integer>>
{

	public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) 
	{
		String key1 = o1.getKey();
		String key2 = o2.getKey();
		
		if(key1.length() < key2.length())
			return(1);
		else if(key1.length() > key2.length())
			return(-1);
		
		// compare alphabetically if lengths are equal
		return(key1.compareToIgnoreCase(key2));
	}

}
