/*
Copyright (C) 2013 Ray Zhou

JadeRead is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

JadeRead is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with JadeRead.  If not, see <http://www.gnu.org/licenses/>

Author: Ray Zhou
Date: 2013 04 26

*/
package com.zyz.mobile.util;

import android.content.Context;
import org.jetbrains.annotations.Nullable;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.util.ArrayList;

public class Text {

	/**
	 * Guess the encoding of the file pointed by the specified path
	 * @param path the path of the file for which the encoding is wanted
	 * @return the encoding, or null if failed to guess.
	 */
	@Nullable
	public static String guessFileEncoding(String path) {
		UniversalDetector detector = new UniversalDetector(null);

		try {
			byte[] buf = new byte[4096];
			FileInputStream fis = new FileInputStream(path);

			int nread;
			while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, nread);
			}
			detector.dataEnd();

			return detector.getDetectedCharset();

		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}

		return null;
	}

	/**
	 * split the string at the given char
	 * (the split method from the String is too slow for simple split like this)
	 * @param s string to split
	 * @param c the split char
	 * @return the array of strings computed by splitting the given string at the given char
	 */
	public static String[] split(String s, char c) {
		int count = 0;
		// count the # of string in the resulting array
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == c) {
				count++;
			}
		}
		String[] result = new String[count + 1];

		int index = 0;
		for (int j = 0; j < count; j++) {
			int end = s.indexOf(c, index);
			result[j] = s.substring(index, end);
			index = end + 1;
		}
		result[count] = s.substring(index); // split one last time
		return result;
	}

	/**
	 * return the text in the 'path' as a single string
	 * @param path path to a text file
	 * @return a single string containing the text in the given file
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readAll(String path) throws
			  UnsupportedEncodingException, FileNotFoundException, IOException{
		return readAll(path, null);
	}

	/**
	 * Read the text from the specified path with the given encoding.
	 * If the encoding given is not valid, this function read the file
	 * with the default encoding.
	 *
	 * @param path the path of the file to read from
	 * @param enc the desired encoding
	 * @return a string representing the text in the given file
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readAll(String path, String enc) throws
			  UnsupportedEncodingException, FileNotFoundException, IOException{

		File file = new File(path);

		StringBuilder sb = new StringBuilder((int)file.length());



			FileInputStream stream = new FileInputStream(file);
			InputStreamReader sr;

			if (enc == null) {
				sr = new InputStreamReader(stream);
			} else {
				sr = new InputStreamReader(stream, enc);
			}

			Reader r = new BufferedReader(sr);
			char[] buf = new char[8192];
			int len;
			while ((len = r.read(buf)) > 0) {
				sb.append(buf, 0, len);
			}

			/*
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = "";
			while ((line = br.readLine()) != null) {
				// should add system's newline character instead of
				// blindly assuming '\n' is the one used by the system.
				sb.append(line).append('\n');
			}
			*/

			// needs to read the whole file at once because StringBuilder
			// cannot read bytes.
			// if we read X bytes at a time, and append it to the StringBuilder
			// as a new String, we can potentially cut off in the middle
			// of a multi-byte character
			/*
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			byte[] raw = new byte[len];
			if((len = raf.read(raw)) > 0) {
				sb.append(new String(raw, 0, len, "UTF8"));
			}
			*/

		return sb.toString();
	}

	/**
	 * reads the file in the default app external directory with the given file name
	 * into an array.
	 * @param context the context
	 * @param filename the name of the file
	 * @param capacity initial capacity of the returned ArrayList
	 * @return an ArrayList where each element is a line in the given file
	 */
	public static ArrayList<String> readToArray(Context context, String filename, int capacity) {
		ArrayList<String> result = new ArrayList<String>(capacity > 0 ? capacity : 16);

		try {
			File f = new File(context.getExternalFilesDir(null), filename);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(f), "UTF8"));

			String line;
			while ((line = reader.readLine())!= null) {
				result.add(line);
			}
		} catch (Exception e) {
		}

		return result;
	}

	public static ArrayList<String> readToArray(String filename, int capacity) {
		ArrayList<String> result = new ArrayList<String>(capacity > 0 ? capacity : 16);

		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(filename), "UTF8"));

			String line;
			while ((line = reader.readLine())!= null) {
				result.add(line);
			}
		} catch (Exception e) {
		}

		return result;
	}

	public static String readToString(Context context, String filename) {
		StringBuilder result = new StringBuilder();
		try {
			InputStream raw = context.getAssets().open(filename);
			Reader reader = new BufferedReader(new InputStreamReader(raw, "UTF8"));

			int len = 0;
			char[] buf = new char[8192];
			while ((len = reader.read(buf)) > 0) {
				result.append(buf, 0, len);
			}

		} catch (IOException e) {

		}
		return result.toString();
	}

	public static String readToString(String filename) {
		File file = new File(filename);

		StringBuilder result = new StringBuilder((int)file.length());

		try {
			Reader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(filename), "UTF8"));

			int len = 0;
			char[] buf = new char[8192];
			while ((len = reader.read(buf)) > 0) {
				result.append(buf, 0, len);
			}

		} catch (Exception e) {

		}
		return result.toString();
	}
}
