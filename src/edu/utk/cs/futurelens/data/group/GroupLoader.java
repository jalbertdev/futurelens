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

package edu.utk.cs.futurelens.data.group;

import java.io.BufferedReader;
import java.io.FileReader;

import edu.utk.cs.futurelens.data.Loader;
/**
 * @author Greg
 * @author Andrey
 *
 */
public class GroupLoader implements Loader
{
	private Group group;
	
	public GroupLoader()
	{
		group = null;
	}
	
	public Group getGroup()
	{
		return(group);
	}
	
	public void load(String path) throws java.io.IOException, GroupException
	{
		// load the dictionary file
		BufferedReader reader = new BufferedReader(new FileReader(path));
		String line, terms[];
		String score, index, term;
		int lineNum = 0, numTerms = 0, numEntities = 0;
		
		// new group
		group = new Group();
		group.setNameFromPath(path);
		
		while((line = reader.readLine()) != null)
		{
			lineNum++;
			
			// remove leading whitespace
			line = line.trim();
			
			// ignore comments
			if(line.startsWith("#"))
				continue;
			
			// ignore lines that don't start with numbers
			if(! Character.isDigit(line.charAt(0)))
				continue;
			
			// split the terms out
			terms = line.split("\\s+");
			
			// if there are fewer than 3 elements something is wrong
			if(terms.length < 3)
				throw new GroupException("Line " + lineNum + ": too few elements");
			
			// the first element should be the score
			score = terms[0];
			
			// second element is the index (idx)
			index = terms[1];
			
			// if the last element is the index this is an entity
			if(terms[terms.length - 1].equals(index))
			{
				// clear the term
				term = new String();
				
				// build the entity
				for(int i = 2; i < terms.length - 1; i++)
				{
					term += terms[i];
					if(i != terms.length - 2)
						term += " ";
				}
				
				group.addEntity(term, Double.parseDouble(score));
				
				numEntities++;
			}
			else
			{
				// just a term
				term = new String();
				
				for(int i = 2; i < terms.length; i++)
				{
					term += terms[i];
					if(i != terms.length - 1)
						term += " ";
				}
			
				group.addTerm(term, Double.parseDouble(score));
				
				numTerms++;
			}
			
		}
		
		if(numTerms == 0)
			throw new GroupException("No terms found");
		else if(numEntities == 0)
			throw new GroupException("No entities found");
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
		return(false);
	}

	public boolean isLoaded()
	{
		return(true);
	}
	
	public void startOperation() 
	{
		
	}
}
