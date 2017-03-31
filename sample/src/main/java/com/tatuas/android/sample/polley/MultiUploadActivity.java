package com.tatuas.android.sample.polley;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.OnClick;

public class MultiUploadActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstannceState) {
        super.onCreate(savedInstannceState);
        setContentView(R.layout.activity_multi_upload);
    }

    @OnClick(R.id.pick_images_button)
    void onPickedImagesClicked() {
        // wip
    }
}
