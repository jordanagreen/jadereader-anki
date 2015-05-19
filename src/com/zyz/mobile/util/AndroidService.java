package com.zyz.mobile.util;
/*
Copyright (C) 2014 Ray Zhou

Author: ray
Date: 2014-02-04

*/

import android.app.Activity;
import android.content.res.Configuration;
import android.view.Display;
import android.view.WindowManager;

public class AndroidService {


	/**
	 * returns the orientation of the device
	 *
	 * @param activity the activity
	 * @return Configuration.ORIENTATION_PORTRAIT or Configuration.ORIENTATION_LANDSCAPE;
	 */
	public static int getScreenOrientation(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		int orientation;

		if (display.getWidth() <= display.getHeight()) {
			orientation = Configuration.ORIENTATION_PORTRAIT;
		}
		else {
			orientation = Configuration.ORIENTATION_LANDSCAPE;
		}

		return orientation;
	}

	/**
	 * show/hide the status bar of the specified activity
	 *
	 * @param activity the activity of which the status bar this function is working on
	 * @param show     show the status bar if true, hide the status bar if false
	 */
	public static void showStatusBar(Activity activity, boolean show) {
		if (show) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		else {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
	}
}
