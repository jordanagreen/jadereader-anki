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

import android.content.Context;
import android.os.Environment;
import com.zyz.mobile.util.Util;

import java.io.File;

/**
 * User: ray
 * Date: 2013-01-21
 *
 * A simple class that contains the url and path information of the dictionaries used
 * by this app. The path is depended on a context, hence the methods are not static.
 */
final public class DictionaryInfo {
	private static final String DICTIONARY_ZIP_FILE = "edict2.zip";
	private static final String DICTIONARY_SQL_FILE = "edict2.sql";
	private static final String DICTIONARY_DEINFECT_FILE = "deinflect.dat";
	private static final String DICTIONARY_ZIP_URL = "http://www.appwalk.com/edict2.zip";
	private final Context mContext;

	public DictionaryInfo(Context context) {
		mContext = context;
	}

	public String dataPath() {
		return mContext.getExternalFilesDir(null).toString();
	}

	public String url() {
		return DICTIONARY_ZIP_URL;
	}

	public String zip() {
		return Util.makePath(dataPath(), DICTIONARY_ZIP_FILE);
	}

	public String dictionary() {
		return Util.makePath(dataPath(), DICTIONARY_SQL_FILE);
	}

	public String deinflect_data() {
		return Util.makePath(dataPath(), DICTIONARY_DEINFECT_FILE);
	}

	public boolean exists() {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return false;
		}

		if (new File(dictionary()).exists() && new File(deinflect_data()).exists()) {
			return true;
		}

		return false;
	}
}
