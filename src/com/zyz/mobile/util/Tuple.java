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

package com.zyz.mobile.util;


public class Tuple
{

	public static class Pair<A, B> {

		private A a;
		private B b;
		public Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}

		public A getA() { return a; }
		public B getB() { return b; }
	}

	public static class BookOpener
	{
		private String mPath;
		private String mMessage;
		private CharSequence mText;

		public BookOpener() {}

		public String getMessage() {
			return mMessage;
		}

		public BookOpener setMessage(String message) {
			mMessage = message;
			return this;
		}

		public String getPath() {
			return mPath;
		}

		public String getPath(String defaultValue) {
			return mPath == null ? defaultValue : mPath;
		}

		public BookOpener setPath(String path) {
			mPath = path;
			return this;
		}

		public CharSequence getText() {
			return mText;
		}

		public BookOpener setText(CharSequence text) {
			mText = text;
			return this;
		}
	}
}
