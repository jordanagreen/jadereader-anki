package com.zyz.mobile.bookmark;
/*
Copyright (C) 2014 Ray Zhou

Author: ray
Date: 2014-02-06

*/

import com.zyz.mobile.book.UserSpan;

import java.util.List;

public class HighlightFragment extends SpanFragment
{

	@Override
	protected List<UserSpan> getSpanList()
	{
		return mUserBookData.getHighlightListReadOnly();
	}
}
