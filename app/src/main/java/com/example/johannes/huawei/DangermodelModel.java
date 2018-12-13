package com.example.johannes.huawei;

import android.content.res.AssetManager;
import android.util.Log;


public class DangermodelModel {


    /**** user load model manager sync interfaces ****/
    public static int load(AssetManager mgr){
            return ModelManager.loadModelSync("Dangermodel", mgr);
    }

    public static float[] predict(float[] buf){
        return ModelManager.runModelSync("Dangermodel",buf);
    }

    public static int unload(){
        return ModelManager.unloadModelSync();
    }


    /**** load user model async interfaces ****/
    public static int registerListenerJNI(ModelManagerListener listener){
        return ModelManager.registerListenerJNI(listener);
    }

    public static void loadAsync(AssetManager mgr){
        ModelManager.loadModelAsync("Dangermodel", mgr);
    }

    public static void predictAsync(float[] buf) {
        ModelManager.runModelAsync("Dangermodel",buf);
    }

    public static void unloadAsync(){
        ModelManager.unloadModelAsync();
    }
}
