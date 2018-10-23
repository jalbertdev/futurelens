/******************************************************************************

                        FutureLens 

Copyright 2011 J. Strange, M.W. Berry 
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
/**
 * @author Joshua Strange
 *
 */

package edu.utk.cs.futurelens.data;

import java.io.*;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import edu.utk.cs.futurelens.FutureLens;
import edu.utk.cs.futurelens.ui.FLInterface;
/**
 * @author Joshua
 */

public class LicenseFunctions {
	final static int NOFILE = 1;
	final static int BADHASH = 2;
	final static int BADDATE = 3;
	
	public static boolean verifyHash() throws Exception{
		try{
		MessageDigest md;
	    md = MessageDigest.getInstance("SHA-256");
	    String name ="";
	    if(FLInterface.isMac()){
	    name = "/Applications/FutureLens/.License.fl";
	    }
	    else if(FLInterface.isWindows()){
			String path = FutureLens.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String decodedPath = URLDecoder.decode(path, "UTF-8");
			name = decodedPath + "License.fl";
			System.out.println(name);
	    }
	    
	    /*else if(FLInterface.isLinux()){
	    	name = ClassLoader.getSystemClassLoader().getResource(".").getPath().toString();
	    	name = name + ".License.fl";
	    }*/
	    
	    if(!(new File(name).exists())){
	    	PrintError(NOFILE);
	    }
	    
        BufferedReader br = new BufferedReader(new FileReader(name));
	         
        String tempd = br.readLine(); //date
        String templ = br.readLine(); //licensee
        String temph = br.readLine(); //hash
        
        br.close();
        md.update(templ.getBytes(),0,templ.length());
	    md.update(tempd.getBytes(),0,tempd.length());
	    
        byte[] hash = md.digest();

        String test = byteArray2Hex(hash);
        
        if(temph.equals(test)){
        	return verifyDate(tempd);
        }
        else{
        	PrintError(BADHASH);
        }
	    }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
	    }
		return false;
	}
	
	private static boolean verifyDate(String date){
		Calendar cal = Calendar.getInstance();
		long diff = Long.parseLong(date);
		diff = diff - cal.getTimeInMillis();
		if(diff >= 0){
        return true;
		}
		else{
			PrintError(BADDATE);
			return false;
		}
	}
	
    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
    private static void PrintError(int errnum){
    	Display display = new Display();
        Shell shell = new Shell(display);
        String error_msg = "";
        
        switch(errnum){
        case NOFILE: error_msg = "License can not be found.\nReinstall using the software package you where given to fix this problem."; break;
        case BADHASH: error_msg = "License has a bad hash value. Make sure you have not modified the License file.\nReinstall using the software package you where given to fix this problem."; break;
        case BADDATE: error_msg = "Your license has expired.\n"; break;
        }
        
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR| SWT.OK);
        
        messageBox.setText("Warning");
        messageBox.setMessage(error_msg);
        int buttonID = messageBox.open();
        if(buttonID == SWT.OK){
        	System.exit(0);
        }
    }
}
    