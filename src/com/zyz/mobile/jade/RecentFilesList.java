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
package com.zyz.mobile.jade;

import android.content.Context;
import com.zyz.mobile.book.BookMetadata;
import com.zyz.mobile.util.Text;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


public class RecentFilesList {

	public static final int MAX_HISTORY = 500;
	private static final String mFileName = "recent_files_list.txt";

	private Context mContext;
	private boolean mOpened;
	private LinkedList<BookMetadata> mRecentFileList = new LinkedList<BookMetadata>();

	private static final String TAG = "JTEXT";

	/**
	 * Constructor
	 *
	 * @param context used to open and create database
	 */
	public RecentFilesList(Context context) {
		mContext = context;
	}

	/**
	 * Open the recent files database
	 * if it's already opened, this function does nothing and returns true
	 *
	 * @return true if opened successfully, false otherwise.
	 */
	public boolean open() {
		if (mOpened) {
			return true;
		}

		try {
			BufferedReader br = new BufferedReader(
					  new InputStreamReader(mContext.openFileInput(mFileName)));

			String line;
			while ((line = br.readLine()) != null) {
				mRecentFileList.add(new BookMetadata(Text.split(line, '\t')));
			}

			br.close();
			mOpened = true;
		} catch (FileNotFoundException e) {
			// file not found, this means it's the first time the user opens the application
			mOpened = true;
		} catch (Exception e) {
			//Log.e(TAG, "failed to open recent files database");
		}

		return mOpened;
	}

	/**
	 * close and save the recent file list. call this when you finished with the object.
	 */
	public boolean save() {
		boolean success = false;
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					  mContext.openFileOutput(mFileName, Context.MODE_PRIVATE)));

			ListIterator<BookMetadata> iter = mRecentFileList.listIterator();

			while (iter.hasNext()) {
				bw.write(iter.next().getSaveString());
				bw.write('\n');
			}

			bw.close();
			success = true;
		} catch (Exception e) {
			//Log.e(TAG, "failed to save recent files database");
		}
		return success;
	}

	/**
	 * remove all files from this
	 */
	public void clear() {
		mRecentFileList.clear();
	}

	/**
	 * remove the specified file from the recent file list
	 *
	 * @param bookMetadata        the information of the file to be removed
	 * @param deleteFromPhone delete the file from the phone
	 * @return true if a file is removed, false otherwise
	 */
	public boolean removeFromRecentFiles(BookMetadata bookMetadata, boolean deleteFromPhone) {
		for (int i = 0; i < mRecentFileList.size(); i++) {
			if (mRecentFileList.get(i).equals(bookMetadata)) {
				mRecentFileList.remove(i);
				if (deleteFromPhone) {
					(new File(bookMetadata.getFilePath())).delete();
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * remove the file at the specified locaiton from the recent file list
	 *
	 * @param location the location of the file in the list
	 * @return true if an item is deleted from the list, false otherwise
	 */
	@SuppressWarnings("unused")
	public boolean removeFromRecentFiles(int location) {
		if (location < mRecentFileList.size()) {
			mRecentFileList.remove(location);
			return true;
		}
		return false;
	}

	/**
	 * Get the path of the file from the list at the specified index
	 *
	 * @param index the index of the desired file
	 * @return the path of the desired file, empty string if not found
	 */
	public String getRecentFilePath(int index) {
		if (index < mRecentFileList.size()) {
			return mRecentFileList.get(index).getFilePath();
		}
		return "";
	}

	/**
	 * Get the BookMetadata at the specified index
	 *
	 * @param index the index
	 * @return the BookMetadata at the specified index, null if not found
	 */
	public BookMetadata get(int index) {
		if (index < mRecentFileList.size()) {
			return mRecentFileList.get(index);
		}
		return null;
	}

	public BookMetadata get(String path) {
		ListIterator<BookMetadata> iter = mRecentFileList.listIterator();
		while (iter.hasNext()) {
			BookMetadata info = iter.next();
			if (info.getFilePath().equalsIgnoreCase(path)) {
				return info;
			}
		}
		return null;
	}

	/**
	 * add a new entry to the recent files list. If the path already exists in the
	 * list, it will be moved to the beginning of the list instead.
	 *
	 * @param path the path of the book file
	 * @return true if added successfully, false otherwise.
	 */
	public boolean addToRecentFiles(String path) {
		if (path == null) {
			return false;
		}

		boolean found = false;
		int index = 0;
		ListIterator<BookMetadata> iter = mRecentFileList.listIterator();
		while (iter.hasNext()) {
			if (iter.next().getFilePath().equalsIgnoreCase(path)) {
				BookMetadata info = mRecentFileList.remove(index);
				mRecentFileList.addFirst(info);
				found = true;
				break;
			}
			index++;
		}

		if (!found) {
			File f = new File(path);
			BookMetadata info = new BookMetadata();
			info.setFilePath(path);
			info.setBookName(f.getName());
			info.setXmlPath(getXMLFile(f).getAbsolutePath());
			mRecentFileList.addFirst(info);
		}

		if (mRecentFileList.size() > MAX_HISTORY) {
			mRecentFileList.removeLast();
		}
		return true;
	}

	/**
	 * get the xml info File given the path of the book's text file
	 *
	 * @param bookPath the path of the book file
	 * @return the xml File
	 */
	public static File getXMLFile(String bookPath) {
		return getXMLFile(new File(bookPath));
	}

	/**
	 * get the xml info File given the book's text File
	 *
	 * @param file the book File
	 * @return the xml File
	 */
	public static File getXMLFile(File file) {
		String fileName = file.getName();
		String xmlPath  = file.getParent() + File.separator;

		if (fileName.toLowerCase().endsWith(".txt")) {
			xmlPath += fileName.substring(0, fileName.length() - 4) + ".xml";
		} else {
			xmlPath += fileName + ".xml";
		}
		return new File(xmlPath);
	}

	/**
	 * return the current recent file list
	 *
	 * @return the current recent file list of {@code BookMetadata}
	 */
	public List<BookMetadata> geList() {
		return Collections.unmodifiableList(mRecentFileList);
	}


}
