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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import edu.utk.cs.futurelens.data.dictionary.Dictionary.SortMethod;

public class SearchResults 
{
	private Hashtable<String, Integer> results;
	
	//Overrides to make the hashing more efficient
	@Override
	public int hashCode() {
		final int prime = 92821;
		int result = 1;
		result = prime * result + ((results == null) ? 0 : results.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchResults other = (SearchResults) obj;
		if (results == null) {
			if (other.results != null)
				return false;
		} else if (!results.equals(other.results))
			return false;
		return true;
	}

	public SearchResults()
	{
		results = new Hashtable<String, Integer>();
	}
	
	public void add(String result, int value)
	{
		results.put(result, value);
	}
	
	public int get(String result)
	{
		if(results.get(result) == null)
			return(-1);
		
		return(results.get(result));
	}
	
	public ArrayList<String> sort(SortMethod method)
	{
		ArrayList<Entry<String, Integer>> hashentries = new ArrayList<Map.Entry<String, Integer>>(results.entrySet());
		ArrayList<String> entries = new ArrayList<String>();
		
		switch(method)
		{
		case ALPHABETICAL:
			Collections.sort(hashentries, new LexicographicCmp());
			break;
		case FREQUENCY:
			Collections.sort(hashentries, new FrequencyCmp());
			break;
		case LENGTH:
			Collections.sort(hashentries, new LengthCmp());
			break;
		default:
			throw new IllegalArgumentException("Unknown sort method");
		}
		
		for(Entry<String, Integer> key : hashentries)
			entries.add(key.getKey());
		
		return(entries);
	}
	public boolean isEmpty() {
		return results.isEmpty();
	}
}
