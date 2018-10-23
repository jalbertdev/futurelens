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

import java.util.Hashtable;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.utk.cs.futurelens.data.parser.Handler;
import edu.utk.cs.futurelens.data.parser.Parser;

public class SGMLParser implements Parser
{
	private enum State
	{
		DEFAULT,
		TEXT,
		START_TAG,
		OPEN_TAG,
		IN_TAG,
		CLOSE_TAG,
		ATTRIBUTE_NAME,
		ATTRIBUTE_EQUAL,
		ATTRIBUTE_VALUE,
		QUOTE,
		DONE
	}
	
	private State state;
	private StringBuilder buffer;
	private Hashtable<String, String> attrs;
	private String tagName, attrName, attrValue;
	private Stack<State> prevStates;
	private SGMLHandler handler;
	private char quoteChar;
	private int line, depth;
	
	private final Pattern pSGML = Pattern.compile("<.*?>");
	private final Matcher mSGML = pSGML.matcher("");
	
	// options
	private boolean forceLowerCase;
	
	//Overrides to make the hashing more efficient
	@Override
	public int hashCode() {
		final int prime = 92821;
		int result = 1;
		result = prime * result + ((attrs == null) ? 0 : attrs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SGMLParser other = (SGMLParser) obj;
		if (attrs == null) {
			if (other.attrs != null)
				return false;
		} else if (!attrs.equals(other.attrs))
			return false;
		return true;
	}

	public void setForceLowerCase(boolean forceLowerCase) 
	{
		this.forceLowerCase = forceLowerCase;
	}

	public SGMLParser()
	{
		// don't change case by default
		forceLowerCase = false;
		
		// reset the state
		reset();
	}

	/**
	 * 
	 */
	private void reset() 
	{
		// clear everything
		state = State.DEFAULT;
		buffer = new StringBuilder();
		tagName = null;
		prevStates = new Stack<State>();
		quoteChar = '\0';
		depth = 0;
		
		// start at line 1
		line = 1;
	}
	
	public void parse(String input, Handler handler) throws SGMLException
	{
		int len = input.length();
		char c;
		
		// check some args
		if(handler == null)
			throw new IllegalArgumentException();
		
		if(!(handler instanceof SGMLHandler))
			throw new IllegalArgumentException();
		
		// check if this document is sgml or not
		mSGML.reset(input);
		
		if(! mSGML.find())
			throw new SGMLException("Not an SGML file");
		
		// reset everything
		reset();
		
		this.handler = (SGMLHandler)handler;
		
		// start the document
		handler.startDocument();
		
		for(int i = 0; i < len; i++)
		{
			c = input.charAt(i);
			//System.out.println(c);
			if(c == '\n')
				line++;
			
			// figure out what to do
			switch(state)
			{
			case DONE:
				// this is sort of useless in sgml
				break;
			
			// DEFAULT and TEXT are synonymous
			case DEFAULT:
			case TEXT:
				// text between tags
				stateText(c);
				break;	
			case START_TAG:
				// figure out if this a opening tag (<blah>) or closing tag (</blah>)
				stateStartTag(c);
				break;
			case OPEN_TAG:
				// figure out if this is the end of the tag, an attribute, or the tag name
				stateOpenTag(c);
				break;
			case IN_TAG:
				// figure out if this the end of the tag, an attribute, or whitespace
				stateInTag(c);
				break;
			case CLOSE_TAG:
				// in a closing tag (</blah>)
				stateCloseTag(c);
				break;
			case ATTRIBUTE_NAME:
				// in a tag at an attribute
				stateAttributeName(c);
				break;
			case ATTRIBUTE_EQUAL:
				// at the = in an attribute
				stateAttributeEqual(c);
				break;
			case ATTRIBUTE_VALUE:
				// find the quote character
				stateAttributeValue(c);
				break;				
			case QUOTE:
				// inside a quoted attribute
				stateQuote(c);
				break;
			}
			//System.out.println("Buffer: " + buffer);
		}
		
		// dump the remaining text
		if(buffer.length() > 0)
			handler.characters(buffer.toString());
		
		// done parsing
		if(depth == 0)
			handler.endDocument();
		else
			parseError("Unterminated tag");
	}

	/**
	 * @param c
	 */
	private void stateQuote(char c) {
		if(c == quoteChar)
		{
			attrValue = buffer.toString();
			buffer.setLength(0);
			
			if(forceLowerCase)
				attrs.put(attrName.toLowerCase(), attrValue.toLowerCase());
			else
				attrs.put(attrName, attrValue);
			
			state = State.IN_TAG;
		}
		else
		{
			buffer.append(c);
		}
	}

	/**
	 * @param c
	 * @throws Exception 
	 */
	private void stateAttributeValue(char c) throws SGMLException {
		if(c == '\'' || c == '"')
		{
			quoteChar = c;
			state = State.QUOTE;
		}
		else if(! Character.isWhitespace(c))
		{
			// this is bad
			parseError("Invalid attribute value (expected quote)");
		}
	}

	/**
	 * @param c
	 * @throws Exception 
	 */
	private void stateAttributeEqual(char c) throws SGMLException {
		if(c == '=')
		{
			// next state
			state = State.ATTRIBUTE_VALUE;
		}
		else if(! Character.isWhitespace(c))
		{
			// this is bad!
			parseError("Invalid character after attribute name");
		}
	}

	/**
	 * @param c
	 */
	private void stateAttributeName(char c) {
		if(Character.isWhitespace(c))
		{
			// end of the attribute
			attrName = buffer.toString();
			buffer.setLength(0);
			state = State.ATTRIBUTE_EQUAL;
		}
		else if(c == '=')
		{
			attrName = buffer.toString();
			buffer.setLength(0);
			state = State.ATTRIBUTE_VALUE;
		}
		else
		{
			buffer.append(c);
		}
	}

	/**
	 * @param c
	 */
	private void stateCloseTag(char c) 
	{
		if(c == '>')
		{
			state = popState(prevStates);
			tagName = buffer.toString();
			buffer.setLength(0);
			depth--;
			
			// end the element
			if(forceLowerCase)
				handler.endElement(tagName.toLowerCase());
			else
				handler.endElement(tagName);
		}
		else
		{
			buffer.append(c);
		}
	}

	/**
	 * @param c
	 */
	private void stateInTag(char c) 
	{
		if(c == '>')
		{
			// restore the state
			state = popState(prevStates);
			
			// call the handler
			if(forceLowerCase)
				handler.startElement(tagName.toLowerCase(), attrs);
			else
				handler.startElement(tagName, attrs);
			
			// clear everything
			depth++;
			tagName = null;
			attrs = new Hashtable<String, String>();
		}
		else if(! Character.isWhitespace(c))
		{
			// found an attribute
			state = State.ATTRIBUTE_NAME;
			buffer.append(c);
		}
	}

	/**
	 * @param c
	 */
	private void stateOpenTag(char c) 
	{
		if(c == '>')
		{
			// end of the tag
			if(tagName == null)
				tagName = buffer.toString();
			
			buffer.setLength(0);
			depth++;
			
			// call the handler
			if(forceLowerCase)
				handler.startElement(tagName.toLowerCase(), attrs);
			else
				handler.startElement(tagName, attrs);
			
			// clear everything
			tagName = null;
			attrs = new Hashtable<String, String>();
			
			// restore the state
			state = popState(prevStates);
		}
		else if(Character.isWhitespace(c))
		{
			// inside the tag...look for attributes
			tagName = buffer.toString();
			buffer.setLength(0);
			
			state = State.IN_TAG;
		}
		else
		{
			buffer.append(c);
		}
	}

	/**
	 * @param c
	 */
	private void stateStartTag(char c) 
	{
		if(c == '/')
		{
			// set the next state
			state = State.CLOSE_TAG;
		}
		else
		{
			state = State.OPEN_TAG;
			
			tagName = null;
			attrs = new Hashtable<String, String>();
			
			buffer.append(c);
		}
	}

	/**
	 * @param c
	 */
	private void stateText(char c) 
	{
		// check for <
		if(c == '<')
		{
			// store the current state and set the next state
			prevStates.push(state);
			state = State.START_TAG;
			
			// dump the current text
			if(buffer.length() > 0)
			{
				handler.characters(buffer.toString());
				buffer.setLength(0);
			}
		}
		else
		{
			// add on to the current text
			buffer.append(c);
		}
	}
	
	private void parseError(String error) throws SGMLException
	{
		throw new SGMLException(error + ": at line " + line);
	}
	
	private State popState(Stack<State> stack)
	{
		// default state is TEXT
		if(stack.isEmpty())
			return(State.DEFAULT);
		
		return(stack.pop());
	}
}
