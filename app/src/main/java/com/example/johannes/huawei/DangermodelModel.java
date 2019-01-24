package com.example.johannes.huawei;

import android.content.res.AssetManager;
import android.util.Log;


public class DangermodelModel {


    /**** user load model manager sync interfaces ****/
    // TODO change the name of the model you want to use (4 times)
    public static int load(AssetManager mgr){
            return ModelManager.loadModelSync("DangerDemo", mgr);
    }

    public static float[] predict(float[] buf){
        return ModelManager.runModelSync("DangerDemo",buf);
    }

    public static int unload(){
        return ModelManager.unloadModelSync();
    }


    /**** load user model async interfaces ****/
    public static int registerListenerJNI(ModelManagerListener listener){
        return ModelManager.registerListenerJNI(listener);
    }

    public static void loadAsync(AssetManager mgr){
        ModelManager.loadModelAsync("DangerDemo", mgr);
    }

    public static void predictAsync(float[] buf) {
        ModelManager.runModelAsync("DangerDemo",buf);
    }

    public static void unloadAsync(){
        ModelManager.unloadModelAsync();
    }
}
