package com.zyz.mobile.jade;/*
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

Author: ray
Date: 2013-06-13

*/

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.zyz.mobile.R;

/**
 * implementation copied from http://blog.sqisland.com/2012/09/android-swipe-image-viewer-with-viewpager.html
 */
public class HelpActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.help_activity);

		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		ImagePagerAdapter adapter = new ImagePagerAdapter();
		viewPager.setAdapter(adapter);
	}

	private class ImagePagerAdapter extends PagerAdapter {
		private int[] mImages = new int[]{
				  R.drawable.help01_1,
				  R.drawable.help02,
				  R.drawable.help03,
				  R.drawable.help04,
				  R.drawable.help05,
				  R.drawable.help06,
				  R.drawable.help07,
				  R.drawable.help08,
				  R.drawable.help09,
				  R.drawable.help10
		};

		@Override
		public int getCount() {
			return mImages.length;
		}

		@SuppressWarnings("RedundantCast")
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == (ImageView) object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Context context = HelpActivity.this;
			ImageView imageView = new ImageView(context);
			int padding = context.getResources().getDimensionPixelSize(R.dimen.padding_medium);

			imageView.setPadding(padding, padding, padding, padding);
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setImageResource(mImages[position]);
			//noinspection RedundantCast
			((ViewPager) container).addView(imageView, 0);

			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			//noinspection RedundantCast
			((ViewPager) container).removeView((ImageView) object);
		}
	}

}
