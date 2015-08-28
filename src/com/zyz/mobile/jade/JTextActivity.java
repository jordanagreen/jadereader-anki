/*
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.webkit.MimeTypeMap;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.zyz.mobile.R;
import com.zyz.mobile.book.UserBookData;
import com.zyz.mobile.book.UserSpan;
import com.zyz.mobile.book.UserSpanType;
import com.zyz.mobile.misc.DataKeeper;
import com.zyz.mobile.misc.RatingReminder;
import com.zyz.mobile.rikai.Entries;
import com.zyz.mobile.rikai.Entry;
import com.zyz.mobile.rikai.RikaiDroid;
import com.zyz.mobile.util.*;
import com.zyz.mobile.widget.*;
import com.zyz.mobile.widget.ExpandableListView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an imitation of the android reader I loved Moon+ reader
 */
public class JTextActivity extends BaseActivity implements
		  OnLongClickListener, OnClickListener, OnTouchListener
{

	private final static char FULLWIDTH_LEFT_PARENTHESIS = '\uFF08';
	private final static char FULLWIDTH_RIGHT_PARENTHESIS = '\uFF09';
	private final static char FULLWIDTH_PIPE = '\uFF5C';
	private final static char LEFT_PARENTHESIS = '(';
	private final static char RIGHT_PARENTHESIS = ')';
	private final static char DOUBLE_LEFT_ANGLE_BRACKET = '\u300A';
	private final static char DOUBLE_RIGHT_ANGLE_BRACKET = '\u300B';

	private final static int MAX_TOAST_SIZE = 140;
	private final static int WORD_SEARCH_LEN = 15;

	private final static int MENU_BRIGHTNESS = Menu.FIRST;
	private final static int MENU_FONTSIZE = Menu.FIRST + 1;
	private final static int MENU_SCROLLER = Menu.FIRST + 2;
	private final static int MENU_OPTION = Menu.FIRST + 3;

	private final static int MENU_TEXT_SELECT = Menu.FIRST + 4;
	private final static int MENU_ADD_BOOKMARK = Menu.FIRST + 5;
	private final static int MENU_MY_NOTE = Menu.FIRST + 6;
	private final static int MENU_BOOKMARK = Menu.FIRST + 7;

	private final static int ACTION_COPY = 1;
	private final static int ACTION_HIGHLIGHT = 2;
	private final static int ACTION_GOOGLE = 3;
	private final static int ACTION_SHARE = 4;
	private final static int ACTION_ATTENTION_DELETE = 5;
	private final static int ACTION_SAVE = 6;

	/**
	 * The key used in preference for various settings You can change the name of the variables, but
	 * you should not change the String value !!!
	 */
	public final static String KEY_GLOSSHEIGHT = "glossheight";
	public final static String KEY_GLOSS_TEXT_SCALE = "glossscale";
	public final static String KEY_BRIGHTNESS = "brightness";
	public final static String KEY_TEXTSIZE = "textsize";

	public final static int DEFAULT_TEXT_SIZE = 25;

	private final static String mGoogleQuery = "http://www.google.ca/search?q=";

	// keys for bundle
	private final static String BUNDLE_EXISTS = "bundle.save";
	private final static String BUNDLE_TEXT = "bundle.text";
	private final static String BUNDLE_WORD = "bundle.word";
	private final static String BUNDLE_PATH = "bundle.path";

	@SuppressWarnings("UnusedDeclaration")
	private final static String TAG = "JTEXT"; // debug tag


	private CharSequence mText; // the text from the main textview

	private JTextView mJTextView; // the main textview of this activity

	private UserBookData mUserBookData; // the (xml) text info of this text file

	private JScrollView mJScrollView; // scrollview that contains this text

	private ExpandableListView mGlossView; // the view that display the definition of the searched words

	private Slider mBrightnessSlider; // slider that change the brightness
	private final static float BRIGHTNESS_MIN_VALUE = 0.01f;
	private float mBrightness;

	private Slider mTextSlider; // slider that changes the scroll position of the current text

	private Button textSizePlus;
	private Button textSizeMinus;
	private EditText textSizeEdit;

	private int mSelectionDuration;

	private String mVocabularyFilePath = null;
	private BufferedWriter mVocabularyFileWriter = null;
	private boolean mVocabularyFileChanged = true;

	private boolean mAutoDismissGlossView = false;

	/**
	 * The menu bar for action to perform on selected text
	 */
	private MenuBar mActionMenuBar;

	/**
	 * indicates the current position of {@code mActionMenuBar}. True if it's on the top, false if it's
	 * on the bottom.
	 */
	private int mActionMenuBarPosition = POSITION_TOP;

	private final static int POSITION_TOP = 1;
	private final static int POSITION_BOTTOM = 2;
	private final static int mShiftThresholdDP = 50;
	private int mActionBarRepositionThreashold;

	private MenuBar mMainMenuBar; // option bar on the bottom
	private MenuBar mSecondaryMenuBar; // option bar on the top
	private MenuBar mWordActionBar; // action bar for definition view

	private ArrayList<Concealable> mControls;

	/**
	 * the last touched coordinates
	 */
	private float mTouchX;
	private float mTouchY;

	/**
	 * the x and y position the user click at to invoke the option
	 */
	private int mPrepareOptionX;
	private int mPrepareOptionY;

	/**
	 * The dicitonary. Ported from RikaiChan
	 */
	private RikaiDroid mRikai;
	private boolean mDictinoaryLoaded = false;

	/**
	 * the path of the text file loaded or the path of file created for text shared from other
	 * applications
	 */
	private String mTextPath;

	/**
	 * whether to keep the height when re-opening the defintion view
	 */
	private boolean mKeepGlossHeight;

	/**
	 * the saved height of the definition view
	 */
	private int mGlossViewHeight;


	/**
	 * the current user span the user is operating on
	 */
	@Nullable
	private UserSpan mCurrentSelectedSpan;

	/**
	 * An improvement to target the character when the user clcik on the screen. TODO: consider remove
	 * the ability for user to set this to false
	 */
	private boolean mUsePreciseOffset = true;


	private int mSelectionColor;

	/**
	 * The two pointy cursors for user to manipulate the selection
	 */
	private JTextView.SelectionModifier mSelectionModifier;

	/**
	 * the offset before any option dialogs*
	 */
	private int mRecentOffset;

	/**
	 * combines {@code mMainMenuBar} and {@code mSecondaryMenuBar} for convenience only. (does it
	 * really???)
	 */
	private MyMenuBar mMenuBar;

	/**
	 * A reminder for the user to rate the application TODO: move this to the main activity to decrease
	 * the LOC of this class (doesn't help much, but better)
	 */
	private RatingReminder mRatingReminder;

	/**
	 * can be Configuration.ORIENTATION_PORTRAIT or Configuration.ORIENTATION_LANDSCAPE see function
	 * {@code getOrientation}
	 */
	private int mScreenOrientation;

	/**
	 * for convenience only. for annoymous function to use {@code mContext} instead of {@code
	 * JTextActivity.this}
	 */
	private Context mContext = this;

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

	/**
	 * if we have scrolled to the saved offset
	 */
	private boolean mScrolledToSavedOffset = false;

	/**
	 * the original saved offset from the settings when first opened the file
	 */
	private int mSavedOffset = -1;

	/**
	 * Ignores kana (usually the reading) inside brackets when searching
	 */
	private boolean mIgnoreBracket = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.jtext_activity);

		mJScrollView = (JScrollView) findViewById(R.id.scroller);
		mJScrollView.addOnScrollChangedListener(new OnTextScrolled());

		// this makes the TextView scrollable even if it's not contained
		// within a ScrollView, but user cannot fling to scroll.
		// mJTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

		// this variable is being used in the current method below
		mScreenOrientation = AndroidService.getScreenOrientation(this);

		mGlossView = (ExpandableListView) findViewById(R.id.definition_view);
		mGlossView.setOnItemClickListener(mOnEntryClicked);
		mGlossView.setOnItemLongClickListener(mOnEntryClicked);
		mGlossView.setOnConcealListener(new OnConcealGlossView());

		/** TextView settings **/
		mJTextView = (JTextView) findViewById(R.id.booktext);
		mJTextView.setClickable(true);
		mJTextView.setOnClickListener(this);
		mJTextView.setOnLongClickListener(this);
		mJTextView.setOnTouchListener(this);
		mJTextView.setOnCursorStateChangedListener(new OnSelectionCursorStateChanged());
		mSelectionModifier = mJTextView.getSelectionModifier();

		setTextFromIntent(savedInstanceState, getIntent(), mJTextView);

		mText = mJTextView.getText();

		mBrightnessSlider = (Slider) findViewById(R.id.brightness_seeker);
		setupBrightnessSlider();

		mTextSlider = (Slider) findViewById(R.id.text_seeker);
		setupTextSlider();

		mMainMenuBar = (MenuBar) findViewById(R.id.main_option_bar);
		setupMainOptionBar(mMainMenuBar);

		mSecondaryMenuBar = (MenuBar) findViewById(R.id.secondary_option_bar);
		setupSecondaryOptionBar(mSecondaryMenuBar);

		mMenuBar = new MyMenuBar();

		mActionMenuBar = (MenuBar) findViewById(R.id.action_bar);
		setupActionBar(mActionMenuBar);

		mWordActionBar = (MenuBar) findViewById(R.id.word_action_bar);
		setupWordActionBar(mWordActionBar);

		mControls = new ArrayList<Concealable>() {{
			if (mScreenOrientation == Configuration.ORIENTATION_PORTRAIT) {
				add(mMainMenuBar);
			}
			add(mWordActionBar);
			add(mSecondaryMenuBar);
			add(mBrightnessSlider);
			add(mTextSlider);
			add(mSelectionModifier);
		}};

		// load dictionary
		mRikai = new RikaiDroid();

		boolean freshStart = savedInstanceState == null ||
				  !savedInstanceState.getBoolean(BUNDLE_EXISTS, false);

		loadDictionary(freshStart);

		mActionBarRepositionThreashold =
				  Util.DPtoPX(mShiftThresholdDP, getResources().getDisplayMetrics());

		mRatingReminder = new RatingReminder(this);

		if (freshStart) { mRatingReminder.countdown(); }

		Log.i(TAG, "JTextActivity onCreate");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.i(TAG, "JTextActivity onNewIntent");

		// go to bookmark
		int offset = intent.getIntExtra(getString(R.string.key_bookmark_offset), -1);
		if (offset >= 0) {
			goToOffset(offset);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(BUNDLE_EXISTS, true); // indicates we saved the bundle
		outState.putCharSequence(BUNDLE_TEXT, mText);
		outState.putString(BUNDLE_PATH, mTextPath);
		if (mGlossView.isDisplaying()) {
			outState.putParcelable(BUNDLE_WORD, (Entries) mGlossView.getTag(R.string.tag_word_list));
		}
		Log.i(TAG, "Bundle Saved.");
	}


	/**
	 * Get the path and the text from the bundle
	 *
	 * @param bundle the bundle
	 * @return {path, text}
	 */
	private Tuple.BookOpener setupFromBundle(Bundle bundle) {

		Tuple.BookOpener result = new Tuple.BookOpener();
		if (bundle != null) {
			result.setText(bundle.getCharSequence(BUNDLE_TEXT));
			result.setPath(bundle.getString(BUNDLE_PATH));
			Log.i(TAG, "Loaded from Bundle");
		}

		return result;
	}

	/**
	 * Returns the given the text in the {@code Tuple.BookOpener} object. In addition, returns a valid
	 * path that can be used to save the given text
	 *
	 * @param text the text
	 * @return {path, text}
	 */
	private Tuple.BookOpener setupFromString(String text) {

		Tuple.BookOpener result = new Tuple.BookOpener();

		if (text != null) {
			File file = saveTextToFolder(text, SettingActivity.getSharedTextSaveFolder(this));

			if (file != null) {
				result.setPath(file.getPath());
			}
			result.setText(text);
		}

		return result;
	}

	/**
	 * Return the path in the specified URI. In addition, read the text from the specified path, and
	 * return the text, and the encoding used to read it.
	 *
	 * @param uri            the uri
	 * @param recentFileList the recently opened file list (read the encoding, if specified)
	 * @return {text, path, encoding}
	 */
	private Tuple.BookOpener setupFromURI(Uri uri, @NotNull RecentFilesList recentFileList) {
		Tuple.BookOpener result = new Tuple.BookOpener();
		try {
			File file = new File(uri.getPath());

			if (!file.exists()) {
				return result;
			}

			String encoding = null;
//			if (recentFileList.open()) {
//				BookMetadata getUserBookData = recentFileList.get(file.getPath());
//				if (getUserBookData != null) {
//					encoding = getUserBookData.getEncoding();
//				}
//			}
			String plainText;
			try {
				//noinspection ConstantConditions
				if (encoding != null) {
					plainText = Text.readAll(file.getPath(), encoding);
				}
				else {
					encoding = Text.guessFileEncoding(file.getPath());
					plainText = Text.readAll(file.getPath(), encoding);
				}
			} catch (UnsupportedEncodingException e) {
				plainText = Text.readAll(file.getPath()); // probably won't happen, just in case
			}

			Spanned htmlText = null;

			if (MimeTypeMap.getSingleton().getMimeTypeFromExtension(Util.getExt(file))
					  .equalsIgnoreCase("text/html"))
			{
				htmlText = Html.fromHtml(plainText);
			}

			result.setText(htmlText != null ? htmlText : plainText)
					  .setPath(file.getPath())
					  .setMessage(getString(R.string.fyi_encoding, encoding) +
								 (htmlText != null ? "\n\n" + getString(R.string.fyi_converted_from_html) : ""));

		} catch (Exception e) {
		}

		return result;
	}

	/**
	 * Set the text to the specified textview from the given intent. Side Effect: (1) This will
	 * finish/quit the activity if it fails to parse the intent. (2) This also adds the file retrieved
	 * from the intent to the recent file list. (3) initialized mUserBookData.
	 * <p/>
	 * This method is long. But it's better to do it this way because each actions depends on the text
	 * or failure of the previous action.
	 *
	 * @param bundle   the bundle (if we are restored from previous state e.g. orientation change)
	 * @param intent   the intent
	 * @param textview the text view
	 */
	private void setTextFromIntent(Bundle bundle, Intent intent, TextView textview) {

		Tuple.BookOpener opener = new Tuple.BookOpener();
		RecentFilesList recentFilesList = new RecentFilesList(this);

		if (bundle != null && bundle.getBoolean(BUNDLE_EXISTS, false)) {
			opener = setupFromBundle(bundle);
		}
		else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			opener = setupFromURI(intent.getData(), recentFilesList);
		}
		else if (Intent.ACTION_SEND.equals(intent.getAction())) {
			opener = setupFromString(intent.getStringExtra(Intent.EXTRA_TEXT));
		}

		if (opener.getText() != null) {
			setText(textview, opener.getText());
			showOpeningToast(textview, opener.getMessage());
		}
		else {
			showFailDialog(getString(R.string.failed_to_open_file) + opener.getPath(""));
			return;
		}

		if (opener.getPath() != null) {
			mTextPath = opener.getPath();

			boolean success = recentFilesList.open();
			if (success) {
				recentFilesList.addToRecentFiles(opener.getPath());
				recentFilesList.save();
			}

			mUserBookData = new UserBookData(
					  RecentFilesList.getXMLFile(opener.getPath()),
					  (Spannable) textview.getText());
			mUserBookData.parse();

			goToOffset(mUserBookData.getOffset()); // restore old position
		}
		else {
			mUserBookData = new UserBookData(null, (Spannable) textview.getText());
		}
	}

	private void setText(TextView textview, CharSequence text) {
		textview.setText(text + "\n\n", TextView.BufferType.SPANNABLE);
	}

	/**
	 * display a toast in the textview
	 *
	 * @param textView the textview containing the text
	 * @param toast    the toast. Nothing will be displayed if this is null.s
	 */
	private void showOpeningToast(TextView textView, final String toast) {
		if (toast != null) {
			// need to post it because textview takes time to measure/draw all the text
			// on the screen
			textView.post(new Runnable() {
				@Override
				public void run() {
					showToast(toast);
				}
			});
		}
	}


	/**
	 * write the specified text to the given folder.
	 *
	 * @param text   the text to write to a file
	 * @param folder the folder
	 * @return the File the text is written to. null if unsuccessful.
	 */
	@Nullable
	private File saveTextToFolder(String text, File folder) {
		File outFile = null;

		if (!folder.getName().equals("") && (folder.mkdirs() || folder.isDirectory())) {
			int end = text.length() < 10 ? text.length() : 10;
			String filename = Util.getFreeName(folder, text.substring(0, end));

			if (filename != null) {
				outFile = new File(folder, filename);

				try {
					BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile));
					bufferedWriter.write(text);
					bufferedWriter.close();
				} catch (IOException e) {
					outFile = null;
				}
			}
		}

		return outFile;
	}

	/**
	 * show a dialog with an OK button with the specified message. Pressing OK will `finish` the
	 * current activity
	 *
	 * @param message the message to be displayed
	 */
	private void showFailDialog(String message) {
		new AlertDialog.Builder(this)
				  .setMessage(message)
				  .setPositiveButton(R.string.msg_ok, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  finish();
					  }
				  })
				  .setCancelable(false)
				  .create()
				  .show();
	}

	/*
	 * android activity cycle onCreate->onStart->onResume->ACTIVITY->onPause->....
	 */
	@Override
	protected void onResume() {
		super.onResume();
		applyPreferences();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mRatingReminder.timeToRemind()) {
			mRatingReminder.show();
		}
	}

	/**
	 * download, extract and load the Rikaichan dictioary
	 */
	private void loadDictionary(boolean showDownloadDialog) {
		final DictionaryInfo dictInfo = new DictionaryInfo(this); // path and url info of the required dictionaries

		if (dictInfo.exists()) {
			loadDictionary(dictInfo);
		}
		else if (showDownloadDialog) {
			downloadAndExtract(dictInfo);
		}
	}

	private void downloadAndExtract(final DictionaryInfo dictInfo) {
		new AlertDialog.Builder(this)
				  .setTitle(R.string.dm_dict_title)
				  .setMessage(R.string.dm_dict_message)
				  .setPositiveButton(R.string.dm_dict_yes, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  final SimpleDownloader downloader = new SimpleDownloader(mContext);
						  final SimpleExtractor extractor = new SimpleExtractor(mContext);

						  downloader.setOnFinishTaskListener(new OnFinishTaskListener() {
							  @Override
							  public void onFinishTask(boolean success) {
								  if (success) {
									  extractor.execute(dictInfo.zip());
								  }
								  else {
									  (new File(dictInfo.zip())).delete();
									  (new File(dictInfo.dictionary())).delete();
									  (new File(dictInfo.deinflect_data())).delete();
									  showDownloadTroubleDialog();
								  }
							  }
						  });
						  extractor.setOnFinishTasklistener(new OnFinishTaskListener() {
							  @Override
							  public void onFinishTask(boolean success) {
								  if (success) {
									  loadDictionary(dictInfo);
								  }
								  else {
									  (new File(dictInfo.dictionary())).delete();
									  (new File(dictInfo.deinflect_data())).delete();
									  showDownloadTroubleDialog();
								  }
								  (new File(dictInfo.zip())).delete();
							  }
						  });
						  downloader.execute(dictInfo.url(), dictInfo.zip());
					  }
				  })
				  .setNegativeButton(R.string.dm_dict_no, null)
				  .create()
				  .show();
	}

	private void showDownloadTroubleDialog() {
		new AlertDialog.Builder(this)
				  .setMessage(R.string.dm_dict_alternate_download_address)
				  .setPositiveButton(R.string.msg_ok, null)
				  .create()
				  .show();
	}

	/**
	 * load the dictionary with the path information specified in the {@code dictInfo}
	 *
	 * @param dictInfo the dictionary path information
	 */
	private void loadDictionary(final DictionaryInfo dictInfo) {
		mDictinoaryLoaded = mRikai.loadData(dictInfo.dictionary(), dictInfo.deinflect_data());
		if (!mDictinoaryLoaded) {
			new AlertDialog.Builder(this)
					  .setMessage(R.string.dm_dict_failed_to_load)
					  .setPositiveButton(R.string.msg_yes, new DialogInterface.OnClickListener() {
						  @Override
						  public void onClick(DialogInterface dialog, int which) {
							  (new File(dictInfo.zip())).delete();
							  (new File(dictInfo.dictionary())).delete();
							  (new File(dictInfo.deinflect_data())).delete();
							  downloadAndExtract(dictInfo);
						  }
					  })
					  .setNegativeButton(R.string.msg_no, null)
					  .create()
					  .show();
		}
	}

	/**
	 * setup the operations to perform when sliding the {@code mTextSlider}
	 */
	private void setupTextSlider() {
		mTextSlider.setMax(mJTextView.length());
		mTextSlider.conceal();
		mTextSlider.setOnRevealListener(new OnRevealListener() {
			@Override
			public void onReveal(Concealable concealable) {
				mTextSlider.setProgress(getCurrentOffset());
			}
		});
		mTextSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					goToOffset(progress);
				}
				float percentage = (float) progress / (float) seekBar.getMax() * 100;
				mTextSlider.setText(String.format("%.2f%%", percentage));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	/**
	 * set the initial brightness of the text and the behaviour of the brightness slider
	 */
	private void setupBrightnessSlider() {
		final SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
		mBrightnessSlider.conceal();
		mBrightnessSlider.setOnRevealListener(new OnRevealListener() {
			@Override
			public void onReveal(Concealable concealable) {
				WindowManager.LayoutParams layout = getWindow().getAttributes();

				// layout.screenBrightness probably equals to -1 when this app is first installed
				// brightness = -1 means it's using the preferred system brightness,
				// and it seems there is no reliable way to get the current system brightness value
				mBrightness = Math.max(preference.getFloat(KEY_BRIGHTNESS, layout.screenBrightness), 0f);
				int progress = (int) (mBrightness * mBrightnessSlider.getMax());
				mBrightnessSlider.setProgress(progress);
				mBrightnessSlider.setText(String.valueOf((int) (mBrightness * 100)) + "%");
			}
		});

		mBrightnessSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				WindowManager.LayoutParams layout = getWindow().getAttributes();
				mBrightness = progress / (float) seekBar.getMax();
				setBrightness(layout, mBrightness);

				mBrightnessSlider.setText(String.valueOf((int) (mBrightness * 100)) + "%");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// save the current brightness
				SharedPreferences.Editor editor = preference.edit();
				editor.putFloat(KEY_BRIGHTNESS, seekBar.getProgress() / (float) seekBar.getMax());
				editor.commit();
			}
		});
	}

	private void setBrightness(WindowManager.LayoutParams layout, float brightness) {
		// setting the brightness to 0 will turn off screen on some of the devices
		// use the FLAG_KEEP_SCREEN_ON to keep it from turning off
		layout.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		if (brightness == -1.0f) {
			// automatic brightness, do nothing
		}
		else {
			layout.screenBrightness = Math.max(brightness, BRIGHTNESS_MIN_VALUE);
			getWindow().setAttributes(layout);
		}
	}

	/**
	 * tells whether there are any controls that are visible on the screen
	 *
	 * @return true if there are controls visible, false otherwise
	 */
	private boolean controlsVisible() {
		for (Concealable c : mControls) {
			if (c.isDisplaying()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Hide all the visible controls on the screen except {@code mGlossView}
	 */
	private void hideControls() {
		for (Concealable c : mControls) {
			c.conceal();
		}
	}

	/**
	 * Hide all visible controls except the specified excpetion. Note that {@code mGlossView} is not
	 * considered as a Control
	 *
	 * @param exception the view that need not be to hide
	 */
	@SuppressWarnings("unused")
	private void hideControlsExcept(Concealable exception) {
		for (Concealable c : mControls) {
			if (c != exception) {
				c.conceal();
			}
		}
	}

	@Override
	public void onBackPressed() {
		// close the glossView on back pressed if it's opened instead
		if (controlsVisible() || mGlossView.isDisplaying()) {
			hideControls();
			mGlossView.conceal();
		}
		else {
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
	}

	/**
	 * Build and show the dialog that manipulates the font size
	 */
	private void showTextSizeDialog() {
		mRecentOffset = getCurrentOffset();

		// inflates the layout and initialize the controls
		// {
		LayoutInflater factory = LayoutInflater.from(this);
		
		/* controls for text size adjusting */
		View textSizeDialog = factory.inflate(R.layout.text_size_picker, null);

		textSizeMinus = (Button) textSizeDialog.findViewById(R.id.text_minus);
		textSizeMinus.setOnClickListener(this);

		textSizePlus = (Button) textSizeDialog.findViewById(R.id.text_plus);
		textSizePlus.setOnClickListener(this);

		textSizeEdit = (EditText) textSizeDialog.findViewById(R.id.text_size);
		textSizeEdit.setInputType(InputType.TYPE_NULL); // disable editing
		// }

		// set the edit field to the current size 
		// {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		int font_size = pref.getInt(KEY_TEXTSIZE, DEFAULT_TEXT_SIZE);
		textSizeEdit.setText(String.valueOf(font_size));
		// }

		new AlertDialog.Builder(this)
				  .setTitle(R.string.text_size_change_dialog)
				  .setView(textSizeDialog)
				  .create()
				  .show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		showMenus(true /* fromOptionButton */);
		return false; // option menu will not be displayed (because i'm use my own mMainMenuBar)
	}

	/**
	 * display the main and secondary menus
	 *
	 * @param fromOptionButton true if the menu is invoked from the Option button for device that has
	 *                         it
	 */
	private void showMenus(boolean fromOptionButton) {

		boolean option_hidden = !mMenuBar.isDisplaying();

		hideControls();

		if (option_hidden) {
			mPrepareOptionX = (int) mTouchX;
			mPrepareOptionY = (int) mTouchY;

			if (fromOptionButton) {
				// if the menu is inoked from the option menu, we are not sure where the
				// mTouchX and mTouchY are, it could be off the screen. fix it.
				final int y = mJScrollView.getScrollY();
				final int height = mJScrollView.getHeight();
				if (mPrepareOptionY < y || mPrepareOptionY > y + height) {
					mPrepareOptionY = y + height / 2;
				}
			}

			mMenuBar.reveal();
		}
	}

	/**
	 * Make the toast about the specified text being copied successfully to the clipboard
	 *
	 * @param text the text copied
	 */
	private void makeClipboardToast(String text) {
		if (text.length() > MAX_TOAST_SIZE) {
			text = text.substring(0, MAX_TOAST_SIZE) + "\n...........";
		}
		showToast(getString(R.string.clipboard_copy_success, text));
	}

	private void setupWordActionBar(final MenuBar menuBar) {
		menuBar.addButton(ACTION_SHARE, getString(R.string.edit_share), R.drawable.action_share);
		menuBar.addButton(ACTION_COPY, getString(R.string.edit_copy), R.drawable.action_copy);
		menuBar.addButton(ACTION_SAVE, getString(R.string.edit_save), R.drawable.action_save);

		menuBar.setDefault(ACTION_COPY, ACTION_SHARE, ACTION_SAVE).showDefault();
		menuBar.setOnMenuButtonClickListener(new MenuBar.OnClickMenuButtonListener() {
			@Override
			public void onMenuButtonClicked(int buttonId) {
				if (!mGlossView.isDisplaying()) {
					return;
				}

				int position = (Integer) menuBar.getTag(R.string.tag_word_pos);

				switch (buttonId) {
					case ACTION_COPY:
						copyWordToClipboard(position);
						break;
					case ACTION_SAVE:
						sendWordToAnki(position);
						break;
					case ACTION_SHARE:
						try {
							Intent intent = new Intent(Intent.ACTION_SEND);
							Entries entries = (Entries) mGlossView.getTag(R.string.tag_word_list);
							intent.putExtra(Intent.EXTRA_TEXT, entries.get(position).toString());
							intent.setType("text/plain");
							startActivity(intent);
						} catch (Exception e) {
							Toast.makeText(JTextActivity.this, R.string.failed_to_share_word, Toast.LENGTH_SHORT).show();
						}
						break;
				}
				menuBar.conceal();
				if (mAutoDismissGlossView) {
					mGlossView.conceal();
				}
			}
		});
	}

	private void setupActionBar(MenuBar menuBar) {
		menuBar.addButton(ACTION_SHARE, getString(R.string.edit_share), R.drawable.action_share);
		menuBar.addButton(ACTION_GOOGLE, getString(R.string.edit_google), R.drawable.action_search_web);
		menuBar.addButton(ACTION_HIGHLIGHT, getString(R.string.edit_highlight), R.drawable.action_highlight);
		menuBar.addButton(ACTION_COPY, getString(R.string.edit_copy), R.drawable.action_copy);
		menuBar.addButton(ACTION_ATTENTION_DELETE, getString(R.string.edit_delete), R.drawable.action_delete);

		menuBar.setDefault(ACTION_SHARE, ACTION_GOOGLE, ACTION_HIGHLIGHT, ACTION_COPY).showDefault();
		menuBar.setOnMenuButtonClickListener(new MenuBar.OnClickMenuButtonListener() {
			@Override
			public void onMenuButtonClicked(int buttonId) {
				switch (buttonId) {
					case ACTION_ATTENTION_DELETE: {
						if (mCurrentSelectedSpan != null) {
							mUserBookData.removeSpan(mCurrentSelectedSpan);
							mCurrentSelectedSpan = null;
						}
						break;
					}
					case ACTION_HIGHLIGHT: {
						HighlightColorPicker picker = new HighlightColorPicker(JTextActivity.this);
						picker.setOnColorPickedListener(new HighlightColorPicker.OnColorPickedListener() {
							@Override
							public void onColorPicked(int color) {
								int start = Math.min(mSelectionModifier.getStart(), mSelectionModifier.getEnd());
								int end = Math.max(mSelectionModifier.getStart(), mSelectionModifier.getEnd());
								UserSpan spanObj = (new UserSpan.Builder())
										.setType(UserSpanType.HIGHLGHT)
										.setColor(color)
										.setStart(start)
										.setEnd(end)
										.create();
								spanObj.setDescription(mUserBookData.getDefaultDescription(spanObj));

								if (mCurrentSelectedSpan != null) {
									mUserBookData.replaceSpan(spanObj, mCurrentSelectedSpan);
									mCurrentSelectedSpan = spanObj;
								} else {
									mUserBookData.insertSpan(spanObj);
								}

								hideControls();
							}
						});
						picker.show();
						break;
					}
					case ACTION_COPY: {
						String text = mSelectionModifier.getSelectedText();
						copyToClipboard(text);
						makeClipboardToast(text);
						break;
					}
					case ACTION_GOOGLE: {
						startActivity(new Intent(Intent.ACTION_VIEW,
								Uri.parse(mGoogleQuery + mSelectionModifier.getSelectedText())));
						break;
					}
					case ACTION_SHARE: {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.putExtra(Intent.EXTRA_TEXT, mSelectionModifier.getSelectedText());
						intent.setType("text/plain");
						startActivity(intent);
					}
				}
				if (buttonId != ACTION_HIGHLIGHT) {
					hideControls();
				}
			}
		});

		menuBar.setOnRevealListener(new OnRevealListener() {
			@Override
			public void onReveal(Concealable concealable) {
				setActionMenuBarPosition(
						mJScrollView.getScrollY() < mActionBarRepositionThreashold ?
								POSITION_BOTTOM :
								POSITION_TOP);
			}
		});
	}

	/**
	 * position {@code mActionMenuBar} to the specified position
	 *
	 * @param desiredPosition POSITION_TOP or POSITION_BOTTOM
	 */
	private void setActionMenuBarPosition(int desiredPosition) {
		// Note that this is called in mActionMenuBar's onReveal, so DO NOT call
		// mActionMenuBar.reveal() in this function

		if (desiredPosition != mActionMenuBarPosition) {
			boolean position_to_top = desiredPosition == POSITION_TOP;

			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mActionMenuBar.getLayoutParams();

			params.addRule(position_to_top ? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.ALIGN_PARENT_BOTTOM);
			removeRule(params, position_to_top ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);

			mActionMenuBar.setLayoutParams(params);
			mActionMenuBarPosition = desiredPosition;
		}
	}

	/**
	 * Remove the specified rule from the given RelativeLayout param.
	 *
	 * @param params the LayoutParams to be changed
	 * @param rule   the rule to remove
	 */
	private void removeRule(RelativeLayout.LayoutParams params, int rule) {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // API 17
			params.removeRule(rule);
		}
		else {
			params.addRule(rule, 0);
		}
	}

	/**
	 * setup the option menu of this activity
	 */
	private void setupMainOptionBar(MenuBar menuBar) {
		menuBar.addButton(MENU_BOOKMARK, getString(R.string.opt_bookmark), R.drawable.action_bookmark);
		menuBar.addButton(MENU_BRIGHTNESS, getString(R.string.opt_brightness), R.drawable.action_brightness);
		menuBar.addButton(MENU_FONTSIZE, getString(R.string.opt_text_size), R.drawable.action_fontsize);
		menuBar.addButton(MENU_SCROLLER, getString(R.string.opt_seek_text), R.drawable.action_scroll);
		menuBar.addButton(MENU_OPTION, getString(R.string.opt_settings), R.drawable.action_settings);

		menuBar.setDefault(MENU_BOOKMARK, MENU_BRIGHTNESS, MENU_SCROLLER, MENU_FONTSIZE, MENU_OPTION).showDefault();
		menuBar.setOnMenuButtonClickListener(new MenuBar.OnClickMenuButtonListener() {
			@Override
			public void onMenuButtonClicked(int buttonId) {
				if (buttonId != MenuBar.OVERFLOW_BUTTON_ID) {
					hideControls(); // commonn operations
				}
				switch (buttonId) {
					case MENU_BOOKMARK: {
						Intent intent = new Intent(mContext, BookmarkActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						intent.putExtra(getString(R.string.key_bookmark_offset), getCurrentOffset());
						DataKeeper.getInstance().save(getString(R.string.key_bookdata), mUserBookData);
						startActivity(intent);
						break;
					}
					case MENU_FONTSIZE:
						showTextSizeDialog();
						break;
					case MENU_SCROLLER:
						mTextSlider.reveal();
						break;
					case MENU_OPTION: {
						Intent intent = new Intent(mContext, SettingActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(intent);
						break;
					}
					case MENU_BRIGHTNESS:
						mBrightnessSlider.reveal();
						break;
					default:
						break;
				}
			}
		});
	}

	private void setupSecondaryOptionBar(MenuBar menuBar) {
		menuBar.addButton(MENU_TEXT_SELECT, getString(R.string.opt_text_select), R.drawable.action_text_select);
		menuBar.addButton(MENU_ADD_BOOKMARK, getString(R.string.opt_add_bookmark), R.drawable.action_add_bookmark);
		menuBar.addButton(MENU_MY_NOTE, getString(R.string.opt_my_note));

		menuBar.setDefault(MENU_ADD_BOOKMARK, MENU_TEXT_SELECT).showDefault();
		menuBar.setOnMenuButtonClickListener(new MenuBar.OnClickMenuButtonListener() {
			@Override
			public void onMenuButtonClicked(int buttonId) {
				hideControls();
				switch (buttonId) {
					case MENU_TEXT_SELECT:
						mActionMenuBar.showDefault();
						showSelectionCursors(mPrepareOptionX, mPrepareOptionY);
						break;
					case MENU_ADD_BOOKMARK:
						addBookMark(getCurrentOffset());
						break;
				}
			}
		});
	}

	/**
	 * add a bookmark positioned at the specified offset. Display a toast to the user confirming the
	 * bookmark is saved.
	 *
	 * @param offset the offset of the bookmark
	 */
	private void addBookMark(int offset) {
		UserSpan bookmark = (new UserSpan.Builder())
				  .setType(UserSpanType.BOOKMARK)
				  .setStart(offset)
				  .create();
		bookmark.setDescription(mUserBookData.getDefaultDescription(bookmark));
		mUserBookData.insertSpan(bookmark);
		showToast(R.string.bookmark_saved);
	}

	/**
	 * apply the preferences related to this activity
	 */
	private void applyPreferences() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		mKeepGlossHeight = true; // pref.getBoolean(getString(R.string.pref_gloss_height), true);
		mGlossViewHeight = pref.getInt(KEY_GLOSSHEIGHT, 0);

		mGlossView.setTextSizeScaleFactor(pref.getFloat(KEY_GLOSS_TEXT_SCALE, 1.0f));

		mUsePreciseOffset = pref.getBoolean(getString(R.string.pref_precise_offset), true);

		boolean show = pref.getBoolean(getString(R.string.pref_show_status_bar), true);
		AndroidService.showStatusBar(this, show);

		String newVocabularyFilePath = SettingActivity.getVocabularySavePath(this);
		if (!newVocabularyFilePath.equals(mVocabularyFilePath)) {
			mVocabularyFilePath = newVocabularyFilePath;
			mVocabularyFileChanged = true;
		}
		Util.createFileIfNotExist(mVocabularyFilePath);

		// sets the highlight duration
		mSelectionDuration = Integer.parseInt(pref.getString(
				  getString(R.string.pref_highlight_time),
				  getString(R.string.default_highlight_duration)
		));

		// sets the background color
		mJScrollView.setBackgroundColor(pref.getInt(
				  getString(R.string.pref_bg_color),
				  getResources().getColor(R.color.default_bg_color)));

		// sets the selection color
		mSelectionColor = pref.getInt(
				  getString(R.string.pref_highlight_color),
				  getResources().getColor(R.color.default_highlight_color)
		);
		mJTextView.setDefaultSelectionColor(mSelectionColor);

		// set the text color
		mJTextView.setTextColor(pref.getInt(
				  getString(R.string.pref_text_color),
				  getResources().getColor(R.color.default_text_color)
		));

		// restores previous brightness settings to this activity
		WindowManager.LayoutParams layout = getWindow().getAttributes();
		mBrightness = pref.getFloat(KEY_BRIGHTNESS, layout.screenBrightness);
		setBrightness(layout, mBrightness);

		// restores previous font size settings to this activity
		int font_size = pref.getInt(KEY_TEXTSIZE, DEFAULT_TEXT_SIZE);
		mJTextView.setTextSize(font_size);

		// set the line spacing of the text
		float line_spacing = Float.parseFloat(pref.getString(getString(R.string.pref_line_spacing), "1"));
		mJTextView.setLineSpacing(0, line_spacing);

		// sets padding on the side
		if (pref.getBoolean(getString(R.string.pref_text_padding), false)) {
			mJScrollView.setPadding(40, 0, 40, 0);
		}
		else {
			mJScrollView.setPadding(0, 0, 0, 0);
		}

		// set the text color of the defintion view
		mGlossView.setTextColor(
				  pref.getInt(
							 getString(R.string.pref_definition_text_color),
							 getResources().getColor(R.color.default_def_text_color)
				  )
		);

		// set the background color of the definition view
		int glossBackgroundColor = pref.getInt(
				  getString(R.string.pref_definition_bg_color),
				  getResources().getColor(R.color.default_def_bg_color)
		);
		mGlossView.setDefintionBackgroundColor(glossBackgroundColor);

		mAutoDismissGlossView = pref.getBoolean(getString(R.string.pref_auto_dismiss_defn_view), false);

		mIgnoreBracket = pref.getBoolean(getString(R.string.pref_ignore_bracket), true);
	}

	/**
	 * save the word from the gloss view at the specified position
	 *
	 * @param position the position of the word to be saved
	 */
	private void saveWordAtPosition(int position) {
		// on click on the gloss view, save the word
		if (mVocabularyFileChanged || mVocabularyFileWriter == null) {
			try {
				mVocabularyFileWriter = new BufferedWriter(new FileWriter(mVocabularyFilePath, true));
				mVocabularyFileChanged = false;
			} catch (IOException e) {

			}
		}
		Entries entries = (Entries) mGlossView.getTag(R.string.tag_word_list);
		try {
			Entry entry = entries.get(position);
			mVocabularyFileWriter.append(entry.toString() + "\n");
			mVocabularyFileWriter.flush();
			showToast(getString(R.string.success_save_word, entry.getWord()));
		} catch (Exception e) {
			showToast(R.string.failed_to_save_word);
		}

		if (mAutoDismissGlossView) {
			mGlossView.conceal();
		}
	}

	private void sendWordToAnki(int position) {
		Entries entries = (Entries) mGlossView.getTag(R.string.tag_word_list);
		try {
			Entry entry = entries.get(position);
			String word = entry.getWord();
			// remove the / from the beginning of the translation and switch / to ;
			String meaning = entry.getReading() + "\n" + entry.getGloss().substring(1).replace("/", "; ");
			Intent intent = new Intent("org.openintents.action.CREATE_FLASHCARD");
			intent.putExtra("SOURCE_LANGUAGE", "ja");
			intent.putExtra("TARGET_LANGUAGE", "en");
			intent.putExtra("SOURCE_TEXT", word);
			intent.putExtra("TARGET_TEXT", meaning);
			startActivity(intent);
		} catch (Exception e) {
			showToast(R.string.failed_to_save_word);
		}

		if (mAutoDismissGlossView) {
			mGlossView.conceal();
		}

	}

	/**
	 * copy the word at the specified position of the mGlossView
	 *
	 * @param position the position of the word to be copied
	 */
	private void copyWordToClipboard(int position) {
		Entries entries = (Entries) mGlossView.getTag(R.string.tag_word_list);
		String word;
		try {
			word = entries.get(position).getWord();
			copyToClipboard(word);
			makeClipboardToast(word);
		} catch (Exception e) {
			showToast(R.string.clipboard_copy_failed);
		}
	}

	/**
	 * copy the specified text to the clipboard as plain text.
	 *
	 * @param text the text to be copied
	 */
	private void copyToClipboard(String text) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		}
		else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData.newPlainText("clipboard", text);
			clipboard.setPrimaryClip(clip);
		}
	}

	/**
	 * Checks whether the specified char is a (full width) left parenthesis or double left angle
	 * bracket
	 *
	 * @param c the character to check
	 * @return true if the specified char is a left bracket
	 */
	private boolean isLeftBracket(char c) {
		return c == FULLWIDTH_LEFT_PARENTHESIS || c == LEFT_PARENTHESIS || c == DOUBLE_LEFT_ANGLE_BRACKET;
	}

	/**
	 * Checks whether the specified char is a (full width) right parenthesis or double right angle
	 * bracket
	 *
	 * @param c the character to check
	 * @return true if the specified char is a right bracket
	 */
	private boolean isRightBracket(char c) {
		return c == FULLWIDTH_RIGHT_PARENTHESIS || c == RIGHT_PARENTHESIS || c == DOUBLE_RIGHT_ANGLE_BRACKET;
	}

	/**
	 * Checks whether the specified char is a left bracket, right bracket or a pipe
	 *
	 * @param c the char to check
	 * @return true if the char is a left bracket, right bracket or pipe, false otherwise
	 */
	private boolean isIgnorable(char c) {
		return isLeftBracket(c) || isRightBracket(c) || c == FULLWIDTH_PIPE;
	}

	/**
	 * find relavent words starting from the given offset
	 *
	 * @param offset the offset the beginning of the text
	 * @return all the words found
	 */
	private EntriesWrapper findWords(final int offset) {

		CharSequence text;
		Entries entries = null;
		int[] positions = null;

		int textLen = mJTextView.length();

		if (mIgnoreBracket && offset < textLen &&
				  !isIgnorable(mText.charAt(offset)))
		{
			// ignores the bracket (readings) when searching words, only do this
			// if the first character to search for is not a left bracket or pipe

			positions = new int[WORD_SEARCH_LEN];

			StringBuilder sb = new StringBuilder(WORD_SEARCH_LEN);
			int count = 0; // current # of character added
			int i = offset; // current offset of the text
			boolean inBracket = false; // whether are in inside a bracket
			boolean finish = false; // should we finish

			while (!finish && count < WORD_SEARCH_LEN && i < textLen && i - 50 < offset) {
				char c = mText.charAt(i);

				if (isLeftBracket(c)) {
					inBracket = true;
				}

				if (!inBracket && c != FULLWIDTH_PIPE) { // also ignores pipe
					sb.append(c);
					positions[count] = i - offset;
					count++;
				}

				if (isRightBracket(c)) {
					if (inBracket == false) {
						// we did not encounter a left parenthesis before
						// something is wrong, break now
						finish = true;
					}
					inBracket = false;
				}

				i++;
			}
			text = sb.toString();

		}
		else {
			text = mText.subSequence(offset, Math.min(textLen, offset + WORD_SEARCH_LEN));
		}

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) &&
				  mDictinoaryLoaded)
		{
			entries = mRikai.wordSearch(text.toString());
		}

		return new EntriesWrapper(entries, positions);
	}

	/**
	 * display the definition in the gloss view
	 *
	 * @param entries the definitions
	 */
	private void showDefinition(Entries entries) {

		List<String> display = entries.getAllEntriesCompact();

		if (display.size() == 0) {
			// this will make sure the entries size is not 0, otherwise
			// setting the adapter will crash
			display.add(getString(R.string.failed_word_not_found));
		}

		// fill listview with words and definitions
		AdvancedArrayAdapter<String> adapter
				  = new AdvancedArrayAdapter<String>(this, R.layout.definition_row, display);
		mGlossView.setAdapter(adapter);
		mGlossView.setTag(R.string.tag_word_list, entries);

		// set the height of the mGlossView to only occupied part of the screen
		// do this only if the mGlossView is invisible, this is the default height
		if (mScreenOrientation == Configuration.ORIENTATION_PORTRAIT) {
			if (!mGlossView.isDisplaying()) {
				if (mGlossViewHeight <= 0) {
					mGlossViewHeight = (int) (mJScrollView.getHeight() / 2.6);
				}
				mGlossView.setHeight(mGlossViewHeight);
				mGlossView.reveal();
			}
		}
		else {
			if (!mGlossView.isDisplaying()) {
				mGlossView.setHeight(getWindowManager().getDefaultDisplay().getHeight());
				mGlossView.reveal();
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// save the touch position
		// getX() and getY() returns the (x, y) of the touch position
		// within the specified View (not the screen)
		// (e.g. if you have a long TextView that scrolls several screen
		// downward, the value getY() can be really large
		if (v == mJTextView) {
			mTouchX = event.getX();
			mTouchY = event.getY();
			mBrightnessSlider.conceal();
			mTextSlider.conceal();
		}

		// True if the listener has consumed the event, false otherwise.
		return false;
	}


	/**
	 * display the selection cursors and the selection at the spot where the finger is touching
	 *
	 * @param x the x coordinate (relative to the textview) of the starting cursor position
	 * @param y the y coordinate (relative to the textview) of the starting cursor position
	 */
	private void showSelectionCursors(int x, int y) {
		int start = mUsePreciseOffset ?
				  mJTextView.getPreciseOffset(x, y) :
				  mJTextView.getOffset(x, y);

		if (start > -1) {
			EntriesWrapper entriesWrapper = findWords(start);

			int len = entriesWrapper.getLen();
			int end = Math.min(start + len, mText.length() - 1);

			mJTextView.showSelectionControls(start, end);
		}
	}

	/**
	 * get the length of the longest entry in the specified entries
	 *
	 * @param entries     the entries
	 * @param default_len the default length to use if the specified entries is null
	 * @param max_len     the max length of the text
	 * @return the length of the longest entry
	 */
	private int getWordLen(Entries entries, int default_len, int max_len) {
		int len = 0;
		if (entries != null && entries.size() > 0) {
			len = entries.getMaxLen();
		}
		if (len == 0) {
			len = Math.min(default_len, max_len);
		}
		return len;
	}

	@Override
	public boolean onLongClick(View v) {
		if (v == mJTextView) {
			onClickTextAction(true);
			return true; // event has consumed the long click, this prevent onClick from being called
		}
		return false;
	}

	@Override
	public void onClick(View v) {

		if (v == mJTextView) {
			onClickTextAction(false);
		}
		else if (v == textSizePlus || v == textSizeMinus) {
			adjustTextSize(v);
		}
	}


	/**
	 * actions to perform when user clicks on the textview
	 *
	 * @param isLongClick true if the click is a long click
	 */
	private void onClickTextAction(boolean isLongClick) {
		if (controlsVisible()) {
			hideControls();
		}
		else {
			if (!isLongClick) {
				findWordAtCurrentPosition();
			}
			else {
				UserSpan spanObj = mUserBookData.findHighlightSpan(getLastTouchedOffset());
				if (spanObj != null &&
						  (spanObj.getType() == UserSpanType.HIGHLGHT
									 || spanObj.getType() == UserSpanType.UNDERLINE))
				{
					mCurrentSelectedSpan = spanObj;
					mActionMenuBar.show(ACTION_ATTENTION_DELETE, ACTION_SHARE, ACTION_GOOGLE, ACTION_HIGHLIGHT, ACTION_COPY);
					mJTextView.showSelectionControls(spanObj.getStart(), spanObj.getEnd());
					return;
				}
				else {
					showMenus(false /* fromOptionButton */);
				}
			}
		}
	}

	/**
	 * adjust the text size depended on the buttonPressed
	 *
	 * @param buttonPressed either the plus or minus button
	 */
	private void adjustTextSize(View buttonPressed) {
		int new_size = Integer.parseInt(textSizeEdit.getText().toString()) + (buttonPressed == textSizePlus ? 1 : -1);

		mJTextView.setTextSize(new_size);
		textSizeEdit.setText(String.valueOf(new_size));

		/**** PROGRAMMER NOTE ******
		 * if you call Layout layout = mJTextView.getLayout() here,
		 * the returned layout is most probably null because the font
		 * size has just changed. That's why you must call post a runnable
		 * if you want to do anything with the new layout, which is what
		 * the function {@link #goToOffset} does.
		 */

		// font size changed, so that the screen no longer display the text
		// that was displaying before the font size change, need to go back
		// to the original offset before the size is changed
		goToOffset(mRecentOffset);

		// save the text size to the preference
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(KEY_TEXTSIZE, Integer.parseInt(textSizeEdit.getText().toString()));
		editor.commit();
	}

	/**
	 * find and display the definition of the word at the current position
	 */
	private void findWordAtCurrentPosition() {
		final int offset = getLastTouchedOffset();

		if (offset > -1) {
			EntriesWrapper entriesWrapper = findWords(offset);
			Entries entries = entriesWrapper.getEntries();
			if (entries != null) {
				mJTextView.removeSelection();
				mJTextView.setSelection(mSelectionColor, offset, entriesWrapper.getLen(), mSelectionDuration);
				showDefinition(entries);
			}
			else {
				showToast(R.string.failed_load_dict);
			}
		}
	}

	/**
	 * get the offset of the last touched position
	 *
	 * @return the offset of the last touched position
	 */
	private int getLastTouchedOffset() {
		return mUsePreciseOffset ?
				  mJTextView.getPreciseOffset((int) mTouchX, (int) mTouchY) :
				  mJTextView.getOffset((int) mTouchX, (int) mTouchY);
	}


	@Override
	protected void onPause() {
		super.onPause();
		// save the current position of the text view so that
		// the next time the reader returns, it can go back to
		// the last read position


		if (mUserBookData != null) {

			// we need to make sure we have scrolled to the saved offset before we use
			// {@code getCurrentOffset()}. If the app changes its orientation before Android
			// can draw the content, getCurrentOffset will return 0 because we have not yet scrolled
			// to the last saved offset.
			// So use getCurretnOFfset() if we have scrolled there successfully. Otherwise, we
			// we use the last saved offset obtained from the saved files directly.
			if (mScrolledToSavedOffset) {
				mUserBookData.setOffset(getCurrentOffset());
			}
			else if (mSavedOffset >= 0) {
				mUserBookData.setOffset(mSavedOffset);
			}

			mUserBookData.save();
		}

		if (mVocabularyFileWriter != null) {
			try {
				mVocabularyFileWriter.flush();
				mVocabularyFileWriter.close();
				mVocabularyFileWriter = null;
			} catch (IOException e) {}
		}

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

		if (mScreenOrientation == Configuration.ORIENTATION_PORTRAIT) {
			if (!mKeepGlossHeight) {
				mGlossViewHeight = 0;
			}
			if (mGlossView.isDisplaying()) {
				mGlossViewHeight = mGlossView.getHeight();
			}
			editor.putInt(KEY_GLOSSHEIGHT, mGlossViewHeight);
			Log.i(TAG, "saving height: " + mGlossViewHeight);
		}
		editor.putFloat(KEY_GLOSS_TEXT_SCALE, mGlossView.getTextSizeScaleFactor());
		editor.commit();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mRikai.close();
	}


	/**
	 * returns the current offset of the textview
	 *
	 * @return the current offset of the textview
	 */
	private int getCurrentOffset() {
		// TODO: fix it so it doesn't depend on the magic number (20)
		// why plus 20??????
		// if i don't add 20 it seems like the offset will be off by a line
		// this is definitely an ugly workaround
		// to see this, remove the 20, and keep opening and closing the same text file,
		// you'll see the the offset will be shifted each time the file is opened
		int scrollY = mJScrollView.getScrollY() + 20;
		Layout layout = mJTextView.getLayout();

		int offset = 0;
		if (layout != null) {
			int topVisibleLine = layout.getLineForVertical(scrollY);
			offset = layout.getOffsetForHorizontal(topVisibleLine, 0f);
		}
		return offset;
	}

	/**
	 * move the main textview to the given offset
	 *
	 * @param offset the offset to move to
	 */
	private void goToOffset(final int offset) {
		mSavedOffset = offset;
		Runnable runnable = new Runnable() {
			public void run() {
				Layout layout = mJTextView.getLayout();
				if (layout != null) {
					int line = layout.getLineForOffset(offset);
					mJScrollView.scrollTo(0, line * mJTextView.getLineHeight());
					mScrolledToSavedOffset = true;
				}
			}
		};

		mJScrollView.post(runnable);
	}


	private class OnSelectionCursorStateChanged implements JTextView.OnCursorStateChangedListener {
		@Override
		public void onHideCursors(View v) {
			mActionMenuBar.conceal();
		}

		@Override
		public void onShowCursors(View v) {
			mActionMenuBar.reveal();
		}

		@Override
		public void onDragStarts(View v) {
			if (mScreenOrientation == Configuration.ORIENTATION_PORTRAIT) {
				mMenuBar.conceal();
				mGlossView.conceal();
			}
		}

		@Override
		public void onPositionChanged(View v, int x, int y, int oldx, int oldy) {
			if (mCurrentSelectedSpan != null) {
				mCurrentSelectedSpan = null;
				mActionMenuBar.showDefault();
			}
		}
	}


	/**
	 * Behaviour of when user scrolls the textview
	 */
	private class OnTextScrolled implements OnScrollChangedListener {
		@Override
		public void onScrollChanged(JScrollView scrollView, int x, int y, int oldx, int oldy) {
			if (mScreenOrientation == Configuration.ORIENTATION_PORTRAIT) {
				mMenuBar.conceal();

				if (mAutoDismissGlossView) {
					mGlossView.conceal();
				}
			}
			mBrightnessSlider.conceal();
			mWordActionBar.conceal();


			setActionMenuBarPosition(y < mActionBarRepositionThreashold ? POSITION_BOTTOM : POSITION_TOP);
		}
	}

	private class OnConcealGlossView implements OnConcealListener {
		@Override
		public void onConceal(Concealable concealable) {
			if (concealable == mGlossView) {
				if (mScreenOrientation == Configuration.ORIENTATION_PORTRAIT && mGlossView.isDisplaying()) {
					// if it's not displaying, the returned height will be 0
					mGlossViewHeight = mGlossView.getHeight();
					mWordActionBar.conceal();
					Log.i(TAG, "saving height: " + mGlossViewHeight);
				}
			}
		}
	}


	OnItemClick mOnEntryClicked = new OnItemClick();

	private class OnItemClick implements AdapterView.OnItemClickListener,
			  AdapterView.OnItemLongClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			sendWordToAnki(position);
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			mWordActionBar.setTag(R.string.tag_word_view, view);
			mWordActionBar.setTag(R.string.tag_word_pos, position);
			mWordActionBar.reveal();
			return true; // consumed the event, so that onItemClick is not triggered
		}
	}

	private class MyMenuBar implements Concealable {

		@Override
		public void conceal() {
			if (mScreenOrientation == Configuration.ORIENTATION_PORTRAIT) {
				mMainMenuBar.conceal();
			}
			mSecondaryMenuBar.conceal();
		}

		@Override
		public void reveal() {
			if (mScreenOrientation == Configuration.ORIENTATION_PORTRAIT) {
				mMainMenuBar.reveal();
				mGlossView.conceal();
			}
			mSecondaryMenuBar.reveal();
		}

		@Override
		public boolean isDisplaying() {

			return mMainMenuBar.isDisplaying() && mSecondaryMenuBar.isDisplaying();
		}
	}

	private class EntriesWrapper {
		Entries mEntries;
		int[] mPositions;

		/**
		 * @param entries   list of word
		 * @param positions the actual position of each character. A character might be a few characters
		 *                  away from its previous one becuase of furigana readings in between.
		 */
		public EntriesWrapper(Entries entries, int[] positions) {
			mEntries = entries;
			mPositions = positions;
		}

		@Nullable
		public Entries getEntries() {
			return mEntries;
		}


		/**
		 * Gets the length of the entry with the maximum length.
		 *
		 * @return the length of the longest entry, or 5 if no entry exist.
		 */
		public int getLen() {
			if (mEntries != null && mEntries.size() > 0) {
				int l = mEntries.getMaxLen();
				if (mPositions != null) {
					if (l < mPositions.length) {
						int p = mPositions[l]; // position of the next char
						if (p == l + 1) {
							// this probably means there is a pipe at the end, so don't include it
							return l;
						}
						return p;
					}
					else {
						return mPositions[mPositions.length - 1] + 1;
					}
				}
				else {
					return l;
				}
			}
			return 5;
		}
	}
}
