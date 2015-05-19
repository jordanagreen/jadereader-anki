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
Date: 2013 07 31

*/

package com.zyz.mobile.util;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.zyz.mobile.widget.MenuButton;

import java.util.List;

public class MenuItemAdapter extends BaseAdapter {


	private final int mResource;

	private final LayoutInflater mInflater;
	/**
	 * Color of the text in all the textview
	 */
	private int mTextColor = -1;

	/**
	 * The text size of the text in all textview
	 */
	private int mTextSize = -1;


	private int mBackgroundColor = -1;

	private List<MenuButton> mItems;

	public MenuItemAdapter(Context context, int textviewResourceId, List<MenuButton> items) {
		mResource = textviewResourceId;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItems = items;
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public MenuButton getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			// the third parameter here must be false
			convertView = mInflater.inflate(mResource, parent, false);

			holder = new ViewHolder();
			holder.textView = (TextView) convertView;
			convertView.setTag(holder);

			MenuButton item = mItems.get(position);
			holder.textView.setText(item.getOverflowText());
			holder.textView.setCompoundDrawablesWithIntrinsicBounds(item.getImageResource(), 0, 0, 0);

			if (mTextColor != -1) {
				holder.textView.setTextColor(mTextColor);
			}
			if (mBackgroundColor != -1) {
				holder.textView.setBackgroundColor(mBackgroundColor);
			}
			if (mTextSize != -1) {
				holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
			}
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		return holder.textView;
	}


	/**
	 * set the color of the text
	 *
	 * @param color the color of the text
	 */
	public void setTextColor(int color) {
		mTextColor = color;
		notifyDataSetChanged();
	}

	public void setBackgroundColor(int backgroundColor) {
		mBackgroundColor = backgroundColor;
		notifyDataSetChanged();
	}

	/**
	 * Set the size of the text (in pixel)
	 *
	 * @param size the size in pixel
	 */
	public void setTextSize(int size) {
		mTextSize = size;
		notifyDataSetChanged();
	}

	/**
	 * holder to make loading ListView faster (hold the text/image so that i don't have to spend time
	 * inflating the xml layout over and over again) see Google I/O 2010 World of ListView for detail
	 */
	static class ViewHolder {
		TextView textView;
	}
}
