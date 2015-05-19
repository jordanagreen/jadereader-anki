package com.zyz.mobile.book;
/*
Copyright (C) 2013 Ray Zhou

Author: ray
Date: 2013-12-03

*/

public enum UserSpanType
{
	NONE(0),
	HIGHLGHT(1),
	UNDERLINE(2),
	BOOKMARK(3),
	NOTE(4);

	private final int mValue;

	private UserSpanType(int value) {
		mValue = value;
	}

	/**
	 * Convert the current {@code UserSpanType} to an integer value
	 * @return an int representing the {@code UserSpanType}
	 */
	public int toValue() {
		return mValue;
	}

	/**
	 * Convert the specified integer to a {@code UserSpanType}. A valid int can be obtained
	 * from {@code toValue}.
	 * @param value the integer to be converted
	 * @return the {@code UserSpanType} converted from the specified int. {@code UserSpanType.NONE}
	 * 		  is returned if the specified integer is not valid.
	 */
	public static UserSpanType toEnum(int value) {
		switch (value) {
			case 1:
				return HIGHLGHT;
			case 2:
				return UNDERLINE;
			case 3:
				return BOOKMARK;
			case 4:
				return NOTE;
			default:
				return NONE;
		}
	}
}
