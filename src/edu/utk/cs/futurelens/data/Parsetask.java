package edu.utk.cs.futurelens.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//import java.util.concurrent.CountDownLatch;

/**
 * @author Joshua
 */

import edu.utk.cs.futurelens.data.parser.ParseException;

public class Parsetask implements Runnable {

	final FileLoader loader;
	final String filename;


	public Parsetask(FileLoader loader, String filename) {
		this.loader = loader;
		this.filename = filename;
	}

	public void run() {
		try {
			File file = new File(loader.getSourcePath() + File.separator
					+ filename);
			long length = file.length();

			StringBuilder fileData = new StringBuilder((int) length);
			BufferedReader reader = new BufferedReader(new FileReader(
					loader.getSourcePath() + File.separator + filename));
			char[] buf = new char[(int) length];

			reader.read(buf);
			fileData.append(buf, 0, (int) length);

			reader.close();

			loader.parse(fileData.toString(), filename);
			//System.out.println(filename);
			//loader.setNumFilesHandeled();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
