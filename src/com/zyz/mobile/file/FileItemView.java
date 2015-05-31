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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.*;
import com.zyz.mobile.R;

public class FileItemView extends LinearLayout implements Checkable {


	/**
	 * true if this FileItemView is selected/checked, false otherwise
	 */
	private boolean checkState;

	private Context context;

	private ImageView fileIcon;
	private TextView fileName;

	private int uncheckedColor;
	private int checkedColor;

	public FileItemView(Context context) {
		super(context);
		init(context, null);
	}

	public FileItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}


	private void init(Context context, AttributeSet attrs) {
		this.context = context;

		LayoutInflater inflater =
				  (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.file, this);

		fileIcon = (ImageView) view.findViewById(R.id.file_icon);
		fileName = (TextView) view.findViewById(R.id.file_name);

		if (attrs != null) {
			initAttributes(attrs);
		}
	}

	private void initAttributes(AttributeSet attrs) {

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FileItemView);

		uncheckedColor = a.getColor(R.styleable.FileItemView_uncheckedColor,
				  context.getResources().getColor(R.color.white));
		checkedColor = a.getColor(R.styleable.FileItemView_checkedColor,
				  context.getResources().getColor(R.color.grey));

		a.recycle();
	}

	public void setNameColor(int color) {
		fileName.setTextColor(color);
	}

	public void setName(String name) {
		fileName.setText(name);
	}

	public void setIcon(int drawable) {
		fileIcon.setImageResource(drawable);
	}

	/**
	 * update the view depending on the checked state of the item
	 */
	private void updateView() {
		if (checkState) {
			fileName.setTextColor(checkedColor);
			setBackgroundColor(getResources().getColor(R.color.white));
		}
		else {
			fileName.setTextColor(uncheckedColor);
			setBackgroundColor(getResources().getColor(R.color.transparent));
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// use this pattern to change the drawable depending
		// on the choicemode
		// it's not used here, this is for reference
		ViewParent parent = getParent();
		if (parent instanceof ListView) {
			switch (((ListView) parent).getChoiceMode()) {
				case ListView.CHOICE_MODE_MULTIPLE:
					break;
				case ListView.CHOICE_MODE_SINGLE:
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void setChecked(boolean checked) {
		checkState = checked;
		updateView();
	}

	@Override
	public boolean isChecked() {
		return checkState;
	}

	@Override
	public void toggle() {
		checkState = !checkState;
		updateView();
	}
}
