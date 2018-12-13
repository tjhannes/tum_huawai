package com.example.johannes.huawei;

public interface ModelManagerListener {

    void onStartDone(int taskId);

    void onRunDone(int taskId, float[] output);

    void onStopDone(int taskId);

    void onTimeout(int taskId);

    void onError(int taskId, int errCode);

    void onServiceDied();
}
