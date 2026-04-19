# FastFloat [ALPHA]

> **Native-accelerated float/double parsing &amp; formatting for Java.**  
> 5-20x faster than standard Java. Zero GC. Zero overhead.

[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Maven](https://img.shields.io/badge/Maven-3.9+-orange.svg)](https://maven.apache.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io)

---

## Quick Start

```java
import fastfloat.FastFloat;

// Parsing - 5-20x faster than Float.parseFloat()
float f = FastFloat.parseFloat("3.14159");
double d = FastFloat.parseDouble("2.718281828459045");

// Formatting
String s = FastFloat.toString(3.14159f);

// Error-code API (no exceptions, fast-path)
float[] result = new float[1];
int err = FastFloat.parseFloatFast("3.14159", result);
if (err == FastFloat.ERR_OK) {
    System.out.println("Parsed: " + result[0]);
}
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
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastfloat:v1.0.0'
}
```

---

## API Reference

### Core Parsing

| Method | Description |
|--------|-------------|
| `parseFloat(String)` | Parse float (native, throws on error) |
| `parseDouble(String)` | Parse double (native, throws on error) |
| `parseFloatFast(String, float[])` | Error-code return, no exceptions |
| `parseDoubleFast(String, double[])` | Error-code return, no exceptions |

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
| parseFloat | ~50 ns/op | ~5 ns/op | **10x** |
| parseDouble | ~80 ns/op | ~8 ns/op | **10x** |
| toString(float) | ~100 ns/op | ~15 ns/op | **6.7x** |
| Batch 1000 ops | ~50 μs | ~5 μs | **10x** |

*Benchmarks on Intel i7-12700K, JDK 21. Your results may vary.*

---

## Project Structure

```
fastfloat/
├── src/main/java/fastfloat/     # Core API
│   ├── FastFloat.java            # Parsing & formatting
│   └── FastFloatBatch.java       # SIMD batch ops
├── native/                         # C++ JNI code
│   ├── fastfloat.h
│   ├── fastfloat.cpp
│   └── fastfloat.def             # JNI exports
├── examples/
│   ├── 00-basic-usage/           # Hello World demo
│   └── 10-benchmark/             # JMH benchmarks
├── compile.bat                   # Build native DLL
└── pom.xml
```

---

## Building from Source

### Prerequisites

- JDK 17+
- Maven 3.9+
- Visual Studio 2019+ (Windows)

### Build

```bash
# Build native DLL (Windows)
compile.bat

# Build JAR
mvn clean package
```

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
