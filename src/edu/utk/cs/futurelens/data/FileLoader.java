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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import edu.utk.cs.futurelens.data.parser.ParseException;
import edu.utk.cs.futurelens.data.parser.sgml.SGMLException;
import edu.utk.cs.futurelens.data.parser.sgml.SGMLHandler;
import edu.utk.cs.futurelens.data.parser.sgml.SGMLParser;
import edu.utk.cs.futurelens.data.parser.text.TextException;
import edu.utk.cs.futurelens.data.parser.text.TextHandler;
import edu.utk.cs.futurelens.data.parser.text.TextParser;

/**
 * @author Greg
 * @author Andrey
 * @author Joshua
 */
public class FileLoader implements DataLoader {
	private final String extension;
	private volatile int numFiles;
	private volatile int numFilesHandled;
	private volatile boolean isOperationInProgress;
	private volatile boolean isLoaded;
	private volatile boolean isParsed;
	private final int NUM_THREAD = Runtime.getRuntime().availableProcessors() * 2; // Play With this number to try and find
										// the best performance
										// Runtime.getRuntime().availableProcessors()

	// path of source files
	private String sourcePath;
	private static String sPath;
	// list of source file names
	private String[] sourceFiles;
	// file data
	// private Hashtable<String, String> sourceData;

	// the newly minted data set
	private DataSet dataSet;
	
	public DataSet getDataSet() {
		// only return if the set has been loaded and parsed
		if (isLoaded && isParsed)
			return dataSet;

		return (null);
	}

	public static String getPath() {
		return sPath;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	public boolean isOperationInProgress() {
		return isOperationInProgress;
	}

	public boolean isParsed() {
		return isParsed;
	}

	public int getPercentComplete() {
		return ((int) (numFilesHandled / (float) numFiles * 100.0));
	}

	public int getNumFiles() {
		return numFiles;
	}

	public int getNumFilesHandled() {
		return numFilesHandled;
	}

	public void startOperation() {
		this.isOperationInProgress = true;
	}

	public void cancelOperation() {
		this.isOperationInProgress = false;
	}

	public FileLoader(String extension) {
		this.extension = extension;
		isLoaded = false;
	}

	public synchronized void setNumFilesHandeled() {
		numFilesHandled++;
	}

	@SuppressWarnings("unchecked")
	public void load(String path) throws java.io.IOException {

		// get the list of files
		File dir;
		// build up the filter
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				// ignore hidden files
				if (name.startsWith("."))
					return false;
				else if (!name.endsWith(extension))
					return false;
				return true;
			}
		};

		dir = new File(path);

		// get all the files
		sourceFiles = dir.list(filter);
		sPath = sourcePath = path;

		if (sourceFiles == null || sourceFiles.length == 0) {
			// uh oh
			throw new IOException("No readable files with extension \""
					+ extension + "\" were found");
		}

		// set the number of files handled/left
		numFiles = sourceFiles.length;
		numFilesHandled = 0;

		dataSet = new DataSet();


		ExecutorService execSvc = Executors.newFixedThreadPool(NUM_THREAD);
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>(numFiles);
		for (int i = 0; i < numFiles; i++) {
			tasks.add(Executors.callable(new Parsetask(this, sourceFiles[i])));
		}

		try {
			List<Future<Object>> done = execSvc.invokeAll(tasks);

		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		execSvc.shutdown();
		while (!execSvc.isTerminated()) {

		}

		System.out.println("Number of Files Handled:" + numFilesHandled
				+ " Number of Files:" + numFiles);

		if (numFilesHandled == numFiles) {
			isLoaded = true;
			isParsed = true;
		}

		isOperationInProgress = false;
	}

	public void parse(String filedata, String filename) throws ParseException {
		//System.out.println(filename);
		setNumFilesHandeled();
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

	public void parse() throws ParseException {
		/*
		 * String filedata; SGMLParser sgmlParser = new SGMLParser(); TextParser
		 * textParser = new TextParser();
		 * 
		 * if(isLoaded == false) return;
		 * 
		 * // use lowercase everything sgmlParser.setForceLowerCase(true);
		 * 
		 * // create a new dataset dataSet = new DataSet();
		 * 
		 * numFilesHandled = 0;
		 * 
		 * isOperationInProgress = true;
		 * 
		 * for(String filename : sourceData.keySet()) { // make sure this hasn't
		 * been canceled if(! isOperationInProgress) break;
		 * 
		 * filedata = sourceData.get(filename);
		 * 
		 * // parse the data as sgml try { sgmlParser.parse(filedata, new
		 * SGMLHandler(dataSet, filename)); } catch(SGMLException se) { // that
		 * didn't work...default to text try { textParser.parse(filedata, new
		 * TextHandler(dataSet, filename)); } catch(TextException te) { // this
		 * is really bad throw new ParseException("Parsing failed"); } }
		 * 
		 * numFilesHandled++; }
		 * 
		 * if(numFilesHandled == numFiles) isParsed = true;
		 * 
		 * isOperationInProgress = false;
		 */
	}

	/*private static String readFileToString(String path)
			throws java.io.IOException {
		File file = new File(path);
		long length = file.length();
		// RandomAccessFile raf = new RandomAccessFile(path, "r");
		// long length = raf.length();

		StringBuilder fileData = new StringBuilder((int) length);
		BufferedReader reader = new BufferedReader(new FileReader(path));
		char[] buf = new char[(int) length];
		// byte[] buf = new byte[(int) length];
		int bytesRead;

		
		  raf = new RandomAccessFile(path, "r"); raf.read(buf); raf.close();
		  String s = new String(buf); return s;
		 

		// while((bytesRead = reader.read(buf)) != -1)
		reader.read(buf);
		fileData.append(buf, 0, (int) length);

		reader.close();
		return fileData.toString();
	}*/
}
