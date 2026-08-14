# FastFloat API Reference Manual

`FastFloat` provides high-performance native float/double parsing and formatting for Java, optimized with AVX2 SIMD instructions, zero-GC bit-packed results, and the Ryu float-formatting algorithm.

---

## 1. Core Parsing API

### `parseFloat`
Parses a single `float` value from the provided String with automatic dual-mode strategy selection.

```java
float val = FastFloat.parseFloat("3.14159265");
```

* **Dual-Mode Strategy**:
  * Strings ≤20 characters: Pure-Java Eisel-Lemire fast-path (avoids JNI transition overhead).
  * Strings >20 characters: Native AVX2 SIMD C++ parser (maximum throughput).
* **Return**: `float` parsed value.
* **Throws**: `NumberFormatException` if string contains invalid characters, is empty, or overflows.

---

### `parseDouble(String s)`
Parses a single `double` value from the provided String with automatic dual-mode strategy.

```java
double val = FastFloat.parseDouble("2.718281828459045");
```

---

### `parseFloatNative(String s)` & `parseDoubleNative(String s)`
Forces execution through the native C++ AVX2 SIMD parser, bypassing the pure-Java short string optimization.

```java
float val = FastFloat.parseFloatNative("3.14159265358979");
```

---

## 2. Zero-GC & Fast-Path Parsing API

### `parseFloatZeroGC`
Fastest single-value parsing method. Returns a bit-packed `long` primitive containing both the error code and parsed float bits, completely avoiding JVM GC allocations and Exception throwing.

```java
long packed = FastFloat.parseFloatZeroGC("3.14159");
int err = FastFloat.unpackError(packed);
if (err == FastFloat.ERR_OK) {
    float value = FastFloat.unpackFloat(packed);
}
```

* **Packed Format**: High 16 bits = Error Code, Low 48 bits = Float Bits.
* **Helper Methods**:
  * `unpackError(long packed)`: Returns error status code (`ERR_OK`, `ERR_INVALID`, `ERR_EMPTY`, etc.).
  * `unpackFloat(long packed)`: Extracts `float` value. Only valid if `unpackError(packed) == ERR_OK`.

---

### `parseFloatFast(String s, float[] out)`
Parses float value without throwing exceptions, writing the result to `out[0]` and returning an integer error code.

```java
float[] out = new float[1];
int status = FastFloat.parseFloatFast("3.14159", out);
if (status == FastFloat.ERR_OK) {
    float value = out[0];
}
```

---

## 3. Off-Heap ByteBuffer API (Zero-Marshaling)

### `parseFloatBuffer(ByteBuffer buffer, int offset, int length)`
Parses a float directly from an off-heap direct `ByteBuffer` region containing ASCII bytes without creating a `java.lang.String` object.

```java
ByteBuffer buf = ByteBuffer.allocateDirect(64);
buf.put("3.14159".getBytes(StandardCharsets.US_ASCII));
float f = FastFloat.parseFloatBuffer(buf, 0, 7);
```

---

### `parseFloatBatchBuffer(ByteBuffer buffer, int[] offsets, int[] lengths, float[] outputs)`
Batch-parses multiple float values from a single `ByteBuffer` in a single JNI call.

```java
int count = FastFloat.parseFloatBatchBuffer(buffer, offsets, lengths, outputs);
```

---

## 4. Ryu Formatting API

### `toString`
Formats a float or double into its shortest exact string representation using the native Ryu algorithm.

```java
String s1 = FastFloat.toString(3.14159f);        // "3.14159"
String s2 = FastFloat.toString(2.718281828459045); // "2.718281828459045"
```

---

### `toString(float v, int precision)`
Formats float or double with explicit decimal precision control.

```java
String formatted = FastFloat.toString(3.14159265f, 2); // "3.14"
```

---

## 5. Status & Error Codes

| Error Constant | Value | Description |
|----------------|-------|-------------|
| `ERR_OK` | `0` | Parsing succeeded without errors. |
| `ERR_EMPTY` | `1` | Input string or buffer slice was empty. |
| `ERR_INVALID` | `2` | Input contained non-numeric invalid characters. |
| `ERR_OVERFLOW` | `3` | Numerical value exceeded maximum float range. |
| `ERR_UNDERFLOW` | `4` | Numerical value underflowed float precision. |

---

## 6. CPU & Memory Execution Guarantees

* **AVX2 Acceleration**: Detected via CPUID. Uses 256-bit SIMD registers (`_mm256_cmpeq_epi8`, `_mm256_loadu_si256`).
* **Zero Memory Allocations**: ByteBuffer and ZeroGC APIs allocate 0 bytes on the Java heap.
* **Thread Safety**: All static native methods are fully thread-safe and reentrant.
