/*
 * Copyright 2014-2015 Marvin Wißfeld
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <jni.h>
#include <android/log.h>
#include <sys/mman.h>
#include <errno.h>
#include <unistd.h>
#include <sys/ptrace.h>
// add
//#include <cstdlib>
//#include <sys/system_properties.h>
//#include <fcntl.h>

//#include <list>
//#include <stdint.h>
#include <string> // c++ strerror(errno)需要该库
//#include <vector>

#define TAG_NAME    "sanbo.nativeArtHook"
#define LOGV(...)  ((void)__android_log_print(ANDROID_LOG_VERBOSE, TAG_NAME, __VA_ARGS__))
#define LOGD(...)  ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG_NAME, __VA_ARGS__))
#define LOGI(...)  ((void)__android_log_print(ANDROID_LOG_INFO, TAG_NAME, __VA_ARGS__))
#define LOGE(...)  ((void)__android_log_print(ANDROID_LOG_ERROR, TAG_NAME, __VA_ARGS__))

#define JNIHOOK_CLASS "de/larma/arthook/Native"


JNIEXPORT jboolean JNICALL Art_munprotect(JNIEnv *env, jclass _cls, jlong addr, jlong len) {
    int pagesize = sysconf(_SC_PAGESIZE);
    int alignment = (addr % pagesize);

    int i = mprotect((void *) (addr - alignment), (size_t) (len + alignment),
                     PROT_READ | PROT_WRITE | PROT_EXEC);
    if (i == -1) {
        LOGV("mprotect failed: %s (%d)", strerror(errno), errno);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

JNIEXPORT void JNICALL Art_memcpy(JNIEnv *env, jclass _cls, jlong src, jlong dest, jint length) {
    char *srcPnt = (char *) src;
    char *destPnt = (char *) dest;
    for (int i = 0; i < length; ++i) {
        destPnt[i] = srcPnt[i];
    }
}

JNIEXPORT void JNICALL Art_memput(JNIEnv *env, jclass _cls, jbyteArray src, jlong dest) {
    jbyte *srcPnt = env->GetByteArrayElements(src, 0);
    jsize length = env->GetArrayLength(src);
    unsigned char *destPnt = (unsigned char *) dest;
    for (int i = 0; i < length; ++i) {
        destPnt[i] = srcPnt[i];
    }
    env->ReleaseByteArrayElements(src, srcPnt, 0);
}

JNIEXPORT jbyteArray JNICALL Art_memget(JNIEnv *env, jclass _cls, jlong src, jint length) {
    jbyteArray dest = env->NewByteArray(length);
    if (dest == NULL) {
        return NULL;
    }
    unsigned char *destPnt = (unsigned char *) env->GetByteArrayElements(dest, 0);

    unsigned char *srcPnt = (unsigned char *) src;
    for (int i = 0; i < length; ++i) {
        destPnt[i] = srcPnt[i];
    }
    env->ReleaseByteArrayElements(dest, (jbyte *) destPnt, 0);
    return dest;
}

JNIEXPORT jlong JNICALL Art_mmap(JNIEnv *env, jclass _cls, jint length) {
    void *space = mmap(0, length, PROT_READ | PROT_WRITE | PROT_EXEC, MAP_PRIVATE | MAP_ANONYMOUS,
                       -1, 0);
    if (space == MAP_FAILED) {
        LOGV("mmap failed: %s (%d)", strerror(errno), errno);
        return 0;
    }
    return (jlong) space;
}

JNIEXPORT jboolean JNICALL Art_munmap(JNIEnv *env, jclass _cls, jlong addr, jint length) {
    int r = munmap((void *) addr, length);
    if (r == -1) {
        LOGV("munmap failed: %s (%d)", strerror(errno), errno);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

JNIEXPORT void JNICALL Art_ptrace(JNIEnv *env, jclass _cls, jint pid) {
    ptrace(PTRACE_ATTACH, (pid_t) pid, 0, 0);
}

static JNINativeMethod dexposedMethods[] = {

        {"mmap",       "(I)J",   (void *) Art_mmap},
        {"ptrace",     "(I)V",   (void *) Art_ptrace},
        {"munmap",     "(JI)Z",  (void *) Art_munmap},
        {"memcpy",     "(JJI)V", (void *) Art_memcpy},
        {"memput",     "([BJ)V", (void *) Art_memput},
        {"memget",     "(JI)[B", (void *) Art_memget},
        {"munprotect", "(JJ)Z",  (void *) Art_munprotect}
};

static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {
    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env = NULL;

    LOGV(" inside JNI_OnLoad");

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    if (!registerNativeMethods(env, JNIHOOK_CLASS, dexposedMethods,
                               sizeof(dexposedMethods) / sizeof(dexposedMethods[0]))) {
        return -1;
    }

    return JNI_VERSION_1_6;
}
