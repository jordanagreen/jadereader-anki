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
package com.zyz.mobile.jade;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.zyz.mobile.R;
import com.zyz.mobile.book.BookMetadata;
import com.zyz.mobile.file.FileManager;

import java.io.File;

/**
 * Name changed from JadeActivity to BookshelfActivity (This will probably break user's shortcut
 * when the user updates to this version of the application, but I rath update it earlier than
 * later.
 * <p/>
 * Changed it back to JadeActivity just before release :( Because it will probably confuse the hell
 * out of ordinary users.
 */
public class /*BookshelfActivity*/JadeActivity extends BaseActivity
		  implements OnItemClickListener, View.OnClickListener
{


	private static final int ACTION_DELETE_HISTORY_ITEM = Menu.FIRST;
	private static final int ACTION_DELETE_HISTORY_ALL = Menu.FIRST + 1;
	private static final int ACTION_DELETE_ITEM_FROM_PHONE = Menu.FIRST + 2;
	private static final int ACTION_CHANGE_DISPLAY_NAME = Menu.FIRST + 3;
	private static final int ACTION_GO_TO_DIRECTORY = Menu.FIRST + 4;

	private static final String TAG = "JTEXT_BOOKSHELF";

	private ListView mRecentFilesView;
	private RecentFilesList mRecentFilesList;

	private ArrayAdapter<BookMetadata> mRecentFilesSource;

	protected final static int RC_OPEN_FILE = 1;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.bookshelf_activity);

		Button open_button = (Button) findViewById(R.id.bookshelf_open);
		open_button.setOnClickListener(this);

		Button option_button = (Button) findViewById(R.id.bookshelf_option);
		option_button.setOnClickListener(this);

		Button help_button = (Button) findViewById(R.id.bookshelf_help);
		help_button.setOnClickListener(this);

		mRecentFilesView = (ListView) findViewById(R.id.bookshelf_history);
		mRecentFilesView.setOnItemClickListener(this);
		registerForContextMenu(mRecentFilesView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadRecentFilesList();
	}


	private void loadRecentFilesList() {
		mRecentFilesList = new RecentFilesList(this);

		boolean success = mRecentFilesList.open();
		if (!success) {
			new AlertDialog.Builder(this)
					  .setMessage(R.string.confirm_recent_file_list_corruption)
					  .setPositiveButton(R.string.msg_yes, new DialogInterface.OnClickListener() {
						  @Override
						  public void onClick(DialogInterface dialog, int which) {
							  mRecentFilesList.clear();
							  mRecentFilesList.save();
							  refreshRecentFilesList();
						  }
					  })
					  .setNeutralButton(R.string.msg_try_again, new DialogInterface.OnClickListener() {
						  @Override
						  public void onClick(DialogInterface dialog, int which) {
							  loadRecentFilesList();
						  }
					  })
					  .setNegativeButton(R.string.msg_cancel, null)
					  .create()
					  .show();
		}
		else {
			refreshRecentFilesList();
		}
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getBoolean(getString(R.string.pref_reopen_immediately), false)) {
			// the last read file is gonna be the first one on the
			// recently read file list
			String path = mRecentFilesList.getRecentFilePath(0);
			if (!path.isEmpty() && (new File(path)).exists()) {
				startJTextIntent(path);
			}
		}
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		if (v == mRecentFilesView) {
			menu.setHeaderTitle(R.string.delete_history_title);
			menu.add(Menu.NONE, ACTION_DELETE_HISTORY_ITEM, Menu.NONE, R.string.delete_history);
			menu.add(Menu.NONE, ACTION_DELETE_HISTORY_ALL, Menu.NONE, R.string.delete_all_history);
			menu.add(Menu.NONE, ACTION_DELETE_ITEM_FROM_PHONE, Menu.NONE, R.string.delete_from_phone);
			menu.add(Menu.NONE, ACTION_CHANGE_DISPLAY_NAME, Menu.NONE, R.string.change_display_name);
			menu.add(Menu.NONE, ACTION_GO_TO_DIRECTORY, Menu.NONE, R.string.go_to_directory);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (!AdapterView.AdapterContextMenuInfo.class.isInstance(item.getMenuInfo())) {
			return false;
		}

		AdapterView.AdapterContextMenuInfo menuInfo =
				  (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		final BookMetadata selectedFile = (BookMetadata) mRecentFilesView.getAdapter().getItem(menuInfo.position);

		switch (item.getItemId()) {
			case ACTION_DELETE_HISTORY_ALL:
				mRecentFilesList.clear();
				refreshRecentFilesList();
				break;
			case ACTION_DELETE_HISTORY_ITEM:
				mRecentFilesList.removeFromRecentFiles(selectedFile, false /* don't delete from phone */);
				refreshRecentFilesList();
				break;
			case ACTION_DELETE_ITEM_FROM_PHONE:
				new AlertDialog.Builder(this)
						  .setMessage(R.string.file_delete_confirm)
						  .setPositiveButton(R.string.msg_yes, new DialogInterface.OnClickListener() {
							  @Override
							  public void onClick(DialogInterface dialog, int which) {
								  mRecentFilesList.removeFromRecentFiles(selectedFile, true /* delete from phone */);
								  refreshRecentFilesList();
							  }
						  })
						  .setNegativeButton(R.string.msg_no, null)
						  .create()
						  .show();
				break;
			case ACTION_CHANGE_DISPLAY_NAME:
				final EditText textbox = new EditText(this);
				textbox.setText(selectedFile.getBookName());
				textbox.selectAll();

				AlertDialog nameChangeDialog = new AlertDialog.Builder(this)
						  .setTitle(R.string.new_name)
						  .setView(textbox)
						  .setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
							  @Override
							  public void onClick(DialogInterface dialog, int which) {
								  selectedFile.setBookName(textbox.getText().toString());
								  refreshRecentFilesList();
							  }
						  })
						  .setNegativeButton(R.string.msg_cancel, null)
						  .create();
				// this force the keyboard to show up
				nameChangeDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				nameChangeDialog.show();
				break;
			case ACTION_GO_TO_DIRECTORY:
				goToParentDirectory(selectedFile.getFilePath());
				break;
			default:
				return false;
		}
		return true;
	}

	private void refreshRecentFilesList() {

		mRecentFilesSource = new ArrayAdapter<BookMetadata>(this, R.layout.simple_list_item,
				  mRecentFilesList.geList());
		mRecentFilesView.setAdapter(mRecentFilesSource);
	}


	/**
	 * browse the phone for a text file
	 */
	public void chooseFile() {
		try {
			Intent intent = new Intent(this, FileManager.class);
			intent.putExtra("ManagerMode", FileManager.EXTRA_INTERNAL_MANAGER);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(intent, RC_OPEN_FILE);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.failed_no_filemanager, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case RC_OPEN_FILE:

				Uri uri;

				// if the selected files exists, update recent file list
				// and then open the file.
				if (resultCode == RESULT_OK && data != null &&
						  (uri = data.getData()) != null &&
						  uri.getPath() != null &&
						  uri.getLastPathSegment() != null)
				{
					startJTextIntent(uri.getPath());
				}

				break;
		}
	}

	/**
	 * open the text file with FastText
	 *
	 * @param path the text file
	 */
	protected void startJTextIntent(String path) {
		mRecentFilesList.addToRecentFiles(path);
		mRecentFilesSource.notifyDataSetChanged(); // refresh mRecentFilesView

		Intent intent = new Intent(this, JTextActivity.class);

		Uri data = new Uri.Builder().path(path).build();
		intent.setData(data);
		intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
									long id)
	{
		startJTextIntent(mRecentFilesList.geList().get(position).getFilePath());
	}


	@Override
	protected void onPause() {
		super.onPause();
		mRecentFilesList.save();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.bookshelf_open:
				chooseFile();
				break;
			case R.id.bookshelf_option: {
				Intent intent = new Intent(this, SettingActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				break;
			}
			case R.id.bookshelf_help: {
				Intent intent = new Intent(this, HelpActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				break;
			}
		}
	}


	/**
	 * Go to the parent directory of the specified file in Jade Reader's file manager. This function
	 * will fail with a Toast indicating the errors.
	 *
	 * @param filePath the path of the file whose parent directory we want to visit
	 */
	public void goToParentDirectory(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			showToast(R.string.failed_file_not_found);
			return;
		}
		else if (file.getParentFile() == null || !file.getParentFile().exists()) {
			showToast(R.string.failed_dir_not_found);
			return;
		}

		try {
			Intent intent = new Intent(this, FileManager.class);
			intent.putExtra("ManagerMode", FileManager.EXTRA_EXTERNAL_MANAGER);
			intent.setData(Uri.fromFile(file.getParentFile()));
			startActivity(intent);
		} catch (Exception e) {
			showToast(R.string.failed_unknown_error);
		}
	}

}
