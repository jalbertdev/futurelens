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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.utk.cs.futurelens.data.DataSet.DateRange;

public class DocumentSet 
{
	private Date startDate, endDate;
	private DateRange range;
	private ArrayList<DataElement> documents;
	
	private boolean isDateUnknown = false;
	
	public DocumentSet(Date startDate, Date endDate, DateRange range)
	{
		this.startDate = startDate;
		this.endDate = endDate;
		this.range = range;
		
		if(this.startDate == null || this.endDate == null || this.range == null)
			isDateUnknown = true;
		
		documents = new ArrayList<DataElement>();
	}
	
	public boolean isDateUnknown()
	{
		return(isDateUnknown);
	}
	
	public void add(DataElement dataElement)
	{
		documents.add(dataElement);
	}
	
	public ArrayList<DataElement> getDocuments()
	{
		return(documents);
	}
	
	public int getSize()
	{
		return(documents.size());
	}
	
	public String getTitle()
	{
		Calendar cdate;
		String title = "";
		
		if(this.startDate == null || this.endDate == null || this.range == null)
			return("Unknown date");
		
		cdate = Calendar.getInstance();
		cdate.setTime(startDate);
		
		// add the month first
		if(range != DateRange.ONE_YEAR)
			title += getMonthName(cdate.get(Calendar.MONTH));
		
		switch(range)
		{		
		case ONE_DAY:
		case ONE_WEEK:
		case TWO_WEEKS:
			// add on the day
			title += " " + cdate.get(Calendar.DAY_OF_MONTH);
			break;
		}
		
		// put a slash in if preceded by day or month
		if(range != DateRange.ONE_YEAR)
			title += ", ";
		
		// tack on the year
		title += cdate.get(Calendar.YEAR);
		
		return(title);
	}
	
	private String getMonthName(int month)
	{
		switch(month)
		{
		case Calendar.JANUARY:
			return("Jan");
		case Calendar.FEBRUARY:
			return("Feb");
		case Calendar.MARCH:
			return("Mar");
		case Calendar.APRIL:
			return("Apr");
		case Calendar.MAY:
			return("May");
		case Calendar.JUNE:
			return("Jun");
		case Calendar.JULY:
			return("Jul");
		case Calendar.AUGUST:
			return("Aug");
		case Calendar.SEPTEMBER:
			return("Sep");
		case Calendar.OCTOBER:
			return("Oct");
		case Calendar.NOVEMBER:
			return("Nov");
		case Calendar.DECEMBER:
			return("Dec");
		}
		
		return("Unk");
	}
}
