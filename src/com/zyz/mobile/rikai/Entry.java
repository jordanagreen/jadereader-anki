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
package com.zyz.mobile.rikai;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * a dictionary entry
 * consists of the word and the definition (optionally the reading)
 * @author ray
 *
 */
public class Entry implements Parcelable{
	
	/** the word **/
	private String word = "";
	
	/** the reading of this word in hiragana, if exists */
	private String reading = "";
	
	/** the definition of the word */
	private String gloss = "";
	
	/** the inflection reason */
	private String reason = "";
	
	/** the length used internallly for the stringbuilder when returning a string 
	 * representation of this Entry, for efficiency
	 */
	private int length = 0;
	
	public Entry() {}
	
	/**
	 * constructor
	 * @param word the word
	 * @param reading the reading of the word
	 * @param gloss the definition of the word
	 * @param reason how the original inflected word transformed to the given word
	 */
	public Entry(String word, String reading, String gloss, String reason) {
		this.word = word;
		this.reading = reading;
		this.gloss = gloss;
		this.reason = reason;
		this.length = word.length() + gloss.length() + reading.length() + reason.length() + 20;
	}
	
	/**
	 * use to create from parcel
	 * @param word
	 * @param reading
	 * @param gloss
	 * @param reason
	 * @param length
	 */
	private Entry(String word, String reading, String gloss, String reason, int length) {
		this.word = word;
		this.reading = reading;
		this.gloss = gloss;
		this.reason = reason;
		this.length = length;
	}
	
	
	/**
	 * return a string representation of this entry
	 * @return a string representation of this entry
	 */
	public String toString() {
		StringBuilder result = new StringBuilder(length);
		
		result.append(word).append(' ');
		
		if (reading.length() != 0) {
			result.append(reading).append(' ');
		}

//		if (reason.length() != 0) {
//			result.append("(").append(reason).append(") ");
//		}
		
		result.append(gloss);
		
		return result.toString();
	}
	
	/**
	 * returns the word of this entry
	 * @return the word of this entry
	 */
	public String getWord() {
		return word;
	}
	
	/**
	 * return a string representation of this entry in a compact form (max 1 newline character)
	 * @return a string representation of this entry in a compact form.
	 */
	public String toStringCompact() {
		StringBuilder result = new StringBuilder(length);
		
		result.append(word).append(' ');
		
		if (reading.length() != 0) {
			result.append('[').append(reading).append("] ");
		}
		if (reason.length() != 0) {
			result.append("(").append(reason).append(")");
		}
		
		result.append('\n').append(gloss);
		
		return result.toString();
	}

	public final static Parcelable.Creator<Entry> CREATOR = 
		new Parcelable.Creator<Entry>() {
		
		@Override
		public Entry createFromParcel(Parcel source) {
			return new Entry(
					source.readString(),
					source.readString(),
					source.readString(),
					source.readString(),
					source.readInt());
		}

		@Override
		public Entry[] newArray(int size) {
			return new Entry[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(word);
		dest.writeString(reading);
		dest.writeString(gloss);
		dest.writeString(reason);
		dest.writeInt(length);
		// make sure to change createFromParcel when u change this
	}
}
