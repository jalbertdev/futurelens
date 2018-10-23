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

package edu.utk.cs.futurelens.data.parser.sgml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import edu.utk.cs.futurelens.data.DataElement;
import edu.utk.cs.futurelens.data.DataSet;
import edu.utk.cs.futurelens.data.Stoplist;
import edu.utk.cs.futurelens.data.parser.Handler;

public class SGMLHandler implements Handler {
	private DataSet dataSet;

	private String entityTag, entityName, entityValue;
	private String dateText;
	private String fileName;
	private StringBuilder rawText;
	private DataElement document;
	private boolean inEntity;
	private boolean inDateTag, foundDate, foundYear;
	private static ArrayList<String> commonWords = null;
	//private static List <String> list;


	public SGMLHandler(DataSet parent, String fileName) {
		dataSet = parent;
		entityTag = entityName = entityValue = null;
		inDateTag = false;
		foundDate = foundYear = false;
		//rawText = new StringBuilder();

		// build the list of common words to ignore
		if (commonWords == null) {
			commonWords = new ArrayList<String>();
			commonWords = Stoplist.ReadStopList();
			//list = Collections.synchronizedList(commonWords);
		}

		this.fileName = fileName;
	}

	public void characters(String text) {
		// store the entity
		if (inEntity)
			entityValue = text;

		// store the date
		if (inDateTag) {
			// the date comes first
			if (foundDate == false) {
				String upperText = text.toUpperCase();

				foundDate = true;

				// date portion of the tag
				// cut off the day part
				if (upperText.startsWith("MON") || upperText.startsWith("TUE")
						|| upperText.startsWith("WED")
						|| upperText.startsWith("THU")
						|| upperText.startsWith("FRI")
						|| upperText.startsWith("SAT")
						|| upperText.startsWith("SUN"))
					dateText = text.split("\\s", 2)[1];
				else
					dateText = text;

			} else {
				foundYear = true;

				// year portion
				try {
					document.setDate(dateText + ", " + text);
				} catch (Exception e) {
					System.out.println("Failed: " + this.fileName);
					e.printStackTrace();
				}
			}

			inDateTag = false;
		}

		// add terms to the global dictionary
		String[] terms = pTerms.split(text.toLowerCase());

		for (String term : terms) {
			if (term.length() > 0) {
				String newTerm;
				// remove leading punctuation

				String[] temp;
				temp = (term.split("^[^\\d\\w]+",-1));
				newTerm = temp[temp.length-1];
				
				// remove trailing punctuation

				String finalTerm;
				temp = (newTerm.split("[^\\d\\w]+$",-1));
				finalTerm = temp[0];
				
				
				if (finalTerm.length() == 0)
					continue;
				
				// ignore common words
				if (!commonWords.contains(finalTerm.toLowerCase())) {
					dataSet.addGlobalTerm(finalTerm.toLowerCase());
						// don't add entities
					if (inEntity == false)
					document.addTerm(finalTerm.toLowerCase());
				}
			
					// add this term to the table of term/documents
					//dataSet.addDocumentToTerm(document, term);

				
			}
		}

	}

	public void endDocument() {
		// String sRawText = rawText.toString();
		// remove excess white space and newlines from the raw text
		// mNewLines.reset(sRawText);
		// sRawText = mNewLines.replaceAll(" ");

		// mWhiteSpace.reset(sRawText);
		// sRawText = mWhiteSpace.replaceAll(" ");

		// store the raw text
		// document.setRawData(sRawText);

		// add the document to the pile
		dataSet.addDocument(document);

	}

	public void endElement(String elementName) {
		if (inEntity && elementName.equals(entityTag)) {
			// found a complete entity
			dataSet.addEntity(entityName, entityValue);

			// add the entity to the local list of terms
			document.addTerm(entityValue.toLowerCase());

			// add this entity to the list of terms/documents
			//dataSet.addDocumentToTerm(document, entityValue.toLowerCase());
			/*
			 * if (entityName.equals("year")) { if
			 * (!commonWords.contains(entityValue))
			 * commonWords.add(entityValue); }
			 */
			inEntity = false;
		}
	}

	public void startDocument() {
		// start a new document
		document = new DataElement(this.fileName);
	}

	public void startElement(String elementName, Hashtable<String, String> attrs) {
		// check to see if this is the first line and it's a date tag
		if ((foundDate == false || foundYear == false)
				&& elementName.equals("timex")) {
			// make sure it's a date
			if (attrs.containsKey("type") && attrs.get("type").equals("date")) {
				inDateTag = true;
			}
		}

		// why doesn't java have a switch(String)??
		if (elementName.equals("enamex") || elementName.equals("numex")
				|| elementName.equals("timex")) {
			// there should be an associated type
			if (attrs.containsKey("type")) {
				// found an entity
				entityName = attrs.get("type");
				entityTag = elementName;
				inEntity = true;
			}
		}
	}
}
