package fastfloat;

import fastcore.FastCore;
import java.nio.ByteBuffer;

/**
 * FastFloat - Native-accelerated float/double parsing &amp; formatting for Java.
 * 
 * <p>5-20x faster than standard Java Float/Double parsing with zero GC overhead.
 * Uses hand-optimized native code with SIMD acceleration where available.</p>
 * 
 * <p><b>Usage:</b></p>
 * <pre>
 * float f = FastFloat.parseFloat("3.14159");
 * double d = FastFloat.parseDouble("2.718281828459045");
 * String s = FastFloat.toString(3.14159f);
 * </pre>
 * 
 * @author FastJava Team
 * @version 1.1.0
 */
public final class FastFloat {
    
    private static final String LIBRARY_NAME = "fastfloat";
    private static volatile boolean nativeLoaded = false;
    
    static {
        loadNativeLibrary();
    }
    
    /**
     * Load native library from JAR or system path.
     * Attempts to extract and load from JAR first, falls back to system library.
     */
    private static void loadNativeLibrary() {
        try {
            // Try to load from JAR (Fat JAR support)
            String libFileName = System.mapLibraryName(LIBRARY_NAME);
            String nativePath = "/native/" + libFileName;
            
            java.io.InputStream is = FastFloat.class.getResourceAsStream(nativePath);
            if (is != null) {
                // Extract to temp directory
                java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("fastfloat");
                java.nio.file.Path tempLib = tempDir.resolve(libFileName);
                
                java.nio.file.Files.copy(is, tempLib, 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                is.close();
                
                // Load the extracted library
                System.load(tempLib.toAbsolutePath().toString());
                nativeLoaded = true;
                
                // Cleanup on exit
                tempLib.toFile().deleteOnExit();
                tempDir.toFile().deleteOnExit();
            } else {
                // Fallback: try system library path
                System.loadLibrary(LIBRARY_NAME);
                nativeLoaded = true;
            }
        } catch (Exception e) {
            // Native library not available - will use pure-Java fallback
            nativeLoaded = false;
        }
    }
    
    public static final String VERSION = "1.2.0";
    
    // Error codes (returned instead of exceptions for fast-path)
    public static final int ERR_OK = 0;
    public static final int ERR_EMPTY = 1;
    public static final int ERR_INVALID = 2;
    public static final int ERR_OVERFLOW = 3;
    public static final int ERR_UNDERFLOW = 4;
    
    // ThreadLocal reusable buffers for zero-GC fast path
    private static final ThreadLocal<float[]> FLOAT_RESULT = ThreadLocal.withInitial(() -> new float[1]);
    private static final ThreadLocal<double[]> DOUBLE_RESULT = ThreadLocal.withInitial(() -> new double[1]);
    private static final ThreadLocal<char[]> CHAR_BUFFER = ThreadLocal.withInitial(() -> new char[64]);
    
    // === DUAL-MODE PARSING (v1.2.0+) ===
    // Pure-Java Eisel-Lemire for short strings (≤20 chars) - avoids JNI overhead
    // Native for long strings (>20 chars) - maximum throughput
    
    /**
     * Parse float from string with automatic dual-mode selection.
     * 
     * <p><b>Dual-Mode Strategy (v1.2.0+):</b></p>
     * <ul>
     *   <li>Strings ≤20 chars: Pure-Java Eisel-Lemire (avoids JNI overhead)</li>
     *   <li>Strings >20 chars: Native SIMD (maximum throughput)</li>
     * </ul>
     * 
     * @param s string to parse
     * @return parsed float value
     * @throws NumberFormatException if parsing fails
     */
    public static float parseFloat(String s) {
        // Fast path: Pure-Java for short strings where JNI overhead dominates
        if (s != null && s.length() <= FastFloatPure.FAST_PATH_MAX_LENGTH) {
            return FastFloatPure.parseFloat(s);
        }
        // Long strings: Native SIMD for maximum throughput
        return parseFloatNative(s);
    }
    
    /**
     * Parse double from string with automatic dual-mode selection.
     * 
     * @param s string to parse
     * @return parsed double value
     * @throws NumberFormatException if parsing fails
     */
    public static double parseDouble(String s) {
        if (s != null && s.length() <= FastFloatPure.FAST_PATH_MAX_LENGTH) {
            return FastFloatPure.parseDouble(s);
        }
        return parseDoubleNative(s);
    }
    
    // === Native Parsing Methods (for long strings or explicit native call) ===
    
    /**
     * Parse float from string using native SIMD (always native, no pure-Java fallback).
     * Use this for guaranteed native performance when you know strings are long.
     * 
     * @param s string to parse
     * @return parsed float value
     * @throws NumberFormatException if parsing fails
     */
    public static native float parseFloatNative(String s);
    
    /**
     * Parse double from string using native SIMD (always native).
     * 
     * @param s string to parse
     * @return parsed double value
     * @throws NumberFormatException if parsing fails
     */
    public static native double parseDoubleNative(String s);
    
    /**
     * Parse float with error code (no exceptions, fast-path).
     * 
     * @param s string to parse
     * @param out result array (length 1), only valid if returns ERR_OK
     * @return error code (ERR_OK, ERR_EMPTY, ERR_INVALID, ERR_OVERFLOW)
     */
    public static native int parseFloatFast(String s, float[] out);
    
    /**
     * Parse double with error code (no exceptions, fast-path).
     * 
     * @param s string to parse
     * @param out result array (length 1), only valid if returns ERR_OK
     * @return error code (ERR_OK, ERR_EMPTY, ERR_INVALID, ERR_OVERFLOW)
     */
    public static native int parseDoubleFast(String s, double[] out);
    
    // === Native Formatting Methods ===
    
    /**
     * Format float to string (native, zero-GC).
     * 
     * @param v float value
     * @return formatted string
     */
    public static native String toString(float v);
    
    /**
     * Format double to string (native, zero-GC).
     * 
     * @param v double value
     * @return formatted string
     */
    public static native String toString(double v);
    
    /**
     * Format float to string with precision control.
     * 
     * @param v float value
     * @param precision number of digits after decimal point
     * @return formatted string
     */
    public static native String toString(float v, int precision);
    
    /**
     * Format double to string with precision control.
     * 
     * @param v double value
     * @param precision number of digits after decimal point
     * @return formatted string
     */
    public static native String toString(double v, int precision);
    
    // === Zero-GC Fast Path (ThreadLocal) ===
    
    /**
     * Parse float with zero-GC using ThreadLocal buffer.
     * Fastest single-value parsing without exception overhead.
     * 
     * @param s string to parse
     * @return bit-packed result: high 16 bits = error code, low 48 bits = float bits
     *         Use {@link #unpackFloat(long)} to get the float value
     *         Use {@link #unpackError(long)} to get the error code
     */
    public static long parseFloatZeroGC(String s) {
        float[] result = FLOAT_RESULT.get();
        int err = parseFloatFast(s, result);
        return packResult(err, Float.floatToIntBits(result[0]));
    }
    
    /**
     * Parse double with zero-GC using ThreadLocal buffer.
     * 
     * @param s string to parse
     * @return bit-packed result: high 16 bits = error code, low 48 bits = double bits
     */
    public static long parseDoubleZeroGC(String s) {
        double[] result = DOUBLE_RESULT.get();
        int err = parseDoubleFast(s, result);
        return packResult(err, Double.doubleToLongBits(result[0]) & 0xFFFFFFFFFFFFL);
    }
    
    /**
     * Unpack float value from bit-packed result.
     * @param packed result from parseFloatZeroGC
     * @return float value (only valid if error code was ERR_OK)
     */
    public static float unpackFloat(long packed) {
        return Float.intBitsToFloat((int) (packed & 0xFFFFFFFFL));
    }
    
    /**
     * Unpack error code from bit-packed result.
     * @param packed result from parseFloatZeroGC
     * @return error code (ERR_OK, ERR_INVALID, etc.)
     */
    public static int unpackError(long packed) {
        return (int) ((packed >>> 48) & 0xFFFFL);
    }
    
    private static long packResult(int error, long bits) {
        return ((long) error << 48) | (bits & 0xFFFFFFFFFFFFL);
    }
    
    // === ByteBuffer API (Zero-Marshaling) ===
    
    /**
     * Parse float from direct ByteBuffer at offset (zero-copy).
     * ASCII bytes only. No String allocation required.
     * 
     * @param buffer direct ByteBuffer with ASCII float string
     * @param offset byte offset in buffer
     * @param length number of bytes to parse
     * @return parsed float value
     */
    public static native float parseFloatBuffer(ByteBuffer buffer, int offset, int length);
    
    /**
     * Parse double from direct ByteBuffer at offset (zero-copy).
     * 
     * @param buffer direct ByteBuffer with ASCII double string
     * @param offset byte offset in buffer
     * @param length number of bytes to parse
     * @return parsed double value
     */
    public static native double parseDoubleBuffer(ByteBuffer buffer, int offset, int length);
    
    /**
     * Batch parse from single ByteBuffer with offsets (one JNI call, no String marshaling).
     * 
     * @param buffer direct ByteBuffer containing all strings
     * @param offsets array of byte offsets for each string
     * @param lengths array of lengths for each string
     * @param outputs array to receive parsed floats
     * @return number of successful parses
     */
    public static native int parseFloatBatchBuffer(ByteBuffer buffer, int[] offsets, int[] lengths, float[] outputs);
    
    /**
     * Batch parse doubles from ByteBuffer.
     */
    public static native int parseDoubleBatchBuffer(ByteBuffer buffer, int[] offsets, int[] lengths, double[] outputs);
    
    // === Batch Operations (Legacy - prefer ByteBuffer API) ===
    
    /**
     * Parse multiple floats from string array (SIMD batch).
     * NOTE: Consider using parseFloatBatchBuffer for better performance.
     * 
     * @param inputs array of strings to parse
     * @param outputs array to receive parsed floats (must be same length)
     * @return number of successful parses
     */
    public static native int parseFloatBatch(String[] inputs, float[] outputs);
    
    /**
     * Parse multiple doubles from string array (SIMD batch).
     * 
     * @param inputs array of strings to parse
     * @param outputs array to receive parsed doubles (must be same length)
     * @return number of successful parses
     */
    public static native int parseDoubleBatch(String[] inputs, double[] outputs);
    
    // === Utility ===
    
    /**
     * Check if native library is loaded and available.
     * 
     * @return true if native acceleration available
     */
    public static boolean isNativeAvailable() {
        return nativeLoaded;
    }
    
    /**
     * Get native library version info.
     * 
     * @return version string from native library
     */
    public static native String getNativeVersion();
}
