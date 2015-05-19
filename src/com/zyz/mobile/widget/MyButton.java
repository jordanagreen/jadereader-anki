package com.zyz.mobile.widget;/*
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

Author: ray
Date: 2013-06-12

*/

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import com.zyz.mobile.misc.Font;

public class MyButton extends Button {

	@SuppressWarnings("unused")
	public MyButton(Context context) {
		super(context);
		init();
	}

	@SuppressWarnings("unused")
	public MyButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@SuppressWarnings("unused")
	public MyButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setTypeface(Font.createTypeface(getContext(), Font.ROBOTO_LIGHT));
	}
}