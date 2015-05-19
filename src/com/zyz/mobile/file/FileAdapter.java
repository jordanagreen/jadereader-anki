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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import com.zyz.mobile.R;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple adapter for displaying an image alongside with text.
 * <p/>
 * doing no error checking at the moment
 *
 * @author ray
 */
public class FileAdapter extends BaseAdapter {

	/**
	 * the list that holds the data
	 */
	private List<File> mData;

	private Context mContext;

	/**
	 * whether or not to automatically notify when dataset is changed
	 */
	private boolean notifyOnChange = true;

	private LayoutInflater mInflater;

	private int mResource;


	/**
	 * Constructor
	 *
	 * @param context  The current context.
	 * @param resource The resource ID for a layout file containing a FileItemView to use when
	 *                 instantiating views
	 */
	public FileAdapter(Context context, int resource, File[] files) {
		init(context, resource, Arrays.asList(files));
	}


	private void init(Context context, int resource, List<File> files) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mResource = resource;
		mData = files;
		sort();
	}

	/**
	 * Adds the specified object at the end of the array.
	 *
	 * @param file The object to add at the end of the array.
	 */
	public void add(File file) {
		mData.add(file);
		if (notifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Removes the specified file from the array.
	 *
	 * @param file The object to remove.
	 */
	public void remove(File file) {
		mData.remove(file);
		if (notifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Remove all elements from the list
	 */
	public void clear() {
		mData.clear();
		if (notifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Adds the specified files at the end of the array.
	 *
	 * @param files the files to be added
	 */
	public void addAll(File[] files) {
		mData.addAll(Arrays.asList(files));
		if (notifyOnChange) {
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	// This does not have stable IDs
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		File file = mData.get(position);

		if (convertView == null) {
			// the third parameter here must be false
			convertView = mInflater.inflate(mResource, parent, false);

			// save the views here with the ViewHolder for efficiency
			holder = new ViewHolder();
			holder.fileItemView = (FileItemView) convertView;

			convertView.setTag(holder);
		}
		else {
			// convertView is not null, reuse the view
			holder = (ViewHolder) convertView.getTag();
		}

		setFileIcon(file, holder);
		holder.fileItemView.setName(file.getName());

		// This is needed because of the way android recycles views.
		// (i.e. if convertView is not null, it may has the incorrect
		// check status. This clears the status and let the parent ListView
		// handles the check status during setupChild
		if (holder.fileItemView.isChecked()) {
			holder.fileItemView.setChecked(false);
		}


		return convertView;
	}


	/**
	 * find and set the appropriate icon for the given file to the ImageView to the specified
	 * ViewHolder
	 *
	 * @param file   the file
	 * @param holder the ViewHolder that has the ImageView
	 */
	private void setFileIcon(File file, ViewHolder holder) {

		if (file.isFile()) {
			String ext = getExt(file);
			String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
			if (mimeType == null) {
				mimeType = "*/*";
			}

			if (mimeType.startsWith("text/") ||
					  ext.equals("txt~"))
			{
				holder.fileItemView.setIcon(R.drawable.file_text);
			}
			else if (mimeType.startsWith("audio/")) {
				holder.fileItemView.setIcon(R.drawable.file_music);
			}
			else if (mimeType.startsWith("video/") ||
					  ext.equals("mkv") ||
					  ext.equals("rmvb"))
			{
				holder.fileItemView.setIcon(R.drawable.file_video);
			}
			else if (mimeType.startsWith("image/")) {
				holder.fileItemView.setIcon(R.drawable.file_image);
			}
			else {
				holder.fileItemView.setIcon(R.drawable.file_generic);
			}
		}
		else if (file.isDirectory()) {
			holder.fileItemView.setIcon(R.drawable.folder);
		}
	}

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
	 * manually call notifyDataSetChanged() to have the changes reflected in the attached view.
	 * <p/>
	 * The default is true, and calling notifyDataSetChanged() resets the flag to true.
	 *
	 * @param notifyOnChange if true, modifications to the list will automatically call
	 */
	public void setNotifyOnChange(boolean notifyOnChange) {
		this.notifyOnChange = notifyOnChange;
	}

	/**
	 * Returns the context associated with this array adapter. The context is used to create views from
	 * the resource passed to the constructor.
	 *
	 * @return The Context associated with this adapter.
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * sort the files in a default order
	 */
	public void sort() {
		Collections.sort(mData, new Comparator<File>() {

			/**
			 * Compares the two files.
			 * note that a directory is considered smaller than a file.
			 * @param file1 first file
			 * @param file2 second file
			 * @return 1 is file1 > file2, -1 if file2 > file1, 0 otherwise
			 */
			public int compare(File file1, File file2) {
				if (file1.isDirectory() && file2.isFile()) {
					return -1;
				}
				else if (file1.isFile() && file2.isDirectory()) {
					return 1;
				}
				// both are directories or both are files, compare their name
				return file1.getName().compareToIgnoreCase(file2.getName());
			}

		});
		if (notifyOnChange) {
			notifyDataSetChanged();
		}
	}


	/**
	 * holder to make loading ListView faster (hold the text/image so that i don't have to spend time
	 * inflating the xml layout over and over again) see Google I/O 2010 World of ListView for detail
	 */
	static class ViewHolder {
		FileItemView fileItemView;
	}
}
