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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dictionary {
	private Map <String, Integer> dict;
	//private Map m;
	private ArrayList<String> sortCache = null;
	private SortMethod sortCacheMethod = null;

	public static enum SortMethod {
		ALPHABETICAL, FREQUENCY, LENGTH
	}

	// Overrides to make the hashing more efficient
	@Override
	public int hashCode() {
		final int prime = 92821;
		int result = 1;
		result = prime * result + ((dict == null) ? 0 : dict.hashCode());
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
		Dictionary other = (Dictionary) obj;
		if (dict == null) {
			if (other.dict != null)
				return false;
		} else if (!dict.equals(other.dict))
			return false;
		return true;
	}

	public Dictionary() {
		dict = Collections.synchronizedMap(new HashMap<String, Integer>());
	}

	public void addTerm(String term) {
		Integer value;
		synchronized (dict){
		if (dict.containsKey(term)) {
			value = dict.get(term);
			dict.put(term, ++value);
		} else {
			dict.put(term, new Integer(1));
			sortCache = null;
		}
		}
	}

	public void addTerm(String term, int value) {
		synchronized (dict){
		dict.put(term, new Integer(value));
		sortCache = null;
		}
	}

	public Integer getTerm(String term) {
		if (dict.get(term) != null)
			return (dict.get(term));

		return (0);
	}

	public ArrayList<String> getSorted(SortMethod method) {
		if (sortCache == null || !method.equals(sortCacheMethod)) {
			ArrayList<Entry<String, Integer>> hashentries = new ArrayList<Map.Entry<String, Integer>>(
					dict.entrySet());
			sortCache = new ArrayList<String>();

			switch (method) {
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

			for (Entry<String, Integer> key : hashentries)
				sortCache.add(key.getKey());
		}

		return (sortCache);
	}

	public SearchResults searchTerms(String pattern) {
		SearchResults terms = new SearchResults();

		// go through all the terms
		for (String term : dict.keySet())
			if (term.contains(pattern))
				terms.add(term, dict.get(term));

		return (terms);
	}

	public Set<Entry<String, Integer>> getSet() {
		return dict.entrySet();
	}
}
