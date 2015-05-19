package com.zyz.mobile.jade;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Window;
import com.zyz.mobile.R;
import com.zyz.mobile.book.LocationHistory;
import com.zyz.mobile.book.UserBookData;
import com.zyz.mobile.bookmark.*;
import com.zyz.mobile.misc.DataKeeper;
import com.zyz.mobile.util.AndroidService;

/**
 *
 */
public class BookmarkActivity extends FragmentActivity {

	private final static int NUM_TAB = 3;

	UserBookData mUserBookData = null;

	private boolean mIsEdited = false;

	private int mCurrentOffset;

	//ViewPager mViewPager;
	//BookmarkFragmentAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUserBookData = (UserBookData) DataKeeper.getInstance().get(getString(R.string.key_bookdata));

		mCurrentOffset = getIntent().getIntExtra(getString(R.string.key_bookmark_offset), -1);

		// have to request the feature here instead of setting it in the manifest.xml.
		// otherwise the style of the tabs will be different
		requestWindowFeature(Window.FEATURE_NO_TITLE);

//		setContentView(R.layout.bookmark_pager_activity);
//		mViewPager = (ViewPager) findViewById(R.id.bookmark_pager);
//		mAdapter = new BookmarkFragmentAdapter(getSupportFragmentManager());
//		mViewPager.setAdapter(mAdapter);

		setContentView(R.layout.bookmark_tab_activity);
		FragmentTabHost tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		tabHost.setup(this, getSupportFragmentManager(), R.id.bookmarkcontent);

		String highlights = getString(R.string.tab_highlights);
		String bookmarks = getString(R.string.tab_bookmarks);
		String history = getString(R.string.tab_history);

		tabHost.addTab(tabHost.newTabSpec(history).setIndicator(history), HistoryFragment.class, null);
		tabHost.addTab(tabHost.newTabSpec(highlights).setIndicator(highlights), HighlightFragment.class, null);
		tabHost.addTab(tabHost.newTabSpec(bookmarks).setIndicator(bookmarks), BookmarkFragment.class, null);

	}

	@Override
	protected void onResume() {
		super.onResume();

		applyPreference();
	}

	/**
	 * apply preferences that apply to this activity
	 */
	private void applyPreference() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean show = pref.getBoolean(getString(R.string.pref_show_status_bar), true);
		AndroidService.showStatusBar(this, show);
	}


	private OnEditListener mOnEditListener = new OnEditListener() {
		@Override
		public void OnEdit() {
			mIsEdited = true;
		}
	};

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		if (fragment instanceof SpanFragment) {
			((SpanFragment) fragment).setOnEditListener(mOnEditListener);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocationHistory history = mUserBookData.getLocationHistory();
		if (history.size() == 0 ||
				  mCurrentOffset >= 0 && history.get(0).getStart() != mCurrentOffset)
		{
			mUserBookData.getLocationHistory().add(mCurrentOffset);
			mIsEdited = true;
		}
		if (mIsEdited) { // save the xml
			mUserBookData.save();
		}
	}

	//	public static class BookmarkFragmentAdapter extends FragmentPagerAdapter {
//
//		public BookmarkFragmentAdapter(FragmentManager fm)
//		{
//			super(fm);
//		}
//
//		@Override
//		public Fragment getItem(int i)
//		{
//			switch(i) {
//				case 1:
//					return new HighlightFragment();
//				case 2:
//					return new BookmarkFragment();
//				default:
//					return new BookmarkFragment();
//			}
//		}
//
//		@Override
//		public int getCount()
//		{
//			return NUM_TAB;
//		}
//	}
}
