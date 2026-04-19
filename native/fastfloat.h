#ifndef FASTFLOAT_H
#define FASTFLOAT_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

// JNI function declarations for FastFloat

// Parsing
JNIEXPORT jfloat JNICALL Java_fastfloat_FastFloat_parseFloat
  (JNIEnv *, jclass, jstring);

JNIEXPORT jdouble JNICALL Java_fastfloat_FastFloat_parseDouble
  (JNIEnv *, jclass, jstring);

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseFloatFast
  (JNIEnv *, jclass, jstring, jfloatArray);

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseDoubleFast
  (JNIEnv *, jclass, jstring, jdoubleArray);

// Formatting
JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__F
  (JNIEnv *, jclass, jfloat);

JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__D
  (JNIEnv *, jclass, jdouble);

JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__FI
  (JNIEnv *, jclass, jfloat, jint);

JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__DI
  (JNIEnv *, jclass, jdouble, jint);

// Batch parsing
JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseFloatBatch
  (JNIEnv *, jclass, jobjectArray, jfloatArray);

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseDoubleBatch
  (JNIEnv *, jclass, jobjectArray, jdoubleArray);

// ByteBuffer API (Zero-Marshaling)
JNIEXPORT jfloat JNICALL Java_fastfloat_FastFloat_parseFloatBuffer
  (JNIEnv *, jclass, jobject, jint, jint);

JNIEXPORT jdouble JNICALL Java_fastfloat_FastFloat_parseDoubleBuffer
  (JNIEnv *, jclass, jobject, jint, jint);

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseFloatBatchBuffer
  (JNIEnv *, jclass, jobject, jintArray, jintArray, jfloatArray);

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseDoubleBatchBuffer
  (JNIEnv *, jclass, jobject, jintArray, jintArray, jdoubleArray);

// Utility
JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_getNativeVersion
  (JNIEnv *, jclass);

// FastFloatBatch - SIMD operations
JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_add
  (JNIEnv *, jclass, jfloatArray, jfloatArray, jfloatArray);

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_mul
  (JNIEnv *, jclass, jfloatArray, jfloatArray, jfloatArray);

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_sub
  (JNIEnv *, jclass, jfloatArray, jfloatArray, jfloatArray);

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_div
  (JNIEnv *, jclass, jfloatArray, jfloatArray, jfloatArray);

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_fma
  (JNIEnv *, jclass, jfloatArray, jfloatArray, jfloatArray, jfloatArray);

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_scale
  (JNIEnv *, jclass, jfloatArray, jfloat, jfloatArray);

// ByteBuffer operations
JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_bufferToFloats
  (JNIEnv *, jclass, jobject, jint, jfloatArray, jint);

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_floatsToBuffer
  (JNIEnv *, jclass, jfloatArray, jobject, jint, jint);

// Alignment
JNIEXPORT jfloatArray JNICALL Java_fastfloat_FastFloatBatch_allocateAligned
  (JNIEnv *, jclass, jint);

JNIEXPORT jboolean JNICALL Java_fastfloat_FastFloatBatch_isAligned
  (JNIEnv *, jclass, jfloatArray);

#ifdef __cplusplus
}
#endif

#endif // FASTFLOAT_H
