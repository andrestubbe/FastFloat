#!/bin/bash
# Native compilation script for FastFloat JNI library
# Supports Linux and macOS

set -e

echo "========================================"
echo "FastFloat Native Library Builder"
echo "========================================"

# Configuration
LIB_NAME="fastfloat"
OS=$(uname -s)
ARCH=$(uname -m)

echo "OS: $OS"
echo "Architecture: $ARCH"

# Detect JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    if [ -d "/usr/lib/jvm/java-17-openjdk" ]; then
        export JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
    elif [ -d "/usr/lib/jvm/java-21-openjdk" ]; then
        export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
    elif [ -d "/usr/lib/jvm/default-java" ]; then
        export JAVA_HOME="/usr/lib/jvm/default-java"
    elif [ -d "/usr/local/opt/openjdk" ]; then
        export JAVA_HOME="/usr/local/opt/openjdk"
    fi
fi

if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME not set!"
    echo "Please set JAVA_HOME to your JDK installation path"
    exit 1
fi

echo "Using JAVA_HOME: $JAVA_HOME"

# Create build directory
mkdir -p build

# Compiler flags for maximum performance
CFLAGS="-O3 -march=native -ffast-math -fPIC"
CFLAGS="$CFLAGS -Wall -Wextra -Wpedantic"
CFLAGS="$CFLAGS -I\"$JAVA_HOME/include\""

# OS-specific settings
if [ "$OS" = "Linux" ]; then
    CFLAGS="$CFLAGS -I\"$JAVA_HOME/include/linux\""
    LDFLAGS="-shared -fPIC -Wl,-soname,lib${LIB_NAME}.so"
    OUTPUT="build/lib${LIB_NAME}.so"
elif [ "$OS" = "Darwin" ]; then
    CFLAGS="$CFLAGS -I\"$JAVA_HOME/include/darwin\""
    LDFLAGS="-dynamiclib -fPIC -Wl,-install_name,lib${LIB_NAME}.dylib"
    OUTPUT="build/lib${LIB_NAME}.dylib"
else
    echo "Unsupported OS: $OS"
    exit 1
fi

# Detect SIMD support
echo ""
echo "Detecting CPU features..."
if [ "$ARCH" = "x86_64" ]; then
    # Check for AVX512
    if grep -q "avx512" /proc/cpuinfo 2>/dev/null || sysctl -a 2>/dev/null | grep -q "AVX512"; then
        echo "  AVX-512 detected"
        CFLAGS="$CFLAGS -mavx512f -mavx512dq"
    fi
    # Check for AVX2
    if grep -q "avx2" /proc/cpuinfo 2>/dev/null || sysctl -a 2>/dev/null | grep -q "AVX2"; then
        echo "  AVX2 detected"
        CFLAGS="$CFLAGS -mavx2"
    fi
    # Check for FMA
    if grep -q "fma" /proc/cpuinfo 2>/dev/null; then
        echo "  FMA3 detected"
        CFLAGS="$CFLAGS -mfma"
    fi
fi

# Compile
echo ""
echo "Compiling native library..."
echo "Compiler flags: $CFLAGS"
echo ""

g++ $CFLAGS $LDFLAGS \
    -o "$OUTPUT" \
    native/fastfloat.cpp \
    native/fastfloat_ryu.cpp \
    -lm

echo ""
echo "========================================"
echo "Build successful: $OUTPUT"
echo "========================================"

# Verify
if [ -f "$OUTPUT" ]; then
    echo ""
    echo "Library info:"
    if [ "$OS" = "Linux" ]; then
        file "$OUTPUT"
        ldd "$OUTPUT" 2>/dev/null || true
    elif [ "$OS" = "Darwin" ]; then
        file "$OUTPUT"
        otool -L "$OUTPUT" 2>/dev/null || true
    fi
fi

echo ""
echo "To use: mvn clean package -Pnative"
