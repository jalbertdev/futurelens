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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import edu.utk.cs.futurelens.FutureLens;
import edu.utk.cs.futurelens.data.DataSet;
import edu.utk.cs.futurelens.data.Loader;

public class DictionaryLoader implements Loader
{
	private final DataSet dataSet;
	
	public DictionaryLoader(DataSet ds)
	{
		dataSet = ds;
	}
	
	public void load(String path) throws java.io.IOException
	{
		// load the dictionary file
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line, term;
		
		Dictionary dict = dataSet.getGlobalDict();
		Dictionary newDict = new Dictionary();
		
		while((line = reader.readLine()) != null)
		{			
			// remove leading whitespace
			line = line.trim();
			
			// find the word
			term = line.split("\\s+", 2)[0].toLowerCase();
			
			if(term.length() > 0)
			{
				// find the word in the global term list
				if(dict.getTerm(term) != null)
					newDict.addTerm(term, dict.getTerm(term));
			}
		}
		
		// set the new dictionary
		dataSet.setGlobalDict(newDict);
	}

	public void cancelOperation() 
	{
		
	}

	public int getPercentComplete() 
	{
		return 0;
	}

	public boolean isOperationInProgress() 
	{
		return false;
	}

	public void startOperation() 
	{
		
	}
}
