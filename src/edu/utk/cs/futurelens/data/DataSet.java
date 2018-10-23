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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import edu.utk.cs.futurelens.data.dictionary.Dictionary;

/**
 * @author greg
 *
 */
public class DataSet
{
	// important stuff
	private Dictionary globalDict;
	private Hashtable<String, Dictionary> entityDict;
	private ArrayList<DataElement> documents;
	//private Hashtable<String, ArrayList<DataElement>> termList;

	// maximum size of unknown document set
	private final int unknownLength = 50;
	
	// cached stuff
	ArrayList<String> cachedEntities;
	ArrayList<String> cachedSortedEntities;
	ArrayList<DataElement> cachedSortedDocuments;
	
	// date ranges
	public enum DateRange
	{
		ONE_YEAR,
		NINE_MONTHS,
		SIX_MONTHS,
		THREE_MONTHS,
		ONE_MONTH,
		TWO_WEEKS,
		ONE_WEEK,
		ONE_DAY
	}
	
	public DataSet()
	{
		globalDict = new Dictionary();
		entityDict = new Hashtable<String, Dictionary>();
		documents = new ArrayList<DataElement>();
		//termList = new Hashtable<String, ArrayList<DataElement>>();
		
		// clear the cached stuff
		cachedEntities = null;
		cachedSortedEntities = null;
		cachedSortedDocuments = null;
	}
	
	//Overrides to make the hashing more efficient
	@Override
	public int hashCode() {
		final int prime = 92821;
		int result = 1;
		result = prime * result
				+ ((entityDict == null) ? 0 : entityDict.hashCode());
		//result = prime * result
			//	+ ((termList == null) ? 0 : termList.hashCode());
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
		DataSet other = (DataSet) obj;
		if (entityDict == null) {
			if (other.entityDict != null)
				return false;
		} else if (!entityDict.equals(other.entityDict))
			return false;
		/*if (termList == null) {
			if (other.termList != null)
				return false;
		} else if (!termList.equals(other.termList))
			return false;*/
		return true;
	}
	
	public Dictionary getGlobalDict()
	{
		return(globalDict);
	}
	
	public void setGlobalDict(Dictionary dict)
	{
		globalDict = dict;
	}
	
	public Dictionary getEntityDict(String entity)
	{
		return(entityDict.get(entity));
	}
	
	public ArrayList<String> getEntities()
	{
		if(cachedEntities == null)
		{
			cachedEntities = new ArrayList<String>();
		
			for(Enumeration<String> e = entityDict.keys(); e.hasMoreElements(); )
				cachedEntities.add(e.nextElement());
		}
		
		return(cachedEntities);
	}
	
	public ArrayList<String> getSortedEntities()
	{
		if(cachedSortedEntities == null)
		{
			cachedSortedEntities = getEntities();
		
			Collections.sort(cachedSortedEntities);
		}
		
		return(cachedSortedEntities);
	}
	
	public ArrayList<DataElement> getSortedDocumentsByDate()
	{
		if(cachedSortedDocuments == null)
		{
			cachedSortedDocuments = documents;
			Collections.sort(cachedSortedDocuments, new DataElementCmp());
		}

		return(cachedSortedDocuments);
	}
	
	public DataElement getDocumentByIndex(int index)
	{
		if(cachedSortedDocuments == null)
		{
			cachedSortedDocuments = documents;
			Collections.sort(cachedSortedDocuments, new DataElementCmp());
		}
		
		return(cachedSortedDocuments.get(index));
	}
	
	public ArrayList<DocumentSet> getDocumentsByDateRange(DateRange range)
	{	
		ArrayList<DocumentSet> sets = new ArrayList<DocumentSet>();;
		Date startDate, endDate, nextDate;
		Calendar cStartDate;
		int curRange = 0;
		
		// get the list of sorted documents
		if(cachedSortedDocuments == null)
			getSortedDocumentsByDate();
		
		// find the first date
		int index;
		int curIndex = 0;
		for(index = 0; index < cachedSortedDocuments.size(); index++)
			if(cachedSortedDocuments.get(index).getDate() != null)
				break;
		
		// throw in the unknown date documents
		if(index > 0)
		{
			DocumentSet ds = new DocumentSet(null, null, null);
			
			for(int i = 0; i < index; i++, curIndex++)
			{
				if(i > 0 && i % unknownLength == 0)
				{
					sets.add(ds);
					ds = new DocumentSet(null, null, null);
					curRange++;
					curIndex = 0;
				}
				
				DataElement de = cachedSortedDocuments.get(i);
				
				de.setDocumentRangeIndex(curRange);
				de.setDocumentIndex(curIndex);
				
				ds.add(de);
			}
			
			if(ds.getSize() > 0)
			{
				sets.add(ds);
				curRange++;
			}
		}
		
		// build the set if at least one document has a date
		if(index < cachedSortedDocuments.size())
		{		
			// get the first date
			Date firstDate = cachedSortedDocuments.get(index).getDate();
			Calendar cFirstDate = Calendar.getInstance();
			cFirstDate.setTime(firstDate);
			
			// clear out a new date
			cStartDate = Calendar.getInstance();
			cStartDate.clear();
			
			switch(range)
			{
			case ONE_DAY:
			case ONE_WEEK:
			case TWO_WEEKS:
				// set the start day
				cStartDate.set(Calendar.DAY_OF_MONTH, cFirstDate.get(Calendar.DAY_OF_MONTH));
			case ONE_MONTH:
			case THREE_MONTHS:
			case SIX_MONTHS:
			case NINE_MONTHS:
				// set the starting month
				cStartDate.set(Calendar.MONTH, cFirstDate.get(Calendar.MONTH));
			case ONE_YEAR:
				// set the starting year
				cStartDate.set(Calendar.YEAR, cFirstDate.get(Calendar.YEAR));
			}
			
			// store the adjusted start date
			startDate = cStartDate.getTime();
			
			// get the end date
			endDate = cachedSortedDocuments.get(cachedSortedDocuments.size() - 1).getDate();
			
			// keeps track of the current document index
			int curDoc = index;
			
			nextDate = startDate;
			while(nextDate.compareTo(endDate) <= 0)
			{
				// get the end of this span
				Date endSpan = getEndDate(nextDate, range);
				
				// create a new document set
				DocumentSet ds = new DocumentSet(nextDate, endSpan, range);
				
				// go through all the documents
				for(curIndex = 0; curDoc < cachedSortedDocuments.size(); curDoc++, curIndex++)
				{
					// get the dataelement
					DataElement de = cachedSortedDocuments.get(curDoc);
					
					de.setDocumentRangeIndex(curRange);
					de.setDocumentIndex(curIndex);
					
					if(de.getDate().compareTo(endSpan) > 0)
						// done with this set
						break;
					
					// add this document to the current document set
					ds.add(de);
				}
				
				// store the set if it has at least one document
				if(ds.getDocuments().size() > 0)
				{
					sets.add(ds);
					curRange++;
				}

				nextDate = incrementDate(nextDate, range);
			}
		}
		
		return(sets);
	}
	
	private Date getEndDate(Date date, DateRange inc)
	{
		Calendar cdate = Calendar.getInstance();
		
		// get the incremented date
		Date next = incrementDate(date, inc);
		
		cdate.setTime(next);
		cdate.add(Calendar.SECOND, -1);
		
		return(cdate.getTime());
	}
	
	private Date incrementDate(Date date, DateRange inc)
	{
		Calendar cdate = Calendar.getInstance();
		cdate.setTime(date);
		
		switch(inc)
		{
		case ONE_DAY:
			cdate.add(Calendar.DAY_OF_MONTH, 1);
			break;
		case ONE_WEEK:
			cdate.add(Calendar.WEEK_OF_MONTH, 1);
			break;
		case TWO_WEEKS:
			cdate.add(Calendar.WEEK_OF_MONTH, 2);
			break;
		case ONE_MONTH:
			cdate.add(Calendar.MONTH, 1);
			break;
		case THREE_MONTHS:
			cdate.add(Calendar.MONTH, 3);
			break;
		case SIX_MONTHS:
			cdate.add(Calendar.MONTH, 6);
			break;
		case NINE_MONTHS:
			cdate.add(Calendar.MONTH, 9);
			break;
		case ONE_YEAR:
			cdate.add(Calendar.YEAR, 1);
			break;
		}
		
		// return the date
		return(cdate.getTime());
	}
	
	public void addGlobalTerm(String term)
	{
		synchronized(globalDict){
		globalDict.addTerm(term);
		}
	}
	
	public void addEntity(String entity, String term)
	{
		synchronized(entityDict){
		// check if this entity is in the dictionary already
		if(! entityDict.containsKey(entity))
		{
			// it's not, add it
			entityDict.put(entity, new Dictionary());
			
			// clear the cache
			cachedEntities = null;
		}
		
		// dump the sorted entity cache
		cachedSortedEntities = null;
		
		// insert this term
		entityDict.get(entity).addTerm(term);
		}
	}
	
	public void addDocument(DataElement document)
	{
		synchronized(documents){
		documents.add(document);
		}
	}
	
	/*public void addDocumentToTerm(DataElement doc, String term)
	{
		String lTerm = term.toLowerCase();
		
		ArrayList<DataElement> list = termList.get(lTerm);
		
		if(list == null)
		{
			list = new ArrayList<DataElement>();
			termList.put(lTerm, list);
		}
		
		// only add this document once
		if(! list.contains(doc))
			list.add(doc);
	}*/
	
	// search through hash table (single term search)
	/*public ArrayList<DataElement> getDocumentsWithTerm(String term)
	{
		return(termList.get(term.toLowerCase()));
	}*/
	
	// phrase search function
	public ArrayList<DataElement> getDocumentsWithPhrase(String term)
	{
		/*ArrayList<DataElement> temp = getDocumentsWithTerm(term.toLowerCase());
		for(DataElement de1 : temp)
			System.out.println(de1.getFileName() + ":" + de1.getTerm(term));*/
		
		
		ArrayList<DataElement>allDocs = new ArrayList<DataElement>();
		//go through all the docs, search for phrase
		//System.out.println("NON-TERMLIST:");
		for(DataElement de : documents)
		{
			//System.out.println(de.getRawData());
			//String raw = de.getRawData();
			
			//added code to make search non-case-sensitive
			//String CapTerm = (term.length()>0)? Character.toUpperCase(term.charAt(0))+term.substring(1) : term;
			//if(raw.contains(term.toLowerCase()) || raw.contains(CapTerm) || raw.contains(term) || (raw.toLowerCase()).contains(term.toLowerCase()))
			//if((raw.toLowerCase()).contains(term.toLowerCase()))	
			if(de.checkTerm(term))
			//if((de).contains(term) || de.contains(CapTerm) || de.contains(term.toLowerCase()))
			{
				//System.out.println("term: "+term+"Doc: "+de);
				allDocs.add(de);
				//System.out.println(de.getFileName());
			}
		}

		return allDocs;
	
		
	}
	
	// NOTE: re-wrote for version 2.1.8 to make it handle capitalized terms within a chained collection
	public ArrayList<DataElement> getDocumentsWithMultipleTerms(ArrayList<String> terms)
	{
		ArrayList<DataElement>allDocs = new ArrayList<DataElement>();
		boolean notFound;

		//go through all the docs, search for phrase
		for(DataElement de : documents)
		{
			notFound = false;
			
			for(String term : terms)
			{
			//String raw = de.getRawData();
			
				//added code to make search non-case-sensitive
				//if(!(raw.toLowerCase()).contains(term.toLowerCase()))
				//if(!(de).contains(term.toLowerCase()))
				if(!(de.checkTerm(term)))
				{
					notFound = true;
					break;
				}
				
			}
			if(notFound == false)
				allDocs.add(de);
		
		}
		
		return allDocs;
		
	}
	
	// NOTE: the version below is faster, but I can't figure out how to make it handle
	// capitalized terms
	// search that is used for non-phrase chained terms
	/*public ArrayList<DataElement> getDocumentsWithMultipleTerms(ArrayList<String> terms)
	{
		ArrayList<DataElement>allDocs = new ArrayList<DataElement>();
		String firstTerm;
		boolean notFound;
		
		if(terms.size() <= 0)
			throw new IllegalArgumentException("Need more terms");
		
		firstTerm = terms.get(0).toLowerCase();
		
		// make sure there is at least 1 document with this term		
		if(termList.get(firstTerm) == null)
		{
			return(null);
		}
		
		// don't want this to be case-sensitive
		//firstTerm = (firstTerm.length()>0)? Character.toUpperCase(firstTerm.charAt(0))+firstTerm.substring(1) : firstTerm;
		//if(termList.get(firstTerm)==null)
		//{
		//	return(null);
		//}
		
		// make single term search fast
		if(terms.size() == 1)
		{
			return(termList.get(firstTerm));
		}
		
		// go through each document that contains the first term and ignore any missing a term
		for(DataElement de : termList.get(firstTerm))
		{
			notFound = false;
			
			for(String term : terms)
			{
				if(! de.contains(term.toLowerCase()))
				{
					//added code to check make search non-case-sensitive
					//term = (term.length()>0)? Character.toUpperCase(term.charAt(0))+term.substring(1) : term;
					
					//if(! de.contains(term))
					//{
					//	notFound = true;
					//	break;
					//}
					notFound = true;
					break;
				}
				
			}
			
			if(notFound == false)
				allDocs.add(de);
		}
		
	// now try capitalizing the first letter of first term, see what happens...
	firstTerm = (firstTerm.length()>0)? Character.toUpperCase(firstTerm.charAt(0))+firstTerm.substring(1) : firstTerm;
	if(termList.get(firstTerm) != null)
	{

		for(DataElement de : termList.get(firstTerm))
		{
			notFound = false;
			
			for(String term : terms)
			{
				System.out.println("term is: " + term);
				if(! de.contains(term.toLowerCase()))
				{
					//added code to check make search non-case-sensitive
					term = (term.length()>0)? Character.toUpperCase(term.charAt(0))+term.substring(1) : term;
					System.out.println("inner conditional, term is: " + term);

					if(! de.contains(term))
					{
						notFound = true;
						break;
					}
					//notFound = true;
					//break;
				}
				
			}
			
			if(notFound == false)
				allDocs.add(de);
		}
	}
		
		return(allDocs);
	}*/
}
