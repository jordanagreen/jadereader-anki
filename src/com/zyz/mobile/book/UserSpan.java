package com.zyz.mobile.book;
/*
Copyright (C) 2014 Ray Zhou

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
Date: 2014 01 12

*/

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.UnderlineSpan;
import com.zyz.mobile.book.UserBookData.Attribute;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;

public class UserSpan implements Parcelable
{

	private int mStart; // inclusive
	private int mEnd; // inclusive
	private Object mSpanObject;
	private UserSpanType mType;
	private int mColor;
	private String mNote = "";
	private String mDescription = "";


	public UserSpan(UserSpanType type, int color, int start, int end)
	{
		this(type, color, start, end, null, null);
	}

	public UserSpan(UserSpanType type, int color, int start, int end, String note, String description)
	{
		CharacterStyle object = null;

		switch (type) {
			case HIGHLGHT:
				object = new BackgroundColorSpan(color);
				break;
			case UNDERLINE:
				object = new UnderlineSpan();
			case BOOKMARK:
			case NOTE:
				break;
			default:
				break;
		}
		Initialize(object, type, color, start, end, note, description);
	}

	/**
	 * set this span to the specified spannable
	 *
	 * @param spannable the spannable to be styled
	 */
	public void setSpan(Spannable spannable)
	{
		if (spannable != null && isValid(spannable)) {
			spannable.setSpan(mSpanObject, mStart, mEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		}
	}

	/**
	 * checks whether this span is valid against the specified spnnable
	 *
	 * @param spannable the spannable this {@code UserSpan} is created for
	 * @return true if this {@code UserSpan} is valid, false otherwise
	 */
	public boolean isValid(Spannable spannable)
	{
		if (mType == UserSpanType.NONE ||
				  mType != UserSpanType.BOOKMARK && mStart >= mEnd ||
				  mStart < 0) {
			// if it's a bookmark, only the {@ode mStart} is specified, so no need
			// to check for this condition
			return false;
		}
		return mEnd < spannable.length();
	}

	private void Initialize(Object spanObject, UserSpanType type, int color, int start, int end,
									String note, String description)
	{
		mColor = color;
		mType = type;
		mSpanObject = spanObject;
		mStart = start;
		mEnd = end;
		mNote = note == null ? "" : note;
		mDescription = description == null ? "" : description;
	}

	public boolean isStyleSpan()
	{
		return getType() == UserSpanType.HIGHLGHT ||
				  getType() == UserSpanType.UNDERLINE ||
				  getType() == UserSpanType.NOTE;
	}

	public boolean isOffsetInSpan(int offset)
	{
		return offset >= mStart && offset <= mEnd;
	}

	public int getColor()
	{
		return mColor;
	}

	@Nullable
	public Object getObject()
	{
		return mSpanObject;
	}

	public int getStart()
	{
		return mStart;
	}

	public int getEnd()
	{
		return mEnd;
	}

	public String getDescription()
	{
		return mDescription;
	}

	public void setDescription(String description)
	{
		if (description == null) {
			description = "";
		}
		mDescription = description;
	}

	public String getNote()
	{
		return mNote;
	}

	public void appendNote(String note)
	{
		mNote += note;
	}

	public UserSpanType getType()
	{
		return mType;
	}

	@Override
	public String toString()
	{
		return mDescription;
	}


	public String toXML()
	{
		switch (mType) {
			case BOOKMARK:
				return String.format("<%s %s='%d' %s='%s'/>", UserBookData.Element.BOOKMARK,
						  Attribute.POSITION, mStart,
						  Attribute.DESCRIPTION, StringEscapeUtils.escapeXml(mDescription));
			case NOTE:
				break;
			case HIGHLGHT:
			case UNDERLINE:
				return String.format("<%s %s='%d' %s='%d' %s='%d' %s='%d' %s='%s'/>", UserBookData.Element.SPAN,
						  Attribute.TYPE, mType.toValue(),
						  Attribute.COLOR, mColor,
						  Attribute.START, mStart,
						  Attribute.END, mEnd,
						  Attribute.DESCRIPTION, StringEscapeUtils.escapeXml(mDescription));
		}
		return "";
	}

	@SuppressWarnings("UnusedDeclaration")
	public static final Parcelable.Creator<UserSpan> CREATOR = new Parcelable.Creator<UserSpan>()
	{
		@Override
		public UserSpan createFromParcel(Parcel source)
		{
			int start = source.readInt();
			int end = source.readInt();
			UserSpanType type = UserSpanType.toEnum(source.readInt());
			int color = source.readInt();
			String note = source.readString();
			String description = source.readString();
			return new UserSpan(type, color, start, end, note, description);
		}

		@Override
		public UserSpan[] newArray(int size)
		{
			return new UserSpan[size];
		}
	};

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeInt(mStart);
		dest.writeInt(mEnd);
		dest.writeInt(mType.toValue());
		dest.writeInt(mColor);
		dest.writeString(mNote);
		dest.writeString(mDescription);
	}


	public static class Builder
	{

		private int mStart;
		private int mEnd;
		private UserSpanType mType;
		private int mColor;
		private String mNote = "";
		private String mDescription = "";

		public Builder()
		{

		}

		public Builder setStart(int start)
		{
			mStart = start;
			return this;
		}

		public Builder setEnd(int end)
		{
			mEnd = end;
			return this;
		}

		public Builder setNote(String note)
		{
			mNote = note;
			return this;
		}

		public Builder setType(UserSpanType type)
		{
			mType = type;
			return this;
		}

		public Builder setColor(int color)
		{
			mColor = color;
			return this;
		}


		@SuppressWarnings("UnusedDeclaration")
		public Builder setDescription(String description)
		{
			mDescription = description;
			return this;
		}

		public UserSpan create()
		{
			return new UserSpan(mType, mColor, mStart, mEnd, mNote, mDescription);
		}

	}

}
