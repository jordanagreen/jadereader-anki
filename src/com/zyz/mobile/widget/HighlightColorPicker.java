package com.zyz.mobile.widget;
/*
Copyright (C) 2013 Ray Zhou

Author: ray
Date: 2013-11-05

*/

import afzkl.development.mColorPicker.ColorPickerDialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.zyz.mobile.R;

public class HighlightColorPicker extends AlertDialog implements
		  AdapterView.OnItemClickListener,
		  AdapterView.OnItemLongClickListener
{

	private ColorPickerAdapter mColorPickerAdapter;
	private int mColorPicked;
	private OnColorPickedListener mOnColorPickedListener;
	private Context mContext;


	public HighlightColorPicker(Context context)
	{
		super(context);

		mContext = context;
		init();
	}

	private void init()
	{
		// don't dim the background
		// getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

		LayoutInflater inflater = (LayoutInflater) mContext
				  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View highlightPicker = inflater.inflate(R.layout.highlight_picker, null);
		GridView colorPickerGrid = (GridView) highlightPicker.findViewById(R.id.color_grid);

		int backgroundColor = PreferenceManager.getDefaultSharedPreferences(mContext).
				  getInt(mContext.getString(R.string.pref_bg_color), mContext.getResources().getColor(R.color.default_bg_color));
		// colorPickerGrid.setBackgroundColor(backgroundColor);

		mColorPickerAdapter = new ColorPickerAdapter(mContext);
		colorPickerGrid.setAdapter(mColorPickerAdapter);
		colorPickerGrid.setOnItemClickListener(this);
		colorPickerGrid.setOnItemLongClickListener(this);

		this.setView(highlightPicker, 0, 0, 0, 0);
		this.setIcon(0);
		this.setTitle(R.string.title_highlight_picker);
	}

	/**
	 * get the color picked by the user
	 *
	 * @return the color picked by the user
	 */
	public int getColor()
	{
		return mColorPicked;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		mColorPicked = mColorPickerAdapter.getColor(position);
		if (mOnColorPickedListener != null) {
			mOnColorPickedListener.onColorPicked(mColorPicked);
		}
		this.dismiss();
	}

	public void setOnColorPickedListener(OnColorPickedListener onColorPickedListener)
	{
		mOnColorPickedListener = onColorPickedListener;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		final ColorPickerDialog colorPickerDialog =
				  new ColorPickerDialog(mContext, mColorPickerAdapter.getColor(position));

		final int i = position;

		colorPickerDialog.setAlphaSliderVisible(true);

		// on ok, change the color at the current position
		colorPickerDialog.setButton(
				  BUTTON_POSITIVE,
				  mContext.getString(R.string.msg_ok),
				  new OnClickListener()
				  {
					  @Override
					  public void onClick(DialogInterface dialog, int which)
					  {
						  mColorPickerAdapter.setColor(i, colorPickerDialog.getColor());
						  mColorPickerAdapter.notifyDataSetChanged();
					  }
				  });

		// on cancel, do nothing
		colorPickerDialog.setButton(
				  BUTTON_NEGATIVE,
				  mContext.getString(R.string.msg_cancel),
				  new OnClickListener()
				  {
					  @Override
					  public void onClick(DialogInterface dialog, int which)
					  {
					  }
				  });

		colorPickerDialog.show();

		return true;
	}

	public interface OnColorPickedListener
	{
		public void onColorPicked(int color);
	}
}
