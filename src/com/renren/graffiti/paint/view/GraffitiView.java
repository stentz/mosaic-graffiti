package com.renren.graffiti.paint.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.renren.graffiti.commands.Command;
import com.renren.graffiti.commands.CommandManager;

public class GraffitiView extends SurfaceView implements Callback, Runnable {
	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;
	private static final String TAG = "GraffitiView";
	private Path mPath;
	// 用于控制SurfaceView
	private SurfaceHolder sfh;
	private Paint mPaint;
	private Thread th;

	private Canvas canvas;
	private Bitmap srcBitmap;
	private Bitmap masBitmap;
	private boolean flag = false;

	private final CommandManager commandManager;
	private Bitmap drawingBitmap;
	private final Canvas bitmapCanvas;
	private boolean isDraw = false;
	public boolean isMask = false;
	private boolean firstMas = true;
	private PathsNumbersListener mListener;
	private Bitmap srcScaledbitmap;
	private Bitmap masScaledbitmap;

	/**
	 * SurfaceView init
	 */
	public GraffitiView(Context t, Bitmap bmp) {
		super(t);
		this.srcBitmap = bmp;

		mPath = new Path();
		commandManager = new CommandManager();
		bitmapCanvas = new Canvas();
		firstMas = true;

		// Init draw paint
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xFFFF0000);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(20);

		sfh = this.getHolder();
		sfh.addCallback(this);

		// this.setZOrderOnTop(true);
		// this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		// setFocusable(true);
	}

	public synchronized void undoLast() {
		commandManager.undoLast(bitmapCanvas);
	}

	public void changeColor(int color) {
		mPaint.setColor(color);
		mPaint.setXfermode(null);
		mPaint.setAlpha(0xFF);
		mPaint.setShader(null);

		this.isMask = false;
	}

	public void addMosaic(int level) {
		if (firstMas) {
			if (level < 5 || level > 30) {
				level = 30;
			}
			this.masBitmap = mosaicBitmapAverage(srcBitmap, level);
			srcBitmap.recycle();
			int ww = masBitmap.getWidth();
			int hh = masBitmap.getHeight();
			float scaleWidth = ((float) this.getWidth()) / ww;
			float scaleHeight = ((float) this.getHeight()) / hh;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);

			// resize bitmap
			masScaledbitmap = Bitmap.createBitmap(masBitmap, 0, 0, ww, hh,
					matrix, true);
			masBitmap.recycle();

			firstMas = false;
		}

		// mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		Bitmap checkerboard = masScaledbitmap;
		BitmapShader shader = new BitmapShader(checkerboard,
				Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		mPaint.setShader(shader);
		this.isMask = true;
	}

	public synchronized boolean saveDrawStream(FileOutputStream fOut) {
		try {
			Bitmap bmptmp = Bitmap.createScaledBitmap(drawingBitmap, 640, 640,
					true);
			bmptmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
			fOut.flush();
			bmptmp.recycle();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public synchronized boolean saveDrawPath(String path) {
		File f = new File(path);
		try {
			f.createNewFile();
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(f);
			Bitmap bmptmp = Bitmap.createScaledBitmap(drawingBitmap, 640, 640,
					true);
			bmptmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
			fOut.flush();
			fOut.close();
			bmptmp.recycle();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void setWidth(float width) {
		mPaint.setStrokeWidth(width);
	}

	public void turnOn(boolean turnFlag) {
		isDraw = turnFlag;
	}

	public int getPathNums() {
		return commandManager.getCommandIndex();
	}

	public void setPathNumbersListener(PathsNumbersListener listener) {
		mListener = listener;
	}

	public void resetGraffiti() {
		drawingBitmap = Bitmap.createBitmap(srcScaledbitmap).copy(
				Bitmap.Config.ARGB_8888, true);
		commandManager.reset(drawingBitmap);
		bitmapCanvas.setBitmap(drawingBitmap);
	}

	private void save(String path) {
		// drawCache is not useful for surfaceview
		// this.setDrawingCacheEnabled(true);
		// this.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
		// MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		// this.layout(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());
		// this.buildDrawingCache();
		// Log.d("save path", path);
		// Bitmap bmp = this.getDrawingCache();
		// Bitmap bitmap = Bitmap.createBitmap(bmp);
		// if (bitmap != null) {
		// Log.d("bitmap size", bmp.getWidth() + " " + bmp.getHeight());
		// }

		Bitmap bitmap = Bitmap.createBitmap(480, 600, Bitmap.Config.ARGB_8888);
		// Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		// this.draw(canvas);
		// Paint inPaint = new Paint();
		// inPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		// inPaint.setColor(Color.TRANSPARENT);
		// canvas.drawBitmap(masBitmap, 0, 0, null);
		// canvas.drawBitmap(drawingBitmap, 0, 0, null);
		// canvas.save(Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
		// canvas.restore();

		// bitmap = Bitmap.createBitmap(masBitmap);
		Canvas canvas = new Canvas(bitmap);

		// merge in bitmap
		canvas.drawBitmap(masBitmap, 0, 0, null);
		// Paint inPaint = new Paint();
		// inPaint = mPaint;
		// inPaint.setColor(Color.TRANSPARENT);
		// inPaint.setAlpha(255);
		// canvas.drawColor(Color.argb(0x11, 0xff, 0x00, 0x00));
		canvas.drawBitmap(drawingBitmap, 0, 0, null);

		int toumingColor = drawingBitmap.getPixel(100, 100);
		Log.i(TAG,
				"save------toumingColor = " + Integer.toHexString(toumingColor));

		// mPaint.setAlpha(255);
		// canvas.drawPath(mPath, mPaint);
		// canvas.save(Canvas.ALL_SAVE_FLAG);
		// // 存储新合成的图片
		// canvas.restore();

		// merge in drawable
		// BitmapDrawable[] array = new BitmapDrawable[2];
		// array[0] = new BitmapDrawable(masBitmap);
		// array[1] = new BitmapDrawable(drawingBitmap);
		// LayerDrawable la = new LayerDrawable(array);
		// la.setLayerInset(0, 1, 1, this.getWidth(), this.getHeight());
		// la.setLayerInset(1, 2, 2, 400, 600);
		// // la.draw(canvas);
		// this.setBackgroundDrawable(la);

		File f = new File(path);
		try {
			f.createNewFile();
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(f);
			// Bitmap bmptmp = Bitmap
			// .createScaledBitmap(mBitmap, 1600, 1200, true);

			long a, b;
			a = System.currentTimeMillis();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			b = System.currentTimeMillis();
			Log.d("compress time", "cost: " + (b - a) + " ms");
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("compress fail", "fail");
		}

	}

	private void saveWithPixles(String path) {
		int ww = drawingBitmap.getWidth();
		int hh = drawingBitmap.getHeight();
		float scaleWidth = ww / ((float) srcBitmap.getWidth());
		float scaleHeight = hh / ((float) srcBitmap.getHeight());
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		// resize bitmap
		Bitmap bitmap2 = Bitmap.createBitmap(srcBitmap, 0, 0, ww, hh, matrix,
				true);
		int[] srcPixels = new int[ww * hh];
		masBitmap.getPixels(srcPixels, 0, ww, 0, 0, ww, hh);
		bitmap2.recycle();

		int width = drawingBitmap.getWidth();
		int height = drawingBitmap.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);

		int pixMasColor = 0, pixDraColor = 0, pixSrcColor = 0;
		int i = 0, k = 0;
		int length = height - 1;
		int len = width - 1;
		int[] masPixels = new int[width * height];
		masBitmap.getPixels(masPixels, 0, width, 0, 0, width, height);
		int[] draPixels = new int[width * height];
		drawingBitmap.getPixels(draPixels, 0, width, 0, 0, width, height);
		int toumingColor = drawingBitmap.getPixel(100, 100);
		// Log.i(TAG, "toumingColor = " + Integer.toHexString(toumingColor));
		int[] pixels = new int[width * height];

		masBitmap.recycle();
		drawingBitmap.recycle();
		for (i = 0; i < length; i++) {
			for (k = 0; k < len; k++) {
				pixMasColor = masPixels[i * width + k];
				pixDraColor = draPixels[i * width + k];
				pixSrcColor = srcPixels[i * width + k];
				// Log.i(TAG,
				// "transparent color is: 0x"
				// + Integer.toHexString(pixDraColor));
				// if (pixDraColor != 0xff000000) {
				// pixels[i * width + k] = pixDraColor;
				//
				// } else if (pixSrcColor != 0xff000000) {
				// pixels[i * width + k] = pixMasColor;
				// } else if (pixMasColor == 0xff000000) {
				// pixels[i * width + k] = 0xff000000;
				// }else{
				// bitmap.
				// }

				// if (pixDraColor == Color.TRANSPARENT) {
				// Log.d("pixels", i + " " + k);
				// pixels[i * width + k] = pixMasColor;
				// } else {
				// pixels[i * width + k] = pixDraColor;
				// }
			}
		}
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

		File f = new File(path);
		try {
			f.createNewFile();
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(f);
			long a, b;
			a = System.currentTimeMillis();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			b = System.currentTimeMillis();
			Log.d("compress time2", "cost: " + (b - a) + " ms");
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("compress fail2", "fail");
		}
	}

	private void saveWithPath(String path) {
		Long a, b, c;
		a = System.currentTimeMillis();

		int ww = drawingBitmap.getWidth();
		int hh = drawingBitmap.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(ww, hh, Bitmap.Config.ARGB_8888);

		Bitmap bitmap2 = Bitmap.createBitmap(ww, hh, Bitmap.Config.ARGB_8888);
		Canvas can = new Canvas(bitmap2);
		can.drawColor(Color.TRANSPARENT);
		if (commandManager.getCommandIndex() > 0) {
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setDither(true);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStrokeWidth(20);
			for (int i = 0; i < commandManager.getCommandIndex(); i++) {
				Command command = commandManager.getCommandStack().get(i);
				// command.get
				if (command.isMask) {
					Log.d("commond", "run " + i);
					command.setCanvas(can);
					command.setCmdPaint(paint);
					command.run(); // do on ui thread, no queue
				}
			}
		}

		int[] newPixels = new int[ww * hh];
		bitmap2.getPixels(newPixels, 0, ww, 0, 0, ww, hh);
		bitmap2.recycle();

		int width = drawingBitmap.getWidth();
		int height = drawingBitmap.getHeight();
		int w = masBitmap.getWidth();
		int h = masBitmap.getHeight();
		float scaleWidth = ((float) width) / w;
		float scaleHeight = ((float) height) / h;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// resize bitmap
		Bitmap bitmap3 = Bitmap.createBitmap(masBitmap, 0, 0, w, h, matrix,
				false);

		int[] masPixels = new int[width * height];
		bitmap3.getPixels(masPixels, 0, width, 0, 0, width, height);
		int[] draPixels = new int[width * height];
		drawingBitmap.getPixels(draPixels, 0, width, 0, 0, width, height);
		int[] pixels = new int[width * height];

		bitmap3.recycle();
		drawingBitmap.recycle();

		int pixMasColor = 0, pixDraColor = 0, pixNewColor = 0;
		int i = 0, k = 0, sum = 0;
		int length = height - 1;
		int len = width - 1;
		int rm, rd, gm, gd, bm, bd, red = 0, green = 0, blue = 0;

		for (i = 0; i < length; i++) {
			for (k = 0; k < len; k++) {
				pixMasColor = masPixels[i * width + k];
				pixDraColor = draPixels[i * width + k];
				pixNewColor = newPixels[i * width + k];

				int r_i = Color.alpha(pixNewColor);
				float ratio = 1.0f - r_i / 255.0f;
				if (ratio < 1.0f) {
					// Log.d("ratio", ratio + "");
				}
				rm = Color.red(pixMasColor);
				gm = Color.green(pixMasColor);
				bm = Color.blue(pixMasColor);

				rd = Color.red(pixDraColor);
				gd = Color.green(pixDraColor);
				bd = Color.blue(pixDraColor);

				// red = (int) (rm * (1 - ratio) + rd * ratio);
				// green = (int) (gm * (1 - ratio) + gd * ratio);
				// blue = (int) (bm * (1 - ratio) + bd * ratio);
				// pixels[i * width + k] = Color.argb(0xff, red,
				// green, blue);

				// if (ratio > 0 && ratio < 0.95) {
				// pixels[i * width + k] = pixMasColor;
				// }

				sum = 0;
				sum = rd + gd + bd;
				red = (r_i == 0 && sum == 0) ? rm : rd;
				green = (r_i == 0 && sum == 0) ? gm : gd;
				blue = (r_i == 0 && sum == 0) ? bm : bd;

				sum = red + green + blue;
				if (sum == 0) {
					red = rm;
					green = gm;
					blue = bm;
				}

				// if (pixels[i * width + k] > 255) {
				// pixels[i * width + k] = 255;
				// }

				// if (pixNewColor != Color.TRANSPARENT
				// && pixNewColor != Color.BLACK) {
				// Log.i(TAG,
				// "pixNewColor = 0x"
				// + Integer.toHexString(pixNewColor));
				// }
				// if (pixNewColor != Color.TRANSPARENT) {
				// pixels[i * width + k] = pixMasColor;
				// } else {
				// pixels[i * width + k] = pixDraColor;
				// }
			}
		}
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

		b = System.currentTimeMillis();

		File f = new File(path);
		try {
			f.createNewFile();
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(f);
			long m, n;
			m = System.currentTimeMillis();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			n = System.currentTimeMillis();
			Log.d("compress time2", "cost: " + (m - n) + " ms");
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("compress fail2", "fail");
		}
		c = System.currentTimeMillis();

		Log.d(TAG, "create bitmap time: " + (b - a) + " ms \n"
				+ "create file time: " + (c - b) + " ms");
	}

	/**
	 * SurfaceView created
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		flag = true;
		th = new Thread(this);
		th.start();
	}

	private synchronized void myDraw() {
		try {
			canvas = sfh.lockCanvas();
			if (canvas != null) {
				canvas.drawBitmap(drawingBitmap, 0, 0, null);
				canvas.drawPath(mPath, mPaint);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {		
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touch_start(x, y);
			if (mListener != null) {
				mListener.onDown();
			}						
			break;
		case MotionEvent.ACTION_MOVE:
			if (!isDraw) {
				return true;
			}			
			touch_move(x, y);
			break;
		case MotionEvent.ACTION_UP:
			if (!isDraw) {
				return true;
			}			
			touch_up();
			if (mListener != null) {
				mListener.onUp();
			}
			break;
		}

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	private synchronized void touch_start(float x, float y) {
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private synchronized void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	private synchronized void touch_up() {
		Command command = new Command(mPaint, mPath);
		command.isMask = isMask;
		commandManager.commitCommand(command, bitmapCanvas);
		mPath.rewind();
	}

	@Override
	public void run() {
		while (flag) {
			long start = System.currentTimeMillis();
			myDraw();
			long end = System.currentTimeMillis();
			try {
				if (end - start < 40) {
					Thread.sleep(40 - (end - start));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (drawingBitmap == null) {
			int ww = srcBitmap.getWidth();
			int hh = srcBitmap.getHeight();
			float scaleWidth = ((float) width) / ww;
			float scaleHeight = ((float) height) / hh;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);

			// resize bitmap 横屏时会调用已经recycle的srcBitmap，会崩溃
			srcScaledbitmap = Bitmap.createBitmap(srcBitmap, 0, 0, ww, hh,
					matrix, true);
			drawingBitmap = Bitmap.createBitmap(srcScaledbitmap).copy(
					Bitmap.Config.ARGB_8888, true);
			commandManager.reset(drawingBitmap);
			bitmapCanvas.setBitmap(drawingBitmap);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		flag = false;
	}

	private Bitmap mosaicBitmap(Bitmap bmp, int level) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int lastR = 0;
		int lastG = 0;
		int lastB = 0;

		int pixColor = 0;
		int pixRowColor = 0;
		int rowR = 0;
		int rowG = 0;
		int rowB = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int val = 0;
		val = level;

		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 0, length = height - 1; i < length; i++) {
			for (int k = 0, len = width - 1; k < len; k++) {
				pixColor = pixels[i * width + k];
				pixR = Color.red(pixColor);
				pixG = Color.green(pixColor);
				pixB = Color.blue(pixColor);
				if (i % val == 0) {
					if (k % val == 0) {
						// remember new color
						lastR = pixR;
						lastG = pixG;
						lastB = pixB;

						newR = pixR;
						newG = pixG;
						newB = pixB;
					} else {
						// use last rememberd color
						newR = lastR;
						newG = lastG;
						newB = lastB;
					}
				} else {
					// copy color in the same position of last row
					pixRowColor = pixels[(i - 1) * width + k];
					rowR = Color.red(pixRowColor);
					rowG = Color.green(pixRowColor);
					rowB = Color.blue(pixRowColor);

					newR = rowR;
					newG = rowG;
					newB = rowB;
				}

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));
				pixels[i * width + k] = Color.argb(255, newR, newG, newB);

				newR = 0;
				newG = 0;
				newB = 0;
			}
		}
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		bmp.recycle();

		return bitmap;
	}

	private Bitmap mosaicBitmapAverage(Bitmap bmp, int level) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);

		int pixRowColor = 0, rowR = 0, rowG = 0, rowB = 0;
		int pixCurColor = 0, curR = 0, curG = 0, curB = 0;

		int newR = 0, newG = 0, newB = 0;
		int aveR = 0, aveG = 0, aveB = 0;
		int sumR = 0, sumG = 0, sumB = 0;

		int i = 0, k = 0, m = 0, n = 0;
		int length = height - 1;
		int len = width - 1;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);

		for (i = 0; i < length; i++) {
			for (k = 0; k < len; k++) {
				if (i % level == 0) {
					if (k % level == 0) {
						// caluate the avergae color
						for (m = 0; m < level && (i + m) < length; m++) {
							for (n = 0; n < level && (k + n) < len; n++) {
								pixCurColor = pixels[(i + m) * width + (k + n)];
								curR = Color.red(pixCurColor);
								curG = Color.green(pixCurColor);
								curB = Color.blue(pixCurColor);

								sumR += curR;
								sumG += curG;
								sumB += curB;
							}
						}

						// remember average color
						aveR = sumR / (level * level);
						aveG = sumG / (level * level);
						aveB = sumB / (level * level);

						// reset color
						sumR = 0;
						sumG = 0;
						sumB = 0;
					}

					// use average color
					newR = aveR;
					newG = aveG;
					newB = aveB;
				} else {
					// copy color in the same position of last row
					pixRowColor = pixels[(i - 1) * width + k];
					rowR = Color.red(pixRowColor);
					rowG = Color.green(pixRowColor);
					rowB = Color.blue(pixRowColor);

					newR = rowR;
					newG = rowG;
					newB = rowB;
				}

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));
				pixels[i * width + k] = Color.argb(255, newR, newG, newB);

				newR = 0;
				newG = 0;
				newB = 0;
			}
		}
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		bmp.recycle();

		return bitmap;
	}

	public static interface PathsNumbersListener {
		public void onDown();
		public void onUp();
	}
}