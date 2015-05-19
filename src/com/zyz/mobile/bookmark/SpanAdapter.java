package com.zyz.mobile.bookmark;
/*
Copyright (C) 2014 Ray Zhou

Author: ray
Date: 2014-02-06

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
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.zyz.mobile.R;
import com.zyz.mobile.book.UserBookData;
import com.zyz.mobile.book.UserSpan;
import com.zyz.mobile.book.UserSpanType;
import com.zyz.mobile.jade.JTextActivity;

import java.util.List;

public class SpanAdapter extends ArrayAdapter<UserSpan> {

	private int mTextViewResourceId;

	private int mFontSize;


	public SpanAdapter(Context context, int textViewResourceId, List<UserSpan> objects) {
		super(context, textViewResourceId, objects);
		mTextViewResourceId = textViewResourceId;

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		mFontSize = pref.getInt(JTextActivity.KEY_TEXTSIZE, JTextActivity.DEFAULT_TEXT_SIZE);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || !(convertView instanceof TextView)) {
			LayoutInflater inflater = (LayoutInflater)
					  getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(mTextViewResourceId, parent, false);
		}

		UserSpan span = getItem(position);
		TextView textView = (TextView) convertView;

		textView.setTextSize(mFontSize);
		textView.setText(span.getDescription(), TextView.BufferType.SPANNABLE);

		if (span.getType() == UserSpanType.HIGHLGHT) {
			((Spannable) (textView.getText())).setSpan(span.getObject(), 0,
					  textView.getText().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		else if (span.getType() == UserSpanType.BOOKMARK) {
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


		convertView.setTag(R.id.tag_span, getItem(position));

		return convertView;
	}

}
