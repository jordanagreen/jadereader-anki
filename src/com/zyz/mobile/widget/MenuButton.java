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
package com.zyz.mobile.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * User: ray Date: 2013-01-26
 */
public class MenuButton extends Button {

	public enum Style {
		TEXT_OR_IMAGE,
		TEXT_AND_IMAGE
	}

	/**
	 * whether the button is checkable
	 */
	private boolean mCheckable;

	/**
	 * the background color of this button when it's checked
	 */
	private int mCheckedColor;

	/**
	 * the background color of this button when it's unchecked
	 */
	private int mUncheckedColor;

	/**
	 * whether this button has different background color when checked/unchecked
	 */
	private boolean mHasStateColor;

	/**
	 * the text of this button
	 */
	private String mOverflowText = "";

	/**
	 * no image
	 */
	public static final int IMAGE_RESOURCE_NO_ID = -1;

	/**
	 * the resource id of the image used for this button
	 */
	private int mImageResourceId = IMAGE_RESOURCE_NO_ID;

	/**
	 * Id for this MenuButton (this is different from setId(int), getId
	 */
	private int mMenuButtonId = -1;

	private boolean mMeasured;

	private Style mStyle = Style.TEXT_OR_IMAGE;

	public MenuButton(Context context, Style style) {
		this(context);
		mStyle = style;
	}

	public MenuButton(Context context) {
		super(context);
	}

	@SuppressWarnings("unused")
	public MenuButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressWarnings("unused")
	public MenuButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MenuButton setCheckable(boolean checkable) {
		mCheckable = checkable;
		return this;
	}

	public MenuButton setCheckedUncheckedColor(int checkedColor, int uncheckedColor) {
		mCheckedColor = checkedColor;
		mUncheckedColor = uncheckedColor;
		mHasStateColor = true;
		return this;
	}

	public String getOverflowText() {
		return mOverflowText;
	}


	public void setContent(String overflowText, int resId) {
		setContent(overflowText, resId, mStyle);
	}

	public void setContent(String overflowText, int resId, Style style) {
		mOverflowText = overflowText;
		mImageResourceId = resId;
		mStyle = style;

		if (style == Style.TEXT_OR_IMAGE) {
			if (resId == IMAGE_RESOURCE_NO_ID) {
				setText(overflowText);
			}
			else {
				setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
			}
		}
		else if (mStyle == Style.TEXT_AND_IMAGE) {
			if (resId != IMAGE_RESOURCE_NO_ID) {
				setCompoundDrawablesWithIntrinsicBounds(0, resId, 0, 0);
			}
			setText(overflowText);
		}

		mMeasured = false;
	}

	public int getImageResource() {
		return mImageResourceId;
	}


	public int getButtonWidth() {
		if (!mMeasured) {
			mMeasured = true;
			final int widthMeasureSpec =
					  MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			final int heightMeasureSpec =
					  MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			measure(widthMeasureSpec, heightMeasureSpec);
		}

//		// this is the old way of me calculating the width
//		// placed here as a reference
//		try {
//			return getResources().getDrawable(mImageResourceId).getIntrinsicWidth() +
//					  getPaddingLeft() +
//					  getPaddingRight();
//		} catch (Resources.NotFoundException e) {
//			return 0;
//		}
		return getMeasuredWidth();

	}


	public boolean isCheckable() {
		return mCheckable;
	}

	public void check() {
		if (isCheckable() && mHasStateColor) {
			setBackgroundColor(mCheckedColor);
		}
	}

	public void uncheck() {
		if (isCheckable() && mHasStateColor) {
			setBackgroundColor(mUncheckedColor);
		}
	}

	// some Adapter makes use of this function
	public String toString() {
		return mOverflowText;
	}

	/**
	 * return the button id of this button (set through {@link #setMenuButtonId(int)})
	 *
	 * @return the button id of this button, defualt -1 if it has not been set
	 */
	public int getMenuButtonId() {
		return mMenuButtonId;
	}

	/**
	 * Set the button id of this button, you can use this however you want
	 *
	 * @param menuButtonId the menu button id
	 */
	public void setMenuButtonId(int menuButtonId) {
		mMenuButtonId = menuButtonId;
	}

}
