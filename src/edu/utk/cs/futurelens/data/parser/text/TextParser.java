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

import edu.utk.cs.futurelens.data.parser.Handler;
import edu.utk.cs.futurelens.data.parser.Parser;

public class TextParser implements Parser
{	
	public TextParser()
	{
	}
	
	public void parse(String input, Handler handler) throws TextException
	{		
		// check some args
		if(handler == null)
			throw new IllegalArgumentException();
		
		if(! (handler instanceof TextHandler))
			throw new IllegalArgumentException();
		
		// start the document
		handler.startDocument();
		
		// easy
		handler.characters(input);
		
		// done parsing
		handler.endDocument();
	}	
}
