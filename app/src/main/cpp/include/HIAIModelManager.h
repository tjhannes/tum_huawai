#ifndef __HIAI_MODEL_MANAGER_H__
#define __HIAI_MODEL_MANAGER_H__

#include <stdbool.h>

/**
This is the HIAI ModelManager C API:
*/
#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
    HIAI_MODELTYPE_ONLINE = 0,
    HIAI_MODELTYPE_OFFLINE
} HIAI_ModelType;

typedef enum {
    HIAI_FRAMEWORK_NONE = 0,
    HIAI_FRAMEWORK_TENSORFLOW,
    HIAI_FRAMEWORK_KALDI,
    HIAI_FRAMEWORK_CAFFE,
    HIAI_FRAMEWORK_INVALID,
} HIAI_Framework;

typedef enum {
    HIAI_DEVPERF_UNSET = 0,
    HIAI_DEVPREF_LOW,
    HIAI_DEVPREF_NORMAL,
    HIAI_DEVPREF_HIGH,
} HIAI_DevPerf;

typedef enum {
    HIAI_DATATYPE_UINT8 = 0,
    HIAI_DATATYPE_FLOAT32 = 1,
} HIAI_DataType;

#define HIAI_DEVPERF_DEFAULT (HIAI_DEVPREF_HIGH)

typedef struct {
    const char* modelNetName;

    const char* modelNetPath;
    bool isModelNetEncrypted;
    const char* modelNetKey;

    const char* modelNetParamPath;
    bool isModelNetParamEncrypted;
    const char* modelNetParamKey;

    HIAI_ModelType modelType;
    HIAI_Framework frameworkType;
    HIAI_DevPerf   perf;
} HIAI_ModelDescription;

#define HIAI_MODEL_DESCRIPTION_INIT { \
    "", \
    "", false, "", \
    "", false, "", \
    HIAI_MODELTYPE_OFFLINE, \
    HIAI_FRAMEWORK_CAFFE, \
    HIAI_DEVPERF_DEFAULT \
}

typedef struct {
    int number;
    int channel;
    int height;
    int width;
    HIAI_DataType dataType;  /* optional */
} HIAI_TensorDescription;

#define HIAI_TENSOR_DESCRIPTION_INIT {0, 0, 0, 0, HIAI_DATATYPE_FLOAT32}

typedef struct HIAI_ModelManagerListener_struct
{
    void (*onLoadDone)(void* userdata, int taskStamp);
    void (*onRunDone)(void* userdata, int taskStamp);
    void (*onUnloadDone)(void* userdata, int taskStamp);
    void (*onTimeout)(void* userdata, int taskStamp);
    void (*onError)(void* userdata, int taskStamp, int errCode);
    void (*onServiceDied)(void* userdata);

    void* userdata;
} HIAI_ModelManagerListener;

#define HIAI_MODEL_MANAGER_LISTENER_INIT {NULL, NULL, NULL, NULL, NULL, NULL, NULL}


typedef struct HIAI_ModelBuffer HIAI_ModelBuffer;

/*
 * Load the model from the path and create a HIAI_ModelBuffer (used to load the model from the application layer sdcard)
 * @param name: The name of the model to load (does not include the file suffix name. For example, to load "alexnet.cambricon", please enter "alexnet")
 * @param path: the path of the model to be loaded
 * @param perf: NPU frequency, corresponding to high, medium and low third gear
 * @return returns HIAI_ModelBuffer
 */
HIAI_ModelBuffer* HIAI_ModelBuffer_create_from_file(const char* name, const char* path, HIAI_DevPerf perf);

/**
 * Load the model after reading the model data, create HIAI_ModelBuffer (used to load the model from the application layer assets)
 * @param name The name of the model to load (does not include the file suffix name. For example to load "alexnet.cambricon", please enter "alexnet")
 * @param modelBuf model data address
 * @param size model data length
 * @param perf PU frequency, corresponding to high, medium and low third gear
 * @return returns HIAI_ModelBuffer
 */
HIAI_ModelBuffer* HIAI_ModelBuffer_create_from_buffer(const char* name, void* modelBuf, int size, HIAI_DevPerf perf);

const char* HIAI_ModelBuffer_getName(HIAI_ModelBuffer* b);
const char* HIAI_ModelBuffer_getPath(HIAI_ModelBuffer* b);
HIAI_DevPerf HIAI_ModelBuffer_getPerf(HIAI_ModelBuffer* b);

int HIAI_ModelBuffer_getSize(HIAI_ModelBuffer* b);

/**
 * Destroy HIAI_ModelBuffer
 * @param b HIAI_ModelBuffer to be destroyed
 */
void HIAI_ModelBuffer_destroy(HIAI_ModelBuffer* b);



typedef struct HIAI_TensorBuffer HIAI_TensorBuffer;

/**
 * Create HIAI_TensorBuffer
 * @param n tensor's batch
 * @param c tensor's channel
 * @param h tensor height
 * @param w tensor width
 * @return returns HIAI_TensorBuffer
 */
HIAI_TensorBuffer* HIAI_TensorBuffer_create(int n, int c, int h, int w);

HIAI_TensorBuffer* HIAI_TensorBuffer_createFromTensorDesc(HIAI_TensorDescription* tensor);

HIAI_TensorDescription HIAI_TensorBuffer_getTensorDesc(HIAI_TensorBuffer* b);

/**
 * Get the model input or output data address
 * @param b Model input or output HIAI_TensorBuffer
 * @return Model input or output data address
 */
void* HIAI_TensorBuffer_getRawBuffer(HIAI_TensorBuffer* b);

/**
 * Get model input or output data length
 * @param b Model input or output HIAI_TensorBuffer
 * @return Returns the length of the model input or output data
 */
int HIAI_TensorBuffer_getBufferSize(HIAI_TensorBuffer* b);

/**
 * Destroy HIAI_TensorBuffer
 * @param b HIAI_TensorBuffer to be destroyed
 */
void HIAI_TensorBuffer_destroy(HIAI_TensorBuffer* b);

typedef struct
{
    int input_cnt;
    int output_cnt;
    int *input_shape;
    int *output_shape;
} HIAI_ModelTensorInfo;


typedef struct HIAI_ModelManager HIAI_ModelManager;

HIAI_ModelTensorInfo* HIAI_ModelManager_getModelTensorInfo(HIAI_ModelManager* manager, const char* modelName);

void HIAI_ModelManager_releaseModelTensorInfo(HIAI_ModelTensorInfo* modelTensor);

HIAI_ModelManager* HIAI_ModelManager_create(HIAI_ModelManagerListener* listener);

/**
 *  Destroy model steward
 *  @param manager model management engine object interface
 */
void HIAI_ModelManager_destroy(HIAI_ModelManager* manager);

/**
 * Load model file
 * @param manager model management engine object
 * @param bufferArray HIAI_ModelBuffer, both single and multiple models.
 * @param nBuffers Number of loaded models (bufferArray array size)
 * @return Executes successfully, returns 0; execution failed, returns the corresponding error value (see: ErrorCode.h).
 */
int HIAI_ModelManager_loadFromModelBuffers(HIAI_ModelManager* manager, HIAI_ModelBuffer* bufferArray[], int nBuffers);

int HIAI_ModelManager_loadFromModelDescriptions(HIAI_ModelManager* manager, HIAI_ModelDescription descsArray[], int nDescs);

/**
 * Model running interface
 * @param manager model management engine object
 * @param input model input, support for multiple inputs
 * @param nInput The number of model input
 * @param output model output, support for multiple outputs
 * @param nOutput The number of model output
 * @param ulTimeout timeout, does not take effect when called synchronously
 * @param modelName Model name (does not include file suffix name. For example, to run "alexnet.cambricon", please enter "alexnet")
 * @return Executes successfully, returns 0; execution failed, returns the corresponding error value (refer to ErrorCode.h).
 */
int HIAI_ModelManager_runModel(
        HIAI_ModelManager* manager,
        HIAI_TensorBuffer* input[],
        int nInput,
        HIAI_TensorBuffer* output[],
        int nOutput,
        int ulTimeout,
        const char* modelName);

/**
 * Set the model's input and output
 * @param manager model management engine object interface
 * @param modelname Model name (does not include file suffix name. For example to run "alexnet.cambricon", please enter "alexnet")
 * @param input model input, support for multiple inputs
 * @param nInput Number of model inputs
 * @param output model output, support for multiple outputs
 * @param nOutput The number of models output
 * @return Executes successfully, returns 0; execution failed, returns the corresponding error value (refer to ErrorCode.h).
 */
int HIAI_ModelManager_setInputsAndOutputs(
        HIAI_ModelManager* manager,
        const char* modelname,
        HIAI_TensorBuffer* input[],
        int nInput,
        HIAI_TensorBuffer* output[],
        int nOutput);

/**
 * Start computing interface
 * @param manager Model management engine object interface.
 * @param modelname Model name (does not include file suffix name. For example to run "alexnet.cambricon", please enter "alexnet")
 * @return Executes successfully, returns 0; execution failed, returns the corresponding error value (refer to ErrorCode.h).
 */
int HIAI_ModelManager_startCompute(HIAI_ModelManager* manager, const char* modelname);

/**
 * Unload model
 * @param manager model management engine object interface
 * @return Executes successfully, returns 0; execution failed, returns the corresponding error value (refer to ErrorCode.h).
 */
int HIAI_ModelManager_unloadModel(HIAI_ModelManager* manager);

/**
 * Get the DDK version number in the system
 * @return executes successfully and returns the corresponding DDK version number. The version number name, which is described in the form <major>.<middle>.<minor>.<point>.
 * <major>: product form, 1XX: mobile phone form, 2XX: edge calculation form, 3XX: cloud form
 * <middle>: XXX three-digit number, V version of the same product form, such as mobile phone form, HiAIV100, HiAIV200, etc.
 * <minor>: Increment C version, new increase feature, XXX three digits represent < point >: B version or patch version, XXX three digits, the last one is non-zero, indicating the patch version.
 * For example, the version number returned by the kirin970 system is 100.150.010.010. If 000.000.000.000 is returned, it means that this version does not support NPU acceleration;
 * The execution failed and the corresponding error value is returned.
 */
char* HIAI_GetVersion();

#ifdef __cplusplus
}  // extern "C"
#endif

#endif  // __HIAI_MODEL_MANAGER_H__
