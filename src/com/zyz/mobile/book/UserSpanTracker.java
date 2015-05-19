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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class UserSpanTracker implements Iterable<UserSpan>, Parcelable
{

	/**
	 * Array of all the (highlight) span, must be kept sorted
	 */
	private ArrayList<UserSpan> mUserSpans = new ArrayList<UserSpan>();


	public UserSpanTracker()
	{
	}

	public UserSpanTracker(ArrayList<UserSpan> in)
	{
		mUserSpans = in;
	}

	public List<UserSpan> getList()
	{
		return Collections.unmodifiableList(mUserSpans);
	}

	public int size()
	{
		return mUserSpans.size();
	}

	public UserSpan get(int i)
	{
		if (i >= 0 && i < mUserSpans.size()) {
			return mUserSpans.get(i);
		}
		return null;
	}

	/**
	 * find the first UserSpan that overlap the given offset.
	 *
	 * @param offset the offset
	 * @return the UserSpan if found, null otherwise
	 */
	public UserSpan findSpan(int offset)
	{
		for (UserSpan spanObj : mUserSpans) {
			if (spanObj.isOffsetInSpan(offset)) {
				return spanObj;
			}
		}
		return null;
	}

//	public void removeSpansBetween(int start, int end)
//	{
//		for (int i = 0; i < mUserSpans.size(); ++i) {
//			if (mUserSpans.get(i).getStart() >= start &&
//					  mUserSpans.get(i).getEnd() <= end) {
//				mSpannable.removeSpan(mUserSpans.remove(i).getObject());
//			}
//			else if (mUserSpans.get(i).getStart() >= end) {
//				// since this is sorted, we break when the current
//				// span objects passed the 'end' we are searching for
//				break;
//			}
//		}
//	}


	/**
	 * replace the old span with the new span
	 *
	 * @param newSpan the new span
	 * @param oldSpan the old span to be replaced
	 * @return true if replaced successfully, false if the the span is not found
	 */
	public boolean replaceSpan(UserSpan newSpan, UserSpan oldSpan)
	{
		int index = mUserSpans.indexOf(oldSpan);
		if (index >= 0) {
			mUserSpans.set(index, newSpan);
			return true;
		}
		return false;
	}

	public boolean removeSpan(UserSpan span)
	{
		if (mUserSpans.remove(span)) {
			return true;
		}
		return false;
	}

	/**
	 * insert the specified {@code UserSpan} to the tracker
	 *
	 * @param userSpan the span to be inserted.
	 * @return true if insertion is successful, false otherwise
	 */
	public boolean insertSpan(UserSpan userSpan)
	{
		if (mUserSpans.isEmpty() ||
				  userSpan.getStart() >= mUserSpans.get(mUserSpans.size() - 1).getStart()) {
			mUserSpans.add(userSpan);
		}
		else {
			// find the insert location, then insert
			// using linear search for now
			// TODO: change be changed to binary search

			for (int i = 0; i <= mUserSpans.size(); ++i) {
				if (mUserSpans.get(i).getStart() > userSpan.getStart()) {
					mUserSpans.add(i, userSpan);
					break;
				}
			}
		}
		return true;
	}

	/////////////////////////////////
	// Iterable<UserSpan> overrides
	/////////////////////////////////
	@Override
	public Iterator<UserSpan> iterator()
	{
		return mUserSpans.iterator();
	}

	//////////////////////////////
	// Parcelable overrides
	//////////////////////////////
	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeTypedList(mUserSpans);
	}

	public static final Parcelable.Creator<UserSpanTracker> CREATOR =
			  new Parcelable.Creator<UserSpanTracker>()
			  {
				  @Override
				  public UserSpanTracker createFromParcel(Parcel source)
				  {
					  ArrayList<UserSpan> userSpans = source.createTypedArrayList(UserSpan.CREATOR);
					  return new UserSpanTracker(userSpans);
				  }

				  @Override
				  public UserSpanTracker[] newArray(int size)
				  {
					  return new UserSpanTracker[size];
				  }
			  };
}
