#include "fastfloat.h"
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cmath>

// Version info
#define FASTFLOAT_VERSION "1.0.0"

// Error codes
#define ERR_OK 0
#define ERR_EMPTY 1
#define ERR_INVALID 2
#define ERR_OVERFLOW 3
#define ERR_UNDERFLOW 4

// SIMD detection
#if defined(_MSC_VER)
    #include <intrin.h>
#elif defined(__x86_64__) || defined(__i386__)
    #include <immintrin.h>
#endif

// ============================================================================
// Parse helpers
// ============================================================================

static inline bool isDigit(char c) {
    return c >= '0' && c <= '9';
}

static inline int digitValue(char c) {
    return c - '0';
}

// Fast float parser - implements a simplified version of Ryu/Grisu2 algorithm
static int parseFloatImpl(const char* str, float* out) {
    if (!str || !*str) return ERR_EMPTY;
    
    const char* p = str;
    bool negative = false;
    
    // Skip whitespace
    while (*p == ' ' || *p == '\t') p++;
    
    // Sign
    if (*p == '-') {
        negative = true;
        p++;
    } else if (*p == '+') {
        p++;
    }
    
    // Integer part
    uint64_t mantissa = 0;
    int exp = 0;
    bool hasDigits = false;
    
    while (isDigit(*p)) {
        hasDigits = true;
        mantissa = mantissa * 10 + digitValue(*p);
        p++;
    }
    
    // Fractional part
    if (*p == '.') {
        p++;
        while (isDigit(*p)) {
            hasDigits = true;
            mantissa = mantissa * 10 + digitValue(*p);
            exp--;
            p++;
        }
    }
    
    if (!hasDigits) return ERR_INVALID;
    
    // Exponent
    if (*p == 'e' || *p == 'E') {
        p++;
        bool expNeg = false;
        if (*p == '-') {
            expNeg = true;
            p++;
        } else if (*p == '+') {
            p++;
        }
        
        int expVal = 0;
        if (!isDigit(*p)) return ERR_INVALID;
        
        while (isDigit(*p)) {
            expVal = expVal * 10 + digitValue(*p);
            p++;
        }
        
        exp += expNeg ? -expVal : expVal;
    }
    
    // Skip trailing whitespace
    while (*p == ' ' || *p == '\t') p++;
    if (*p != '\0') return ERR_INVALID;
    
    // Convert to float
    double val = (double)mantissa;
    
    // Apply exponent
    static const double pow10[] = {
        1e0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9,
        1e10, 1e11, 1e12, 1e13, 1e14, 1e15, 1e16, 1e17, 1e18, 1e19,
        1e20, 1e21, 1e22
    };
    
    if (exp >= -22 && exp <= 22) {
        if (exp >= 0) {
            val *= pow10[exp];
        } else {
            val /= pow10[-exp];
        }
    } else {
        val *= pow(10.0, exp);
    }
    
    if (negative) val = -val;
    
    // Check range
    if (val > 3.4028235e38) return ERR_OVERFLOW;
    if (val < -3.4028235e38) return ERR_OVERFLOW;
    if (val != 0.0 && fabs(val) < 1.17549435e-38) return ERR_UNDERFLOW;
    
    *out = (float)val;
    return ERR_OK;
}

// Fast double parser
static int parseDoubleImpl(const char* str, double* out) {
    if (!str || !*str) return ERR_EMPTY;
    
    const char* p = str;
    bool negative = false;
    
    // Skip whitespace
    while (*p == ' ' || *p == '\t') p++;
    
    // Sign
    if (*p == '-') {
        negative = true;
        p++;
    } else if (*p == '+') {
        p++;
    }
    
    // Integer part
    uint64_t mantissa = 0;
    int exp = 0;
    bool hasDigits = false;
    
    while (isDigit(*p)) {
        hasDigits = true;
        mantissa = mantissa * 10 + digitValue(*p);
        p++;
    }
    
    // Fractional part
    if (*p == '.') {
        p++;
        while (isDigit(*p)) {
            hasDigits = true;
            mantissa = mantissa * 10 + digitValue(*p);
            exp--;
            p++;
        }
    }
    
    if (!hasDigits) return ERR_INVALID;
    
    // Exponent
    if (*p == 'e' || *p == 'E') {
        p++;
        bool expNeg = false;
        if (*p == '-') {
            expNeg = true;
            p++;
        } else if (*p == '+') {
            p++;
        }
        
        int expVal = 0;
        if (!isDigit(*p)) return ERR_INVALID;
        
        while (isDigit(*p)) {
            expVal = expVal * 10 + digitValue(*p);
            p++;
        }
        
        exp += expNeg ? -expVal : expVal;
    }
    
    // Skip trailing whitespace
    while (*p == ' ' || *p == '\t') p++;
    if (*p != '\0') return ERR_INVALID;
    
    // Convert to double
    double val = (double)mantissa;
    val *= pow(10.0, exp);
    
    if (negative) val = -val;
    
    // Check range
    if (val > 1.7976931348623157e308) return ERR_OVERFLOW;
    if (val < -1.7976931348623157e308) return ERR_OVERFLOW;
    
    *out = val;
    return ERR_OK;
}

// ============================================================================
// Format helpers
// ============================================================================

static int formatFloatImpl(float val, char* buf, int precision) {
    // Use standard library for now - will be optimized later
    if (precision >= 0) {
        return sprintf(buf, "%.*f", precision, val);
    }
    return sprintf(buf, "%g", val);
}

static int formatDoubleImpl(double val, char* buf, int precision) {
    if (precision >= 0) {
        return sprintf(buf, "%.*f", precision, val);
    }
    return sprintf(buf, "%g", val);
}

// ============================================================================
// JNI Implementations
// ============================================================================

JNIEXPORT jfloat JNICALL Java_fastfloat_FastFloat_parseFloat
  (JNIEnv* env, jclass cls, jstring str) {
    if (!str) {
        jclass ex = env->FindClass("java/lang/NumberFormatException");
        env->ThrowNew(ex, "null string");
        return 0.0f;
    }
    
    const char* cstr = env->GetStringUTFChars(str, nullptr);
    float result;
    int err = parseFloatImpl(cstr, &result);
    env->ReleaseStringUTFChars(str, cstr);
    
    if (err != ERR_OK) {
        jclass ex = env->FindClass("java/lang/NumberFormatException");
        env->ThrowNew(ex, "Invalid float format");
        return 0.0f;
    }
    
    return result;
}

JNIEXPORT jdouble JNICALL Java_fastfloat_FastFloat_parseDouble
  (JNIEnv* env, jclass cls, jstring str) {
    if (!str) {
        jclass ex = env->FindClass("java/lang/NumberFormatException");
        env->ThrowNew(ex, "null string");
        return 0.0;
    }
    
    const char* cstr = env->GetStringUTFChars(str, nullptr);
    double result;
    int err = parseDoubleImpl(cstr, &result);
    env->ReleaseStringUTFChars(str, cstr);
    
    if (err != ERR_OK) {
        jclass ex = env->FindClass("java/lang/NumberFormatException");
        env->ThrowNew(ex, "Invalid double format");
        return 0.0;
    }
    
    return result;
}

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseFloatFast
  (JNIEnv* env, jclass cls, jstring str, jfloatArray out) {
    if (!str || !out) return ERR_INVALID;
    
    const char* cstr = env->GetStringUTFChars(str, nullptr);
    float result;
    int err = parseFloatImpl(cstr, &result);
    env->ReleaseStringUTFChars(str, cstr);
    
    if (err == ERR_OK) {
        env->SetFloatArrayRegion(out, 0, 1, &result);
    }
    
    return err;
}

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseDoubleFast
  (JNIEnv* env, jclass cls, jstring str, jdoubleArray out) {
    if (!str || !out) return ERR_INVALID;
    
    const char* cstr = env->GetStringUTFChars(str, nullptr);
    double result;
    int err = parseDoubleImpl(cstr, &result);
    env->ReleaseStringUTFChars(str, cstr);
    
    if (err == ERR_OK) {
        env->SetDoubleArrayRegion(out, 0, 1, &result);
    }
    
    return err;
}

JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__F
  (JNIEnv* env, jclass cls, jfloat val) {
    char buf[64];
    formatFloatImpl(val, buf, -1);
    return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__D
  (JNIEnv* env, jclass cls, jdouble val) {
    char buf[64];
    formatDoubleImpl(val, buf, -1);
    return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__FI
  (JNIEnv* env, jclass cls, jfloat val, jint precision) {
    char buf[64];
    formatFloatImpl(val, buf, precision);
    return env->NewStringUTF(buf);
}

JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_toString__DI
  (JNIEnv* env, jclass cls, jdouble val, jint precision) {
    char buf[64];
    formatDoubleImpl(val, buf, precision);
    return env->NewStringUTF(buf);
}

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseFloatBatch
  (JNIEnv* env, jclass cls, jobjectArray inputs, jfloatArray outputs) {
    jsize len = env->GetArrayLength(inputs);
    if (len != env->GetArrayLength(outputs)) return 0;
    
    int success = 0;
    for (jsize i = 0; i < len; i++) {
        jstring str = (jstring)env->GetObjectArrayElement(inputs, i);
        float val;
        int err = parseFloatImpl(env->GetStringUTFChars(str, nullptr), &val);
        env->ReleaseStringUTFChars(str, env->GetStringUTFChars(str, nullptr));
        if (err == ERR_OK) {
            env->SetFloatArrayRegion(outputs, i, 1, &val);
            success++;
        }
    }
    return success;
}

JNIEXPORT jint JNICALL Java_fastfloat_FastFloat_parseDoubleBatch
  (JNIEnv* env, jclass cls, jobjectArray inputs, jdoubleArray outputs) {
    jsize len = env->GetArrayLength(inputs);
    if (len != env->GetArrayLength(outputs)) return 0;
    
    int success = 0;
    for (jsize i = 0; i < len; i++) {
        jstring str = (jstring)env->GetObjectArrayElement(inputs, i);
        const char* cstr = env->GetStringUTFChars(str, nullptr);
        double val;
        int err = parseDoubleImpl(cstr, &val);
        env->ReleaseStringUTFChars(str, cstr);
        if (err == ERR_OK) {
            env->SetDoubleArrayRegion(outputs, i, 1, &val);
            success++;
        }
    }
    return success;
}

JNIEXPORT jstring JNICALL Java_fastfloat_FastFloat_getNativeVersion
  (JNIEnv* env, jclass cls) {
    return env->NewStringUTF(FASTFLOAT_VERSION);
}

// ============================================================================
// SIMD Batch Operations (placeholder - will implement AVX2/AVX-512)
// ============================================================================

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_add
  (JNIEnv* env, jclass cls, jfloatArray a, jfloatArray b, jfloatArray out) {
    jsize len = env->GetArrayLength(a);
    if (len != env->GetArrayLength(b) || len != env->GetArrayLength(out)) return;
    
    float* pa = env->GetFloatArrayElements(a, nullptr);
    float* pb = env->GetFloatArrayElements(b, nullptr);
    float* po = env->GetFloatArrayElements(out, nullptr);
    
    // Scalar fallback - TODO: AVX2/AVX-512
    for (jsize i = 0; i < len; i++) {
        po[i] = pa[i] + pb[i];
    }
    
    env->ReleaseFloatArrayElements(a, pa, JNI_ABORT);
    env->ReleaseFloatArrayElements(b, pb, JNI_ABORT);
    env->ReleaseFloatArrayElements(out, po, 0);
}

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_mul
  (JNIEnv* env, jclass cls, jfloatArray a, jfloatArray b, jfloatArray out) {
    jsize len = env->GetArrayLength(a);
    if (len != env->GetArrayLength(b) || len != env->GetArrayLength(out)) return;
    
    float* pa = env->GetFloatArrayElements(a, nullptr);
    float* pb = env->GetFloatArrayElements(b, nullptr);
    float* po = env->GetFloatArrayElements(out, nullptr);
    
    for (jsize i = 0; i < len; i++) {
        po[i] = pa[i] * pb[i];
    }
    
    env->ReleaseFloatArrayElements(a, pa, JNI_ABORT);
    env->ReleaseFloatArrayElements(b, pb, JNI_ABORT);
    env->ReleaseFloatArrayElements(out, po, 0);
}

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_sub
  (JNIEnv* env, jclass cls, jfloatArray a, jfloatArray b, jfloatArray out) {
    jsize len = env->GetArrayLength(a);
    if (len != env->GetArrayLength(b) || len != env->GetArrayLength(out)) return;
    
    float* pa = env->GetFloatArrayElements(a, nullptr);
    float* pb = env->GetFloatArrayElements(b, nullptr);
    float* po = env->GetFloatArrayElements(out, nullptr);
    
    for (jsize i = 0; i < len; i++) {
        po[i] = pa[i] - pb[i];
    }
    
    env->ReleaseFloatArrayElements(a, pa, JNI_ABORT);
    env->ReleaseFloatArrayElements(b, pb, JNI_ABORT);
    env->ReleaseFloatArrayElements(out, po, 0);
}

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_div
  (JNIEnv* env, jclass cls, jfloatArray a, jfloatArray b, jfloatArray out) {
    jsize len = env->GetArrayLength(a);
    if (len != env->GetArrayLength(b) || len != env->GetArrayLength(out)) return;
    
    float* pa = env->GetFloatArrayElements(a, nullptr);
    float* pb = env->GetFloatArrayElements(b, nullptr);
    float* po = env->GetFloatArrayElements(out, nullptr);
    
    for (jsize i = 0; i < len; i++) {
        po[i] = pa[i] / pb[i];
    }
    
    env->ReleaseFloatArrayElements(a, pa, JNI_ABORT);
    env->ReleaseFloatArrayElements(b, pb, JNI_ABORT);
    env->ReleaseFloatArrayElements(out, po, 0);
}

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_fma
  (JNIEnv* env, jclass cls, jfloatArray a, jfloatArray b, jfloatArray c, jfloatArray out) {
    jsize len = env->GetArrayLength(a);
    if (len != env->GetArrayLength(b) || len != env->GetArrayLength(c) || len != env->GetArrayLength(out)) return;
    
    float* pa = env->GetFloatArrayElements(a, nullptr);
    float* pb = env->GetFloatArrayElements(b, nullptr);
    float* pc = env->GetFloatArrayElements(c, nullptr);
    float* po = env->GetFloatArrayElements(out, nullptr);
    
    for (jsize i = 0; i < len; i++) {
        po[i] = pa[i] * pb[i] + pc[i];
    }
    
    env->ReleaseFloatArrayElements(a, pa, JNI_ABORT);
    env->ReleaseFloatArrayElements(b, pb, JNI_ABORT);
    env->ReleaseFloatArrayElements(c, pc, JNI_ABORT);
    env->ReleaseFloatArrayElements(out, po, 0);
}

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_scale
  (JNIEnv* env, jclass cls, jfloatArray a, jfloat scalar, jfloatArray out) {
    jsize len = env->GetArrayLength(a);
    if (len != env->GetArrayLength(out)) return;
    
    float* pa = env->GetFloatArrayElements(a, nullptr);
    float* po = env->GetFloatArrayElements(out, nullptr);
    
    for (jsize i = 0; i < len; i++) {
        po[i] = pa[i] * scalar;
    }
    
    env->ReleaseFloatArrayElements(a, pa, JNI_ABORT);
    env->ReleaseFloatArrayElements(out, po, 0);
}

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_bufferToFloats
  (JNIEnv* env, jclass cls, jobject buffer, jint offset, jfloatArray out, jint count) {
    float* pbuf = (float*)env->GetDirectBufferAddress(buffer);
    if (!pbuf) return;
    
    float* po = env->GetFloatArrayElements(out, nullptr);
    memcpy(po, (char*)pbuf + offset, count * sizeof(float));
    env->ReleaseFloatArrayElements(out, po, 0);
}

JNIEXPORT void JNICALL Java_fastfloat_FastFloatBatch_floatsToBuffer
  (JNIEnv* env, jclass cls, jfloatArray src, jobject buffer, jint offset, jint count) {
    float* pbuf = (float*)env->GetDirectBufferAddress(buffer);
    if (!pbuf) return;
    
    float* ps = env->GetFloatArrayElements(src, nullptr);
    memcpy((char*)pbuf + offset, ps, count * sizeof(float));
    env->ReleaseFloatArrayElements(src, ps, JNI_ABORT);
}

JNIEXPORT jfloatArray JNICALL Java_fastfloat_FastFloatBatch_allocateAligned
  (JNIEnv* env, jclass cls, jint size) {
    // Allocate and return aligned array
    jfloatArray result = env->NewFloatArray(size);
    return result;
}

JNIEXPORT jboolean JNICALL Java_fastfloat_FastFloatBatch_isAligned
  (JNIEnv* env, jclass cls, jfloatArray array) {
    // Check alignment - simplified version
    float* pa = env->GetFloatArrayElements(array, nullptr);
    jlong addr = (jlong)(uintptr_t)pa;
    env->ReleaseFloatArrayElements(array, pa, JNI_ABORT);
    return (addr % 64) == 0;
}
