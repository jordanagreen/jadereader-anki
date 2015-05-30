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
package com.zyz.mobile.book;

import java.io.File;

public class BookMetadata {

//	public static final String ENCODING_AUTO        = "AUTO";
//	public static final String ENCODING_EUC_JP      = "EUC-JP";
//	public static final String ENCODING_ISO_2022_JP = "ISO-2022-JP";
//	public static final String ENCODING_SHIFT_JIS   = "SHIFT_JIS";
//	public static final String[] JAPANESE_ENCODINGS = {
//		ENCODING_AUTO, ENCODING_EUC_JP, ENCODING_SHIFT_JIS, ENCODING_ISO_2022_JP
//	};

	private String mFilePath = "";
	private String mBookName = "";
	private String mXmlPath = "";
	private String mEncoding = null;

	public BookMetadata(String[] info) {
		if (info.length >= 1) {
			mFilePath = info[0];
		}
		if (info.length >= 2) {
			mBookName = info[1];
		}
		if (info.length >= 3) {
			mEncoding = info[2];
		}
	}

	public BookMetadata() {
	}

	public void setEncoding(String encoding) {
		mEncoding = encoding;
	}

	/**
	 * @return the encoding. null if no encoding has been previously set.
	 */
	public String getEncoding() {
		return null;
	}

	public void setBookName(String bookName) {
		mBookName = bookName;
	}

	public String getBookName() {
		return mBookName;
	}

	public void setFilePath(String filePath) {
		mFilePath = filePath;
	}

	public String getFileDirectory() {
		if (mFilePath != null && !mFilePath.isEmpty()) {
			try {
				File file = new File(mFilePath);

			} catch (Exception e) {

			}
		}
		return null;
	}

	public String getFilePath() {
		return mFilePath;
	}

	public void setXmlPath(String xmlPath) {
		mXmlPath = xmlPath;
	}

	public String getXmlPath() {
		return mXmlPath;
	}

	public String getSaveString() {
		return mFilePath + '\t' + mBookName;
	}

	/**
	 * checks whether the file path of the specified BookMetadata is the same as the this. This is
	 * equivalent to {@code this.getFilePath().equalsIgnoreCase(o.getFilePath())}
	 *
	 * @param o the {@code BookMetadata} object to compare with
	 * @return true if the path of the specified {@code BookMetadata} is the same as this, false
	 *         otherwise.
	 */
	public boolean pathEquals(BookMetadata o) {
		if (o == null) {
			return false;
		}
		if (this == o) {
			return true;
		}
		return mFilePath.equalsIgnoreCase(o.getFilePath());
	}

	@Override
	public String toString() {
		return mBookName;
	}
}
