// Ryu float/double to string conversion
// Optimized implementation for FastFloat
// Based on https://github.com/ulfjack/ryu (Apache 2.0)

#include "fastfloat_ryu.h"
#include <cstdint>
#include <cstring>

// Ryu tables - 64-bit constants
static const uint64_t FLOAT_MANTISSA_BITS = 23;
static const uint64_t FLOAT_EXPONENT_BITS = 8;
static const uint64_t FLOAT_BIAS = 127;

static const uint64_t DOUBLE_MANTISSA_BITS = 52;
static const uint64_t DOUBLE_EXPONENT_BITS = 11;
static const uint64_t DOUBLE_BIAS = 1023;

// Helper: count decimal digits
static inline int decimalLength9(uint32_t v) {
    if (v >= 1000000000) return 10;
    if (v >= 100000000) return 9;
    if (v >= 10000000) return 8;
    if (v >= 1000000) return 7;
    if (v >= 100000) return 6;
    if (v >= 10000) return 5;
    if (v >= 1000) return 4;
    if (v >= 100) return 3;
    if (v >= 10) return 2;
    return 1;
}

// Copy digits to buffer
static inline char* u32toa(uint32_t v, char* buffer) {
    int length = decimalLength9(v);
    for (int i = length - 1; i >= 0; i--) {
        buffer[i] = (char)('0' + (v % 10));
        v /= 10;
    }
    buffer += length;
    return buffer;
}

// Fast path for small integers
static int toStringShort(float value, char* result) {
    union { float f; uint32_t u; } converter;
    converter.f = value;
    uint32_t bits = converter.u;
    
    uint32_t mantissa = bits & ((1u << FLOAT_MANTISSA_BITS) - 1);
    uint32_t exponent = (bits >> FLOAT_MANTISSA_BITS) & ((1u << FLOAT_EXPONENT_BITS) - 1);
    
    if (exponent == 0 && mantissa == 0) {
        result[0] = '0';
        result[1] = '\0';
        return 1;
    }
    
    if (exponent == (1u << FLOAT_EXPONENT_BITS) - 1) {
        if (mantissa) {
            memcpy(result, "NaN", 4);
            return 3;
        }
        if (bits >> 31) {
            memcpy(result, "-Infinity", 10);
            return 9;
        }
        memcpy(result, "Infinity", 9);
        return 8;
    }
    
    return 0; // Not a special case, need full algorithm
}

// Simple sprintf fallback for now (will be replaced with full Ryu)
int ryu_ftoa(float value, char* result) {
    int shortResult = toStringShort(value, result);
    if (shortResult > 0) return shortResult;
    
    // Fallback to standard library (full Ryu implementation would go here)
    return sprintf(result, "%.9g", value);
}

int ryu_dtoa(double value, char* result) {
    union { double d; uint64_t u; } converter;
    converter.d = value;
    uint64_t bits = converter.u;
    
    uint64_t mantissa = bits & ((1ull << DOUBLE_MANTISSA_BITS) - 1);
    uint64_t exponent = (bits >> DOUBLE_MANTISSA_BITS) & ((1ull << DOUBLE_EXPONENT_BITS) - 1);
    
    if (exponent == 0 && mantissa == 0) {
        result[0] = '0';
        result[1] = '\0';
        return 1;
    }
    
    if (exponent == (1ull << DOUBLE_EXPONENT_BITS) - 1) {
        if (mantissa) {
            memcpy(result, "NaN", 4);
            return 3;
        }
        if (bits >> 63) {
            memcpy(result, "-Infinity", 10);
            return 9;
        }
        memcpy(result, "Infinity", 9);
        return 8;
    }
    
    return sprintf(result, "%.17g", value);
}

int ryu_ftoa_precision(float value, char* result, int precision) {
    if (precision < 0) return ryu_ftoa(value, result);
    return sprintf(result, "%.*f", precision, value);
}

int ryu_dtoa_precision(double value, char* result, int precision) {
    if (precision < 0) return ryu_dtoa(value, result);
    return sprintf(result, "%.*f", precision, value);
}
