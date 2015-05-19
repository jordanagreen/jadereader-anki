package com.zyz.mobile.widget;
/*
Copyright (C) 2013 Ray Zhou

Author: ray
Date: 2013-11-07

*/

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.zyz.mobile.R;

public class ColorPickerAdapter extends BaseAdapter {
	private final Context mContext;


	private static int[] DEFAULT_COLORS = {
			  0x80822111, 0x80AC2B16, 0x80CC3A21,
			  0x801A764D, 0x802A9C68, 0x803DC789,
			  0x8083334C, 0x80B65775, 0x80E07798,
	};

	private int[] mColors = new int[DEFAULT_COLORS.length];

	private static String[] COLORS_PREF_KEYS = {
			  "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8",
	};


	public ColorPickerAdapter(Context context) {
		mContext = context;

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);

		for (int i = 0; i < mColors.length; i++) {
			mColors[i] = pref.getInt(COLORS_PREF_KEYS[i], DEFAULT_COLORS[i]);
		}
	}

	@Override
	public int getCount() {
		return DEFAULT_COLORS.length;
	}


	public int getColor(int position) {
		if (position >= mColors.length || position < 0) {
			return 0;
		}
		return mColors[position];
	}

	public void setColor(int position, int color) {
		if (position >= 0 && position < mColors.length) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
			pref.edit().putInt(COLORS_PREF_KEYS[position], color).commit();

			mColors[position] = color;
		}
	}

	public String getKey(int position) {
		if (position >= COLORS_PREF_KEYS.length || position < 0) {
			return "cerror";
		}
		return COLORS_PREF_KEYS[position];
	}

	/**
	 * returns the color as an Integer
	 *
	 * @param position
	 * @return
	 */
	@Override
	public Object getItem(int position) {
		return new Integer(mColors[position]);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {  // if it's not recycled, initialize some attributes
//			imageView = new ImageView(mContext);
//			imageView.setLayoutParams(new GridView.LayoutParams(90, 80));
//			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//			imageView.setPadding(8, 8, 8, 8);

			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			imageView = (ImageView) inflater.inflate(R.layout.highlight_color_square, null);
		}
		else {
			imageView = (ImageView) convertView;
		}

		imageView.setBackgroundColor(mColors[position]);

		return imageView;
	}
}
