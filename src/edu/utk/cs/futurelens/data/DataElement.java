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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import edu.utk.cs.futurelens.data.dictionary.Dictionary;

public class DataElement 
{
	private String rawData;
	private String fileName;
	private Date docDate = null;
	private Dictionary terms;
	private int documentRangeIndex;
	private int documentIndex;
	
	public boolean checkTerm(String term)
	{
		Iterator<Entry<String, Integer>> it = terms.getSet().iterator();
		while(it.hasNext()){
			Entry<String, Integer> s = it.next();
				if((s.getKey().toLowerCase()).contains(term.toLowerCase())){
					return true;
				}
		}
		return false;
	}
	
	public int getDocumentIndex() 
	{
		return documentIndex;
	}

	public void setDocumentIndex(int documentIndex) 
	{
		this.documentIndex = documentIndex;
	}

	public int getDocumentRangeIndex() 
	{
		return documentRangeIndex;
	}

	public void setDocumentRangeIndex(int documentRangeIndex) 
	{
		this.documentRangeIndex = documentRangeIndex;
	}

	public DataElement(String fileName)
	{
		this.fileName = fileName;
		terms = new Dictionary();
	}
	
	public String getFileName()
	{
		return(this.fileName);
	}
	
	public void setRawData(String rawdata)
	{
		this.rawData = rawdata;
	}
	
	public String getRawData()
	{
		return(this.rawData);
	}
	
	public void setDate(String date) throws ParseException
	{
		docDate = DateFormat.getDateInstance().parse(date);
	}
	
	public Date getDate()
	{
		return(docDate);
	}
	
	public void addTerm(String term)
	{
		//terms.addTerm(term);
		terms.addTerm(term.toLowerCase());

	}
	
	public int getTerm(String term)
	{
		return(terms.getTerm(term.toLowerCase()));
	}
	
	public boolean contains(String term)
	{
		if(getTerm(term) != 0)
			return(true);
		
		return(false);
	}
}
