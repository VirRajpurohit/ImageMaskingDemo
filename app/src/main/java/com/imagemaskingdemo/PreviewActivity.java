package com.imagemaskingdemo;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class PreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ((ImageView)findViewById(R.id.mImageView)).setImageURI(Uri.parse(getIntent().getStringExtra("img")));
    }
}
