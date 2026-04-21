/**
 * @file fastfloat.h
 * @brief FastFloat JNI Header - Fast float/double parsing and formatting
 *
 * @details High-performance decimal-to-binary conversion using:
 * - Custom fast parser (2-4x faster than Double.parseDouble)
 * - Ryu algorithm for formatting (guaranteed shortest representation)
 * - SIMD batch operations for data processing
 * - ByteBuffer API for zero-copy parsing
 *
 * @par Performance
 * - Parsing: ~2-4x faster than standard Java parser
 * - Formatting: ~3x faster with Ryu algorithm
 * - Batch: Up to 8x faster with SIMD
 *
 * @par Features
 * - Full IEEE 754 compliance
 * - Correct rounding (round-to-nearest, ties-to-even)
 * - Scientific and fixed notation
 * - Configurable precision for formatting
 *
 * @author FastJava Team
 * @version 1.1.0
 * @copyright MIT License
 */

#ifndef FASTFLOAT_H
#define FASTFLOAT_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

/** @defgroup Parse Parsing Functions
 *  @brief Float/double parsing from strings and buffers
 *  @{ */

/** @name Native Parsing (v1.2.0+) */
JNIEXPORT jfloat JNICALL Java_fastfloat_FastFloat_parseFloatNative
  (JNIEnv *, jclass, jstring);

JNIEXPORT jdouble JNICALL Java_fastfloat_FastFloat_parseDoubleNative
  (JNIEnv *, jclass, jstring);

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseFloatFast
  (JNIEnv *, jclass, jstring, jfloatArray);

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseDoubleFast
  (JNIEnv *, jclass, jstring, jdoubleArray);

/** @} */

/** @defgroup Format Formatting Functions
 *  @brief Float/double to string conversion (Ryu algorithm)
 *  @{ */
JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__F
  (JNIEnv *, jclass, jfloat);

JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__D
  (JNIEnv *, jclass, jdouble);

JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__FI
  (JNIEnv *, jclass, jfloat, jint);

JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__DI
  (JNIEnv *, jclass, jdouble, jint);

/** @} */

/** @defgroup Batch Batch Parsing
 *  @brief Parse multiple values efficiently
 *  @{ */
JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseFloatBatch
  (JNIEnv *, jclass, jobjectArray, jfloatArray);

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseDoubleBatch
  (JNIEnv *, jclass, jobjectArray, jdoubleArray);

/** @} */

/** @defgroup ByteBuffer ByteBuffer API
 *  @brief Zero-copy parsing from direct ByteBuffer
 *  @{ */
JNIEXPORT jfloat JNICALL Java_fastfloat_FastFloat_parseFloatBuffer
  (JNIEnv *, jclass, jobject, jint, jint);

JNIEXPORT jdouble JNICALL Java_fastfloat_FastFloat_parseDoubleBuffer
  (JNIEnv *, jclass, jobject, jint, jint);

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseFloatBatchBuffer
  (JNIEnv *, jclass, jobject, jintArray, jintArray, jfloatArray);

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseDoubleBatchBuffer
  (JNIEnv *, jclass, jobject, jintArray, jintArray, jdoubleArray);

/** @} */

/** @defgroup Utility Utility Functions
 *  @brief Version and helper functions
 *  @{ */
JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_getNativeVersion
  (JNIEnv *, jclass);

/** @} */

/** @defgroup SIMD SIMD Batch Operations
 *  @brief SIMD-accelerated vector math
 *  @{ */

/** @name Arithmetic Operations (SIMD) */
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

/** @name ByteBuffer Operations */
JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_bufferToFloats
  (JNIEnv *, jclass, jobject, jint, jfloatArray, jint);

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_floatsToBuffer
  (JNIEnv *, jclass, jfloatArray, jobject, jint, jint);

/** @name Memory Alignment */
JNIEXPORT jfloatArray JNICALL Java_fastfloat_FastFloatBatch_allocateAligned
  (JNIEnv *, jclass, jint);

JNIEXPORT jboolean JNICALL Java_fastfloat_FastFloatBatch_isAligned
  (JNIEnv *, jclass, jfloatArray);

/** @} */

#ifdef __cplusplus
}
#endif

#endif // FASTFLOAT_H
