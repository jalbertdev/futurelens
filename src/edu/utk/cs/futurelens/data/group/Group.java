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

package edu.utk.cs.futurelens.data.group;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

public class Group 
{
	private Hashtable<String, Double> entities;
	private Hashtable<String, Double> terms;
	
	private String groupName;
	private String baseName;

	//Override to try and improve the efficient of hashing
	@Override
	public int hashCode() {
		final int prime = 92821;
		int result = 1;
		result = prime * result
				+ ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((terms == null) ? 0 : terms.hashCode());
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
		Group other = (Group) obj;
		if (entities == null) {
			if (other.entities != null)
				return false;
		} else if (!entities.equals(other.entities))
			return false;
		if (terms == null) {
			if (other.terms != null)
				return false;
		} else if (!terms.equals(other.terms))
			return false;
		return true;
	}

	private class ElementCmp implements Comparator<Map.Entry<String, Double>>
	{
		public int compare(Entry<String, Double> o1, Entry<String, Double> o2) 
		{
			if(o1.getValue().compareTo(o2.getValue()) == 0)
				// sort alphabetically
				return(o1.getKey().compareTo(o2.getKey()));
				
			return o2.getValue().compareTo(o1.getValue());
		}
	}
	
	public void setNameFromPath(String path)
	{
		// get the filename
		if(path.lastIndexOf(File.separatorChar) >= 0)
			groupName = path.substring(path.lastIndexOf(File.separatorChar) + 1);
		else
			groupName = path;
		
		// figure out the base name (filename without extension)
		if(groupName.lastIndexOf('.') >= 0)
			baseName = groupName.substring(0, groupName.lastIndexOf('.'));
		else
			baseName = groupName;
	}
	
	public String getName()
	{
		return(groupName);
	}
	
	public String getBaseName()
	{
		return(baseName);
	}
	
	public Group()
	{
		entities = new Hashtable<String, Double>();
		terms = new Hashtable<String, Double>();
	}
	
	public void addEntity(String entity, double score)
	{
		entities.put(entity, score);
	}
	
	public void addTerm(String term, double score)
	{
		terms.put(term, score);
	}
	
	public double getTermScore(String term)
	{
		if(terms.get(term) == null)
			return(0);
		
		return(terms.get(term));
	}
	
	public double getEntityScore(String entity)
	{
		if(entities.get(entity) == null)
			return(0);
		
		return(entities.get(entity));
	}
	
	public ArrayList<String> getEntities()
	{
		ArrayList<String> allEntities = new ArrayList<String>();
		
		for(String key : entities.keySet())
			allEntities.add(key);
		
		return(allEntities);
	}
	
	public ArrayList<String> getSortedEntities()
	{
		ArrayList<String> allEntities = new ArrayList<String>();
		ArrayList<Entry<String, Double>> entityKeys = new ArrayList<Map.Entry<String, Double>>(entities.entrySet());
		
		Collections.sort(entityKeys, new ElementCmp());
		
		for(Map.Entry<String, Double> entity : entityKeys)
			allEntities.add(entity.getKey());
		
		return(allEntities);
	}
	
	public ArrayList<String> getSortedTerms()
	{
		ArrayList<String> allTerms = new ArrayList<String>();
		ArrayList<Entry<String, Double>> termKeys = new ArrayList<Map.Entry<String, Double>>(terms.entrySet());
		
		Collections.sort(termKeys, new ElementCmp());
		
		for(Map.Entry<String, Double> entity : termKeys)
			allTerms.add(entity.getKey());
		
		return(allTerms);
	}
	
	public ArrayList<String> getTerms()
	{
		ArrayList<String> allTerms = new ArrayList<String>();
		
		for(String key : terms.keySet())
			allTerms.add(key);
		
		return(allTerms);
	}
}
