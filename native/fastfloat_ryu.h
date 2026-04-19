// Ryu float/double to string conversion
// Based on https://github.com/ulfjack/ryu (Apache 2.0 / Boost license compatible)
// Simplified for FastFloat

#ifndef FASTFLOAT_RYU_H
#define FASTFLOAT_RYU_H

#include <cstdint>
#include <cstring>

#ifdef __cplusplus
extern "C" {
#endif

// Float to string
int ryu_ftoa(float value, char* result);

// Double to string
int ryu_dtoa(double value, char* result);

// With precision
int ryu_ftoa_precision(float value, char* result, int precision);
int ryu_dtoa_precision(double value, char* result, int precision);

#ifdef __cplusplus
}
#endif

#endif // FASTFLOAT_RYU_H
