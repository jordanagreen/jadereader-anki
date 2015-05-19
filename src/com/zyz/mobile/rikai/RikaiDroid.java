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

MOTE that the code in the file ported from the RikaiChan plugin for Firefox
which is the algorithm on how to searh and deinflect Japanese word

If you are interested in the FireFox plugin, please visit www.polarcloud.com/rikaichan/
*/
package com.zyz.mobile.rikai;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import com.zyz.mobile.util.Text;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;


public class RikaiDroid {
	private SQLiteDatabase m_DictDB = null;

	public final static int DEFAULT_MAX_COUNT = 10;

	public final static char[] ch = new char[]{
			  '\u3092', '\u3041', '\u3043', '\u3045', '\u3047', '\u3049', '\u3083', '\u3085', '\u3087', '\u3063', '\u30FC', '\u3042', '\u3044', '\u3046',
			  '\u3048', '\u304A', '\u304B', '\u304D', '\u304F', '\u3051', '\u3053', '\u3055', '\u3057', '\u3059', '\u305B', '\u305D', '\u305F', '\u3061',
			  '\u3064', '\u3066', '\u3068', '\u306A', '\u306B', '\u306C', '\u306D', '\u306E', '\u306F', '\u3072', '\u3075', '\u3078', '\u307B', '\u307E',
			  '\u307F', '\u3080', '\u3081', '\u3082', '\u3084', '\u3086', '\u3088', '\u3089', '\u308A', '\u308B', '\u308C', '\u308D', '\u308F', '\u3093'};
	public final static char[] cv = new char[]{
			  '\u30F4', '\uFF74', '\uFF75', '\u304C', '\u304E', '\u3050', '\u3052', '\u3054', '\u3056', '\u3058', '\u305A', '\u305C', '\u305E', '\u3060',
			  '\u3062', '\u3065', '\u3067', '\u3069', '\uFF85', '\uFF86', '\uFF87', '\uFF88', '\uFF89', '\u3070', '\u3073', '\u3076', '\u3079', '\u307C'};
	public final static char[] cs = new char[]{
			  '\u3071', '\u3074', '\u3077', '\u307A', '\u307D'};

	private ArrayList<String> difReasons = new ArrayList<String>();
	private ArrayList<RuleGroup> difRuleGroups = new ArrayList<RuleGroup>();

	public RikaiDroid() {
	}

	/**
	 * load the dictionary sqlitedatabase for use. make sure to call close() (whether this returns true
	 * or false) when you no longer use the dictionary, usually onDestroy.
	 *
	 * @return true if dictionary is loaded, false otherwise
	 */
	public boolean loadData(String dictionary_path, String deinflect_data_path) {
		return loadEdict(dictionary_path) && loadDeinflectionData(deinflect_data_path);
	}

	private boolean loadEdict(String dictionary_path) {

		try {
			m_DictDB = SQLiteDatabase.openDatabase(dictionary_path, null, SQLiteDatabase.OPEN_READONLY);
			return true;
		} catch (SQLiteException e) {

		}
		return false;

	}

	/**
	 * close the database connection to the dictionary. this should be called if loadData() is called
	 */
	public void close() {
		if (m_DictDB != null) {
			m_DictDB.close();
		}
	}

	private boolean loadDeinflectionData(String deinflect_data_path) {
		ArrayList<String> difData = Text.readToArray(deinflect_data_path, 350);

		if (difData.size() == 0) {
			return false;
		}

		RuleGroup group = new RuleGroup();

		int prevLen = -1;
		// i = 1, skip header
		for (int i = 1; i < difData.size(); i++) {
			String[] f = difData.get(i).split("\t");

			if (f.length == 1) {
				difReasons.add(f[0]);
			}
			else if (f.length == 4) {
				Rule rule = new Rule(f);

				if (prevLen != rule.from.length()) {
					group = new RuleGroup();
					group.flen = rule.from.length();
					prevLen = group.flen;
					difRuleGroups.add(group);
				}
				group.add(rule);
			}
		}

		return true;
	}

	/**
	 * find the word in the dictionary database that matches either the word or the kana column
	 *
	 * @param word the word
	 * @return cursor
	 */
	private Cursor findWord(String word) {
		return m_DictDB.rawQuery(
				  " SELECT * " +
							 " FROM dictionary" +
							 " WHERE word = ? OR kana = ?",
				  new String[]{word, word}
		);

	}

	/**
	 * find all variants of the specified word, if any
	 *
	 * @param word the base word
	 * @return all the variants of the specified word
	 */
	public Entries wordSearch(String word) {
		return findVariants(word, false, DEFAULT_MAX_COUNT);
	}

	/**
	 * finds all possible deinflected words of the given word
	 *
	 * @param word       the word to search for
	 * @param searchName true if we are searching name, false if we are searching regular dictionary
	 * @param maxCount   maximum number of variants to return, default to DEFAULT_MAX_COUNT
	 * @return an Entries encompassing the results.
	 */
	@SuppressWarnings("ConstantConditions")
	public Entries findVariants(String word, boolean searchName, int maxCount) {

		int[] trueLen = new int[word.length()]; // modify by toHiragana
		word = toHiragana(word, true, trueLen);

		maxCount = maxCount > 0 ? maxCount : DEFAULT_MAX_COUNT;
		int count = 0;

		// final result
		Entries result = new Entries();

		Pattern pattern = Pattern.compile("[,\\(\\)]");

SEARCH_WORDS:
		while (word.length() > 0) {
			boolean showInf = (count != 0);

			// current word, plus all of its possible deinflected words
			ArrayList<DeinflectedWord> variants;

			if (searchName) {
				// no need to deinflect a name
				variants = new ArrayList<DeinflectedWord>();
				variants.add(new DeinflectedWord(word, 0xff, null));
			}
			else {
				variants = this.deinflect(word);
			}

			// search dictionary and see if each deinflected word exists
			for (int i = 0; i < variants.size(); i++) {

				DeinflectedWord dw = variants.get(i);


				// find this deinflected word in the dictionary.
				// if coursor.count() > 0, that means this is a valid word
				Cursor cursor = findWord(dw.word);

				while (cursor.moveToNext()) {

					String defn = cursor.getString(cursor.getColumnIndex("defn"));

					boolean valid = true;

					// check if the entry is a valid de-inflected word
					if (i > 0) {
						// i = 0 is the original word, no need to check
						// i > 0 a de-inflected word, need to check
						String[] parts = pattern.split(defn);

						int k;
						for (k = parts.length - 1; k >= 0; k--) {
							// i am guessing, only words of these part of speech
							// can have inflection.
							if ((dw.type & 1) != 0 && parts[k].equals("v1")) { break; }
							if ((dw.type & 4) != 0 && parts[k].equals("adj-i")) { break; }
							if ((dw.type & 2) != 0 && parts[k].startsWith("v5")) { break; }
							if ((dw.type & 16) != 0 && parts[k].startsWith("vs-")) { break; }
							if ((dw.type & 8) != 0 && parts[k].equals("vk")) { break; }
						}
						valid = (k != -1);
					}

					if (valid) {

						String reason = "";
						if (variants.get(i).reason != "") {
							if (showInf) {
								reason = "< " + variants.get(i).reason + " < " + word;
							}
							else {
								reason = "< " + variants.get(i).reason;
							}
						}

						// the Table value
						// (  0,    1,     2,    3,     4,    5,    6)
						// (_id, word, wmark, kana, kmark, show, defn)
						result.add(new Entry(cursor.getString(1),
								  cursor.getInt(5) == 1 ? cursor.getString(3) : "",
								  cursor.getString(6),
								  reason));

						if (result.size() == 1) {
							// the first word has the longest length of the variants because
							// this loop find words by keep taking away characters from the end
							result.setMaxLen(word.length());
						}

						if (++count >= maxCount) {
							result.setComplete(true);
							cursor.close();
							break SEARCH_WORDS;
							// no need to search anymore, break out of all loops
						}
					}
				}
				cursor.close();
			}
			word = word.substring(0, word.length() - 1);
		}

		return result;
	}

	/**
	 * returns a list of deinflected word, including the original word
	 *
	 * @param word the word to deinflect
	 * @return a list of deinflected word
	 */
	public ArrayList<DeinflectedWord> deinflect(String word) {

		ArrayList<DeinflectedWord> result = new ArrayList<DeinflectedWord>();

		// All the words added to the result list so far.
		// (key, value) where key is the word, and value is the index
		// of the word in the result
		Map<String, Integer> dws = new TreeMap<String, Integer>();

		DeinflectedWord dw = new DeinflectedWord(word, 0xff, "");
		result.add(dw); // add the word itself to the result
		dws.put(word, 0);

		// note that result.size() may increase after each iteration
		for (int i = 0; i < result.size(); i++) {
			word = result.get(i).word;

			// check the word against each Rule in each RuleGroup
			for (int j = 0; j < difRuleGroups.size(); j++) {
				RuleGroup group = difRuleGroups.get(j);

				if (group.flen > word.length()) {
					/*
					 * if the word length is shorter than the inflection From-Length,
					 * no need to check this group, move to the next group (with a 
					 * small length)
					 */
					continue;
				}
				
				/* get the last part of the word (precisely the last group.flen 
				 * characters of the word) and check if it's a valid inflection
				 */
				String tail = word.substring(word.length() - group.flen);

				for (int k = 0; k < group.size(); k++) {
					Rule rule = group.get(k);

					if ((result.get(i).type & rule.type) == 0 ||
							  !tail.equals(rule.from))
					{
						continue; // failed, go to the next rule
					}

					String newWord = word.substring(0, word.length() - group.flen) + rule.to;

					if (newWord.length() <= 1) {
						continue;
					}

					dw = new DeinflectedWord();
					if (dws.get(newWord) != null) {
						// deinflected word is same as previous ones
						// but under A different rule
						dw = result.get(dws.get(newWord));
						dw.type |= (rule.type >> 8);
						continue;
					}

					dws.put(newWord, result.size());
					dw.word = newWord;
					dw.type = (rule.type >> 8);
					if (result.get(i).reason.length() > 0) {
						dw.reason = difReasons.get(rule.reasonIndex) + " < " + result.get(i).reason;
					}
					else {
						dw.reason = difReasons.get(rule.reasonIndex);
					}

					result.add(dw);
				}
			}
		}

		return result;
	}


	/**
	 * converts all katakana and half-width kana to full-width hiragana
	 *
	 * @param word the word to convert
	 * @return the hiragana
	 */
	public static String toHiragana(String word) {
		return toHiragana(word, false, null);
	}

	/**
	 * convert katakana and half-width kana to full-width hiragana
	 *
	 * @param word    the word to convert
	 * @param discard if true, conversion is stopped when a non-japanese char is encountered
	 * @return the hiragana
	 */
	@SuppressWarnings("ConstantConditions")
	public static String toHiragana(String word, boolean discard, int[] trueLen) {
		char u, v;
		char previous = 0;
		String result = "";

		for (int i = 0; i < word.length(); ++i) {
			u = v = word.charAt(i);

			if (u <= 0x3000) {
				if (discard) {
					break;
				}
				// else nothing to do for this char
			}
			// full-width katakana to hiragana
			else if ((u >= 0x30A1) && (u <= 0x30F3)) {
				u -= 0x60;
			}
			// half-width katakana to hiragana
			else if ((u >= 0xFF66) && (u <= 0xFF9D)) {
				u = ch[u - 0xFF66];
			}
			// voiced (used in half-width katakana) to hiragana
			else if (u == 0xFF9E) {
				if ((previous >= 0xFF73) && (previous <= 0xFF8E)) {
					result = result.substring(0, result.length() - 1);
					u = cv[previous - 0xFF73];
				}
			}
			// semi-voiced (used in half-width katakana) to hiragana
			else if (u == 0xFF9F) {
				if ((previous >= 0xFF8A) && (previous <= 0xFF8E)) {
					result = result.substring(0, result.length() - 1);
					u = cs[previous - 0xFF8A];
				}
			}
			// ignore J~
			else if (u == 0xFF5E) {
				previous = 0;
				continue;
			}

			result += u;
			if (trueLen != null) {
				trueLen[result.length() - 1] = i + 1;
			}
			previous = v;
		}

		return result;
	}
}
