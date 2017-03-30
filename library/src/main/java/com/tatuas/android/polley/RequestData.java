package com.tatuas.android.polley;

import java.io.File;

public class RequestData {

    public static class PostFile {
        private File mFile;
        private String mTagName;
        private String mMimeType;

        public PostFile(String tagName, String mimeType, File file) {
            this.mTagName = tagName;
            this.mMimeType = mimeType;
            this.mFile = file;
        }

        public File getFile() {
            return mFile;
        }

        public String getTagName() {
            return mTagName;
        }

        public String getMimeType() {
            return mMimeType;
        }

        public boolean fileStatus() {
            return mFile != null
                    && mFile.exists()
                    && mFile.canRead();
        }
    }

    public static class PostString {

        private String tagName;
        private String postValue;

        public PostString(String tagName, String postValue) {
            this.tagName = tagName;
            this.postValue = postValue;
        }

        public String getName() {
            return tagName;
        }

        public String getValue() {
            return postValue;
        }
    }

    public static class GetString {
        private String mTagName;
        private String mGetValue;

        public GetString(String tagName, String getValue) {
            mTagName = tagName;
            mGetValue = getValue;
        }

        public String getKey() {
            return mTagName;
        }

        public String getValue() {
            return mGetValue;
        }
    }
}
