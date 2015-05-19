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

public class Rule {
	public String from;
	public String to;
	public int type;
	public int reasonIndex;
	
	public Rule() {
		
	}
	
	public Rule(String[] rule) {
		// no error checking
		from = rule[0];
		to = rule[1];
		type = Integer.parseInt(rule[2]);
		reasonIndex = Integer.parseInt(rule[3]);
	}
	
	public Rule(String from, String to, int type, int reasonIndex) {
		this.from = from;
		this.to = to;
		this.type = type;
		this.reasonIndex = reasonIndex;
	}
}