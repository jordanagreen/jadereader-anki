package com.zyz.mobile.bookmark;
/*
Copyright (C) 2014 Ray Zhou

Author: ray
Date: 2014-02-06

*/

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.zyz.mobile.R;
import com.zyz.mobile.book.UserBookData;
import com.zyz.mobile.book.UserSpan;
import com.zyz.mobile.jade.JTextActivity;
import com.zyz.mobile.misc.DataKeeper;

import java.util.List;

public abstract class SpanFragment extends Fragment {

	protected UserBookData mUserBookData = null;

	private ListView mSpanListView;


	private final static int ACTION_GOTO = Menu.FIRST;
	private final static int ACTION_DELETE = Menu.FIRST + 1;

	private OnEditListener mOnEditListener = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUserBookData = (UserBookData) DataKeeper.getInstance().get(getString(R.string.key_bookdata));
	}


	private void refreshSpanList() {
		mSpanListView.setAdapter(new SpanAdapter(getActivity(), R.layout.bookmark_item, getSpanList()));

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.bookmark_fragment, container, false);

		mSpanListView = (ListView) view.findViewById(R.id.bookmark_items);
		mSpanListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				goToBookmark((UserSpan) view.getTag(R.id.tag_span));
			}
		});
		registerForContextMenu(mSpanListView);

//		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//		view.setBackgroundColor(pref.getInt(getString(R.string.pref_bg_color),
//				  getActivity().getResources().getColor(R.color.default_bg_color)));

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshSpanList();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if (v == mSpanListView) {
			menu.add(Menu.NONE, ACTION_GOTO, Menu.NONE, R.string.action_span_goto);
			menu.add(Menu.NONE, ACTION_DELETE, Menu.NONE, R.string.action_span_delete);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (!AdapterView.AdapterContextMenuInfo.class.isInstance(item.getMenuInfo())) {
			return false;
		}

		AdapterView.AdapterContextMenuInfo menuInfo =
				  (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		final UserSpan userSpan = (UserSpan) mSpanListView.getAdapter().getItem(menuInfo.position);

		switch (item.getItemId()) {
			case ACTION_GOTO:
				goToBookmark(userSpan);
				break;
			case ACTION_DELETE:
				deleteBookmark(userSpan);
				refreshSpanList();
				break;
			default:
				return false;
		}
		return true;
	}

	private void deleteBookmark(UserSpan userSpan) {
		if (userSpan != null) {
			boolean success = mUserBookData.removeSpan(userSpan);
			if (success) {
				if (mOnEditListener != null) {
					mOnEditListener.OnEdit();
				}
			}
		}
	}


	private void goToBookmark(UserSpan userSpan) {
		if (userSpan != null) {
			Intent intent = new Intent(getActivity(), JTextActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.putExtra(getString(R.string.key_bookmark_offset), userSpan.getStart());
			startActivity(intent);
			getActivity().finish();
		}
	}

	public void setOnEditListener(OnEditListener onEditListener) {
		mOnEditListener = onEditListener;
	}

	protected abstract List<UserSpan> getSpanList();
}
