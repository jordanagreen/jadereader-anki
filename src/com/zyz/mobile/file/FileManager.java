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
package com.zyz.mobile.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.*;
import com.zyz.mobile.R;
import com.zyz.mobile.datastructure.Stack;
import com.zyz.mobile.util.Util;
import com.zyz.mobile.widget.MenuBar;
import com.zyz.mobile.widget.MyButton;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Specialized file manager for JadeRead
 * <p/>
 * This activity can return a result of the Uri of the file picked by the user ONLY IF the selected
 * file is a text file. Otherwise, this file manager will try to find the appropriate program to
 * open the file picked by the user.
 * <p/>
 * To change the current directory of the manager, set the current directory with <code>
 * mCurrentDirectory = new File("/some/directory"); refreshView(); // refresh display with new
 * current directory value </code>
 *
 * @author ray
 */
public class FileManager extends Activity {

	@SuppressWarnings("UnusedDeclaration")
	private static final String TAG = "JTEXT_FILE_MANAGER";

	/*
	Start the FileManager with one of these extra parameters
	#EXTRA_INTERNAL_MANAGER will create a full fledge file manager
	#EXTRA_INTERNAL_FILE_PICKER will create a file chooser
	 */
	public static final int EXTRA_INTERNAL_MANAGER = 0; // invoked from within Jade Reader
	public static final int EXTRA_INTERNAL_FILE_PICKER = 1;
	public static final int EXTRA_INTERNAL_FOLDER_PICKER = 2;
	public static final int EXTRA_EXTERNAL_MANAGER = 7; // invoked from outside Jade Reader
	// TODO: Consider changing EXTRA_INTERNAL_FILE_PICKER and EXTRA_INTERNAL_FOLDER_PICKER
	// to a more generic Android intent such as ACTION_PICK

	private int mManagerMode; // can be one of the above constants

	public static final String FILE_PARCEL = "FilePicked";


	// please access this variable using getSelectionMode and setSelectionMode
	private FileSelectionMode mSelectionMode = FileSelectionMode.NONE;

	/**
	 * for readability only, no real purpose
	 */
	private static final boolean ITEM_CHECKED = true;

	// used for the mClipboard operations, to determine how a paste should function
	private static final int ACTION_CUT_FILES = 1;
	private static final int ACTION_COPY_FILES = 2;

	// File Manager list context menu id
	// private static final int BUTTON_ID_MULTISELECT = 0;
	private static final int BUTTON_ID_NEW = 1;
	private static final int BUTTON_ID_PASTE = 2;
	private static final int BUTTON_ID_CUT = 3;
	private static final int BUTTON_ID_COPY = 4;
	private static final int BUTTON_ID_DELETE = 5;
	private static final int BUTTON_ID_RENAME = 6;
	//private static final int BUTTON_ID_CHOOSE = 7;
	private static final int BUTTON_ID_BACK = 8;
	//private static final int BUTTON_ID_SEARCH = 9;
	private static final int BUTTON_ID_CANCEL = 10;
	//private static final int BUTTON_ID_TITLE = 11;

	/**
	 *
	 */
	private Toast mToast;

	/**
	 * the display list of all files in the current directory
	 */
	private ListView mFileListView;

	/**
	 * represents an empty direcotry
	 */
	private TextView mEmptyDirectoryView;

	/**
	 * menu bar
	 */
	private MenuBar mMenuBar;

	/**
	 * current directory
	 */
	private File mCurrentDir;
	private int mInitialDirIndex;

	/**
	 * saves the position of the fileList of each previously visited directory to restore the position
	 * when the user moves back.
	 */
	private Stack<NavigationInfo> mNavigationStack = new Stack<NavigationInfo>(10);

	private TextView mHeader;

	/**
	 * acted as the mClipboard cut/copy and pasting files
	 */
	private FileClipboard mClipboard = new FileClipboard();


	private HorizontalScrollView mPathScroller;
	private LinearLayout mPathLayout;


	/**
	 * Handle back button press twice to exit
	 */
	private Handler mBackHandler = new Handler();
	private boolean mBackPressedOnce = false;
	private Runnable mResetBackPressState = new Runnable() {
		@Override
		public void run() {
			mBackPressedOnce = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.file_manager_activity);

		mToast = new Toast(this);

		mHeader = new TextView(this);
		mHeader.post(new Runnable() {
			public void run() {
				mHeader.setHeight(mMenuBar.getHeight());
			}
		});

		// horizontal scrolling of the clickable path near the top of the file manager
		mPathScroller = (HorizontalScrollView) findViewById(R.id.file_manager_path_scroller);
		mPathLayout = (LinearLayout) findViewById(R.id.file_manager_path_container);

		File file;
		if (getIntent().getData() != null && getIntent().getData().getPath() != null) {
			file = mCurrentDir = new File(getIntent().getData().getPath());
		}
		else {
			file = mCurrentDir = Environment.getExternalStorageDirectory();
		}

		Stack<File> stack = new Stack<File>();
		while ((file = file.getParentFile()) != null) {
			stack.push(file);
		}
		mInitialDirIndex = stack.size();
		while (!stack.isEmpty()) {
			file = stack.pop();
			appendNavigationButton(file.getName());
			mNavigationStack.push(new NavigationInfo(file));
		}
		appendNavigationButton(mCurrentDir.getName());

		mManagerMode = getIntent().getIntExtra("ManagerMode", EXTRA_INTERNAL_MANAGER);
		mMenuBar = (MenuBar) findViewById(R.id.file_manager_menu_bar);

		switch (mManagerMode) {
			case EXTRA_INTERNAL_FILE_PICKER:
				createPickerMenuBar(EXTRA_INTERNAL_FILE_PICKER);
				break;
			case EXTRA_INTERNAL_FOLDER_PICKER:
				createPickerMenuBar(EXTRA_INTERNAL_FOLDER_PICKER);
				break;
			default:
				createManagerMenuBar();
				break;
		}
		initializeFileList();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mToast.cancel();
	}

	/**
	 * on back pressed, go to parent directory
	 */
	@Override
	public void onBackPressed() {
		if (getSelectionMode() == FileSelectionMode.SELECT) {
			setSelectionMode(FileSelectionMode.NONE, FileOperationState.NORMAL);
		}
		else if (mInitialDirIndex == mNavigationStack.size()) {
			if (mBackPressedOnce) {
				mBackHandler.removeCallbacks(mResetBackPressState);
				super.onBackPressed();
			}
			else {
				mBackPressedOnce = true;
				showToast(R.string.fyi_press_again_to_exit);
				// reset mBackPressedOnce to false after 2 seconds
				mBackHandler.postDelayed(mResetBackPressState, 2000);
			}
		}
		else {
			navigateToParent();
		}
	}


	/**
	 * navigate to the directory at the specified index in the stack
	 *
	 * @param index the index
	 * @return true if we are able to go to the specified index , false otherwise
	 */
	private boolean navigateToPosition(int index) {
		if (index < 0 || mNavigationStack.isEmpty() || mNavigationStack.size() <= index) {
			return false;
		}

		NavigationInfo ni = null;
		int count = mNavigationStack.size() - index;
		while (mNavigationStack.size() > index) {
			ni = mNavigationStack.pop();
		}
		if (ni != null) {
			refreshView(ni);
			mCurrentDir = ni.file;

			if (index < mInitialDirIndex) {
				mInitialDirIndex = index;
			}

			mPathLayout.removeViews(mPathLayout.getChildCount() - count, count);
			return true;
		}
		else {
			//noinspection ConstantConditions
			assert (false);
		}
		return false;
	}

	/**
	 * navigate to the parent of this directory
	 *
	 * @return true if there is a parent folder, false otherwise
	 */
	private boolean navigateToParent() {
		if (!mNavigationStack.isEmpty()) {
			return navigateToPosition(mNavigationStack.size() - 1);
		}
		return false;
	}

	/**
	 * add a navigation button to the end with the specified name
	 *
	 * @param name the name of the button
	 */
	private void appendNavigationButton(String name) {
		MyButton button = new MyButton(this);
		button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		button.setTextColor(getResources().getColor(R.color.white));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			button.setBackground(getResources().getDrawable(R.drawable.jade_button));
		}
		else {
			button.setBackgroundDrawable(getResources().getDrawable(R.drawable.jade_button));
		}
		button.setText(name == null || name.isEmpty() ? "/" : name);

		final int position = mNavigationStack.size();
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				navigateToPosition(position);
			}
		});

		mPathLayout.addView(button, new LinearLayout.LayoutParams(
				  ViewGroup.LayoutParams.WRAP_CONTENT,
				  ViewGroup.LayoutParams.WRAP_CONTENT));

		mPathLayout.post(new Runnable() {
			@Override
			public void run() {
				mPathScroller.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		});
	}

	/**
	 * open a file/directory in the current directory
	 *
	 * @param file the file to open
	 */
	private void navigateToChild(File file) {
		if (file.isDirectory()) {
			mNavigationStack.push(new NavigationInfo(mFileListView, mCurrentDir));
			mCurrentDir = file;
			refreshView(mCurrentDir, null);
			appendNavigationButton(mCurrentDir.getName());
		}
		else if (mManagerMode != EXTRA_INTERNAL_FILE_PICKER) {
			String ext = FileAdapter.getExt(file);
			String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);

			if (mManagerMode == EXTRA_INTERNAL_MANAGER &&
					  mimeType != null &&
					  mimeType.startsWith("text/"))
			{
				Intent intent = new Intent();
				intent.setDataAndType(Uri.fromFile(file), mimeType);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
			else {
				// let android determines what applications to use to open the specified
				// files. If the mimeType cannot be determined, set the mimeType to "*/*"
				// and android will list all avaliable application for user to choose from

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), mimeType == null ? "*/*" : mimeType);
				startActivity(intent);
			}
		}
	}


	/**
	 * This is the listing part
	 */
	private void initializeFileList() {
		mEmptyDirectoryView = (TextView) findViewById(R.id.emptylist);
		mFileListView = (ListView) findViewById(R.id.filelist);

		// if you want to remove the header, move the MenuBar out of the
		// FrameLayout in file_manager_activity_activity.xml
		// mFileListView.addHeaderView(header, null, false); // unclickable headerview

		// !!!!! IMPORTANT !!!!!!!!
		// do not cache the children when scroll, otherwise, the scrolling
		// will be laggy!!
		mFileListView.setScrollingCacheEnabled(false);

		refreshView();
		mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (getSelectionMode() == FileSelectionMode.NONE) {
					navigateToChild((File) mFileListView.getAdapter().getItem(position));
				}
				else if (getSelectionMode() == FileSelectionMode.SELECT) {
					if (mFileListView.getCheckedItemCount() == 0) {
						setSelectionMode(FileSelectionMode.NONE, FileOperationState.NORMAL);
					}
					else {
						setSelectionMode(FileSelectionMode.SELECT, FileOperationState.NORMAL);
					}
				}
			}
		});

		mFileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (mManagerMode == EXTRA_INTERNAL_FILE_PICKER ||
						  mManagerMode == EXTRA_INTERNAL_FOLDER_PICKER)
				{
					actionChoose((File) mFileListView.getAdapter().getItem(position));
				}
				else {
					if (getSelectionMode() == FileSelectionMode.NONE) {
						setSelectionMode(FileSelectionMode.SELECT, FileOperationState.NORMAL);

						// must be called after setSelectionMode, since doing setItemChecked when
						// the is NONE will do nothing
						mFileListView.setItemChecked(position, true);
						return true;
					}
				}
				return false;
			}
		});
	}

	/**
	 * Rebuild the FileAdapter with the current directory.
	 */
	private void refreshView() {
		refreshView(mCurrentDir, null);
	}

	/**
	 * rebuild the FileAdapter with the current directory and then move back to the specified position
	 *
	 * @param info the position to move to after refreshing
	 */
	private void refreshView(NavigationInfo info) {
		refreshView(info.file, info);
	}

	/**
	 * set the FileAdapter of this listview with the files in the specified directory
	 *
	 * @param directory the directory whose files that are going to be displayed.
	 * @param info      the position to go to after refreshing the view, will position to the top if
	 *                  null
	 */
	private void refreshView(File directory, NavigationInfo info) {

		File[] files = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return !file.getName().startsWith(".");
			}
		});

		if (files == null) {
			mFileListView.setVisibility(View.GONE);
			mEmptyDirectoryView.setVisibility(View.VISIBLE);
			mEmptyDirectoryView.setText(R.string.files_no_sd_card);

		}
		else if (files.length == 0) {

			// show the textview that dispaly "EMPTY FOLDER"
			// and hide the listview that contains the files
			mEmptyDirectoryView.setText(R.string.files_empty_folder);
			mEmptyDirectoryView.setVisibility(View.VISIBLE);
			mFileListView.setVisibility(View.GONE);
		}
		else {

			mEmptyDirectoryView.setVisibility(View.GONE);
			mFileListView.setVisibility(View.VISIBLE);

			FileAdapter adapter = new FileAdapter(this, R.layout.fileitem, files);
			mFileListView.setAdapter(adapter);

			if (info != null) {
				restorePosition(info);
			}
		}
	}

	private void createPickerMenuBar(int mode) {
		// use this button as a message
		// should use settitle, but it's not implemented correctly now
		// mMenuBar.addButton(BUTTON_ID_CHOOSE, getString(R.string.action_choose_msg), null).setClickable(false);

		if (mode == EXTRA_INTERNAL_FOLDER_PICKER) {
			mMenuBar.setTitle(getString(R.string.action_choose_folder));
		}
		else {
			mMenuBar.setTitle(getString(R.string.action_choose_file));
		}

		// create NEW folder/files
		mMenuBar.addButton(BUTTON_ID_NEW, R.string.overflow_new, R.drawable.action_new);

		mMenuBar.showAll();
	}

	/**
	 * Initialize the MenuBar. This is a LONG method. :|
	 */
	private void createManagerMenuBar() {

		// Add all available buttons to this MenuBar
		// Define the function of each button with a View.OnClickListener()

		// TODO SEARCH
		// mMenuBar.addButton(BUTTON_ID_SEARCH, R.drawable.action_search, null);
		mMenuBar.addButton(BUTTON_ID_BACK, R.string.overflow_back, R.drawable.action_up);
		mMenuBar.addButton(BUTTON_ID_COPY, R.string.overflow_copy, R.drawable.action_copy);
		mMenuBar.addButton(BUTTON_ID_CUT, R.string.overflow_cut, R.drawable.action_cut);
		mMenuBar.addButton(BUTTON_ID_PASTE, R.string.overflow_paste, R.drawable.action_paste);
		mMenuBar.addButton(BUTTON_ID_DELETE, R.string.overflow_delete, R.drawable.action_delete);
		mMenuBar.addButton(BUTTON_ID_RENAME, R.string.overflow_rename, R.drawable.action_rename);
		mMenuBar.addButton(BUTTON_ID_NEW, R.string.overflow_new, R.drawable.action_new);
		mMenuBar.addButton(BUTTON_ID_CANCEL, R.string.overflow_cancel, R.drawable.action_cancel);

		mMenuBar.setDefault(BUTTON_ID_BACK, BUTTON_ID_NEW).showDefault();

		mMenuBar.setOnMenuButtonClickListener(new MenuBar.OnClickMenuButtonListener() {
			@Override
			public void onMenuButtonClicked(int buttonId) {
				switch (buttonId) {
					case BUTTON_ID_RENAME:
						actionRenameSelectedFiles();
						break;
					case BUTTON_ID_BACK:
						navigateToParent();
						break;
					case BUTTON_ID_COPY:
						actionCopySelectedFiles();
						break;
					case BUTTON_ID_CUT:
						actionCutSelectedFiles();
						break;
					case BUTTON_ID_PASTE:
						actionPaste();
						break;
					case BUTTON_ID_DELETE:
						actionDeleteSelectedFiles();
						break;
					case BUTTON_ID_NEW:
						actionNew();
						break;
					case BUTTON_ID_CANCEL:
						mClipboard.empty();
						setSelectionMode(FileSelectionMode.NONE, FileOperationState.NORMAL);
						break;
					default:
						break;
				}
			}
		});
	}

	/**
	 * prompt to create a new folder or file in the current directory
	 */
	private void actionNew() {
		final EditText textbox = new EditText(this);
		textbox.setText(R.string.folder_new); // new folder name
		textbox.selectAll();

		AlertDialog newFolderDialog = new AlertDialog.Builder(this)
				  .setTitle(R.string.folder_new_title)
				  .setView(textbox)
				  .setPositiveButton(R.string.folder_new, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  File newFolder = new File(mCurrentDir, textbox.getText().toString());
						  if (!newFolder.exists() && newFolder.mkdir()) {
							  // created successfully
							  showToast(R.string.folder_created);
							  refreshView(getCurrentPosition());
						  }
						  else {
							  showToast(R.string.folder_created_failed);
						  }
					  }
				  })
				  .setNeutralButton(R.string.files_new, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  File newFile = new File(mCurrentDir, textbox.getText().toString());
						  boolean fileCreated;

						  try {
							  fileCreated = !newFile.exists() && newFile.createNewFile();
							  refreshView(getCurrentPosition());
						  } catch (IOException e) {
							  fileCreated = false;
						  }

						  showToast(fileCreated ? R.string.files_created : R.string.files_created_failed);
					  }
				  })
				  .setNegativeButton(R.string.msg_cancel, null)
				  .create();
		// this force the keyboard to show up
		newFolderDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		newFolderDialog.show();
	}

	private void actionChoose(File file) {
		File chosenFile = file;
		int messageResourceId;
		if (mManagerMode == EXTRA_INTERNAL_FILE_PICKER) {
			if (file.isDirectory()) {
				String name = "Vocabulary.txt";
				chosenFile = new File(file, name);
			}
			messageResourceId = R.string.files_chosen;

		}
		else { // mManagerMode == EXTRA_INTERNAL_FOLDER_PICKER
			if (file.isFile()) {
				new AlertDialog.Builder(this)
						  .setTitle(R.string.folder_choose_title)
						  .setMessage(R.string.folder_choose_must_be_folder)
						  .setPositiveButton(R.string.msg_ok, null)
						  .create()
						  .show();
				return;
			}
			messageResourceId = R.string.folder_chosen;
		}

		// OKAY, the chosen file is valid
		String message = String.format(getResources().getString(messageResourceId), chosenFile.getAbsolutePath());
		final File finalFile = chosenFile;

		new AlertDialog.Builder(this)
				  .setTitle(R.string.files_choose_title)
				  .setMessage(message)
				  .setPositiveButton(R.string.msg_yes, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  Intent intent = new Intent();
						  intent.putExtra(FILE_PARCEL, finalFile);
						  setResult(Activity.RESULT_OK, intent);
						  finish();
					  }
				  })
				  .setNegativeButton(R.string.msg_cancel, null)
				  .create()
				  .show();
	}


	/**
	 * get a list of the currently selected files.
	 *
	 * @return the list of currently selected files.
	 */
	private ArrayList<FileInfo> getSelectedFiles() {
		ArrayList<FileInfo> files = new ArrayList<FileInfo>();

		SparseBooleanArray items = mFileListView.getCheckedItemPositions();

		for (int i = 0; items != null && i < items.size(); i++) {

			int itemPos = items.keyAt(i);
			boolean itemCheckedValue = items.valueAt(i);

			if (itemCheckedValue == ITEM_CHECKED) {
				files.add(new FileInfo((File) mFileListView.getAdapter().getItem(itemPos), itemPos));
			}
		}

		return files;
	}

	/**
	 * rename the selected file. Currently can only rename if 1 file is selected.
	 */
	private void actionRenameSelectedFiles() {

		ArrayList<FileInfo> files = getSelectedFiles();

		if (files.size() != 1) {
			showToast(R.string.files_no_multi_rename);
			return;
		}

		final File file = files.get(0).getFile();

		final EditText textbox = new EditText(this);
		textbox.setText(file.getName()); // file text box with original name
		textbox.selectAll();

		AlertDialog renameDialog = new AlertDialog.Builder(this)
				  .setTitle(R.string.files_rename)
				  .setView(textbox)
				  .setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  String new_name = textbox.getText().toString();
						  if (new_name.equals("") || !file.renameTo(new File(Util.makePath(file.getParent(), new_name)))) {
							  showToast(R.string.files_rename_failed);
						  }
						  refreshView(getCurrentPosition());
					  }
				  })
				  .setNegativeButton(R.string.msg_cancel, null)
				  .create();
		// this force the keyboard to show up
		renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		renameDialog.show();
		setSelectionMode(FileSelectionMode.NONE, FileOperationState.NORMAL);
	}

	/**
	 * delete the selected file from the file system
	 */
	private void actionDeleteSelectedFiles() {
		actionDelete(null);
	}

	/**
	 * delete the specified file, or if file is null, delete the selected file
	 *
	 * @param file the file to be deleted
	 */
	private void actionDelete(final File file) {
		new AlertDialog.Builder(this)
				  .setTitle(R.string.files_delete_confirm)
				  .setPositiveButton(R.string.msg_yes, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  actionDeleteHelper(file);
					  }
				  })
				  .setNegativeButton(R.string.msg_cancel, null).create().show();
	}

	private void actionDeleteHelper(File file) {
		int message = R.string.files_no_selection;

		SparseBooleanArray items = mFileListView.getCheckedItemPositions();

		try {
			if (file != null) { // delete a single file
				FileClipboard.deleteFile(file);
				message = R.string.files_deleted;
			}
			else { // delete the checked file
				for (int i = 0; i < items.size(); i++) {
					if (items.valueAt(i) == ITEM_CHECKED) {
						message = R.string.files_deleted;

						File f = ((File) mFileListView.getAdapter().getItem(items.keyAt(i)));
						FileClipboard.deleteFile(f);
					}
				}
			}
		} catch (IOException e) {
			message = R.string.files_delete_failed;
		}

		refreshView();
		showToast(message);
		setSelectionMode(FileSelectionMode.NONE, FileOperationState.NORMAL);
	}


	/**
	 * copy the selected files from the file manager. #actionPaste can be used to paste the copied
	 * file
	 */
	private void actionCopySelectedFiles() {
		actionCopy(null);
	}

	/**
	 * copy the specified file to the clipboard or if the file is null, copy the selected files to the
	 * clipboard
	 *
	 * @param file the file to be copied
	 */
	private void actionCopy(File file) {
		boolean copySuccess;
		if (file != null) {
			mClipboard.copy(file);
			copySuccess = true;
		}
		else {
			copySuccess = putSelectionToClipboard(ACTION_COPY_FILES);
		}

		int message = R.string.files_copy_failed;
		if (copySuccess) {
			message = R.string.files_copied;
			setSelectionMode(FileSelectionMode.NONE, FileOperationState.CLIPBOARD_PENDING);
		}
		showToast(message);
	}


	/**
	 * paste the files from the clipboard to the current directory
	 */
	private void actionPaste() {
		boolean success = mClipboard.paste(mCurrentDir);
		refreshView();
		showToast(success ? R.string.files_pasted : R.string.files_paste_failed);
		setSelectionMode(FileSelectionMode.NONE, FileOperationState.NORMAL);
	}

	/**
	 * cut the selected files from the file manager. #actionPaste can be use to paste the files
	 */
	private void actionCutSelectedFiles() {
		actionCut(null);
	}

	/**
	 * cut the specified file, or if file is null, cut the selected file.
	 *
	 * @param file the file to be cut
	 */
	private void actionCut(File file) {
		boolean cutSuccess;

		if (file != null) {
			mClipboard.cut(file);
			cutSuccess = true;
		}
		else {
			cutSuccess = putSelectionToClipboard(ACTION_CUT_FILES);
		}

		int message = R.string.files_cut_failed;
		if (cutSuccess) {
			message = R.string.files_cut;
			setSelectionMode(FileSelectionMode.NONE, FileOperationState.CLIPBOARD_PENDING);
		}

		showToast(message);
	}

	/**
	 * Cut or copy the checked files in {@link #mFileListView}
	 *
	 * @param operation either {@link #ACTION_CUT_FILES} or {@link #ACTION_COPY_FILES}
	 * @return true if any file is copied/cut, false otherwise
	 */
	private boolean putSelectionToClipboard(int operation) {

		mClipboard.empty();

		ArrayList<FileInfo> selectedFiles = getSelectedFiles();

		for (FileInfo fileInfo : selectedFiles) {
			if (operation == ACTION_COPY_FILES) {
				mClipboard.copy(fileInfo.getFile());
			}
			else if (operation == ACTION_CUT_FILES) {
				mClipboard.cut(fileInfo.getFile());
			}
			mFileListView.setItemChecked(fileInfo.getPosition(), false);
		}

		return selectedFiles.size() > 0;
	}

	/**
	 * returns the current selection mode (either multi-select or normal)
	 *
	 * @return the current selection mode.
	 */
	private FileSelectionMode getSelectionMode() {
		return mSelectionMode;
	}

	/**
	 * Sets the choice mode of {@link #mFileListView}. And depending on the specified mode and state,
	 * change the menubar icons to reflect to the currently allowed operations.
	 *
	 * @param mode  FileSelectionMode.NONE or FileSelectionMode.SELECT
	 * @param state the state of the file manager
	 */
	private void setSelectionMode(FileSelectionMode mode, FileOperationState state) {

		if (mSelectionMode != mode) { // only do this if the modes are different

			if (mode == FileSelectionMode.NONE) {
				/*
				 * clearing the choices DOES NOT automatically refresh
				 * the ListView.
				 * if you looked at android's source code, you can see that setChecked(boolean)
				 * is only called in ListView.setupChild(...) when choiceMode is
				 * CHOICE_MODE_MULTIPLE
				 */
				// fileList.clearChoices();

				SparseBooleanArray items = mFileListView.getCheckedItemPositions();
				for (int i = 0; items != null && i < items.size(); i++) {
					if (items.valueAt(i) == ITEM_CHECKED) {
						/*
						 * setItemChecked(position, boolean) works correctly. However, due to
						 * the way android recycles views in the adapter, if you are using
						 * the converView parameter in the getView method of the adapter,
						 * the items WILL have the incorrect state (checked/unchecked) drawable.
						 *
						 * To solve this issue, i have call setChecked(false) in the getView
						 * method of the adapter. Since the "checked state" of the item is stored
						 * in the ListView that contains the item, but not the item itself, so calling
						 * setChecked(false) in getView does not invalidate the previous checked
						 * state of the item.
						 */
						mFileListView.setItemChecked(items.keyAt(i), false);
					}
				}

				mFileListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
			}
			else if (mode == FileSelectionMode.SELECT) {
				mFileListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
				showToast(R.string.files_select_ready);
			}
		}

		// change the buttons on the menu bar depending on the current state
		switch (state) {
			case CLIPBOARD_PENDING:
				mMenuBar.show(BUTTON_ID_BACK, BUTTON_ID_PASTE, BUTTON_ID_NEW, BUTTON_ID_CANCEL);
				break;
			case NORMAL:
				if (mode == FileSelectionMode.NONE) {
					mMenuBar.showDefault();
				}
				else if (mode == FileSelectionMode.SELECT) {
					if (mFileListView.getCheckedItemCount() <= 1) {
						mMenuBar.show(BUTTON_ID_RENAME, BUTTON_ID_COPY, BUTTON_ID_CUT,
								  BUTTON_ID_DELETE, BUTTON_ID_CANCEL);
					}
					else {
						mMenuBar.show(BUTTON_ID_COPY, BUTTON_ID_CUT,
								  BUTTON_ID_DELETE, BUTTON_ID_CANCEL);
					}
				}
				break;
		}

		// we change this at the end, so we know, in the entire function the
		// previous mode we are in.
		mSelectionMode = mode;
	}

	private NavigationInfo getCurrentPosition() {
		return new NavigationInfo(mFileListView, mCurrentDir);
	}

	private void restorePosition(NavigationInfo info) {
		mFileListView.setSelectionFromTop(info.position, info.y);
	}


	/**
	 * display a message for a short duration
	 *
	 * @param resid the resource id of the string to display
	 */
	private void showToast(int resid) {
		mToast.cancel();
		mToast = Toast.makeText(this, resid, Toast.LENGTH_SHORT);
		mToast.show();
	}


	private static class FileInfo {
		private File mFile; // the file
		private int mPos; // the position of the file within the listivew

		public FileInfo(File file, int pos) {
			this.mFile = file;
			this.mPos = pos;
		}

		public File getFile() {
			return mFile;
		}

		public int getPosition() {
			return mPos;
		}
	}

	/**
	 * stores the current scroll position of the ListView
	 */
	private static class NavigationInfo {
		public int position;
		public int y;
		public java.io.File file;

		@SuppressWarnings("unused")
		public NavigationInfo() {
		}

		public NavigationInfo(java.io.File file) {
			this.file = file;
		}

		public NavigationInfo(ListView view, File file) {
			this(file);
			updatePosition(view);
		}

		public void updatePosition(ListView view) {
			if (view != null) {
				View child = view.getChildAt(0);
				y = child == null ? 0 : child.getTop();
				position = view.getFirstVisiblePosition();
			}
		}
	}
}
