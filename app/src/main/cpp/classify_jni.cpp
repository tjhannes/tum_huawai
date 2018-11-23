#include <jni.h>
#include <string>

#include <memory.h>
#include "include/HIAIModelManager.h"
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <cmath>
#include <sstream>
#include "common.h"

#define LOG_TAG "SYNC_DDK_MSG"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

using namespace std;

static HIAI_ModelManager *modelManager = NULL;

static HIAI_TensorBuffer *inputtensor = NULL;

static HIAI_TensorBuffer *outputtensor = NULL;

static HIAI_ModelBuffer *modelBuffer = NULL;

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_johannes_huawei_ModelManager_loadModelSync(JNIEnv *env, jobject instance,
                                                    jstring jmodelName, jobject assetManager) {
    const char *modelName = env->GetStringUTFChars(jmodelName, 0);

    char modelname[128] = {0};

    strcat(modelname, modelName);
    strcat(modelname, ".cambricon");

    modelManager = HIAI_ModelManager_create(NULL);

    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    LOGI("Attempting to load model...\n");

    LOGE("model name is %s", modelname);

    AAsset *asset = AAssetManager_open(mgr, modelname, AASSET_MODE_BUFFER);

    if (nullptr == asset) {
        LOGE("AAsset is null...\n");
    }

    const void *data = AAsset_getBuffer(asset);

    if (nullptr == data) {
        LOGE("model buffer is null...\n");
    }

    off_t len = AAsset_getLength(asset);

    if (0 == len) {
        LOGE("model buffer length is 0...\n");
    }

    HIAI_ModelBuffer *modelBuffer = HIAI_ModelBuffer_create_from_buffer(modelName,
                                                                        (void *) data, len,
                                                                        HIAI_DevPerf::HIAI_DEVPREF_HIGH);
    HIAI_ModelBuffer *modelBufferArray[] = {modelBuffer};

    int ret = HIAI_ModelManager_loadFromModelBuffers(modelManager, modelBufferArray, 1);

    LOGI("load model from assets ret = %d", ret);

    env->ReleaseStringUTFChars(jmodelName, modelName);

    AAsset_close(asset);

    return ret;

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_johannes_huawei_ModelManager_unloadModelSync(JNIEnv *env, jobject instance) {
    if (NULL == modelManager) {
        LOGE("please load model first.");
        return -1;
    } else {
        if (modelBuffer != NULL) {
            HIAI_ModelBuffer_destroy(modelBuffer);
            modelBuffer = NULL;
        }

        int ret = HIAI_ModelManager_unloadModel(modelManager);

        LOGE("JNI unload model ret:%d", ret);

        HIAI_ModelManager_destroy(modelManager);
        modelManager = NULL;

        return ret;
    }
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_example_johannes_huawei_ModelManager_runModelSync(JNIEnv *env, jclass type, jstring jmodelName,
                                                   jfloatArray jbuf) {

    const char *modelName = env->GetStringUTFChars(jmodelName, 0);

    if (NULL == modelManager) {
        LOGE("please load model first");

        return NULL;
    }

    float *dataBuff = NULL;

    if (NULL != jbuf) {
        dataBuff = env->GetFloatArrayElements(jbuf, NULL);
    }

    inputtensor = HIAI_TensorBuffer_create(1, 3, 227, 227);

    HIAI_TensorBuffer *inputtensorbuffer[] = {inputtensor};

    outputtensor = HIAI_TensorBuffer_create(1, 1000, 1, 1);

    HIAI_TensorBuffer *outputtensorbuffer[] = {outputtensor};


    float *inputbuffer = (float *) HIAI_TensorBuffer_getRawBuffer(inputtensor);

    int length = HIAI_TensorBuffer_getBufferSize(inputtensor);

    LOGE("SYNC JNI runModel modelname:%s", modelName);
    memcpy(inputbuffer, dataBuff, length);

    float time_use;
    struct timeval tpstart, tpend;
    gettimeofday(&tpstart, NULL);

    int ret = HIAI_ModelManager_runModel(
            modelManager,
            inputtensorbuffer,
            1,
            outputtensorbuffer,
            1,
            1000,
            modelName);


    LOGE("run model ret: %d", ret);

    gettimeofday(&tpend, NULL);
    time_use = 1000000 * (tpend.tv_sec - tpstart.tv_sec) + tpend.tv_usec - tpstart.tv_usec;

    LOGI("infrence time %f ms.", time_use / 1000);

    int outputSize = 1000;

    float *outputBuffer = (float *) HIAI_TensorBuffer_getRawBuffer(outputtensor);

    float *scale_data = (float *) malloc(outputSize * sizeof(float));

    double max = outputBuffer[0];
    double sum = 0;
    for (int i = 0; i < 1000; i++) {
        if (outputBuffer[i] > max)
            max = outputBuffer[i];
    }

    for (int i = 0; i < 1000; i++) {
        scale_data[i] = exp(outputBuffer[i] - max);
        sum += scale_data[i];
    }

    int max_index[3] = {0};
    double max_num[3] = {0};

    for (int i = 0; i < 1000; i++) {
        outputBuffer[i] = scale_data[i] / sum;
        double tmp = outputBuffer[i];
        int tmp_index = i;
        for (int j = 0; j < 3; j++) {
            if (tmp > max_num[j]) {
                tmp_index += max_index[j];
                max_index[j] = tmp_index - max_index[j];
                tmp_index -= max_index[j];
                tmp += max_num[j];
                max_num[j] = tmp - max_num[j];
                tmp -= max_num[j];
            }
        }
    }

    /* 是否有更简单的方法？ */
    jobjectArray result;

    ostringstream stringStream;

    stringStream << clabels[max_index[0]].c_str() << " - " << max_num[0] * 100 << "%\n";

    stringStream.flush();
    const char *top1Result = strdup(stringStream.str().c_str());

    stringStream.str("");
    stringStream.clear();


    stringStream << clabels[max_index[1]].c_str() << " - " << max_num[1] * 100
                 << "%\n" << clabels[max_index[2]].c_str() << " - " << max_num[2] * 100
                 << "%\n";

    stringStream.flush();
    const char *otherResults = strdup(stringStream.str().c_str());

    stringStream.str("");
    stringStream.clear();

    stringStream << "inference time:" << time_use / 1000 << "ms\n";

    stringStream.flush();
    const char *inferenceTime = strdup(stringStream.str().c_str());

    stringStream.str("");
    stringStream.clear();

    result = (jobjectArray) env->NewObjectArray(3, env->FindClass("java/lang/String"),
                                                env->NewStringUTF(""));

    env->SetObjectArrayElement(result, 0, env->NewStringUTF(top1Result));
    env->SetObjectArrayElement(result, 1, env->NewStringUTF(otherResults));
    env->SetObjectArrayElement(result, 2, env->NewStringUTF(inferenceTime));

    env->ReleaseStringUTFChars(jmodelName, modelName);
    env->ReleaseFloatArrayElements(jbuf, dataBuff, 0);

    free(scale_data);

    free((void *) top1Result);
    free((void *) otherResults);
    free((void *) inferenceTime);

    if (inputtensor != NULL) {
        HIAI_TensorBuffer_destroy(inputtensor);
        inputtensor = NULL;
    }

    if (outputtensor != NULL) {
        HIAI_TensorBuffer_destroy(outputtensor);
        outputtensor = NULL;
    }

    return result;
}