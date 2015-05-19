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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.zyz.mobile.R;

public class Slider extends RelativeLayout implements Concealable{

	private SeekBar seeker;
	
	private TextView display;
	private OnConcealListener mOnConcealListener;
	private OnRevealListener mOnRevealListener;

	@SuppressWarnings("unused")
	public Slider(Context context) {
		super(context);
		init(context);
	}

	@SuppressWarnings("unused")
	public Slider(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		init(attrs);
	}
	
	/**
	 * initialize the control
	 * @param context
	 */
	private void init(Context context) {
		LayoutInflater inflater = 
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View view = inflater.inflate(R.layout.slider, this);
		
		display = (TextView) view.findViewById(R.id.slider_text);
		seeker = (SeekBar) view.findViewById(R.id.slider_seeker);
	}
	
	/**
	 * initialize the control with the specified attributes 
	 * @param attrs the attributes specified in the xml files
	 */
	private void init(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Slider);
		
		// set default progress to 100 slices
		int max = a.getInt(R.styleable.Slider_max, 100); 
		seeker.setMax(max);
		
		a.recycle();
	}
	
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
		seeker.setOnSeekBarChangeListener(listener);
	}
	
	public void setText(String text) {
		display.setText(text);
	}

	@Override
	public int getVisibility() {
		return seeker.getVisibility();
	}

	@Override
	public void setVisibility(int visibility) {
		if (visibility == View.VISIBLE && mOnRevealListener != null) {
			mOnRevealListener.onReveal(this);
		}
		else if ((visibility == View.INVISIBLE || visibility == View.GONE)&& mOnConcealListener != null)
		{
			mOnConcealListener.onConceal(this);
		}
		seeker.setVisibility(visibility);
		display.setVisibility(visibility);
	}
	
	public void setProgress(int progress) {
		seeker.setProgress(progress);
	}

	public void setMax(int max) {
		seeker.setMax(max);
	}

	public int getMax() {
		return seeker.getMax();
	}

	@SuppressWarnings("unused")
	public void setOnConcealListener(OnConcealListener onConcealListener) {
		mOnConcealListener = onConcealListener;
	}

	public void setOnRevealListener(OnRevealListener onRevealListener) {
		mOnRevealListener = onRevealListener;
	}

	@Override
	public void conceal() {
		setVisibility(View.INVISIBLE);
	}

	@Override
	public void reveal() {
		setVisibility(View.VISIBLE);
	}

	@Override
	public boolean isDisplaying() {
		return seeker.getVisibility() == View.VISIBLE;
	}
}
