package com.zyz.mobile.misc;/*
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
import android.graphics.Typeface;

/**
 * User: ray Date: 2013-05-06 To change this template use File | Settings | File Templates.
 */
public class Font {

	public final static String ROBOTO_THIN = "Roboto-Thin.ttf";
	public final static String ROBOTO_LIGHT = "Roboto-Light.ttf";
	public final static String ROBOTO_BOLD = "Roboto-Bold.ttf";

	/**
	 * return the typeface of the specified name from the asset fonts folder
	 *
	 * @param context context
	 * @param name    the name of the typeface
	 * @return the specified typeface, if the specified typeface is not availabe, this returns Serif
	 *         Normal
	 */
	public static Typeface createTypeface(Context context, String name) {
		try {
			return Typeface.createFromAsset(context.getAssets(), "fonts/" + name);
		} catch (Exception e) {
		}
		return Typeface.create("serif", Typeface.NORMAL);
	}
}