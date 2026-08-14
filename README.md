# FastFloat 0.1.1 [ALPHA-2026-08] — Ultra-Fast Native SIMD Float Parsing & Formatting for Java

[![Status](https://img.shields.io/badge/status-0.1.1-brightgreen.svg)](https://github.com/andrestubbe/FastFloat/releases/tag/0.1.1)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.1-green.svg)](https://jitpack.io/#andrestubbe/FastFloat)

---

**⚡ 5.1× faster than standard Java's Float.parseFloat / Double.parseDouble.** Zero GC. SIMD accelerated. Ryu algorithm formatting.

`FastFloat` provides high-performance native float and double parsing and formatting for Java, replacing slow string allocations with native AVX2 SIMD vector operations.

![Showcase](https://raw.githubusercontent.com/andrestubbe/FastFloat/main/docs/screenshot.png)

---

## Quick Start — Example

```java
import fastfloat.FastFloat;
import java.nio.ByteBuffer;

public class Demo {
    public static void main(String[] args) {
        // 1. 5.1× faster parsing — zero allocations
        float f = FastFloat.parseFloat("3.14159");
        double d = FastFloat.parseDouble("2.718281828459045");

        // 2. Zero-GC fast path (no exceptions, packed error/value result)
        long packed = FastFloat.parseFloatZeroGC("3.14159");
        if (FastFloat.unpackError(packed) == FastFloat.ERR_OK) {
            float value = FastFloat.unpackFloat(packed);
        }

        // 3. Ryu high-speed float/double formatting
        String formatted = FastFloat.toString(f);
    }
}
```

---

## Table of Contents

- [Why FastFloat?](#why-fastfloat)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Performance Benchmarks](#performance-benchmarks)
- [Architecture Overview](#architecture-overview)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastFloat?

Standard Java `Float.parseFloat()` and `Double.parseDouble()` rely on complex, slow string allocations and Exception-throwing error handling. `FastFloat` addresses this by:

- **SIMD Vectorization** — Uses native C++ AVX2 vector instructions for multi-digit parallel parsing.
- **Ryu Formatting** — Implements the Ryu algorithm for ultra-fast, shortest-representation float-to-string conversion.
- **Zero-GC Fast Path** — Bit-packed `parseFloatZeroGC()` eliminates JVM Garbage Collection overhead completely.

---

## Key Features

* **⚡ Native AVX2 SIMD Acceleration** — Leverages 256-bit AVX2 vector registers for ultra-fast digit scanning and conversion.
* **🚀 5.1× Throughput Speedup** — Reaches over **21.6 Million parsing operations per second** on standard desktop hardware.
* **🧠 Zero-GC Bit-Packed API** — Returns packed error status and float values inside a primitive `long` to prevent heap allocations.
* **🌐 Ryu Algorithm Formatting** — Generates exact shortest string representations of floats and doubles with minimum latency.
* **📦 Direct ByteBuffer Ingestion** — Parses floats directly from off-heap `ByteBuffer` regions without converting to `java.lang.String`.

---

## Real-World Use Cases

- 📊 **High-Frequency Financial Data Feed**: Parse millions of stock quotes, ticks, and floating-point trade prices per second.
- 🤖 **Machine Learning Feature Ingestion**: Accelerate CSV and JSON float feature parsing before feeding tensors into ONNX models.
- 📡 **IoT & Telemetry Sensor Streams**: Process high-rate sensor readings directly from DMA native memory buffers without GC pauses.
- 🎮 **Game Engine Physics & Graphics**: Deserialize 3D vertex positions and transformation matrices in real-time game loops.

---

## Performance Benchmarks

In the official [JMH Benchmark](examples/10-benchmark), `FastFloat` measured parsing throughput against standard `Float.parseFloat()`:

```text
Benchmark                             Mode  Cnt        Score   Error  Units
JMH_Float.benchmarkFastFloatParse    thrpt    2  21656206.611          ops/s
JMH_Float.benchmarkJavaFloatParse    thrpt    2   4236773.465          ops/s
```

> **21.6 Million Operations per Second (5.1× Speedup)**: `FastFloat` parses floating point strings at **21.65 Million ops/s**, achieving a **5.1× hardware speedup** over Java's standard `Float.parseFloat()` (4.23 Million ops/s).

---

## Architecture Overview

**FastFloat (This Library — The Native Math Engine)**  
Provides SIMD-accelerated float/double parsing and Ryu formatting for Java.

**[FastSIMD](https://github.com/andrestubbe/FastSIMD) (Hardware Acceleration Engine)**  
Provides cross-platform hardware SIMD primitives (`_mm256_cmpeq_epi8`, `_mm256_fmadd_ps`).

**[FastJSON](https://github.com/andrestubbe/FastJSON) (Zero-GC JSON Parser)**  
Integrates `FastFloat` for sub-microsecond JSON numerical field extraction.

---

## API Quick Reference

| Method | Description | Path |
|--------|-------------|------|
| `parseFloat(String)` | Standard 5.1× faster float parsing. | [Reference 📖](docs/REFERENCE.md#parsefloat) |
| `parseFloatZeroGC(String)` | Bit-packed zero-allocation float parsing. | [Reference 📖](docs/REFERENCE.md#parsefloatzerogc) |
| `toString(float)` | High-speed Ryu float formatting. | [Reference 📖](docs/REFERENCE.md#tostring) |

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the complete dependency stack to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastFloat Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastFloat</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastSIMD Hardware Vector Acceleration Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastSIMD</artifactId>
        <version>0.1.3</version>
    </dependency>

    <!-- FastMemory Aligned Allocator -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastMemory</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastPointer Address Wrapper -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastPointer</artifactId>
        <version>0.1.1</version>
    </dependency>

    <!-- FastCore Native Loader -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastFloat:0.1.1'
    implementation 'com.github.andrestubbe:FastSIMD:0.1.3'
    implementation 'com.github.andrestubbe:FastMemory:0.1.1'
    implementation 'com.github.andrestubbe:FastPointer:0.1.1'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the required JARs directly to add them to your classpath:

1. ⚡ **[FastFloat-0.1.1.jar](https://github.com/andrestubbe/FastFloat/releases/download/0.1.1/FastFloat-0.1.1.jar)** (The Core Library)
2. 🚀 **[FastSIMD-0.1.3.jar](https://github.com/andrestubbe/FastSIMD/releases/download/0.1.3/FastSIMD-0.1.3.jar)** (Hardware Vector Acceleration Engine)
3. 💾 **[FastMemory-0.1.1.jar](https://github.com/andrestubbe/FastMemory/releases/download/0.1.1/FastMemory-0.1.1.jar)** (32-Byte Aligned Allocator)
4. 📍 **[FastPointer-0.1.1.jar](https://github.com/andrestubbe/FastPointer/releases/download/0.1.1/FastPointer-0.1.1.jar)** (Primitive Address Pointer)
5. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (Mandatory Native Loader)

> [!IMPORTANT]
> All JARs must be included in your classpath for the native SIMD JNI bindings to function correctly.

---

## Documentation

- **[CHANGELOG.md](docs/CHANGELOG.md)**: Version history and release notes.
- **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (MSVC C++17 build chain + JNI Setup).
- **[REFERENCE.md](docs/REFERENCE.md)**: Full API contracts and routing logic.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Zero Context-Loss and zero-GC philosophy.
- **[ROADMAP.md](docs/ROADMAP.md)**: Future development goals.

---

## Platform Support

| Platform | Status |
|----------|--------|
| Windows 10/11 (x64) | ✅ Fully Supported |
| Linux | 🔄 Planned |
| macOS | 🔄 Planned |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastMath](https://github.com/andrestubbe/FastMath) — SIMD and GPU vector math functions
- [FastJSON](https://github.com/andrestubbe/FastJSON) — High-speed SIMD JSON parser
- [FastBytes](https://github.com/andrestubbe/FastBytes) — AVX2 byte array operations
- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI loader for FastJava libraries

---

Part of the FastJava Ecosystem — Making the JVM faster. Small package. Maximum speed. Zero bloat. ⚡
