/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.renren.graffiti.paint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.renren.graffiti.R;
import com.renren.graffiti.paint.view.GraffitiView;

public class FingerPaint extends GraphicsActivity implements
		ColorPickerDialog.OnColorChangedListener {
	LinearLayout mLinear;
	GraffitiView mMV;
	Button btn;
	public static boolean flag;
	int screenW, screenH;
	Bitmap bm;
	BitmapDrawable bd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		screenH = display.getHeight();
		screenW = display.getWidth();
		setContentView(R.layout.main);
		mLinear = (LinearLayout) findViewById(R.id.linear);
		btn = (Button) findViewById(R.id.undo);

		bm = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.process).copy(Bitmap.Config.ARGB_8888, true);
		bd = new BitmapDrawable(bm);
		mMV = new GraffitiView(this, bm);

		Log.d("big integer", Integer.MAX_VALUE + "\n" + Integer.MIN_VALUE);
		// mMV.setBackgroundResource(R.drawable.process802);
		mLinear.addView(mMV);
		mLinear.setBackgroundColor(Color.YELLOW);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mMV.undoLast();
			}
		});

		Button saveBtn = (Button) findViewById(R.id.save);
		saveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String path = Environment.getExternalStorageDirectory()
						+ "/graffiti" + System.currentTimeMillis() + ".jpg";
				File f = new File(path);
				boolean s = false;
				try {
					f.createNewFile();
					FileOutputStream fOut = null;
					fOut = new FileOutputStream(f);
					s = mMV.saveDrawStream(fOut);
					fOut.flush();
					fOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (s) {
					Log.d("save", "save success");
				}
			}
		});

		Button turnBtn = (Button) findViewById(R.id.turn);
		turnBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mMV.turnOn(true);
				Log.d("nums", mMV.getPathNums() + "");
			}
		});

		flag = true;

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xFFFF0000);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(40);

		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);

		mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
	}

	private Paint mPaint;
	private MaskFilter mEmboss;
	private MaskFilter mBlur;

	public void colorChanged(int color) {
		mMV.changeColor(color);
	}

	private static final int COLOR_MENU_ID = Menu.FIRST;
	private static final int EMBOSS_MENU_ID = Menu.FIRST + 4;
	private static final int BLUR_MENU_ID = Menu.FIRST + 5;
	private static final int ERASE_MENU_ID = Menu.FIRST + 2;
	private static final int MASOIC_MENU_ID = Menu.FIRST + 1;
	private static final int UNDO_MENU_ID = Menu.FIRST + 3;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
		// menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
		// menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
		menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
		menu.add(0, MASOIC_MENU_ID, 0, "Masoic").setShortcut('5', 'z');
		menu.add(0, UNDO_MENU_ID, 0, "Undo").setShortcut('5', 'z');

		/****
		 * Is this the mechanism to extend with filter effects? Intent intent =
		 * new Intent(null, getIntent().getData());
		 * intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		 * menu.addIntentOptions( Menu.ALTERNATIVE, 0, new ComponentName(this,
		 * NotesList.class), null, intent, 0, null);
		 *****/
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mPaint.setXfermode(null);
		mPaint.setAlpha(0xFF);

		switch (item.getItemId()) {
		case COLOR_MENU_ID:
			mPaint.setXfermode(null);
			new ColorPickerDialog(this, this, mPaint.getColor()).show();
			mMV.setWidth(20);
			break;
		case EMBOSS_MENU_ID:
			if (mPaint.getMaskFilter() != mEmboss) {
				mPaint.setMaskFilter(mEmboss);
			} else {
				mPaint.setMaskFilter(null);
			}
			break;
		case BLUR_MENU_ID:
			if (mPaint.getMaskFilter() != mBlur) {
				mPaint.setMaskFilter(mBlur);
			} else {
				mPaint.setMaskFilter(null);
			}
			break;
		case ERASE_MENU_ID:
			mMV.resetGraffiti();
			break;
		case MASOIC_MENU_ID:
			mMV.addMosaic(25);
			mMV.setWidth(40);
			break;
		case UNDO_MENU_ID:
			mMV.undoLast();
			break;
		}

		return super.onOptionsItemSelected(item);
	}
}
