package com.imagemaskingdemo;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.imagemaskingdemo.views.TouchImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Bitmap maskbitmap;
    private FrameLayout mFrameProductContainerCollageFrame;
    private Button mZoomIn, mZoomOut, mCenter, mSaveBitmap, mRotate;
    private TouchImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFrameProductContainerCollageFrame = (FrameLayout) findViewById(R.id.frame_product_container_collage_frame);
        maskbitmap = getBitmapFromAsset(this, "file:///android_asset/circle.png");
        maskbitmap = Bitmap.createScaledBitmap(maskbitmap, 780, 550, true);
        imageView = new TouchImageView(this, new BitmapDrawable(getResources(), maskbitmap));
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.android);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(700, 700));
        imageView.setImageBitmap(bitmap, true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mFrameProductContainerCollageFrame.addView(imageView);

        mZoomIn = (Button) findViewById(R.id.image_zoomIn);
        mZoomOut = (Button) findViewById(R.id.image_zoomOut);
        mCenter = (Button) findViewById(R.id.image_center);
        mRotate = (Button) findViewById(R.id.image_rotate);
        mSaveBitmap = (Button) findViewById(R.id.image_save);

        mZoomIn.setOnClickListener(this);
        mZoomOut.setOnClickListener(this);
        mCenter.setOnClickListener(this);
        mRotate.setOnClickListener(this);
        mSaveBitmap.setOnClickListener(this);

    }

    public Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();
        InputStream istr;
        Bitmap bitmap = null;
        try {
            String[] separated = filePath.split("android_asset/");
            istr = assetManager.open(separated[1]);
            Log.e("FILE PATH:-", filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void rotateImage() {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap srcBitmap;
        Drawable drawable = ((ImageView) imageView).getDrawable();
        srcBitmap = ((BitmapDrawable) drawable.getCurrent()).getBitmap();
        Bitmap bitmap = Bitmap.createBitmap(
                srcBitmap, 0, 0,
                srcBitmap.getWidth(),
                srcBitmap.getHeight(), matrix, false);
        imageView.setImageBitmap(bitmap, false);
    }

    private Bitmap getBitmap() {
        Bitmap mBitmap = null;
        try {
            if (mBitmap != null) {
                mBitmap.recycle();
            }
            mBitmap = Bitmap.createBitmap(mFrameProductContainerCollageFrame.getWidth(), mFrameProductContainerCollageFrame.getHeight(), Bitmap.Config.ARGB_8888);
            mBitmap.setHasAlpha(true);
            Canvas canvas = new Canvas(mBitmap);
            mFrameProductContainerCollageFrame.draw(canvas);
            return mBitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();
        }
        return mBitmap;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.image_zoomIn:
                imageView.scaleImageManually(imageView.getCurrentZoom() + 0.2f);
                break;
            case R.id.image_zoomOut:
                imageView.scaleImageManually(imageView.getCurrentZoom() - 0.2f);
                break;
            case R.id.image_center:
                imageView.centerImage();
                break;
            case R.id.image_rotate:
                rotateImage();
                break;
            case R.id.image_save:
                getBitmap();
                Intent mIntent = new Intent(MainActivity.this, PreviewActivity.class);
                mIntent.putExtra("img", getImageUri(MainActivity.this, getBitmap()).toString());
                startActivity(mIntent);
                break;
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "MaskingDemo", null);
        return Uri.parse(path);
    }
}
