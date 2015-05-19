package com.zyz.mobile.book;
/*
Copyright (C) 2014 Ray Zhou

Author: ray
Date: 2014-02-13

*/

import com.zyz.mobile.datastructure.ModList;

public class LocationHistory
{

	private final static int MAX_HISTORY_SIZE = 10;

	private ModList<UserSpan> mHistoryList;


	/**
	 * the text
	 */
	private UserBookData mUserBookData;


	public LocationHistory(UserBookData bookdata)
	{
		mHistoryList = new ModList<UserSpan>(MAX_HISTORY_SIZE);
		mUserBookData = bookdata;
	}

	public UserSpan get(int index)
	{
		return mHistoryList.getMostRecentlyAdded(index);
	}


	public void add(int offset)
	{
		UserSpan span = (new UserSpan.Builder())
				  .setType(UserSpanType.BOOKMARK)
				  .setStart(offset)
				  .create();
		span.setDescription(mUserBookData.getDefaultDescription(span));

		mHistoryList.add(span);
	}

	/**
	 * save the history to xml in the least recently added order
	 *
	 * @return
	 */
	public String toXml()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("<%s>", UserBookData.Element.HISTORY));
		for (int i = 0; i < size(); i++) {
			UserSpan userSpan = mHistoryList.getLeastRecentlyAdded(i);
			sb.append(userSpan.toXML());
		}
		sb.append(String.format("</%s>", UserBookData.Element.HISTORY));

		return sb.toString();
	}

	public int size()
	{
		return mHistoryList.size();
	}

}
