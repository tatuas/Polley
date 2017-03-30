package com.tatuas.android.polley;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.io.File;

public abstract class UploadTask {
    /*
     * This system determines the type of the file using the file extension.
     */
    private UploadTaskFailureListener mFailureListener;
    private UploadTaskSuccessListener mSuccessListener;

    private Context mContext;
    private File mUploadFile;
    private UploadType.MimeType mUploadMimeType;
    private Uri mUploadFileUri;

    private Handler mHandler = new Handler();

    public UploadTask(Context context, String fileAbsolutePath, UploadType.MimeType type) {
        mContext = checkContextType(context);
        mUploadMimeType = type;

        try {
            mUploadFile = new File(fileAbsolutePath);
        } catch (Exception e) {
            mUploadFile = null;
        }
    }

    public UploadTask(Context context, Uri uri, UploadType.MimeType type) {
        mContext = checkContextType(context);
        mUploadMimeType = type;
        mUploadFileUri = uri;
        mUploadFile = getUploadFileFromUri();
    }

    protected Context getContext() {
        return mContext;
    }

    protected File getUploadFile() {
        return mUploadFile;
    }

    protected Uri getUploadFileUri() {
        return mUploadFileUri;
    }

    protected UploadType.MimeType getUploadMimeType() {
        return mUploadMimeType;
    }

    /*
     * Set if you use event listener system.
     */
    public void setUploadEventListener(UploadTaskSuccessListener sListener, UploadTaskFailureListener fListener) {
        mSuccessListener = sListener;
        mFailureListener = fListener;
    }

    protected abstract PostRequest newPostRequest();

    public Thread newUploadThread() {
        return new Thread() {
            @Override
            public void run() {
                PostResponse response = newPostRequest().execute();

                final String responseString = response.getResponseString();
                final PostResponse.ResponseStatus status = response.getResponseStatus();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        switch (status) {
                            case RESPONSE_COMPLETE:
                                if (mSuccessListener != null) {
                                    mSuccessListener.onSuccess(responseString);
                                }
                                break;
                            default:
                                if (mSuccessListener != null) {
                                    mFailureListener.onFailure(responseString);
                                }
                                break;
                        }
                    }
                });
            }
        };
    }

    private File getUploadFileFromUri() {
        File file;
        try {
            file = new File(getAbsolutePathFromUri(mUploadFileUri));
            if (file.exists() && file.canRead()
                    && UploadType.checkFileType(mUploadMimeType, file)) {
                return file;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Context checkContextType(Context context) {
        if (context instanceof Activity) {
            throw new ClassCastException("UploadTask can use only application context.");
        } else {
            return context;
        }
    }

    @NonNull
    private String getAbsolutePathFromUri(Uri contentUri) {
        String path = "";
        Cursor cursor = null;

        try {
            ContentResolver contentResolver = mContext.getContentResolver();

            if (contentUri.toString().startsWith("file://")) {
                path = contentUri.getPath();
            } else {
                cursor = contentResolver.query(
                        contentUri,
                        new String[]{MediaStore.MediaColumns.DATA},
                        null,
                        null,
                        null);

                if (cursor == null) {
                    return path;
                }

                int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                if (cursor.moveToFirst()) {
                    path = cursor.getString(index);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return path;
    }

    interface UploadTaskSuccessListener {
        void onSuccess(String successMessage);
    }

    interface UploadTaskFailureListener {
        void onFailure(String errorMessage);
    }
}

