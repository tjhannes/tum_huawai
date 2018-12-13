#ifndef _ERROR_CODE_H
#define _ERROR_CODE_H

typedef enum {
    NO_ERROR = 0,
    MODEL_NAME_LEN_ERROR = 1,           //Model name length is wrong (model length needs to be 1-128)
    MODEL_DIR_ERROR = 2,                //Model file path is empty
    ODEL_SECRET_KEY_ERROR = 3,          //Model file decryption key length error (length is not 0 or 64)
    MODEL_PARA_SECRET_KEY_ERROR = 4,    //Model parameter file decryption key length error (length can not be 0 or 64)
    FRAMEWORK_TYPE_ERROR = 5,           //Model framework type (tensorflow / caffe) selection error (frame selection is not tensorflow or caffe or KALDI)
    MODEL_TYPE_ERROR = 6,               //Online or offline model type selection error (model type selection is not online or offline)
    IPU_FREQUENCY_ERROR = 7,            //IPU frequency setting is incorrect (frequency setting is not LOW, Normal, Hign)
    MODEL_NUM_ERROR = 8,                //The number of models loaded is wrong (the number of models is 0 or more than 20)
    MODEL_SIZE_ERROR = 9,               //Model size error (model size is 0)
    TIMEOUT_ERROR = 10,                 //Timeout setting error (set timeout time exceeds 60000ms)
    INPUT_DATA_SHAPE_ERROR = 11,        //Input data shape error (n * c * h * w is 0)
    OUTPUT_DATA_SHAPE_ERROR = 12,       //Output data shape error (n * c * h * w is 0)
    INPUT_DATA_NUM_ERROR = 13,          //The number of input data is wrong (the number of input data is 0 or more than 20)
    OUTPUT_DATA_NUM_ERROR = 14,         //The number of output data is wrong (the number of output data is 0 or more than 20)
    MODEL_MANAGER_TOO_MANY_ERROR = 15,  //The created model housekeeper instance exceeds the maximum value (a single process creates more than three clients)
    MODEL_NAME_DUPLICATE_ERROR = 18,  //Model name repeat error (duplicate file names in multiple models)
    HIAI_SERVER_CONNECT_ERROR = 19,    //hiaiserver connection failed (hiaiserver service is not started)
    HIAI_SERVER_CONNECT_IRPT = 20,     //hiaiserver connection is broken
    MODEL_TENSOR_SHAPE_NO_MATCH = 500,  //model size does not match (the input and output nchw and the model's nchw do not match)
    EXPIRATION_FUCNTION = 999,        //Interface expired
    INTERNAL_ERROR = 1000             //internal error
} HIAI_ERROR_CODE;

#endif //_ERROR_CODE_H
