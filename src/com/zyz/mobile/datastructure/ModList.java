package com.zyz.mobile.datastructure;
/*
Copyright (C) 2014 Ray Zhou

Author: ray
Date: 2014-02-16

*/


import java.util.ArrayList;

public class ModList<T>
{

	private ArrayList<T> mArrayList;

	private int mMaxSize;

	private int mCurPos;

	public ModList(int maxSize)
	{
		mMaxSize = maxSize;
		mCurPos = -1;
		mArrayList = new ArrayList<T>(maxSize);
	}

	public int size()
	{
		return mArrayList.size();
	}

	public void add(T element)
	{
		if (mArrayList.size() < mMaxSize) {
			mArrayList.add(element);
			mCurPos = increment(mCurPos);
		}
		else {
			mCurPos = increment(mCurPos);
			mArrayList.set(mCurPos, element);
		}
	}

	public void clear()
	{
		mArrayList.clear();
		mCurPos = 0;
	}

	/**
	 * Get the ith item, in most recently added order.
	 *
	 * @param i the index of the item
	 * @return the ith item in most recently added order.
	 */
	public T getMostRecentlyAdded(int i)
	{
		if (i >= mArrayList.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return mArrayList.get(getMostRecentlyAddedIndex(i));
	}

	/**
	 * get the ith item in the least recently added order
	 *
	 * @param i the index of the item
	 * @return the ith item in the least recently added order.
	 */
	public T getLeastRecentlyAdded(int i)
	{
		if (i >= mArrayList.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return mArrayList.get(getLeastRecentlyAddedIndex(i));
	}

	private int getLeastRecentlyAddedIndex(int i)
	{
		int o = mArrayList.size() < mMaxSize ? i : (increment(mCurPos) + i) % mMaxSize;
		if (o < 0) {
			o += mMaxSize;
		}
		return o;
	}

	private int getMostRecentlyAddedIndex(int i)
	{
		int p = (mCurPos - i) % mMaxSize;
		if (p < 0) {
			p += mMaxSize;
		}
		return p;
	}

	private int increment(int pos)
	{
		return (pos + 1) % mMaxSize;
	}

	private int decrement(int pos)
	{
		int p = (pos - 1) % mMaxSize;
		if (p < 0) {
			p += mMaxSize;
		}
		return p;
	}
}
