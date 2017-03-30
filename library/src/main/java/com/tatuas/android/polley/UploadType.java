package com.tatuas.android.polley;

import java.io.File;

public class UploadType {

    public enum MimeType {

        IMAGE_JPEG("image/jpeg"),
        IMAGE_PNG("image/png"),
        IMAGE_GIF("image/gif"),
        IMAGE_ALL("image/*");

        private String mValue;

        MimeType(String value) {
            mValue = value;
        }

        @Override
        public String toString() {
            return mValue;
        }
    }

    private static boolean checkFileExt(File file, String ext) {
        return file.getName().toLowerCase().endsWith(ext);
    }

    public static boolean checkFileType(MimeType type, File file) {
        switch (type) {
            case IMAGE_JPEG:
                return checkFileExt(file, ".jpg") || checkFileExt(file, ".jpeg");
            case IMAGE_PNG:
                return checkFileExt(file, ".png");
            case IMAGE_GIF:
                return checkFileExt(file, ".gif");
            case IMAGE_ALL:
                return checkFileExt(file, ".jpg") || checkFileExt(file, ".jpg")
                        || checkFileExt(file, ".jpeg") || checkFileExt(file, ".png")
                        || checkFileExt(file, ".gif");
            default:
                break;
        }

        return false;
    }
}
