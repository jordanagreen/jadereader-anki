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
Date: 2013 06 03

*/
package com.zyz.mobile.widget;

/**
 * Created with IntelliJ IDEA. User: ray Date: 2013-06-06 Time: 7:42 AM
 */
public interface Concealable {

	public void conceal();
	public void reveal();
	public boolean isDisplaying();

}
