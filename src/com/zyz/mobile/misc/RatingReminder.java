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
Date: 2013-06-14

*/

import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import com.zyz.mobile.R;

public class RatingReminder implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

	private Context mContext;

	public final static int REMINDER_COUNTDOWN_BEGIN = 15;
	public final static int MILLISECONDS_IN_10_DAY = 864000000;
	private final static String TAG = "JTEXT";

	public RatingReminder(Context context) {
		mContext = context;
	}

	/**
	 * reset preference related to the reminder
	 */
	public void reset() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = pref.edit();

		editor.putBoolean(mContext.getString(R.string.pref_reminder_never_show), false);
		editor.putLong(mContext.getString(R.string.pref_reminder_reset_time), System.currentTimeMillis());
		editor.putInt(mContext.getString(R.string.pref_reminder_countdown), REMINDER_COUNTDOWN_BEGIN);

		editor.commit();
	}

	/**
	 * decrement the counter
	 */
	public void countdown() {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		final SharedPreferences.Editor editor = pref.edit();

		int countdown = pref.getInt(mContext.getString(R.string.pref_reminder_countdown),
				  RatingReminder.REMINDER_COUNTDOWN_BEGIN);

		if (countdown >= RatingReminder.REMINDER_COUNTDOWN_BEGIN) {
			long current_time = System.currentTimeMillis();
			editor.putLong(mContext.getString(R.string.pref_reminder_reset_time), current_time);
		}
		if (countdown >= 0) {
			editor.putInt(mContext.getString(R.string.pref_reminder_countdown), countdown - 1);
		}

		editor.commit();
	}

	/**
	 * tell whether it's time to remind the user to rate the app
	 * @return true if it's time to remind the user to rate the app, false otherwise
	 */
	public boolean timeToRemind() {

		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);

		if (pref.getBoolean(mContext.getString(R.string.pref_reminder_never_show), false)) {
			return false;
		}

		long current_time = System.currentTimeMillis();
		long reset_time = pref.getLong(mContext.getString(R.string.pref_reminder_reset_time), current_time);

		int countdown = pref.getInt(mContext.getString(R.string.pref_reminder_countdown), REMINDER_COUNTDOWN_BEGIN);

		if (countdown < 0 && current_time > reset_time + MILLISECONDS_IN_10_DAY) {
			return true;
		}

		return false;
	}

	/**
	 * show the reminder dialog
	 */
	public void show() {
		String message = mContext.getString(R.string.rate_my_app, mContext.getString(R.string.app_name));

		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View title = inflater.inflate(R.layout.reminder_title, null);
		new AlertDialog.Builder(mContext)
				  .setCustomTitle(title)
				  .setMessage(message)
				  .setPositiveButton(R.string.msg_rate, this)
				  .setNeutralButton(R.string.msg_later, this)
				  .setNegativeButton(R.string.msg_never, this)
				  .setOnCancelListener(this)
				  .create()
				  .show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = pref.edit();

		switch(which) {
			case DialogInterface.BUTTON_POSITIVE:
				editor.putBoolean(mContext.getString(R.string.pref_reminder_never_show), true);
				try {
					mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.zyz.mobile")));
				} catch (ActivityNotFoundException e) {
					mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.zyz.mobile")));
				}
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				reset();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				editor.putBoolean(mContext.getString(R.string.pref_reminder_never_show), true);
				break;
		}
		editor.commit();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
	}
}
