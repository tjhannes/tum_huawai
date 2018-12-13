package com.example.johannes.huawei;

import android.content.res.AssetManager;
import android.util.Log;


public class ModelManager {

    private static final String TAG = ModelManager.class.getSimpleName();

    private ModelManager() {
    }

    static {
        System.loadLibrary("hiai");
    }

    public static boolean init() {
        try {
            System.loadLibrary("hiai");
            return true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "failed to load native library: " + e.getMessage());
            return false;
        }
    }

    /* DDK model manager sync interfaces */
    public static native int loadModelSync(String modelName, AssetManager mgr);

    public static native float[] runModelSync(String modelName, float[] buf);

    public static native int unloadModelSync();



    /* DDK model manager async interfaces */
    public static native int registerListenerJNI(ModelManagerListener listener);

    public static native void loadModelAsync(String modelName, AssetManager mgr);

    public static native void runModelAsync(String modelName, float[] buf);

    public static native void unloadModelAsync();
}
