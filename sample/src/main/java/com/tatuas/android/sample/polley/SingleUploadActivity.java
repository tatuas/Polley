package com.tatuas.android.sample.polley;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tatuas.android.polley.PostRequest;
import com.tatuas.android.polley.SingleUploadHelper;
import com.tatuas.android.polley.UploadTask;
import com.tatuas.android.polley.UploadType;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class SingleUploadActivity extends AppCompatActivity {

    private MyUploadHelper helper;

    private static final int REQUEST_CODE = 11234;

    // also use: http://requestb.in/
    private static final String UPLOAD_URL = "http://httpbin.org/post";
    private static final int CONNECTION_TIMEOUT_MILLS = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_upload);
        ButterKnife.bind(this);
        helper = new MyUploadHelper(this, UploadType.MimeType.IMAGE_ALL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        helper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SingleUploadActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void startUpload() {
        helper.startUpload("Select 1 photo", REQUEST_CODE, new SingleUploadHelper.Callback() {
            @Override
            public void onStart() {
                Toast.makeText(SingleUploadActivity.this, "start", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSucceed(SingleUploadHelper.State state, String successMessage) {
                Toast.makeText(SingleUploadActivity.this, "succeed\n\n" + successMessage, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailed(SingleUploadHelper.State state, String errorMessage) {
                Toast.makeText(SingleUploadActivity.this, "failed\n\n" + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @OnClick(R.id.start_button)
    void onStartButtonClicked() {
        SingleUploadActivityPermissionsDispatcher.startUploadWithCheck(this);
    }

    private static final class MyUploadHelper extends SingleUploadHelper {

        MyUploadHelper(Context context, UploadType.MimeType mimeType) {
            super(context, mimeType);
        }

        @Override
        protected UploadTask newUploadTask(Uri uri) {
            return new MyUploadTask(getContext().getApplicationContext(), uri, getMimeType());
        }

        private static final class MyUploadTask extends UploadTask {

            MyUploadTask(Context context, Uri uri, UploadType.MimeType mimeType) {
                super(context, uri, mimeType);
            }

            @Override
            protected PostRequest newPostRequest() {
                final PostRequest postRequest = new PostRequest(UPLOAD_URL);
                postRequest.setCustomReadTimeOut(CONNECTION_TIMEOUT_MILLS);
                postRequest.setCustomConnectionTimeOut(CONNECTION_TIMEOUT_MILLS);

                postRequest.addFileParam("name", getUploadMimeType().toString(), getUploadFile());
                postRequest.addPostParam("post_data_1", "post_data_1_value");
                postRequest.addGetParam("get_data_1", "get_data_1_value");

                final HashMap<String, String> headers = new HashMap<>();
                headers.put("Header1", "header1value");
                headers.put("Header2", "header2value");
                headers.put("Header3", "header3value");
                headers.put("User-Agent", "unknown");

                postRequest.addRequestHeader(headers);

                return postRequest;
            }
        }
    }
}
