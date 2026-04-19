package fastfloat.benchmark;

import fastfloat.FastFloat;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for FastFloat parsing vs Java standard library.
 * 
 * Run: java -jar target/benchmarks.jar
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ParseBenchmark {
    
    private String floatStr;
    private String doubleStr;
    private float[] result;
    
    @Setup
    public void setup() {
        floatStr = "3.14159";
        doubleStr = "2.718281828459045";
        result = new float[1];
    }
    
    // ========== Float Parsing ==========
    
    @Benchmark
    public float javaParseFloat() {
        return Float.parseFloat(floatStr);
    }
    
    @Benchmark
    public float fastFloatParse() {
        return FastFloat.parseFloat(floatStr);
    }
    
    @Benchmark
    public int fastFloatParseFast() {
        return FastFloat.parseFloatFast(floatStr, result);
    }
    
    // ========== Double Parsing ==========
    
    @Benchmark
    public double javaParseDouble() {
        return Double.parseDouble(doubleStr);
    }
    
    @Benchmark
    public double fastFloatParseDouble() {
        return FastFloat.parseDouble(doubleStr);
    }
    
    // ========== Formatting ==========
    
    @Benchmark
    public String javaFloatToString() {
        return Float.toString(3.14159f);
    }
    
    @Benchmark
    public String fastFloatToString() {
        return FastFloat.toString(3.14159f);
    }
    
    // ========== Main ==========
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ParseBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
