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

import android.util.DisplayMetrics;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.UUID;

public class Util {

	private static final String[] ILLEGAL_CHARACTERS =
			  {"/", "\n", "\r", "\t", "\0", "\f", "`", "?", "*", "\\", "<", ">", "|", "\"", ":"};

	/**
	 * get the extension of the given file
	 *
	 * @param file the file
	 * @return the extension string of the specified file
	 */
	public static String getExt(File file) {
		String ext = "";
		String filename = file.getName();

		int dotPos = filename.lastIndexOf('.');
		if (dotPos >= 0) {
			ext = filename.substring(dotPos + 1);
		}
		// doesn't work for file names with non-ascii characters. 
		//ext = MimeTypeMap.getFileExtensionFromUrl(file.toURI().toURL().toString());

		return ext.toLowerCase();
	}

	/**
	 * concatenate the parent directory and the file name together
	 *
	 * @param parentDir the parent directory which may or may not ends with a slash
	 * @param filename  the name of the file
	 * @return conccatenated path
	 */
	public static String makePath(String parentDir, String filename) {
		return parentDir.endsWith("/") ? parentDir + filename : parentDir + "/" + filename;
	}

	/**
	 * get a name that hasn't been used in the specified directory. This function adds a suffix to the
	 * specified name until a free name is found.
	 *
	 * @param dir  the directory to checke
	 * @param name the initial name
	 * @return a free name, or null if it's unable to find a free name
	 */
	@Nullable
	public static String getFreeName(File dir, String name) {

		// remove characters not allowed as filename (not perfect)
		for (String s : ILLEGAL_CHARACTERS) {
			name = name.replace(s, "");
		}
		String ext = ".txt";
		String defaultName = "SharedText";

		for (int i = 0; i <= 20; i++) {
			File file = new File(dir, name + (i == 0 ? "" : String.format("%02d", i)) + ext);
			try {
				// use file.createNewFile() instead of !file.exists()
				// because the filename may contain invalid characters
				if (file.createNewFile()) {
					file.delete();
					return file.getName();
				}
			} catch (IOException e) {
				// the name probably contain some invalid characters, change it.
				name = defaultName;
			}
		}

		// one last try
		File file = new File(dir, String.format("%s-%s%s", defaultName, UUID.randomUUID(), ext));
		try {
			if (file.createNewFile()) {
				file.delete();
				return file.getName();
			}
		} catch (IOException e) {}

		return null;
	}


	/**
	 * try to create an empty file from the specified path. if the path is a folder, this function
	 * returns false. if the path is a file, it tries to create the file. returns true if the file is
	 * successfully created. false otherwise. However, some directories might have been created while
	 * trying to create this file.
	 *
	 * @param path the path of the file
	 * @return true if the file is created successfully, false otherwise.
	 */
	public static boolean createFileIfNotExist(String path) {
		File file = new File(path);

		if (file.exists()) {
			if (file.isDirectory()) {
				return false;
			}
			return true;
		}

		File dir = file.getParentFile();
		boolean success = true;
		if (!dir.exists()) {
			success = dir.mkdirs();
		}

		if (success) {
			success = false;
			try {
				success = file.createNewFile();
			} catch (IOException e) {

			}
		}
		return success;
	}

	public static int DPtoPX(int dps, DisplayMetrics displayMetrics) {
		final float scale = displayMetrics.density;
		return (int) (dps * scale + 0.5f);
	}

	public static String getDefaultCharSet() {
		OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
		String enc = writer.getEncoding();
		return enc;
	}

	public static int invertColor(int color) {
		return (0x00FFFFFF - (color & 0x00FFFFFF)) | (color & 0xFF000000);
	}
}
