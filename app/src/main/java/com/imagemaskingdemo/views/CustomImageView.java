package com.imagemaskingdemo.views;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import com.imagemaskingdemo.R;


public abstract class CustomImageView extends android.support.v7.widget.AppCompatImageView
{
    private static final PorterDuffXfermode PORTER_DUFF_XFERMODE;
    private static final String TAG;
    private Bitmap drawableBitmap;
    private Canvas drawableCanvas;
    private Paint drawablePaint;
    private boolean invalidated;
    private Bitmap maskBitmap;
    private Canvas maskCanvas;
    private Paint maskPaint;
    private boolean square;
    protected abstract void paintMaskCanvas(Canvas canvas, Paint paint, int i, int i2);
    private boolean enableMasking = true;
    static
    {
        TAG = CustomImageView.class.getSimpleName();
        PORTER_DUFF_XFERMODE = new PorterDuffXfermode(Mode.DST_IN);
    }

    public CustomImageView(Context context)
    {
        super(context);
        this.invalidated = true;
        this.square = false;
        setup(context, null, 0);
    }

    public  void setMaskingEnable(boolean enableMasking)
    {
        this.enableMasking = enableMasking;
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.invalidated = true;
        this.square = false;
        setup(context, attrs, 0);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.invalidated = true;
        this.square = false;
        setup(context, attrs, defStyle);
    }

    public void setSquare(boolean square)
    {
        this.square = square;
    }

    private void setup(Context context, AttributeSet attrs, int defStyle) {

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShaderImageView, defStyle, 0);
            this.square = typedArray.getBoolean(4, false);
            typedArray.recycle();
        }
        if (getScaleType() == ScaleType.FIT_CENTER) {
            setScaleType(ScaleType.CENTER_CROP);
        }

        this.maskPaint = new Paint(1);
        int a = 16777216;
        this.maskPaint.setColor(a);
    }
    public void invalidate()
    {
        this.invalidated = true;
        super.invalidate();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createMaskCanvas(w, h, oldw, oldh);
    }

    private void createMaskCanvas(int width, int height, int oldw, int oldh) {
        boolean isValid = false;
        boolean sizeChanged;
        if (width == oldw && height == oldh) {
            sizeChanged = false;
        } else {
            sizeChanged = true;
        }
        if (width > 0 && height > 0) {
            isValid = true;
        }
        if (!isValid) {
            return;
        }
        if (this.maskCanvas == null || sizeChanged) {
            this.maskCanvas = new Canvas();
            if (this.maskBitmap != null)
                this.maskBitmap.recycle();
            this.maskBitmap = null;
            this.maskBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            this.maskCanvas.setBitmap(this.maskBitmap);
            this.maskPaint.reset();
            paintMaskCanvas(this.maskCanvas, this.maskPaint, width, height);
            this.drawableCanvas = new Canvas();
            if (this.drawableBitmap != null)
                this.drawableBitmap.recycle();
            this.drawableBitmap = null;
            this.drawableBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            this.drawableCanvas.setBitmap(this.drawableBitmap);
            this.drawablePaint = new Paint(1);
            this.invalidated = true;

        }
    }
    @Override
    protected void onDraw(Canvas canvas) {

        if (!isInEditMode()) {
            int saveCount = canvas.saveLayer(0.0f, 0.0f, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
            try {
                if (invalidated) {
                    clear();
                    Drawable drawable = getDrawable();
                    if (drawable != null) {
                        invalidated = false;
                        Matrix imageMatrix = getImageMatrix();
                        if (imageMatrix == null)
                        {
                            drawable.draw(drawableCanvas);
                        } else {
                            int drawableSaveCount = drawableCanvas.getSaveCount();
                            drawableCanvas.save();
                            drawableCanvas.concat(imageMatrix);
                            drawable.draw(drawableCanvas);
                            drawableCanvas.restoreToCount(drawableSaveCount);
                        }


                        if (enableMasking) {
                            drawablePaint.reset();
                            drawablePaint.setFilterBitmap(false);
                            drawablePaint.setXfermode(PORTER_DUFF_XFERMODE);
                            drawableCanvas.drawBitmap(maskBitmap, 0.0f, 0.0f, drawablePaint);
                        }
                    }
                }

                if (!invalidated) {
                    drawablePaint.setXfermode(null);
                    canvas.drawBitmap(drawableBitmap, 0.0f, 0.0f, drawablePaint);
                }
            } catch (Exception e) {
                String log = "Exception occured while drawing " + getId();
                Log.e(TAG, log, e);
            } finally {
                canvas.restoreToCount(saveCount);
            }
        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (square)
        {
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            int dimen = Math.min(width, height);
            setMeasuredDimension(dimen, dimen);
        }
    }

    public void clear()
    {
        Xfermode x = drawablePaint.getXfermode();
        drawablePaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        drawableCanvas.drawPaint(drawablePaint);
        drawablePaint.setXfermode(x);
        invalidate();
    }
}
