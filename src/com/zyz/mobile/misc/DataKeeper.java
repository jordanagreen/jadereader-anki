package com.zyz.mobile.misc;
/*
Copyright (C) 2014 Ray Zhou

Author: ray
Date: 2014-02-05

*/

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class DataKeeper
{
	private final static DataKeeper mDataKeeper = new DataKeeper();
	private final static HashMap<String, Object> mData = new HashMap<String, Object>();

	private DataKeeper()
	{
	}

	public static DataKeeper getInstance()
	{
		return mDataKeeper;
	}

	public void save(String key, Object value)
	{
		mData.put(key, new WeakReference<Object>(value));
	}

	public Object get(String key)
	{
		Object ref = mData.get(key);
		if (ref != null) {
			Object result = ((WeakReference) ref).get();
			if (result != null) {
				return result;
			}
			mData.remove(key);
		}
		return null;
	}
}
