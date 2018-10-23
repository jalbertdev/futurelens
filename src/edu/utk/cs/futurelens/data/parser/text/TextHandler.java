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

package edu.utk.cs.futurelens.data.parser.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import edu.utk.cs.futurelens.FutureLens;
import edu.utk.cs.futurelens.data.DataElement;
import edu.utk.cs.futurelens.data.DataSet;
import edu.utk.cs.futurelens.data.Stoplist;
import edu.utk.cs.futurelens.data.parser.Handler;
import edu.utk.cs.futurelens.ui.Prefs;

public class TextHandler implements Handler
{
	private DataSet dataSet;
	
	private DataElement document;
	private static ArrayList<String> commonWords = null;
	private String fileName;
	private String rawText;
	
	public TextHandler(DataSet parent, String fileName)
	{
		dataSet = parent;
		String path = FutureLens.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String winLoc="";
        try {
			winLoc = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        String name = winLoc + "Dates";
		File dir = new File(name);
		if (!dir.exists()) {
			dir.mkdir();
		}
		name = winLoc + "Dates/Dates.txt";
		if (!(new File(name).exists())) {
			name = winLoc + "Dates/Dates.txt";
		}
		File dateFile=new File(name);
		if(dateFile.exists()){
		String dateString="";
		//read the Date File
		try (Scanner s = new Scanner(dateFile).useDelimiter("\\Z")) {
			   dateString = s.next();
			   s.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//Split the string
			String dateFromString=dateString.substring(0,dateString.indexOf("|"));
			String dateToString=dateString.substring(dateString.indexOf("|")+1,dateString.length());
		//Format into Date variable
			DateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
			Date dateFrom, dateTo;
			System.out.println(dateToString);
			try {
				dateFrom = format.parse(dateFromString);
				dateTo= format.parse(dateToString);
				this.dataSet.trim(dateFrom, dateTo);
				System.out.println("Removing Dates Outside of The Range:");
				System.out.println(dateFrom);
				System.out.println(dateTo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		else {
			System.out.println("Date File not Found!");
		}
		
		// build the list of common words
		if(commonWords == null)
		{
			commonWords = new ArrayList<String>();
			commonWords = Stoplist.ReadStopList();
		}
		
		this.fileName = fileName;
	}
	
	public void endDocument()
	{		
		// remove excess white space and newlines from the raw text
		//mNewLines.reset(rawText);
		//rawText = mNewLines.replaceAll(" ");
		
		//mWhiteSpace.reset(rawText);
		//rawText = mWhiteSpace.replaceAll(" ");
		
		// store the raw text
		//document.setRawData(rawText);
		
		// add the document to the pile
		dataSet.addDocument(document);
	}

	public void characters(String text)
	{
		// store the raw text
		//rawText = text;
		
		// add terms to the global dictionary
		String[] terms = pTerms.split(text.toLowerCase());
		
		for(String term : terms)
		{			
			if(term.length() > 0)
			{
				String newTerm;
				// remove leading punctuation
				//mLeading.reset(term);
				//term = mLeading.replaceAll("");
				String[] temp;
				temp = (term.split("^[^\\d\\w]+",-1));
				newTerm = temp[temp.length-1];
				
				// remove trailing punctuation
				//mTrailing.reset(term);
				//term = mTrailing.replaceAll("");
				String finalTerm;
				temp = (newTerm.split("[^\\d\\w]+$",-1));
				finalTerm = temp[0];

				if (finalTerm.length() == 0)
				//if (term.length() == 0)
					continue;
				
				// ignore common words
				if (!commonWords.contains(finalTerm)) {
					//if (!commonWords.contains(term)) {
					
					//dataSet.addGlobalTerm(term);
					dataSet.addGlobalTerm(finalTerm);

						//document.addTerm(term);
					document.addTerm(finalTerm);
					
					// add this term to the table of term/documents
					//dataSet.addDocumentToTerm(document, term);
				}	
				
			}
		}
	}
	
	public void startDocument() 
	{
		// start a new document
		document = new DataElement(this.fileName);
	}
}
