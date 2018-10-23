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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import edu.utk.cs.futurelens.data.parser.ParseException;
import edu.utk.cs.futurelens.data.parser.sgml.SGMLException;
import edu.utk.cs.futurelens.data.parser.sgml.SGMLHandler;
import edu.utk.cs.futurelens.data.parser.sgml.SGMLParser;
import edu.utk.cs.futurelens.data.parser.text.TextException;
import edu.utk.cs.futurelens.data.parser.text.TextHandler;
import edu.utk.cs.futurelens.data.parser.text.TextParser;

public class CategoriesLoader implements DataLoader 
{
	private volatile int numFiles;
	private volatile int numFilesHandled;
	private volatile boolean isOperationInProgress;
	private volatile boolean isLoaded;
	private volatile boolean isParsed;

	// path of source files
	private String sourcePath;
	
	// list of source file names
	private String[] sourceFiles;
	
	// file data
	//private Hashtable<String, String> sourceData;
	
	// the newly minted data set
	private DataSet dataSet;

	public DataSet getDataSet() 
	{
		// only return if the set has been loaded and parsed
		if(isLoaded && isParsed)
			return dataSet;
		
		return(null);
	}
	
	public String getSourcePath() 
	{
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) 
	{
		this.sourcePath = sourcePath;
	}
	
	public boolean isLoaded() 
	{
		return isLoaded;
	}
	
	public boolean isOperationInProgress() 
	{
		return isOperationInProgress;
	}

	public boolean isParsed() 
	{
		return isParsed;
	}
	
	public int getPercentComplete()
	{
		return((int)(numFilesHandled / (float)numFiles * 100.0));
	}
	
	public int getNumFiles() 
	{
		return numFiles;
	}

	public int getNumFilesHandled() 
	{
		return numFilesHandled;
	}

	public void startOperation()
	{
		this.isOperationInProgress = true;
	}
	
	public void cancelOperation()
	{
		this.isOperationInProgress = false;
	}
	
	public CategoriesLoader()
	{
		isLoaded = false;
	}

	public void load(String path) throws java.io.IOException
	{
		// store the path
		sourcePath = path;
		
		// get the list of files
		InputStream is = getClass().getResourceAsStream(path + "/manifest.txt");
		String fileList = readInputToString(is);
		sourceFiles = fileList.split("\n");
		
		if(sourceFiles.length <= 1)
		{
			// uh oh
			throw new IOException("No readable files were found in resource " + path);
		}
		
		// set the number of files handled/left
		numFiles = sourceFiles.length;
		numFilesHandled = 0;
		
		// create the hash table
		//sourceData = new Hashtable<String, String>();
		
		for(String filename : sourceFiles)
		{
			// check if the operation has been canceled 
			if(! isOperationInProgress)
				break;
			
			// load in the next file
			InputStream isFile = getClass().getResourceAsStream(path + "/" + filename);
			try {
				parse(readInputToString(isFile), filename);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isFile.close();
			
			numFilesHandled++;
		}
		
		is.close();
		
		if(numFilesHandled == numFiles)
			isLoaded = true;
		
		isOperationInProgress = false;
	}
	
	public void parse(String filedata, String filename) throws ParseException {
		//System.out.println(filename);
		SGMLParser sgmlParser = new SGMLParser();
		TextParser textParser = new TextParser();

		sgmlParser.setForceLowerCase(true);
		try {
			sgmlParser.parse(filedata, new SGMLHandler(dataSet, filename));
		} catch (SGMLException se) {
			// that didn't work...default to text
			try {
				textParser.parse(filedata, new TextHandler(dataSet, filename));
			} catch (TextException te) {
				// this is really bad
				throw new ParseException("Parsing failed");
			}
		}
	}
	
	public void parse() throws ParseException
	{
	/*	String filedata;
		SGMLParser sgmlParser = new SGMLParser();
		TextParser textParser = new TextParser();
		
		if(isLoaded == false)
			return;
		
		// use lowercase everything
		sgmlParser.setForceLowerCase(true);
		
		// create a new dataset
		dataSet = new DataSet();
		
		numFilesHandled = 0;
		
		isOperationInProgress = true;
		
		for(String filename : sourceData.keySet())
		{
			// make sure this hasn't been canceled
			if(! isOperationInProgress)
				break;
			
			filedata = sourceData.get(filename);
			
			// parse the data as sgml
			try {
				sgmlParser.parse(filedata, new SGMLHandler(dataSet, filename));
			} catch(SGMLException se) {
				// that didn't work...default to text
				try {
					textParser.parse(filedata, new TextHandler(dataSet, filename));
				} catch(TextException te) {
					// this is really bad
					throw new ParseException("Parsing failed");
				}
			}
			
			numFilesHandled++;
		}
		
		if(numFilesHandled == numFiles)
			isParsed = true;
		
		isOperationInProgress = false;*/
	}
	
	private static String readInputToString(InputStream stream) throws IOException
	{
		StringBuilder fileData = new StringBuilder(1024);
		InputStreamReader reader = new InputStreamReader(stream);
		char buf[] = new char[1024];
		int bytesRead;
		
		while((bytesRead = reader.read(buf)) != -1)
			fileData.append(buf, 0, bytesRead);
		
		reader.close();
		
		return fileData.toString();
	}
}
