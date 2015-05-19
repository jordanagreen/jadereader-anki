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
package com.zyz.mobile.book;

import android.text.Spannable;
import android.text.Spanned;
import android.util.Log;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.List;


/**
 * Manage the xml info file of a book
 */
public class UserBookData extends DefaultHandler
{

	private static final String TAG = "UserBookData";

	public static final String BOOKMARK_LEFT = "[";
	public static final String BOOKMARK_RIGHT = "]";
	/**
	 * path of the info xml
	 */
	private File mInfoFile;

	private int mVersion = 0;

	private UserSpan mCurrentSpanObj; // current span object
	private StringBuilder mCurrentSpanObjNote = null;

	/**
	 * current position of the book
	 */
	private int mOffset;

	private Spannable mSpannable;

	private static final int MAX_DESCRIPTION_LENGTH = 30;

	/**
	 * tracks highlight/bookmarks/notes
	 */
	private UserSpanTracker mBookmarkTracker;
	private UserSpanTracker mNoteTracker;
	private UserSpanTracker mHighlighTracker; // for highlight and underline

	private LocationHistory mLocationHistory;

	private Status mStatus = new Status();

	public UserBookData(File infoFile, Spannable spannable)
	{
		mInfoFile = infoFile;
		mSpannable = spannable;

		mBookmarkTracker = new UserSpanTracker();
		mHighlighTracker = new UserSpanTracker();
		mNoteTracker = new UserSpanTracker();
		mLocationHistory = new LocationHistory(this);
	}

	/**
	 * open the info xml file. create one if it cannot be found
	 */
	public boolean parse()
	{
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader reader = sp.getXMLReader();
			reader.setContentHandler(this);
			reader.parse(new InputSource(new FileReader(mInfoFile)));
		} catch (Exception e) {
			// constructor doesn't check for validity of the file
			// catch all exceptions here
			Log.e(TAG, "Failed to parse xml file?");
			return false;
		}
		return true;
	}

	/**
	 * saves the interests to a xml file
	 */
	public void save()
	{
		boolean success = false;
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("xml", "tmp", mInfoFile.getParentFile());
			BufferedWriter bw = new BufferedWriter(
					  new OutputStreamWriter(new FileOutputStream(tmpFile)));
			bw.write(toXml());
			bw.close();
			success = true;
		} catch (Exception e) {
			Log.e(TAG, "failed to save file");
		} finally {
			if (success) {
				success = tmpFile.renameTo(mInfoFile); // don't care if it fails
				if (!success) {
					Log.e(TAG, "failed to overwrite saved file");
				}
			}
		}
	}

	/**
	 * Sets the current offset (scroll position) of the book
	 *
	 * @param offset the offset
	 */
	public void setOffset(int offset)
	{
		mOffset = offset;
	}

	/**
	 * get the current offset (scroll position) of the book
	 *
	 * @return the current offset of the book
	 */
	public int getOffset()
	{
		return mOffset;
	}


	/**
	 * remove the given span from this book if it exists
	 *
	 * @param span the span to remove
	 * @return true if remove is successful, false otherwise
	 */
	public boolean removeSpan(UserSpan span)
	{
		boolean success = false;

		switch (span.getType()) {
			case BOOKMARK:
				success = mBookmarkTracker.removeSpan(span);
				break;
			case NOTE:
				success = mNoteTracker.removeSpan(span);
				break;
			case HIGHLGHT:
			case UNDERLINE:
				success = mHighlighTracker.removeSpan(span);
				break;
		}

		if (success) {
			mSpannable.removeSpan(span.getObject());
		}

		return success;
	}

	public void insertSpan(UserSpan span)
	{

		if (span == null || span.getStart() < 0 || span.getEnd() >= mSpannable.length()) {
			// illegal arguments, do nothing
			return;
		}

		switch (span.getType()) {
			case BOOKMARK:
				mBookmarkTracker.insertSpan(span);
				break;
			case NOTE:
				mNoteTracker.insertSpan(span);
				break;
			case HIGHLGHT:
			case UNDERLINE:
				mHighlighTracker.insertSpan(span);
				break;
		}

		updateSpannable(span);
	}

	public void replaceSpan(UserSpan newSpan, UserSpan oldSpan)
	{
		boolean success = false;

		switch (newSpan.getType()) {
			case BOOKMARK:
				success = mBookmarkTracker.replaceSpan(newSpan, oldSpan);
				break;
			case NOTE:
				success = mNoteTracker.replaceSpan(newSpan, oldSpan);
				break;
			case HIGHLGHT:
			case UNDERLINE:
				success = mHighlighTracker.replaceSpan(newSpan, oldSpan);
				break;
		}

		if (success) {
			mSpannable.removeSpan(oldSpan.getObject());
			updateSpannable(newSpan);
		}
	}

	/**
	 * get the short text span by the specified {@code UserSpan}
	 *
	 * @param userSpan the user span
	 * @return the short text beginning at the specified {@code UserSpan}
	 */
	public String getDefaultDescription(UserSpan userSpan)
	{
		String description = "";
		int start = userSpan.getStart();
		int end = start + MAX_DESCRIPTION_LENGTH;

		if (start < mSpannable.length()) {
			if (userSpan.getType() == UserSpanType.HIGHLGHT ||
					  userSpan.getType() == UserSpanType.UNDERLINE) {
				end = Math.min(userSpan.getEnd(), end);

			}

			if (userSpan.getType() == UserSpanType.BOOKMARK) {
				float percentage = ((float) start / (float) mSpannable.length() * 100);
				description = String.format("%s%.2f%%%s ", BOOKMARK_LEFT, percentage, BOOKMARK_RIGHT);
			}

			end = Math.min(end, mSpannable.length());
			description += mSpannable.subSequence(start, end).toString();

			if ((userSpan.getType() == UserSpanType.HIGHLGHT ||
					  userSpan.getType() == UserSpanType.UNDERLINE) &&
					  userSpan.getEnd() > end) {
				description += "...";
			}
		}
		return description;
	}


	/**
	 * Find the first span that covers the specified offset
	 *
	 * @param offset an offset of the text
	 * @return the span that covers the specified offset, null if not found.
	 */
	public UserSpan findHighlightSpan(int offset)
	{
		return mHighlighTracker.findSpan(offset);
	}

	/**
	 * draw the specified {@code UserSpan} on the spannable
	 *
	 * @param userSpan the span to be drawn
	 */
	private void updateSpannable(UserSpan userSpan)
	{
		if (userSpan.isStyleSpan()) {
			mSpannable.setSpan(userSpan.getObject(), userSpan.getStart(), userSpan.getEnd(),
					  Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		}
	}

	public String toXml()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<%s %s='%d' %s='%d'>", Element.BOOK,
				  Attribute.OFFSET, mOffset,
				  Attribute.VERSION, mVersion));

		userSpanTrackerToXml(sb, mBookmarkTracker, Element.BOOKMARKS);
		userSpanTrackerToXml(sb, mHighlighTracker, Element.SPANS);
		sb.append(mLocationHistory.toXml());

		sb.append(String.format("</%s>", Element.BOOK)); // </book>
		return sb.toString();
	}

	/**
	 * translate the specified {@code UserSpanTracker} to XML
	 *
	 * @param sb      the {@code StringBuilder} to append the resulting XML to
	 * @param tracker the {@code UserSpanTracker}  to translate
	 * @param tag     the xml tag of this {@code UserSpanTracker}
	 */
	private void userSpanTrackerToXml(StringBuilder sb, UserSpanTracker tracker, String tag)
	{
		if (tracker.size() > 0) {
			sb.append(String.format("<%s>", tag));
			for (UserSpan span : tracker) {
				sb.append(span.toXML());
			}
			sb.append(String.format("</%s>", tag));
		}
	}


	public List<UserSpan> getBookmarksListReadOnly()
	{
		return mBookmarkTracker.getList();
	}

	public List<UserSpan> getHighlightListReadOnly()
	{
		return mHighlighTracker.getList();
	}

	public LocationHistory getLocationHistory()
	{
		return mLocationHistory;
	}

	/**********************************/
	/**** ContentHandler methods ******/
	/**
	 * ******************************
	 */

	@Override
	public void startDocument() throws SAXException
	{
	}

	@Override
	public void endDocument() throws SAXException
	{
	}

	@SuppressWarnings("UnusedAssignment")
	@Override
	public void startElement(String namespaceURI, String localName,
									 String qName, Attributes atts) throws SAXException
	{
		if (localName.equals(Element.BOOK)) {
			mStatus.in_book = true;
			try {
				if (atts.getLength() >= 1) {
					setOffset(Integer.parseInt(atts.getValue(0)));
				}
				if (atts.getLength() >= 2) {
					mVersion = Integer.parseInt(atts.getValue(1));
				}
			} catch (NumberFormatException e) {

			}
		}
		else if (localName.equals(Element.SPANS)) {
			mStatus.in_spans = true;
		}
		else if (localName.equals(Element.SPAN)) {
			mStatus.in_span = true;

			try {
				int i = 0;
				if (atts.getLength() >= 4) {
					int type = Integer.parseInt(atts.getValue(i++));
					int color = Integer.parseInt(atts.getValue(i++));
					int start = Integer.parseInt(atts.getValue(i++));
					int end = Integer.parseInt(atts.getValue(i++));

					mCurrentSpanObj = new UserSpan(UserSpanType.toEnum(type), color, start, end);
					insertSpan(mCurrentSpanObj);
				}
				if (atts.getLength() >= 5) {
					mCurrentSpanObj.setDescription(StringEscapeUtils.unescapeXml(atts.getValue(i)));
				}
			} catch (NumberFormatException e) {
				// should not happen unless the file is corrupted or modified incorrectly
			}
		}
		else if (localName.equals(Element.NOTE)) {
			mStatus.in_note = true;
		}
		else if (localName.equals(Element.BOOKMARKS)) {
			mStatus.in_bookmarks = true;
		}
		else if (localName.equals(Element.BOOKMARK)) {
			mStatus.in_bookmark = true;
			int i = 0;

			try {
				if (mStatus.in_bookmarks) {
					if (atts.getLength() >= 1) {
						int start = Integer.parseInt(atts.getValue(i++));
						mCurrentSpanObj = (new UserSpan.Builder()).setType(UserSpanType.BOOKMARK).setStart(start).create();
						insertSpan(mCurrentSpanObj);
					}
					if (atts.getLength() >= 2) {
						mCurrentSpanObj.setDescription(StringEscapeUtils.unescapeXml(atts.getValue(i)));
					}
				}
				else if (mStatus.in_history) {
					if (atts.getLength() >= 1) {
						int offset = Integer.parseInt(atts.getValue(i++));
						mLocationHistory.add(offset);
					}
				}
			} catch (NumberFormatException e) {

			}

		}
		else if (localName.equals(Element.HISTORY)) {
			mStatus.in_history = true;
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName,
								  String qName) throws SAXException
	{
		if (localName.equals(Element.BOOK)) {
			mStatus.in_book = false;
		}
		else if (localName.equals(Element.NOTE)) {
			mStatus.in_note = false;
		}
		else if (localName.equals(Element.BOOKMARKS)) {
			mStatus.in_bookmarks = false;
		}
		else if (localName.equals(Element.BOOKMARK)) {
			mStatus.in_bookmark = false;
		}
		else if (localName.equals(Element.SPANS)) {
			mStatus.in_spans = false;
		}
		else if (localName.equals(Element.SPAN)) {
			mStatus.in_span = false;
		}
		else if (localName.equals(Element.HISTORY)) {
			mStatus.in_history = false;
		}
	}

	@Override
	public void characters(char[] ch, int start, int len) throws SAXException
	{
		if (mStatus.in_note) {
			mCurrentSpanObj.appendNote(new String(ch, start, len));
			if (mCurrentSpanObjNote == null) {
				mCurrentSpanObjNote = new StringBuilder(new String(ch, start, len));
			}
			else {
				mCurrentSpanObjNote.append(new String(ch, start, len));
			}
		}
	}


	private static class Status
	{
		/**
		 * parser tag status
		 */
		public boolean in_book = false;
		public boolean in_note = false;
		public boolean in_bookmarks = false;
		public boolean in_bookmark = false;
		public boolean in_spans = false;
		public boolean in_span = false;
		public boolean in_history = false;
	}


	public static class Element
	{
		public static final String BOOK = "book";
		public static final String SPAN = "span";
		public static final String SPANS = "spans";
		public static final String NOTE = "note";
		public static final String BOOKMARK = "mark";
		public static final String BOOKMARKS = "bookmarks";
		public static final String HISTORY = "history";
	}

	public static class Attribute
	{
		public static final String START = "a";
		public static final String END = "b";
		public static final String POSITION = "p";
		public static final String TYPE = "t";
		public static final String COLOR = "c";
		public static final String DESCRIPTION = "d";
		public static final String VERSION = "version";
		public static final String OFFSET = "offset";
	}
}
