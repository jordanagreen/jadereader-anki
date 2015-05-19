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
package com.zyz.mobile.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import com.zyz.mobile.R;
import com.zyz.mobile.util.AdvancedArrayAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This a MenuBar that's similar to the ActionBar in android 3.0 (honeycomb). Implemented because
 * 2.3 has no ActionBar
 */
public class MenuBar extends RelativeLayout implements Concealable {

	@SuppressWarnings("unused")
	private static final String TAG = "JTEXT_MENU_BUTTON";

	private Context mContext;

	/**
	 * default menu bar background transparent
	 */
	private int mBackgroundColor = 0;

	/**
	 * background color of button when pressed
	 */
	private int mBackgroundColorPressed = 0;

	/**
	 * button text color
	 */
	private int mButtonTextColor = 0xffffffff;

	/**
	 * divider color default to black
	 */
	private int mDividerColor = 0xffffffff;

	/**
	 * Overflow button
	 */
	private MenuButton mOverflowButton = null;
	private PopupWindow mOverflowMenuContainer = null;
	public static final int OVERFLOW_BUTTON_ID = -127; // id reserved for the overflow button

	// reference to buttons, mapped wih their corresponding id
	private Map<Integer, MenuButton> mMenuButtons = new LinkedHashMap<Integer, MenuButton>();
	private ArrayList<MenuButton> mOverflowedButtons = new ArrayList<MenuButton>();
	private int mMenuBarWidth;

	private String mButtonPosition = "right";

	// parent layout for the all menu buttons
	private LinearLayout mMenuLayout;

	// size use for the text title if used
	private int mTitleTextSize = 20;

	// current view used as the title
	private View currentTitleView = null;

	// default buttons
	private int[] mDefaultButtons = new int[0];

	private boolean mShowButtonText = true;

	/**
	 * listeners
	 */
	private OnClickMenuButtonListener mOnClickMenuButtonListener;
	private OnLongClickMenuButtonListener mOnLongClickMenuButtonListener;
	private OnRevealListener mOnRevealListener;
	private OnConcealListener mOnConcealListener;


	// private ViewGroup mMeasureParent;

	public MenuBar(Context context) {
		super(context);
		init(context, null);
	}

	@SuppressWarnings("unused")
	public MenuBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}


	/**
	 * @param context the mContext
	 */
	private void init(Context context, @Nullable AttributeSet attrs) {
		this.mContext = context;

		if (attrs != null) {
			init(attrs);
		}

		// the linear layout for all buttons
		mMenuLayout = new LinearLayout(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				  RelativeLayout.LayoutParams.WRAP_CONTENT,  // width
				  RelativeLayout.LayoutParams.WRAP_CONTENT); // height
		mMenuLayout.setLayoutParams(lp);

		// align the action buttons layout to the right
		lp = new RelativeLayout.LayoutParams(
				  RelativeLayout.LayoutParams.WRAP_CONTENT,  // width
				  RelativeLayout.LayoutParams.WRAP_CONTENT); // height

		if (mButtonPosition.equals("right")) {
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}
		else {
			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		}

		// add the LinearLayout under this RelativeLayout
		addView(mMenuLayout, lp);
	}

	/**
	 * set the attributes
	 *
	 * @param attrs attributes
	 */
	private void init(AttributeSet attrs) {

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MenuBar);

		mDividerColor = a.getColor(
				  R.styleable.MenuBar_divider_color,
				  getResources().getColor(R.color.white)
		);
		mTitleTextSize = a.getDimensionPixelSize(R.styleable.MenuBar_title_size, 20);
		mBackgroundColor = a.getColor(R.styleable.MenuBar_button_background, 0);
		mBackgroundColorPressed = a.getColor(R.styleable.MenuBar_button_background_on_pressed, 0);
		mButtonTextColor = a.getColor(
				  R.styleable.MenuBar_button_text_color,
				  getResources().getColor(R.color.white)
		);
		mButtonPosition = a.getString(R.styleable.MenuBar_button_position);
		if (mButtonPosition == null) {
			mButtonPosition = "right";
		}
		mShowButtonText = a.getBoolean(R.styleable.MenuBar_show_text, true);
		this.setBackgroundColor(mBackgroundColor);
		a.recycle();
	}

	/**
	 * Create an ImageView with the given drawable and set the resulting View as the title of the menu
	 * bar
	 *
	 * @param drawable the image of the title
	 */
	public void setTitle(int drawable) {
		ImageView titleView = new ImageView(mContext);
		titleView.setImageResource(drawable);
		titleView.setBackgroundColor(getResources().getColor(R.color.transparent));

		setTitle(titleView);
	}

	/**
	 * Create a TextView with the given string and set the resulting View as the title of the menu bar
	 *
	 * @param title the title text
	 */
	public void setTitle(String title) {
		TextView titleView = new TextView(mContext);

		titleView.setText(title);
		titleView.setTextColor(mButtonTextColor);
		titleView.setBackgroundColor(getResources().getColor(R.color.transparent));
		titleView.setTextSize(mTitleTextSize);

		setTitle(titleView);
	}

	/**
	 * Used the specified view as the title of the menu bar. Previously set title will be erased
	 *
	 * @param view the title view
	 */
	private void setTitle(View view) {
		if (currentTitleView != null) {
			this.removeView(currentTitleView);
			currentTitleView = null;
		}
		currentTitleView = view;

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				  ViewGroup.LayoutParams.WRAP_CONTENT,  // width
				  ViewGroup.LayoutParams.WRAP_CONTENT); // height
		params.addRule(ALIGN_PARENT_LEFT | CENTER_VERTICAL);
		this.addView(view, params);
	}


	/**
	 * ///NOT USED/// add a divider
	 */
	private void addDivider() {
		final float scale = getResources().getDisplayMetrics().density;
		int dividerWidth = (int) (1 * scale + 0.5f);

		TextView divider = new TextView(mContext);
		divider.setBackgroundColor(mDividerColor);
		divider.setWidth(dividerWidth);

		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				  ViewGroup.LayoutParams.WRAP_CONTENT,  // width
				  ViewGroup.LayoutParams.MATCH_PARENT); // height
		mMenuLayout.addView(divider, params);
	}

	// stolen from MenuPopupHelper.java
	private int measureContentWidth(ListAdapter adapter, ViewGroup measureParent) {
		// Menus don't tend to be long, so this is more sane than it looks.
		int width = 0;
		View itemView = null;
		int itemType = 0;
		final int widthMeasureSpec =
				  MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		final int heightMeasureSpec =
				  MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final int positionType = adapter.getItemViewType(i);
			if (positionType != itemType) {
				itemType = positionType;
				itemView = null;
			}
//			if (mMeasureParent == null) {
//				mMeasureParent = new FrameLayout(mContext);
//			}
			itemView = adapter.getView(i, itemView, measureParent);
			itemView.measure(widthMeasureSpec, heightMeasureSpec);
			width = Math.max(width, itemView.getMeasuredWidth());
		}
		return width;
	}

	/**
	 * the button append to the end of the MenuBar when the size of the Bar cannot be accommodate all
	 * the available options
	 *
	 * @return the MenuButton added to the menuBar
	 */
	private MenuButton getOverflowButton() {

		if (mOverflowButton == null) {
			mOverflowButton = new MenuButton(mContext);
			mOverflowButton.setId(R.id.overflow_button);
			mOverflowButton.setContent(" ", R.drawable.action_overflow, MenuButton.Style.TEXT_AND_IMAGE);
			mOverflowButton.setBackgroundColor(getResources().getColor(R.color.transparent));
			mOverflowButton.setClickable(true);
			mOverflowButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					/*
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						// in API Level 11 (HoneyComb) we can simply use PopupMenu to do all
						// the heavy lifting for us
						// currently not used.

						PopupMenu overflowMenu = new PopupMenu(getContext(), mOverflowButton);
						for (int i = 0; i < mOverflowedButtons.size(); i++) {
							overflowMenu.getMenu().add(Menu.NONE, i, Menu.NONE, mOverflowedButtons.get(i).getOverflowText());
						}
						...
						...
					}
					*/
					if (mOnClickMenuButtonListener != null) {
						mOnClickMenuButtonListener.onMenuButtonClicked(OVERFLOW_BUTTON_ID);
					}
					// In Gingerbread or below, we can use PopupWindow to achieve this
					if (mOverflowMenuContainer == null) {
						final AdvancedArrayAdapter<MenuButton> adapter = new AdvancedArrayAdapter<MenuButton>(
								  getContext(),
								  R.layout.simple_list_item_1,
								  mOverflowedButtons
						);
//						final MenuItemAdapter adapter = new MenuItemAdapter(
//								  getContext(),
//								  R.layout.simple_list_item_1,
//								  mOverflowedButtons
//						);
						adapter.setColor(getResources().getColor(R.color.white));

						ListView overflowListView = new ListView(getContext());
						overflowListView.setAdapter(adapter);
						overflowListView.setBackgroundColor(getResources().getColor(R.color.black));
						overflowListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
								mOverflowMenuContainer.dismiss();
								if (mOnClickMenuButtonListener != null) {
									mOnClickMenuButtonListener.onMenuButtonClicked(
											  adapter.getItem(position).getMenuButtonId()
									);
								}
							}
						});

						/*
						 * MUST give a context to the popupwindow, otherwise, the popupwindow
						 * does not dismiss when clicked ouside!!!! (need setFocusable(true))
						 * WHY? I DO NOT KNOW!!
						 * I still don't know what setOutsideTouchable does. I wasn't able to
						 * intercept any ACTION_OUTSIDE MotionEvent.. fk
						 */
						mOverflowMenuContainer = new PopupWindow(getContext());
						mOverflowMenuContainer.setContentView(overflowListView);

						// setWidth(WindowManager.LayoutParams.WRAP_CONTENT) results a width
						// longer than tha longest content, and non-text area becomes unclickable.
						// why? only god knows.
						// so instead, i had to call {@code #measureContentWidth}
						int maxWidth = getResources().getDisplayMetrics().widthPixels / 2;
						mOverflowMenuContainer.setWidth(
								  Math.min(measureContentWidth(adapter, overflowListView), maxWidth));
						mOverflowMenuContainer.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
						mOverflowMenuContainer.setFocusable(true);

						// MUST set the background drawable to EMPTY BitmapDrawable, otherwise
						// the width of the PopupWindow will be a tiny bit smaller than the width
						// set by setWidth.
						// (Setting it to null will rendered the popupwindow not detecting outside click)
						//
						// WHY? I DON'T FUCKING KNOW!!! FUCK !!!
						mOverflowMenuContainer.setBackgroundDrawable(new BitmapDrawable(getResources()));
					}

					mOverflowMenuContainer.showAsDropDown(mOverflowButton);
				}
			});

			setMenuButtonColor(mOverflowButton);
		}
		return mOverflowButton;
	}


	/**
	 * add a button to this MenuBar
	 *
	 * @param buttonId     the id of this button
	 * @param overflowText the overflow text of the button
	 * @return the newly created MenuButton
	 */
	public MenuButton addButton(int buttonId, String overflowText) {
		return addButton(buttonId, overflowText, MenuButton.IMAGE_RESOURCE_NO_ID);
	}

	/**
	 * add a button to this MenuBar.
	 *
	 * @param buttonId             the id of the button
	 * @param overflowTextStringId the string id of the overflow text
	 * @param resId                the resource id of the
	 * @return the newly created MenuButton
	 */
	public MenuButton addButton(final int buttonId, final int overflowTextStringId, final int resId) {
		return addButton(buttonId, getResources().getString(overflowTextStringId), resId);
	}

	/**
	 * add a button to this MenuBar
	 *
	 * @param buttonId     the id of this button used to distinguish this button
	 * @param overflowText the overflow text of the button
	 * @param resId        the resource id of the drawable of the button
	 * @return the newly created MenuButton
	 */
	public MenuButton addButton(final int buttonId, @NotNull final String overflowText, final int resId) {
		if (mMenuButtons.containsKey(buttonId) || buttonId == OVERFLOW_BUTTON_ID) {
			// does not allow duplicate ID
			throw new IllegalArgumentException("buttonId: " + buttonId + " already exists");
		}

		final MenuButton menuButton = new MenuButton(mContext);
		setMenuButtonOnClickListener(buttonId);
		menuButton.setMenuButtonId(buttonId);
		menuButton.setContent(overflowText, resId,
				  mShowButtonText ? MenuButton.Style.TEXT_AND_IMAGE : MenuButton.Style.TEXT_OR_IMAGE);
		menuButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);

		setMenuButtonColor(menuButton);
		mMenuButtons.put(buttonId, menuButton);

		return menuButton;
	}

	/**
	 * return the button with the specified id
	 *
	 * @param buttonId the id of the button
	 * @return ghe button with the specified id, or null if the id doesn't exists.
	 */
	public MenuButton getButton(int buttonId) {
		return mMenuButtons.get(buttonId);
	}

	/**
	 * checks whether a actionId has already been used
	 *
	 * @param id the id of a button
	 * @return true if the specified id has been used, false otherwise
	 */
	public boolean actionIdExists(int id) {
		return mMenuButtons.containsKey(id);
	}


	/**
	 * add the specified button to the menu button layout
	 *
	 * @param button the button to be added
	 */
	private void addButtonToLayout(MenuButton button) {
		// even though the layout for each button is the same, a NEW layout must
		// be created for each button. If the same layout is reference for different buttons,
		// the buttons will end up overlapping with each other
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				  ViewGroup.LayoutParams.WRAP_CONTENT,  // width
				  ViewGroup.LayoutParams.WRAP_CONTENT,  // height
				  1.0f); // weight
		mMenuLayout.addView(button, params);
	}

	/**
	 * set the color of the menu button
	 *
	 * @param button the button
	 */
	private void setMenuButtonColor(final MenuButton button) {
		button.setTextColor(mButtonTextColor);
		button.setBackgroundColor(getResources().getColor(R.color.transparent));
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						button.setBackgroundColor(mBackgroundColorPressed);
						break;
					case MotionEvent.ACTION_UP:
						button.setBackgroundColor(getResources().getColor(R.color.transparent));
						break;
				}
				return false; // has not consumed the event
			}
		});

	}

	/**
	 * Redraw this to show only the button with the specified button id.
	 *
	 * @param buttonIds the buttonIds of each buttons.
	 * @return this menu bar
	 */
	public MenuBar show(int... buttonIds) {
		mMenuLayout.removeAllViews();
		for (int id : buttonIds) {
			MenuButton button = getButton(id);
			if (button != null) {
				addButtonToLayout(button);
			}
		}
		invalidate();
		return this;
	}

	/**
	 * Redraw this to show all buttons added through {@link #addButton}
	 *
	 * @return this menu bar
	 */
	public MenuBar showAll() {
		mMenuLayout.removeAllViews();
		for (MenuButton button : mMenuButtons.values()) {
			addButtonToLayout(button);
		}
		invalidate();
		return this;
	}

	/**
	 * Set the defualt buttons of this MenuBar
	 *
	 * @param buttonIds the buttonIds of the default buttons
	 * @return this {@link MenuBar}
	 */
	public MenuBar setDefault(int... buttonIds) {
		mDefaultButtons = buttonIds;
		return this;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if (mMenuBarWidth == 0) {
			mMenuBarWidth = this.getWidth();

			// we can only get the width of this menubar during layout
			// if the buttons 'overflow' (i.e. too many buttons to fit the width)
			// we have make adjustment and do the layout again
			if (mMenuBarWidth != 0) {
				// calculate the total width first
				int totalWidth = 0;
				for (int i = 0; i < mMenuLayout.getChildCount(); i++) {
					totalWidth += ((MenuButton) mMenuLayout.getChildAt(i)).getButtonWidth();
				}

				if (totalWidth <= mMenuBarWidth) {
					return;
				}

				int index = mMenuLayout.getChildCount() - 1;
				int overflowButtonWidth = getOverflowButton().getButtonWidth();
				while (totalWidth + overflowButtonWidth > mMenuBarWidth) {
					MenuButton button = (MenuButton) mMenuLayout.getChildAt(index);
					mOverflowedButtons.add(button);
					mMenuLayout.removeViewAt(index--);
					totalWidth -= button.getButtonWidth();
				}
				addButtonToLayout(getOverflowButton());
			}
		}
	}

	/**
	 * Show the default layout of the MenuBar. {@link #setDefault(int...)} should be called before
	 * calling this method. If {@link #setDefault(int...)} was never called, this method behaves the
	 * same as {@link #showAll()}
	 */
	public void showDefault() {
		if (mDefaultButtons.length == 0) {
			showAll();
		}
		else {
			show(mDefaultButtons);
		}
	}

	/**
	 * hide this MenuBar
	 */
	@Override
	public void conceal() {
		setVisibility(View.INVISIBLE);
		if (mOverflowMenuContainer != null) {
			mOverflowMenuContainer.dismiss();
		}
		if (mOnConcealListener != null) {
			mOnConcealListener.onConceal(this);
		}
	}

	/**
	 * show this MenuBar
	 */
	@Override
	public void reveal() {
		if (mOnRevealListener != null) {
			mOnRevealListener.onReveal(this);
		}
		setVisibility(View.VISIBLE);
	}

	public void setOnRevealListener(OnRevealListener onRevealListener) {
		mOnRevealListener = onRevealListener;
	}

	public void setOnConcealListener(OnConcealListener onConcealListener) {
		mOnConcealListener = onConcealListener;
	}

	/**
	 * @return true the MenuBar is displaying, false otherwise
	 */
	@Override
	public boolean isDisplaying() {
		return getVisibility() == View.VISIBLE;
	}

	/**
	 * return the background color of this {@link #MenuBar}
	 *
	 * @return the background color
	 */
	public int getBackgroundColor() {
		return mBackgroundColor;
	}

	/**
	 * user can override this to define the action to take when the specified button is clicked
	 */
	public void setOnMenuButtonClickListener(OnClickMenuButtonListener listener) {
		mOnClickMenuButtonListener = listener;
		for (int buttonId : mMenuButtons.keySet()) {
			setMenuButtonOnClickListener(buttonId);
		}
	}

	public void setOnMenuButtonLongClickListener(OnLongClickMenuButtonListener listener) {
		mOnLongClickMenuButtonListener = listener;
		for (int buttonId : mMenuButtons.keySet()) {
			setMenuButtonOnLongClickListener(buttonId);
		}
	}

	private void setMenuButtonOnLongClickListener(final int buttonId) {
		if (mMenuButtons.containsKey(buttonId) || buttonId == OVERFLOW_BUTTON_ID) {
			getButton(buttonId).setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if (mOnLongClickMenuButtonListener != null) {
						return mOnLongClickMenuButtonListener.onMenuButtonLongClicked(buttonId);
					}
					return false;
				}
			});
		}

	}

	/**
	 * set the specified button's onClickListener to the internal {@link #mOnClickMenuButtonListener}
	 * action
	 *
	 * @param buttonId the id of the button
	 */
	private void setMenuButtonOnClickListener(final int buttonId) {
		if (mMenuButtons.containsKey(buttonId) || buttonId == OVERFLOW_BUTTON_ID) {
			getButton(buttonId).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnClickMenuButtonListener != null) {
						mOnClickMenuButtonListener.onMenuButtonClicked(buttonId);
					}
				}
			});
		}
	}

	public interface OnClickMenuButtonListener {

		public void onMenuButtonClicked(int buttonId);

	}

	public interface OnLongClickMenuButtonListener {

		public boolean onMenuButtonLongClicked(int buttonId);
	}
}
