package com.tatuas.android.polley;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

public abstract class SingleUploadHelper {

    private Context mContext;
    private UploadType.MimeType mMimeType;

    private Callback mCallback;

    private int mRequestCode = 0;

    public enum State {
        SUCCESS, ERR_OTHER, ERR_FILE, ERR_NETWORK
    }

    /*
     * Must insert Fragment Activity context.
     * This idea was inspired by Facebook Android SDK.
     */
    public SingleUploadHelper(Context context, UploadType.MimeType mimeType) {
        if (context instanceof FragmentActivity) {
            mContext = context;
            mMimeType = mimeType;
        } else {
            throw new ClassCastException("Insert fragment_activity context.");
        }
    }

    /*
     * Actually execute timing is on onActivityResult cycle.
     */
    public void startUpload(String dialogTitle, int requestCode, @NonNull Callback callback) {
        mRequestCode = requestCode;
        mCallback = callback;

        Intent chooser = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        chooser.setType(mMimeType.toString());
        Intent intent = Intent.createChooser(chooser, dialogTitle);
        ((FragmentActivity) mContext).startActivityForResult(intent, mRequestCode);
    }

    public void startUploadWithoutChooser(Uri localImageUri, @NonNull Callback callback) {
        mCallback = callback;

        UploadTask task = newUploadTask(localImageUri);
        task.setUploadEventListener(newSuccessListener(), newFailureListener());

        // Progress
        mCallback.onStart();

        UploadManager.getInstance().enQueue(task);
    }

    /*
     * Must set on executing method call activity.
     * "requestCode" hack is for FragmentActivity.
     * Show "http://y-anz-m.blogspot.jp/2012/05/support-package-fragment.html".
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if ((requestCode & 0xffff) == mRequestCode) {
                // Get uri
                Uri uri = data.getData();

                // Create task
                UploadTask task = newUploadTask(uri);

                if (task == null) {
                    mCallback.onFailed(State.ERR_OTHER, "Upload task is not defined.");
                    return;
                }

                task.setUploadEventListener(newSuccessListener(), newFailureListener());

                if (task.getUploadFileUri() != null) {
                    if (task.getUploadFileUri().toString()
                            .startsWith("content://com.google.android.apps.photos.content/")) {
                        mCallback.onFailed(State.ERR_FILE, "Google plus online photo.");
                        return;
                    }
                }

                if (task.getUploadFile() == null) {
                    mCallback.onFailed(State.ERR_FILE, "File not exists");
                    return;
                }

                // Progress
                mCallback.onStart();

                // Start to upload
                UploadManager.getInstance().enQueue(task);
            }
        }
    }

    protected Context getContext() {
        return mContext;
    }

    protected UploadType.MimeType getMimeType() {
        return mMimeType;
    }

    /*
     * Must override.
     */
    protected abstract UploadTask newUploadTask(Uri uri);

    /*
     * Override if you want to custom listener.
     */
    protected UploadTask.UploadTaskSuccessListener newSuccessListener() {
        return new UploadTask.UploadTaskSuccessListener() {
            @Override
            public void onSuccess(String successMessage) {
                mCallback.onSucceed(State.SUCCESS, successMessage);
            }
        };
    }

    /*
     * Override if you want to custom listener.
     */
    protected UploadTask.UploadTaskFailureListener newFailureListener() {
        return new UploadTask.UploadTaskFailureListener() {
            @Override
            public void onFailure(String errorMessage) {
                mCallback.onFailed(State.ERR_NETWORK, errorMessage);
            }
        };
    }

    public interface Callback {

        void onStart();

        void onSucceed(State state, String successMessage);

        void onFailed(State state, String errorMessage);
    }
}
