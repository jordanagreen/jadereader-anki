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
Date: 2013 07 31

*/

package com.zyz.mobile.jade;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.zyz.mobile.R;

public abstract class BaseActivity extends Activity {

	private Toast mToast = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mToast = new Toast(this);
	}


	@Override
	protected void onPause() {
		super.onPause();
		mToast.cancel();
	}

	@Override
	protected void onResume() {
		super.onResume();
		BaseActivity.setPreferredScreenOrientation(this);
	}

	public static void setPreferredScreenOrientation(Activity activity) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

		String orientation = sharedPreferences.getString(activity.getString(R.string.pref_orientation), "0");

		// see arrays.xml and pref.xml for detail
		if (orientation.equals("1")) { // portrait
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		else if (orientation.equals("2")) { // landscape
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
	}

	protected void showToast(int resId) {
		mToast.cancel();
		mToast = Toast.makeText(this, resId, Toast.LENGTH_SHORT);
		mToast.show();
	}

	protected void showToast(String toast) {
		mToast.cancel();
		mToast = Toast.makeText(this, toast, Toast.LENGTH_SHORT);
		mToast.show();
	}
}
