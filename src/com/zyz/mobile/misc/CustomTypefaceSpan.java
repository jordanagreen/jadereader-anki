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

package com.zyz.mobile.misc;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * User: ray Date: 2013-04-27 a TypefaceSpan that allows to use your own fonts instead of using the
 * predefined family provided by Android.
 * <p/>
 * The body of this class is a modification of the class TypefaceSpan
 *
 * @see android.text.style.TypefaceSpan
 */
public class CustomTypefaceSpan extends MetricAffectingSpan {
	private final Typeface mTypeface;

	public CustomTypefaceSpan(Typeface tf) {
		mTypeface = tf;
	}

	/**
	 * Returns the Typeface of this object
	 */
	@SuppressWarnings("UnusedDeclaration")
	public Typeface getTypeface() {
		return mTypeface;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		apply(ds, mTypeface);
	}

	@Override
	public void updateMeasureState(TextPaint paint) {
		apply(paint, mTypeface);
	}

	private static void apply(Paint paint, Typeface tf) {
		int oldStyle;

		Typeface old = paint.getTypeface();
		if (old == null) {
			oldStyle = 0;
		} else {
			oldStyle = old.getStyle();
		}

		int fake = oldStyle & ~tf.getStyle();

		if ((fake & Typeface.BOLD) != 0) {
			paint.setFakeBoldText(true);
		}

		if ((fake & Typeface.ITALIC) != 0) {
			paint.setTextSkewX(-0.25f);
		}

		paint.setTypeface(tf);
	}
}
