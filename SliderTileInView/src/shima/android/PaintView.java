package shima.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class PaintView extends View {
	private static final String TAG = PaintView.class.getSimpleName();
	private Bitmap offScreenBitmap;
	private Canvas offScreenCanvas;
	private ImageView backgroundView;
	private Paint paint;
	private Path path;
	public enum PenType { PEN, ERASER } PenType penType = PenType.PEN;
	private PorterDuffXfermode eraserMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); //ok
	private PorterDuffXfermode dstOver = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER); //ok
	private PorterDuffXfermode srcOver = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
	private PorterDuffXfermode dstATop = new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP);
	private PorterDuffXfermode srcIn = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN); //ok
	
	private Point cp = new Point();
	private Point dp = new Point();
	private Point limiter = new Point();;
	private Point vec;
	private Rect invalidated;
	private List<Tile> movables = new ArrayList<Tile>();
	private Board board;
	
	public PaintView(Context context) { this(context, null); }
	public PaintView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundColor(Color.alpha(0));
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(4);
		paint.setColor(Color.RED);
		path = new Path();
	}
	@Override protected void onSizeChanged(int w, int h, int pw, int ph) {
		super.onSizeChanged(w, h, pw, ph);
		offScreenBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		offScreenCanvas = new Canvas(offScreenBitmap);
		
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sky);
		board = new Board(bitmap, 4, 3);
		board.initializeTiles(w, h);
		board.shuffle();
		
	}
	@Override public boolean onTouchEvent(MotionEvent e) {
		cp.x = (int)e.getX(); cp.y = (int)e.getY();
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			dp.x = cp.x; dp.y = cp.y;
			invalidated = board.getMovables(dp, movables, limiter);
			break;
		case MotionEvent.ACTION_MOVE:
			if (invalidated == null) break;;
			vec = Utils.getAdjustedVector(dp, cp, limiter);
			if (vec == null) break;
			invalidate(invalidated);
			break;
		case MotionEvent.ACTION_UP:
			if (vec == null) break;
			if (Utils.isSlided(vec, limiter)) {
				for (Tile t : movables) board.slide(t);
			}
			invalidate(invalidated);
			invalidated = null;
			vec = null;
			break;
		}
		return true;
	}
	@Override protected void onDraw(Canvas canvas) {
		if (invalidated == null) {	// ゲームボード上の全タイルを描画する
			board.draw(canvas);
		} else {					// 移動したタイルだけを描画する
			for (Tile t : movables) board.drawTile(canvas, t, vec);
		}
	}
	boolean setPenType(PenType type) {
		if (type == penType) return false;
		switch (penType = type) {
		case PEN:
//			paint.setXfermode(null);
			paint.setXfermode(null);
			paint.setAlpha(255);
			break;
		case ERASER:
			paint.setXfermode(eraserMode);
			paint.setAlpha(0);
			
			paint.setXfermode(dstOver);
			paint.setXfermode(srcIn);
			paint.setAlpha(255);
			paint.setColor(Color.RED);
			break;
		}
		return true;
	}
	boolean setPenColor(int color) {
		color = Color.GREEN;
		if (penType == PenType.PEN) {
			paint.setColor(color);
			return true;
		}
		return false;
	}
	void setBgColor(int color) {
		color = Color.RED;
		if (backgroundView != null) {
			backgroundView.setImageDrawable(null);
			backgroundView.setBackgroundColor(color);
		}
	}
	void setBgImage(Drawable drawable) {
		if (backgroundView != null) {
			backgroundView.setImageDrawable(drawable);
		}
	}
}