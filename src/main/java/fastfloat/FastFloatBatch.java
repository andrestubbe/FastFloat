package fastfloat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * FastFloatBatch - SIMD-accelerated batch operations for float arrays.
 * 
 * <p>Zero-GC operations for high-throughput numerical processing.</p>
 * 
 * @author FastJava Team
 * @version 1.0.0
 */
public final class FastFloatBatch {
    
    // SIMD alignment (64 bytes for AVX-512)
    public static final int SIMD_ALIGNMENT = 64;
    
    // === Batch Arithmetic (SIMD) ===
    
    /**
     * Element-wise addition: out[i] = a[i] + b[i]
     * Uses SIMD acceleration (AVX2/AVX-512) when available.
     * 
     * @param a first input array
     * @param b second input array
     * @param out output array (must be same length as inputs)
     */
    public static native void add(float[] a, float[] b, float[] out);
    
    /**
     * Element-wise multiplication: out[i] = a[i] * b[i]
     * Uses SIMD acceleration (AVX2/AVX-512) when available.
     * 
     * @param a first input array
     * @param b second input array
     * @param out output array (must be same length as inputs)
     */
    public static native void mul(float[] a, float[] b, float[] out);
    
    /**
     * Element-wise subtraction: out[i] = a[i] - b[i]
     * Uses SIMD acceleration (AVX2/AVX-512) when available.
     * 
     * @param a first input array
     * @param b second input array
     * @param out output array (must be same length as inputs)
     */
    public static native void sub(float[] a, float[] b, float[] out);
    
    /**
     * Element-wise division: out[i] = a[i] / b[i]
     * Uses SIMD acceleration (AVX2/AVX-512) when available.
     * 
     * @param a first input array
     * @param b second input array
     * @param out output array (must be same length as inputs)
     */
    public static native void div(float[] a, float[] b, float[] out);
    
    /**
     * Fused multiply-add: out[i] = a[i] * b[i] + c[i]
     * Uses SIMD FMA instructions when available.
     * 
     * @param a first input array
     * @param b second input array
     * @param c third input array
     * @param out output array (must be same length as inputs)
     */
    public static native void fma(float[] a, float[] b, float[] c, float[] out);
    
    /**
     * Scalar multiply: out[i] = a[i] * scalar
     * Uses SIMD acceleration when available.
     * 
     * @param a input array
     * @param scalar value to multiply
     * @param out output array (must be same length as input)
     */
    public static native void scale(float[] a, float scalar, float[] out);
    
    // === ByteBuffer Conversion (Zero-GC) ===
    
    /**
     * Convert ByteBuffer to float array (zero-copy where possible).
     * ByteBuffer must be native/direct and aligned to 4 bytes.
     * 
     * @param buffer source ByteBuffer (must be direct)
     * @param offset byte offset in buffer
     * @param out destination float array
     * @param count number of floats to read
     */
    public static native void bufferToFloats(ByteBuffer buffer, int offset, float[] out, int count);
    
    /**
     * Convert float array to ByteBuffer (zero-copy where possible).
     * ByteBuffer must be native/direct and aligned to 4 bytes.
     * 
     * @param src source float array
     * @param buffer destination ByteBuffer (must be direct)
     * @param offset byte offset in buffer
     * @param count number of floats to write
     */
    public static native void floatsToBuffer(float[] src, ByteBuffer buffer, int offset, int count);
    
    // === Memory Alignment ===
    
    /**
     * Allocate aligned float array for SIMD operations.
     * Array is guaranteed to be 64-byte aligned (for AVX-512).
     * 
     * @param size number of floats
     * @return aligned float array
     */
    public static native float[] allocateAligned(int size);
    
    /**
     * Check if array is properly aligned for SIMD.
     * 
     * @param array float array to check
     * @return true if 64-byte aligned
     */
    public static native boolean isAligned(float[] array);
    
    // === Java Helpers ===
    
    /**
     * Create a direct ByteBuffer optimized for FastFloat operations.
     * Buffer will be properly aligned and native-ordered.
     * 
     * @param capacity size in bytes
     * @return direct ByteBuffer
     */
    public static ByteBuffer createDirectBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity)
                .order(ByteOrder.nativeOrder());
    }
    
    /**
     * Calculate aligned size for SIMD operations.
     * Rounds up to next multiple of 16 floats (64 bytes).
     * 
     * @param minSize minimum number of floats needed
     * @return aligned size suitable for SIMD
     */
    public static int alignedSize(int minSize) {
        return ((minSize + 15) / 16) * 16;
    }
}
