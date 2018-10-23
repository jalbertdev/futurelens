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

package edu.utk.cs.futurelens.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * @author greg
 *
 */
public class Callback implements Listener 
{
	private final Object callbackTarget;
	
	private final Method callbackMethod;

	private boolean callbackArgument;
	
	@SuppressWarnings("unchecked")
	public Callback(Object object, String function)
	{	
		Class targetClass;
		
		callbackTarget = object;
		
		targetClass = callbackTarget.getClass();
		
		while(targetClass != null)
		{
			for(Method method : targetClass.getDeclaredMethods())
			{
				if(method.getName().equals(function))
				{
					// check if the method takes an argument
					Class params[] = method.getParameterTypes();
					
					if(params.length > 0 && params[0].isAssignableFrom(Event.class))
						callbackArgument = true;
					else
						callbackArgument = false;
					
					callbackMethod = method;
					
					return;
				}
			}
			
			// get the parent class
			targetClass = targetClass.getSuperclass();
		}
		
		// couldn't find the method
		throw new IllegalArgumentException("Method " + callbackTarget.getClass().getName() + "." + function + " could not be found");
	}
	
	public static void connect(Widget widget, int event, Object object, String function)
	{
		widget.addListener(event, new Callback(object, function));
	}
	
	public void handleEvent(Event event) 
	{
		boolean override = false;
		
		if(! callbackMethod.isAccessible())
		{
			callbackMethod.setAccessible(true);
			override = true;
		}
		
		try 
		{
			if(callbackArgument)
				callbackMethod.invoke(callbackTarget, new Object[] { event } );
			else
				callbackMethod.invoke(callbackTarget, new Object[0]);
		} 
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(override)
			{
				callbackMethod.setAccessible(false);
			}
		}
	}

}
