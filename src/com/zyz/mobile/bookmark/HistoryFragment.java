package com.zyz.mobile.bookmark;
/*
Copyright (C) 2014 Ray Zhou

Author: ray
Date: 2014-02-16

*/

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.zyz.mobile.R;
import com.zyz.mobile.book.UserBookData;
import com.zyz.mobile.book.UserSpan;
import com.zyz.mobile.jade.JTextActivity;
import com.zyz.mobile.misc.DataKeeper;

public class HistoryFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragmentView = inflater.inflate(R.layout.bookmark_fragment, container, false);

		ListView historyView = (ListView) fragmentView.findViewById(R.id.bookmark_items);
		historyView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				goToBookmark((UserSpan) parent.getAdapter().getItem(position));
			}
		});

		UserBookData bookata = (UserBookData) DataKeeper.getInstance().get(getString(R.string.key_bookdata));

		HistoryAdapter adapter = new HistoryAdapter(getActivity(), R.layout.bookmark_item, bookata.getLocationHistory());
		historyView.setAdapter(adapter);
		return fragmentView;
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
}
