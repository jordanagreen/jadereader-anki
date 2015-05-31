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
package com.zyz.mobile.file;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class FileClipboard implements Iterable<File> {

	private ArrayList<File> clipboard = new ArrayList<File>();

	public final static int MODE_NONE = 1;
	public final static int MODE_CUT = 2;
	public final static int MODE_COPY = 3;

	private int mode = MODE_NONE;

	public FileClipboard() {

	}

	private void setMode(int mode) {
		this.mode = mode;
	}

	/**
	 * Returns whether or not the clipboard is empty.
	 *
	 * @return true if the clipboard is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return clipboard.isEmpty();
	}

	/**
	 * Clears the clipboard.
	 */
	public void empty() {
		clipboard.clear();
	}

	/**
	 * Add file to the clipboard. When paste, the file saved in the clipboard
	 * will be moved (cut)
	 *
	 * @param file the file to be cut
	 */
	public void cut(File file) {
		if (mode != MODE_CUT) {
			setMode(MODE_CUT);
			empty();
		}
		clipboard.add(file);
	}

	/**
	 * Add file to the clipboard. When paste, the file saved in the clipboard will be
	 * copied.
	 *
	 * @param file the file to be copied
	 */
	public void copy(File file) {
		if (mode != MODE_COPY) {
			setMode(MODE_COPY);
			empty();
		}
		clipboard.add(file);
	}

	/**
	 * Paste the content of the clipboard to the specified destination.
	 *
	 * @param destDir the destination to paste to
	 * @return true if paste is succesful, false otherwise
	 */
	public boolean paste(File destDir) {
		boolean result = true;
		for (File file : clipboard) {
			try {
				if (mode == MODE_CUT) {
					moveFileToDirectory(file, destDir);
				} else if (mode == MODE_COPY) {
					copyFileToDirectory(file, destDir);
				}
			} catch (IOException e) {
				result = false;
			}
		}
		if (mode == MODE_CUT) {
			setMode(MODE_NONE);
			empty();
		}
		return result;
	}

	/**
	 * Delete the specified file.
	 *
	 * @param file the file to be deleted
	 */
	public static void deleteFile(File file) throws IOException {
		if (file.isFile()) {
			file.delete();
		} else {
			FileUtils.deleteDirectory(file);
		}
	}

	/**
	 * Move the specified file/directory to the destination directory.
	 *
	 * @param srcFile the file/directory to be moved
	 * @param destDir the destination directory
	 * @throws IOException
	 */
	public static void moveFileToDirectory(File srcFile, File destDir) throws IOException {
		if (srcFile.isDirectory()) {
			FileUtils.moveDirectoryToDirectory(srcFile, destDir, true);
		} else {
			FileUtils.moveFileToDirectory(srcFile, destDir, false);
		}
	}

	/**
	 * Copy the specified file/directory to the destination directory.
	 *
	 * @param srcFile the file/directory to be copied
	 * @param destDir the destination directory
	 * @throws IOException
	 */
	public static void copyFileToDirectory(File srcFile, File destDir) throws IOException {
		if (srcFile.isDirectory()) {
			FileUtils.copyDirectoryToDirectory(srcFile, destDir);
		} else {
			FileUtils.copyFileToDirectory(srcFile, destDir);
		}
	}

	@Override
	public Iterator<File> iterator() {
		return clipboard.iterator();
	}

}
