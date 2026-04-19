package fastfloat.example;

import fastfloat.FastFloat;
import fastfloat.FastFloatBatch;

/**
 * Demo class showcasing FastFloat functionality.
 * 
 * Run with: mvn compile exec:java
 */
public class Demo {
    
    public static void main(String[] args) {
        printBanner();
        
        // Check native availability
        System.out.println("Native library available: " + FastFloat.isNativeAvailable());
        System.out.println("Native version: " + FastFloat.getNativeVersion());
        System.out.println();
        
        // Basic parsing demo
        demoParsing();
        
        // Fast-path API demo
        demoFastPath();
        
        // Formatting demo
        demoFormatting();
        
        // Batch operations demo
        demoBatchOps();
        
        // Performance comparison
        demoPerformance();
        
        System.out.println("\n✅ Demo complete!");
    }
    
    private static void demoParsing() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Basic Parsing");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        float f = FastFloat.parseFloat("3.14159");
        double d = FastFloat.parseDouble("2.718281828459045");
        
        System.out.println("parseFloat(\"3.14159\") = " + f);
        System.out.println("parseDouble(\"2.718281828459045\") = " + d);
        System.out.println();
    }
    
    private static void demoFastPath() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Fast-Path API (No Exceptions)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        float[] result = new float[1];
        
        String[] inputs = {
            "3.14159",
            "invalid",
            "",
            "1e10"
        };
        
        for (String input : inputs) {
            int err = FastFloat.parseFloatFast(input, result);
            System.out.printf("parseFloatFast(\"%s\") = ", input);
            
            switch (err) {
                case FastFloat.ERR_OK:
                    System.out.println(result[0] + " ✓");
                    break;
                case FastFloat.ERR_EMPTY:
                    System.out.println("ERR_EMPTY ✗");
                    break;
                case FastFloat.ERR_INVALID:
                    System.out.println("ERR_INVALID ✗");
                    break;
                case FastFloat.ERR_OVERFLOW:
                    System.out.println("ERR_OVERFLOW ✗");
                    break;
                default:
                    System.out.println("Unknown error");
            }
        }
        System.out.println();
    }
    
    private static void demoFormatting() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Formatting");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        float pi = 3.14159f;
        double e = 2.718281828459045;
        
        System.out.println("toString(3.14159f) = " + FastFloat.toString(pi));
        System.out.println("toString(2.71828...) = " + FastFloat.toString(e));
        System.out.println("toString(pi, 2) = " + FastFloat.toString(pi, 2));
        System.out.println("toString(e, 4) = " + FastFloat.toString(e, 4));
        System.out.println();
    }
    
    private static void demoBatchOps() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("SIMD Batch Operations");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        float[] a = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        float[] b = {5.0f, 4.0f, 3.0f, 2.0f, 1.0f};
        float[] out = new float[5];
        
        FastFloatBatch.add(a, b, out);
        System.out.print("add(a, b) = ");
        printArray(out);
        
        FastFloatBatch.mul(a, b, out);
        System.out.print("mul(a, b) = ");
        printArray(out);
        
        FastFloatBatch.scale(a, 10.0f, out);
        System.out.print("scale(a, 10) = ");
        printArray(out);
        System.out.println();
    }
    
    private static void demoPerformance() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Performance Comparison");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        final int ITERATIONS = 1_000_000;
        String testStr = "3.14159";
        
        // Warmup
        for (int i = 0; i < 10000; i++) {
            FastFloat.parseFloat(testStr);
            Float.parseFloat(testStr);
        }
        
        // FastFloat benchmark
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            FastFloat.parseFloat(testStr);
        }
        long fastFloatTime = System.nanoTime() - start;
        
        // Java standard benchmark
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            Float.parseFloat(testStr);
        }
        long javaTime = System.nanoTime() - start;
        
        double fastFloatNs = (double) fastFloatTime / ITERATIONS;
        double javaNs = (double) javaTime / ITERATIONS;
        double speedup = javaNs / fastFloatNs;
        
        System.out.printf("%,d iterations:%n", ITERATIONS);
        System.out.printf("Java Float.parseFloat(): %.2f ns/op%n", javaNs);
        System.out.printf("FastFloat.parseFloat():  %.2f ns/op%n", fastFloatNs);
        System.out.printf("Speedup: %.2fx faster%n", speedup);
    }
    
    private static void printArray(float[] arr) {
        System.out.print("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) System.out.print(", ");
            System.out.printf("%.1f", arr[i]);
        }
        System.out.println("]");
    }
    
    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║         FastFloat Demo v" + FastFloat.VERSION + "              ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
    }
}
