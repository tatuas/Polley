package com.tatuas.android.polley;

import android.support.annotation.NonNull;
import android.util.Log;

class PolleyLog {

    private static final String TAG = "Polley";

    static void message(@NonNull Object base, @NonNull String message) {
        if (Constant.ENABLE_LOG) {
            Log.v(TAG, base.getClass().getName() + ": " + message);
        }
    }
}
