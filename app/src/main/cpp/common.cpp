#include <jni.h>
#include <string>
#include <vector>
#include <common.h>

std::vector<std::string> clabels;

std::vector<std::string> split_string(const std::string &str, const std::string &delimiter) {
    std::vector<std::string> strings;

    std::string::size_type pos = 0;
    std::string::size_type prev = 0;
    while ((pos = str.find(delimiter, prev)) != std::string::npos) {
        strings.push_back(str.substr(prev, pos - prev));
        prev = pos + 1;
    }
    strings.push_back(str.substr(prev));
    return strings;
}

JNIEXPORT void JNICALL
Java_com_example_johannes_huawei_ModelManager_initLabels(JNIEnv *env, jclass type, jbyteArray jlabels) {
    int len = env->GetArrayLength(jlabels);
    std::string words_buffer;
    words_buffer.resize(len);
    env->GetByteArrayRegion(jlabels, 0, len, (jbyte *) words_buffer.data());
    clabels = split_string(words_buffer, "\n");
}