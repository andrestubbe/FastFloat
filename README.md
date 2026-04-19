# FastFloat — Ultra-fast float/double parsing & formatting for Java (SIMD, zero-GC, native)

> **5–12× faster than Java's Float.parseFloat / Double.parseDouble.** Zero-GC. SIMD-accelerated. Ryu-powered formatting. JSON/CSV/telemetry parsing without garbage collection overhead.

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Maven](https://img.shields.io/badge/Maven-3.9+-orange.svg)](https://maven.apache.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io)
[![Version](https://img.shields.io/badge/version-1.1.0-blue.svg)]()

---

## Problem → Solution

Java's `Float.parseFloat` and `Double.parseDouble` are slow, create garbage, and bottleneck JSON/CSV parsing. **FastFloat replaces them with a zero-GC, SIMD-optimized, native parser** that processes numbers at **1 GB/s throughput**.

**Perfect for:** JSON parsing · CSV ingestion · Telemetry data · Sensor streams · Game loops · ML preprocessing

## Quick Start

```java
import fastfloat.FastFloat;

// 5–12× faster than Float.parseFloat() — zero allocations
float f = FastFloat.parseFloat("3.14159");
double d = FastFloat.parseDouble("2.718281828459045");
String s = FastFloat.toString(f);  // Ryu formatting

// Zero-GC fast path with bit-packed result (no exceptions, no allocations)
long packed = FastFloat.parseFloatZeroGC("3.14159");
if (FastFloat.unpackError(packed) == FastFloat.ERR_OK) {
    float value = FastFloat.unpackFloat(packed);  // Zero GC!
}

// ByteBuffer API - parse from direct buffer (zero-copy, no String allocation)
ByteBuffer buffer = ByteBuffer.allocateDirect(32);
buffer.put("3.14159".getBytes());
float f2 = FastFloat.parseFloatBuffer(buffer, 0, 7);

// Formatting with Ryu algorithm
String s = FastFloat.toString(3.14159f);
```

---

## Installation

### JitPack (Recommended)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastfloat</artifactId>
        <version>v1.1.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastfloat:v1.1.0'
}
```

---

## Features & Keywords

**Core Capabilities:**
- **Native float/double parsing** — 5-12× faster than Java standard library
- **Zero-GC operation** — No garbage collection pauses, ideal for real-time systems
- **SIMD batch operations** — AVX2/AVX-512 accelerated array processing
- **ByteBuffer parsing** — Zero-copy direct memory access
- **Ryu formatting** — Fastest known double-to-string algorithm
- **Dual-mode parsing** — Pure-Java for short strings, native SIMD for long strings
- **Multi-binary runtime dispatch** — Auto-selects optimal CPU instructions

**Search Tags:** `fast float parsing java` · `java parse double performance` · `java float to string fast` · `java ryu float formatting` · `java json number parsing` · `java zero gc parsing` · `java simd float parsing` · `java high performance parsing` · `java native float parser` · `java fast double parser`

---

## API Reference

### Core Parsing

| Method | Description |
|--------|-------------|
| `parseFloat(String)` | Parse float (native, throws on error) |
| `parseDouble(String)` | Parse double (native, throws on error) |
| `parseFloatFast(String, float[])` | Error-code return, no exceptions |
| `parseDoubleFast(String, double[])` | Error-code return, no exceptions |

### Zero-GC Fast Path (v1.1.0+)

| Method | Description |
|--------|-------------|
| `parseFloatZeroGC(String)` | Parse with bit-packed result, **zero allocations** |
| `parseDoubleZeroGC(String)` | Parse with bit-packed result, **zero allocations** |
| `unpackFloat(long)` | Extract float from bit-packed result |
| `unpackError(long)` | Extract error code from bit-packed result |

**Usage:**
```java
long packed = FastFloat.parseFloatZeroGC("3.14159");
if (FastFloat.unpackError(packed) == FastFloat.ERR_OK) {
    float value = FastFloat.unpackFloat(packed);  // Zero GC!
}
```

### ByteBuffer API (v1.1.0+) - Zero Copy

Parse directly from memory without String allocation:

| Method | Description |
|--------|-------------|
| `parseFloatBuffer(ByteBuffer, offset, len)` | Parse from direct ByteBuffer |
| `parseDoubleBuffer(ByteBuffer, offset, len)` | Parse from direct ByteBuffer |
| `parseFloatBatchBuffer(...)` | Batch parse with offsets (one JNI call) |
| `parseDoubleBatchBuffer(...)` | Batch parse with offsets (one JNI call) |

**Usage:**
```java
ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
// ... fill with ASCII float strings ...
float val = FastFloat.parseFloatBuffer(buffer, 0, 7);
```

### Formatting

| Method | Description |
|--------|-------------|
| `toString(float)` | Format float to string |
| `toString(double)` | Format double to string |
| `toString(float, int)` | Format with precision |
| `toString(double, int)` | Format with precision |

### Batch Operations (SIMD)

```java
// Parse 1000 floats at once
String[] inputs = generateStrings(1000);
float[] outputs = new float[1000];
int success = FastFloat.parseFloatBatch(inputs, outputs);
```

### SIMD Batch Math

```java
import fastfloat.FastFloatBatch;

float[] a = {1.0f, 2.0f, 3.0f};
float[] b = {4.0f, 5.0f, 6.0f};
float[] out = new float[3];

FastFloatBatch.add(a, b, out);  // out = {5.0, 7.0, 9.0}
FastFloatBatch.mul(a, b, out);  // out = {4.0, 10.0, 18.0}
FastFloatBatch.fma(a, b, c, out);  // out = a*b + c
```

---

## Performance

| Operation | Java Standard | FastFloat | Speedup |
|-----------|---------------|-----------|---------|
| `parseFloat` | ~50 ns/op | ~3-5 ns/op | **10-16x** |
| `parseDouble` | ~80 ns/op | ~5-8 ns/op | **10-16x** |
| `parseFloatZeroGC` | - | ~3 ns/op | **Zero GC** |
| `parseFloatBuffer` | - | ~2-4 ns/op | **No String alloc** |
| `toString(float)` | ~100 ns/op | ~10-15 ns/op | **6-10x** |
| Batch 1000 ops | ~50 μs | ~3-5 μs | **10-16x** |

*Benchmarks on Intel i7-12700K, JDK 21. Results vary by CPU and input patterns.*

### Why FastFloat is Faster

- **Eisel-Lemire Algorithm**: State-of-the-art float parsing (used in GCC, Chrome, MySQL)
- **SIMD Acceleration**: AVX2/AVX-512 batch operations
- **Zero-GC Path**: ThreadLocal buffers eliminate allocations
- **ByteBuffer API**: Direct memory access without String marshaling
- **Ryu Formatting**: Fastest known double-to-string algorithm
- **Branchless Parsing**: Optimized for modern CPU branch predictors

---

## Project Structure

```
fastfloat/
├── src/main/java/fastfloat/     # Core API
│   ├── FastFloat.java            # Parsing & formatting
│   └── FastFloatBatch.java       # SIMD batch ops
├── native/                         # C++ JNI code
│   ├── fastfloat.h
│   ├── fastfloat.cpp             # Main implementation
│   ├── fastfloat_ryu.cpp         # Ryu formatting algorithm
│   ├── fastfloat_ryu.h
│   └── fastfloat.def             # JNI exports (Windows)
├── examples/
│   ├── 00-basic-usage/           # Hello World demo
│   └── 10-benchmark/             # JMH benchmarks
├── compile.bat                   # Build native DLL (Windows)
├── compile.sh                    # Build native library (Linux/macOS)
└── pom.xml
```

---

## Installation & Building

### Quick Install

**Download pre-built JAR:** See [COMPILE.md](COMPILE.md#installation-options)

**JitPack:**
```xml
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastfloat</artifactId>
    <version>v1.2.0</version>
</dependency>
```

### Building from Source

See [COMPILE.md](COMPILE.md) for detailed build instructions:
- Windows: `compile.bat`
- Linux/macOS: `./compile.sh`
- Cross-platform build options
- Compiler optimization flags

### Run Examples

```bash
cd examples/00-basic-usage
mvn compile exec:java
```

---

## Error Codes

FastFloat uses error codes instead of exceptions for the fast-path API:

```java
public static final int ERR_OK = 0;         // Success
public static final int ERR_EMPTY = 1;      // Empty string
public static final int ERR_INVALID = 2;    // Invalid format
public static final int ERR_OVERFLOW = 3;     // Value too large
public static final int ERR_UNDERFLOW = 4;    // Value too small
```

---

## Integration

### FastJSON

FastFloat provides the numeric backend for FastJSON parsing:

```java
// FastJSON automatically uses FastFloat when available
FastJSON json = new FastJSON();
json.useFastFloatBackend(true);
```

### FastMath

SIMD batch operations feed into FastMath vector operations:

```java
FastMath.setSIMDBackend(FastFloatBatch.class);
```

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.*
