# Installation Options

## Option 1: Download Pre-built JAR (Recommended)

The easiest way to use FastFloat is downloading the pre-built JAR with integrated native library:

### GitHub Releases

Download the latest release: **[Releases Page](https://github.com/andrestubbe/FastFloat/releases)**

```bash
# Download JAR with integrated DLL/SO/DYLIB
wget https://github.com/andrestubbe/FastFloat/releases/download/v1.2.0/fastfloat-1.2.0.jar

# Or download JAR + separate native library
wget https://github.com/andrestubbe/FastFloat/releases/download/v1.2.0/fastfloat-1.2.0.jar
wget https://github.com/andrestubbe/FastFloat/releases/download/v1.2.0/fastfloat.dll     # Windows
wget https://github.com/andrestubbe/FastFloat/releases/download/v1.2.0/libfastfloat.so   # Linux
wget https://github.com/andrestubbe/FastFloat/releases/download/v1.2.0/libfastfloat.dylib # macOS
```

### Maven (JitPack)

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
        <version>v1.2.0</version>
    </dependency>
</dependencies>
```

### Gradle (JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastfloat:v1.2.0'
}
```

---

# Building from Source

## Prerequisites

- JDK 17+
- Maven 3.9+
- **Windows:** Visual Studio 2019+ or Build Tools
- **Linux:** GCC 9+ or Clang 10+
- **macOS:** Xcode Command Line Tools

## Build

### Windows

```bash
compile.bat
mvn clean package
```

### Linux/macOS

```bash
chmod +x compile.sh
./compile.sh
mvn clean package
```

The build script auto-detects CPU features (AVX2, AVX-512, FMA3) and compiles with optimal flags (`-O3 -march=native -ffast-math`).

### Cross-Platform Build (All Platforms)

For building native libraries for multiple platforms:

```bash
# Windows (MSVC)
compile.bat

# Linux (GCC/Clang)
./compile.sh

# macOS (Clang)
./compile.sh
```

The native library will be placed in `build/` (Windows: `fastfloat.dll`, Linux: `libfastfloat.so`, macOS: `libfastfloat.dylib`).

## Compiler Optimization Flags

### Windows (MSVC)
```bash
cl /O2 /arch:AVX2 /fp:fast /GL /LTCG fastfloat.cpp
```

Flags explained:
- `/O2` - Maximum optimization
- `/arch:AVX2` - Enable AVX2 instructions
- `/fp:fast` - Fast floating-point model (20-30% speedup)
- `/GL` - Whole program optimization
- `/LTCG` - Link-time code generation

### Linux/macOS (GCC/Clang)
```bash
g++ -O3 -march=native -ffast-math -fPIC -shared -o libfastfloat.so fastfloat.cpp
```

Flags explained:
- `-O3` - Maximum optimization
- `-march=native` - Optimize for host CPU
- `-ffast-math` - Aggressive floating-point optimizations
- `-fPIC` - Position-independent code

## Run Examples

```bash
cd examples/00-basic-usage
mvn compile exec:java
```

## Troubleshooting

### JNI UnsatisfiedLinkError

If you get `UnsatisfiedLinkError`, the native library was not found:

1. Check that the DLL/so/dylib exists in `build/`
2. On Windows, ensure the DLL is in PATH or copy to `C:\Windows\System32`
3. On Linux/macOS, set `LD_LIBRARY_PATH` or `DYLD_LIBRARY_PATH`

### CPU Feature Detection Failed

If AVX2/AVX-512 detection fails, you can force specific optimizations:

```bash
# Force SSE4.2 only (older CPUs)
./compile.sh --sse42

# Force AVX2
./compile.sh --avx2

# Force AVX-512
./compile.sh --avx512
```

## Multi-Binary Runtime Dispatch

For optimal performance across different CPUs, build multiple binaries:

```bash
# Build all variants
./compile.sh --multi

# This creates:
# - build/libfastfloat_sse42.so (baseline)
# - build/libfastfloat_avx2.so (AVX2 optimized)
# - build/libfastfloat_avx512.so (AVX-512 optimized)

# The library auto-selects the best version at runtime based on CPUID
```
