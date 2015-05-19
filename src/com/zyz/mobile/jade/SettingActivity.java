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
package com.zyz.mobile.jade;

import afzkl.development.mColorPicker.ColorPickerDialog;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.zyz.mobile.R;
import com.zyz.mobile.file.FileManager;

import java.io.File;


/**
 * preference activity and also the class to store preference related constants.
 *
 * @author ray
 */
public class SettingActivity extends PreferenceActivity implements
		  OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener
{

	private String mKeyBackgroundColor;
	private String mKeyHighlightColor;
	private String mKeyTextColor;
	private String mKeyDefTextColor;
	private String mKeyDefBackgroundColor;
	private String mKeyHighlightTime;
	private String mKeyVocabularySaveLocation;
	private String mKeyShareTextSaveLocation;


	private final static int RC_CHOOSE_VOCABULARY_FILE = 452;
	private final static int RC_CHOOSE_SHARE_TEXT_FOLDER = 738;


	public final static String APP_FOLDER_NAME = "JadeRead";
	public final static String VOCABULARY_FILE = "Vocabulary.txt";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		BaseActivity.setPreferredScreenOrientation(this);

		getWindow().setFormat(PixelFormat.RGBA_8888);

		addPreferencesFromResource(R.xml.pref);

		mKeyHighlightTime = getString(R.string.pref_highlight_time);

		mKeyVocabularySaveLocation = getString(R.string.pref_vocabulary_save_location);
		genericLocationPreference(
				  mKeyVocabularySaveLocation,
				  getString(R.string.pref_vocabulary_save_location_summary),
				  defaultVocabularySavePath()
		);

		mKeyShareTextSaveLocation = getString(R.string.pref_share_text_save_location);
		genericLocationPreference(
				  mKeyShareTextSaveLocation,
				  getString(R.string.pref_share_text_save_location_summary),
				  defaultSaveFolder()
		);

		mKeyBackgroundColor = getString(R.string.pref_bg_color);
		findPreference(mKeyBackgroundColor).setOnPreferenceClickListener(this);

		mKeyHighlightColor = getString(R.string.pref_highlight_color);
		findPreference(mKeyHighlightColor).setOnPreferenceClickListener(this);

		mKeyTextColor = getString(R.string.pref_text_color);
		findPreference(mKeyTextColor).setOnPreferenceClickListener(this);

		mKeyDefTextColor = getString(R.string.pref_definition_text_color);
		findPreference(mKeyDefTextColor).setOnPreferenceClickListener(this);

		mKeyDefBackgroundColor = getString(R.string.pref_definition_bg_color);
		findPreference(mKeyDefBackgroundColor).setOnPreferenceClickListener(this);

		ListPreference orientationPreference =
				  (ListPreference) findPreference(getString(R.string.pref_orientation));
		orientationPreference.setSummary(orientationPreference.getEntry());

		ListPreference lineSpacingPreference =
				  (ListPreference) findPreference(getString(R.string.pref_line_spacing));
		lineSpacingPreference.setSummary(lineSpacingPreference.getEntry());
	}

	private void genericLocationPreference(String key, String formatString, String defaultLocation) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Preference preferenceShareLocation = findPreference(key);
		String summary = String.format(formatString, prefs.getString(key, defaultLocation));
		preferenceShareLocation.setSummary(summary);
		preferenceShareLocation.setOnPreferenceClickListener(this);
	}

	/**
	 * get the current path used to save vocabulary
	 *
	 * @param context the context
	 * @return the current vocabulary file path
	 */
	public static String getVocabularySavePath(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(
				  context.getString(R.string.pref_vocabulary_save_location),
				  SettingActivity.defaultVocabularySavePath()
		);
	}

	/**
	 * get the current path used to save shared text
	 *
	 * @param context the context
	 * @return the current shared text folder
	 */
	public static File getSharedTextSaveFolder(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return new File(pref.getString(
				  context.getString(R.string.pref_share_text_save_location),
				  SettingActivity.defaultSaveFolder())
		);
	}

	private static String defaultVocabularySavePath() {
		if (Environment.getExternalStorageDirectory() != null) {
			File dir = new File(Environment.getExternalStorageDirectory(), APP_FOLDER_NAME);
			File file = new File(dir, VOCABULARY_FILE);
			return file.toString();
		}
		return "";
	}

	private static String defaultSaveFolder() {
		if (Environment.getExternalStorageDirectory() != null) {
			File dir = new File(Environment.getExternalStorageDirectory(), APP_FOLDER_NAME);
			return dir.toString();
		}
		return "";
	}

	/**
	 * Called when a Preference has been called
	 *
	 * @param preference The Preference that was clicked.
	 * @return True if the click was handled
	 */
	@Override
	public boolean onPreferenceClick(Preference preference) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = prefs.edit();

		String key = preference.getKey();

		if (key.equals(mKeyTextColor)) {
			pickColor(mKeyTextColor, getResources().getColor(R.color.default_text_color));
		}
		else if (key.equals(mKeyBackgroundColor)) {
			pickColor(mKeyBackgroundColor, getResources().getColor(R.color.default_bg_color));
		}
		else if (key.equals(mKeyHighlightColor)) {
			pickColor(mKeyHighlightColor, getResources().getColor(R.color.default_highlight_color));
		}
		else if (key.equals(mKeyDefTextColor)) {
			pickColor(mKeyDefTextColor, getResources().getColor(R.color.default_def_text_color));
		}
		else if (key.equals(mKeyDefBackgroundColor)) {
			pickColor(mKeyDefBackgroundColor, getResources().getColor(R.color.default_def_bg_color));
		}
		else if (key.equals(mKeyVocabularySaveLocation)) {
			pickPath(RC_CHOOSE_VOCABULARY_FILE, FileManager.EXTRA_INTERNAL_FILE_PICKER);
		}
		else if (key.equals(mKeyShareTextSaveLocation)) {
			pickPath(RC_CHOOSE_SHARE_TEXT_FOLDER, FileManager.EXTRA_INTERNAL_FOLDER_PICKER);
		}
		else if (key.equals(mKeyHighlightTime)) {
			editor.putString(mKeyHighlightTime,
					  prefs.getString(mKeyHighlightTime, getString(R.string.default_highlight_duration)));
			editor.commit();
		}
		else {
			return false;
		}
		return true;
	}

	/**
	 * open a FileManager for the user to pick a path
	 *
	 * @param requestCode the request code
	 */
	private void pickPath(int requestCode, int extra) {
		try {
			Intent intent = new Intent(this, FileManager.class);
			intent.putExtra("ManagerMode", extra);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(intent, requestCode);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.failed_no_filemanager, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();

		switch (requestCode) {
			case RC_CHOOSE_VOCABULARY_FILE:
				// if the selected files exists, update recent file list
				// and then open the file.
				if (resultCode == RESULT_OK && data != null) {
					File newFile = (File) data.getExtras().getSerializable(FileManager.FILE_PARCEL);

					// save the preference
					String newPath = newFile.getAbsolutePath().toString();
					editor.putString(mKeyVocabularySaveLocation, newPath);

					// change the preference summary to refer to the new location
					String formatString = getString(R.string.pref_vocabulary_save_location_summary);
					findPreference(mKeyVocabularySaveLocation).setSummary(String.format(formatString, newPath));
					editor.commit();
				}
				break;
			case RC_CHOOSE_SHARE_TEXT_FOLDER:
				if (resultCode == RESULT_OK && data != null) {
					File newFolder = (File) data.getExtras().getSerializable(FileManager.FILE_PARCEL);
					String newFolderPath = newFolder.getAbsolutePath().toString();
					editor.putString(mKeyShareTextSaveLocation, newFolderPath);

					String formatString = getString(R.string.pref_share_text_save_location_summary);
					findPreference(mKeyShareTextSaveLocation).setSummary(String.format(formatString, newFolderPath));
					editor.commit();
				}
				break;
		}
	}

	/**
	 * invokes a ColorPickerDialog and stores color value in the specified preference with the given
	 * key.
	 *
	 * @param key           the key of the preference to save the picked color
	 * @param default_color the default color to use
	 */
	private void pickColor(final String key, final int default_color) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int color = prefs.getInt(key, default_color);

		// http://code.google.com/p/color-picker-view/
		final ColorPickerDialog colorPicker = new ColorPickerDialog(this, color);

		colorPicker.setAlphaSliderVisible(true);
		colorPicker.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.msg_ok), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt(key, colorPicker.getColor());
				editor.commit();
			}
		});
		colorPicker.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.msg_default), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt(key, default_color);
				editor.commit();
			}
		});

		colorPicker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.msg_cancel), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		colorPicker.show();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference preference = findPreference(key);

		if (key.equals(getString(R.string.pref_orientation))) {
			BaseActivity.setPreferredScreenOrientation(this);
			preference.setSummary(((ListPreference) preference).getEntry());
		}
		else if (key.equals(getString(R.string.pref_line_spacing))) {
			preference.setSummary(((ListPreference) preference).getEntry());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
}
