package com.zyz.mobile.datastructure;
/*
Copyright (C) 2013 Ray Zhou

Author: ray
Date: 2013-06-20

*/

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * {@code Stack} is a Last-In/First-Out(LIFO) data structure which represents a
 * stack of objects. It enables users to pop to and push from the stack,
 * including null objects. There is no limit to the size of the stack.
 */
public class Stack<E>  {

	protected ArrayList<E> mArray;

	/**
	 * Constructs a stack with the default size of {@code ArrayList}.
	 */
	public Stack() {
		mArray = new ArrayList<E>();
	}

	/**
	 * Constrcuts a stack with the specified inital capacity
	 * @param initialCapacity the initial capacity
	 */
	public Stack(int initialCapacity) {
		mArray = new ArrayList<E>(initialCapacity);
	}

	/**
	 * Returns whether the stack is empty or not.
	 *
	 * @return {@code true} if the stack is empty, {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return mArray.isEmpty();
	}

	/**
	 * Returns the element at the top of the stack without removing it.
	 *
	 * @return the element at the top of the stack.
	 * @throws java.util.EmptyStackException
	 *             if the stack is empty.
	 * @see #pop
	 */
	@SuppressWarnings("unchecked")
	public E peek() {
		try {
			return mArray.get(mArray.size() - 1);
		} catch (IndexOutOfBoundsException e) {
			throw new EmptyStackException();
		}
	}

	/**
	 * Returns the element at the top of the stack and removes it.
	 *
	 * @return the element at the top of the stack.
	 * @throws EmptyStackException
	 *             if the stack is empty.
	 * @see #peek
	 * @see #push
	 */
	@SuppressWarnings("unchecked")
	public E pop() {
		if (mArray.size() == 0) {
			throw new EmptyStackException();
		}
		return mArray.remove(mArray.size() - 1);
	}

	/**
	 * Pushes the specified object onto the top of the stack.
	 *
	 * @param object
	 *            The object to be added on top of the stack.
	 * @return the object argument.
	 * @see #peek
	 * @see #pop
	 */
	public E push(E object) {
		mArray.add(object);
		return object;
	}

	/**
	 * Returns the index of the first occurrence of the object, starting from
	 * the top of the stack.
	 *
	 * @return the index of the first occurrence of the object, assuming that
	 *         the topmost object on the stack has a distance of one.
	 * @param o
	 *            the object to be searched.
	 */
	public int search(E o) {
		int size = mArray.size();
		if (o != null) {
			for (int i = size - 1; i >= 0; i--) {
				if (o.equals(mArray.get(i))) {
					return size - i;
				}
			}
		} else {
			for (int i = size - 1; i >= 0; i--) {
				if (mArray.get(i) == null) {
					return size - i;
				}
			}
		}
		return -1;
	}

	/**
	 * return the size of the stack
	 * @return the size of the stack
	 */
	public int size() {
		return mArray.size();
	}
}
