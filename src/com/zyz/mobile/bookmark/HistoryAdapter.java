package com.zyz.mobile.bookmark;
/*
Copyright (C) 2014 Ray Zhou

Author: ray
Date: 2014-02-16

*/

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.zyz.mobile.book.LocationHistory;
import com.zyz.mobile.book.UserBookData;
import com.zyz.mobile.book.UserSpan;
import com.zyz.mobile.book.UserSpanType;
import com.zyz.mobile.jade.JTextActivity;

public class HistoryAdapter extends BaseAdapter {

	private int mTextViewResourceId;

	private int mFontSize;

	private LocationHistory mLocationHistory;

	private Context mContext;

	public HistoryAdapter(Context context, int textViewResourceId, LocationHistory objects) {
		mTextViewResourceId = textViewResourceId;
		mLocationHistory = objects;
		mContext = context;
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		mFontSize = pref.getInt(JTextActivity.KEY_TEXTSIZE, JTextActivity.DEFAULT_TEXT_SIZE);

	}

	@Override
	public int getCount() {
		return mLocationHistory.size();
	}

	@Override
	public Object getItem(int position) {
		return mLocationHistory.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || !(convertView instanceof TextView)) {
			LayoutInflater inflater = (LayoutInflater)
					  mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(mTextViewResourceId, parent, false);
		}

		UserSpan span = (UserSpan) getItem(position);
		TextView textView = (TextView) convertView;

		textView.setTextSize(mFontSize);
		textView.setText(span.getDescription(), TextView.BufferType.SPANNABLE);

		if (span.getType() == UserSpanType.BOOKMARK) {
			String text = textView.getText().toString();
			int a = text.indexOf(UserBookData.BOOKMARK_LEFT);
			int b = text.indexOf(UserBookData.BOOKMARK_RIGHT);
			if (a != -1 && b != -1 && a < b) {
				StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);

				// using Spanned.SPAN_INCLUSIVE_INCLUSIVE didn't work as expected, so use
				// SPAN_INCLUSIVE_EXCLUSIVE with [a, b+1) instead.
				((Spannable) textView.getText()).setSpan(boldSpan, a, b + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			}
		}


		return convertView;
	}
}
