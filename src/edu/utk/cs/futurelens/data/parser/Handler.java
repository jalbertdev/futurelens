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

package edu.utk.cs.futurelens.data.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Handler 
{
	public void endDocument();
	public void characters(String text);
	public void startDocument();
	
	// used to remove leading punctuation
	public static final Pattern pLeading = Pattern.compile("^[^\\d\\w]+");
	public static final Matcher mLeading = pLeading.matcher("");
	
	// used to remove trailing punctuation
	public static final Pattern pTrailing = Pattern.compile("[^\\d\\w]+$");
	public static final Matcher mTrailing = pTrailing.matcher("");
	
	// splits terms
	public static final Pattern pTerms = Pattern.compile("\\s");
	
	// removes excess whitespace
	public static final Pattern pWhiteSpace = Pattern.compile("\\s{2,}");
	public static final Matcher mWhiteSpace = pWhiteSpace.matcher("");
	
	// removes newlines
	public static final Pattern pNewLines = Pattern.compile("\n");
	public static final Matcher mNewLines = pNewLines.matcher("");
}
