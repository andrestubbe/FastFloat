package fastfloat.benchmark;

import fastfloat.FastFloat;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 2, time = 1)
@Fork(1)
public class JMH_Float {

    private String sampleFloatStr;

    @Setup
    public void setup() {
        sampleFloatStr = "3.141592653589793";
    }

    @Benchmark
    public float benchmarkFastFloatParse() {
        return FastFloat.parseFloat(sampleFloatStr);
    }

    @Benchmark
    public float benchmarkJavaFloatParse() {
        return Float.parseFloat(sampleFloatStr);
    }
}
