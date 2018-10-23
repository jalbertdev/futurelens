package edu.utk.cs.futurelens.data;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;

import edu.utk.cs.futurelens.FutureLens;
import edu.utk.cs.futurelens.ui.FLInterface;
import edu.utk.cs.futurelens.ui.Prefs;
/**
 * @author Joshua
 */

public class Stoplist {

	private static ArrayList<String> stopWords = null;
	private static String name = "";
	private static String macLoc = "/Applications/FutureLens/Stoplist";
	private static String winLoc = null; // This value is set if on a windows
											// machine and based on where the
											// jar is at.
	private static void setUpName() {
		if (FLInterface.isMac()) {
			File dir = new File(macLoc);
			if (!dir.exists()) {
				dir.mkdir();
			}
			name = macLoc + "/stoplist.txt";
			if (!(new File(name).exists())) {
				name = macLoc + "/Stoplist.txt";
			}

		} else if (FLInterface.isWindows()) {
			String path = FutureLens.class.getProtectionDomain()
					.getCodeSource().getLocation().getPath();

			try {
				winLoc = URLDecoder.decode(path, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			name = winLoc + "Stoplist";
			File dir = new File(name);
			if (!dir.exists()) {
				dir.mkdir();
			}
			name = winLoc + "Stoplist/stoplist.txt";
			if (!(new File(name).exists())) {
				name = winLoc + "Stoplist/Stoplist.txt";
			}
		}
	}

	public static ArrayList<String> ReadStopList() {

		BufferedReader br;
		stopWords = new ArrayList<String>();

		if (name.equals("")) {
			setUpName();
		}
		if (!(new File(name).exists())) {
			String allWords = Prefs.getDefaultIgnoredDataSet();
			for (String s : allWords.split("[,\\s]+"))
				if (s.length() > 0)
					stopWords.add(s.toLowerCase());
		} else {
			try {
				br = new BufferedReader(new FileReader(name));
				String s = "";
				try {
					while ((s = br.readLine()) != null) {
						stopWords.add(s.toLowerCase());
					}
					br.close();
				} catch (IOException e) {

					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return stopWords;
	}

	public static String FormatReadList() {
		BufferedReader br;
		String list = "";
		Boolean firstWord = true;
		if (name.equals("")) {
			setUpName();
		}

		if (!(new File(name).exists())) {
			return Prefs.getDefaultIgnoredDataSet();
		} else {
			try {
				br = new BufferedReader(new FileReader(name));
				String s = "";
				try {
					while ((s = br.readLine()) != null) {
						if (!firstWord)
							list = list + ", " + s;
						else {
							list = s;
							firstWord = false;
						}
					}
					br.close();
				} catch (IOException e) {

					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public static void WriteStopList(String words) {
		FileWriter fstream;

		if (name.equals("")) {
			setUpName();
		}
		
		try {
			fstream = new FileWriter(name);
			BufferedWriter out = new BufferedWriter(fstream);
			for (String s : words.split("[,\\s]+")) {
				if (s.length() > 0)
					out.write(s + "\n");
			}
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}