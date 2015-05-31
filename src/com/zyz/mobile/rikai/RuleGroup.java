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

import java.util.ArrayList;

public class RuleGroup {
	// the "from" field of each Rule in a RuleGroup must have the same length
	public int flen; 
	private ArrayList<Rule> rules = new ArrayList<Rule>();
	
	public void add(Rule rule) {
		rules.add(rule);
	}
	
	public Rule get(int index) {
		return rules.get(index);
	}
	
	public int size() {
		return rules.size();
	}

}