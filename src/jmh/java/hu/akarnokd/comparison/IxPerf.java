package hu.akarnokd.comparison;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import com.google.common.collect.FluentIterable;

import ix.*;
import reactor.core.publisher.Flux;
import rsc.publisher.Px;
import rx.Observable;

/**
 * Example benchmark. Run from command line as
 * <br>
 * gradle jmh -Pjmh='IxPerf'
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class IxPerf {

    @Param({"1", "10", "100", "1000", "10000", "100000", "1000000"})
    public int count;

    Observable<Integer> rangeRx;
    Px<Integer> rangeRsc;
    Flux<Integer> rangeFx;
    Ix<Integer> rangeIx;
    FluentIterable<Integer> rangeGx;

    Observable<Integer> takeRx;
    Px<Integer> takeRsc;
    Flux<Integer> takeFx;
    Ix<Integer> takeIx;
    FluentIterable<Integer> takeGx;

    Observable<Integer> flatMapRx;
    Px<Integer> flatMapRsc;
    Flux<Integer> flatMapFx;
    Ix<Integer> flatMapIx;
    FluentIterable<Integer> flatMapGx;

    Observable<Integer> concatMapRx;
    Px<Integer> concatMapRsc;
    Flux<Integer> concatMapFx;
    Ix<Integer> concatMapIx;
    FluentIterable<Integer> concatMapGx;

    Observable<Integer> flatMapXRangeRx;
    Px<Integer> flatMapXRangeRsc;
    Flux<Integer> flatMapXRangeFx;
    Ix<Integer> flatMapXRangeIx;
    FluentIterable<Integer> flatMapXRangeGx;

    Observable<Integer> concatMapXRangeRx;
    Px<Integer> concatMapXRangeRsc;
    Flux<Integer> concatMapXRangeFx;
    Ix<Integer> concatMapXRangeIx;
    FluentIterable<Integer> concatMapXRangeGx;


    @Setup
    public void setup() {
        Integer[] array = new Integer[count];
        Arrays.fill(array, 777);

        rangeRx = Observable.from(array);
        rangeRsc = Px.fromArray(array);
        rangeFx = Flux.fromArray(array);
        rangeIx = Ix.fromArray(array);
        rangeGx = FluentIterable.from(array);

        int half = Math.max(1, count >> 1);
        takeRx = rangeRx.take(half);
        takeRsc = rangeRsc.take(half);
        takeFx = rangeFx.take(half);
        takeIx = rangeIx.take(half);
        takeGx = rangeGx.limit(half);

        flatMapRx = rangeRx.flatMap(Observable::just);
        flatMapRsc = rangeRsc.flatMap(Px::just);
        flatMapFx = rangeFx.flatMap(Flux::just);
        flatMapIx = rangeIx.flatMap(Ix::just);
        flatMapGx = rangeGx.transformAndConcat(v -> FluentIterable.from(new Integer[] { v }));  // no flatMap and just in Guava...

        concatMapRx = rangeRx.concatMap(Observable::just);
        concatMapRsc = rangeRsc.concatMap(Px::just);
        concatMapFx = rangeFx.concatMap(Flux::just);
        concatMapIx = Ix.concat(rangeIx.map(Ix::just)); // Ix doesn't have concatMap...
        concatMapGx = rangeGx.transformAndConcat(v -> FluentIterable.from(new Integer[] { v }));  // no just() in Guava...

        int inner = 1_000_000 / count;

        Integer[] array2 = new Integer[inner];
        Arrays.fill(array2, 888);

        flatMapXRangeRx = rangeRx.flatMap(v -> Observable.from(array2));
        flatMapXRangeRsc = rangeRsc.flatMap(v -> Px.fromArray(array2));
        flatMapXRangeFx = rangeFx.flatMap(v -> Flux.fromArray(array2));
        flatMapXRangeIx = rangeIx.flatMap(v -> Ix.fromArray(array2));
        flatMapXRangeGx = rangeGx.transformAndConcat(v -> FluentIterable.from(array2)); // no flatMap in Guava...

        concatMapXRangeRx = rangeRx.concatMap(v -> Observable.from(array2));
        concatMapXRangeRsc = rangeRsc.concatMap(v -> Px.fromArray(array2));
        concatMapXRangeFx = rangeFx.concatMap(v -> Flux.fromArray(array2));
        concatMapXRangeIx = Ix.concat(rangeIx.map(v -> Ix.fromArray(array2)));  // Ix doesn't have concatMap...
        concatMapXRangeGx = rangeGx.transformAndConcat(v -> FluentIterable.from(array2));

    }

//    @Benchmark
    public void rangeRx(Blackhole bh) {
        rangeRx.subscribe(new LatchedObserver<>(bh));
    }

    @Benchmark
    public void takeRx(Blackhole bh) {
        takeRx.subscribe(new LatchedObserver<>(bh));
    }

//    @Benchmark
    public void flatMapRx(Blackhole bh) {
        flatMapRx.subscribe(new LatchedObserver<>(bh));
    }

//    @Benchmark
    public void concatMapRx(Blackhole bh) {
        concatMapRx.subscribe(new LatchedObserver<>(bh));
    }

//    @Benchmark
    public void flatMapXRangeRx(Blackhole bh) {
        flatMapXRangeRx.subscribe(new LatchedObserver<>(bh));
    }

//    @Benchmark
    public void concatMapXRangeRx(Blackhole bh) {
        concatMapXRangeRx.subscribe(new LatchedObserver<>(bh));
    }

    // -----------------------------------------------------------

//    @Benchmark
    public void rangeRsc(Blackhole bh) {
        rangeRsc.subscribe(new LatchedRSObserver<>(bh));
    }

    @Benchmark
    public void takeRsc(Blackhole bh) {
        takeRsc.subscribe(new LatchedRSObserver<>(bh));
    }

//    @Benchmark
    public void flatMapRsc(Blackhole bh) {
        flatMapRsc.subscribe(new LatchedRSObserver<>(bh));
    }

//    @Benchmark
    public void concatMapRsc(Blackhole bh) {
        concatMapRsc.subscribe(new LatchedRSObserver<>(bh));
    }

//    @Benchmark
    public void flatMapXRangeRsc(Blackhole bh) {
        flatMapXRangeRsc.subscribe(new LatchedRSObserver<>(bh));
    }

//    @Benchmark
    public void concatMapXRangeRsc(Blackhole bh) {
        concatMapXRangeRsc.subscribe(new LatchedRSObserver<>(bh));
    }

    // -----------------------------------------------------------

//    @Benchmark
    public void rangeFx(Blackhole bh) {
        rangeFx.subscribe(new LatchedRSObserver<>(bh));
    }

    @Benchmark
    public void takeFx(Blackhole bh) {
        takeFx.subscribe(new LatchedRSObserver<>(bh));
    }

//    @Benchmark
    public void flatMapFx(Blackhole bh) {
        flatMapFx.subscribe(new LatchedRSObserver<>(bh));
    }

//    @Benchmark
    public void concatMapFx(Blackhole bh) {
        concatMapFx.subscribe(new LatchedRSObserver<>(bh));
    }

//    @Benchmark
    public void flatMapXRangeFx(Blackhole bh) {
        flatMapXRangeFx.subscribe(new LatchedRSObserver<>(bh));
    }

//    @Benchmark
    public void concatMapXRangeFx(Blackhole bh) {
        concatMapXRangeFx.subscribe(new LatchedRSObserver<>(bh));
    }

    // -----------------------------------------------------------

//    @Benchmark
    public void rangeIx(Blackhole bh) {
        rangeIx.foreach((IxConsumer<Integer>)bh::consume);
    }

    @Benchmark
    public void takeIx(Blackhole bh) {
        takeIx.foreach((IxConsumer<Integer>)bh::consume);
    }

//    @Benchmark
    public void flatMapIx(Blackhole bh) {
        flatMapIx.foreach((IxConsumer<Integer>)bh::consume);
    }

//    @Benchmark
    public void concatMapIx(Blackhole bh) {
        concatMapIx.foreach((IxConsumer<Integer>)bh::consume);
    }

//    @Benchmark
    public void flatMapXRangeIx(Blackhole bh) {
        flatMapXRangeIx.foreach((IxConsumer<Integer>)bh::consume);
    }

//    @Benchmark
    public void concatMapXRangeIx(Blackhole bh) {
        concatMapXRangeIx.foreach((IxConsumer<Integer>)bh::consume);
    }

    // -----------------------------------------------------------

//    @Benchmark
    public void rangeGx(Blackhole bh) {
        rangeGx.forEach(bh::consume);
    }

    @Benchmark
    public void takeGx(Blackhole bh) {
        takeGx.forEach(bh::consume);
    }

//    @Benchmark
    public void flatMapGx(Blackhole bh) {
        flatMapGx.forEach(bh::consume);
    }

//    @Benchmark
    public void concatMapGx(Blackhole bh) {
        concatMapGx.forEach(bh::consume);
    }

//    @Benchmark
    public void flatMapXRangeGx(Blackhole bh) {
        flatMapXRangeGx.forEach(bh::consume);
    }

//    @Benchmark
    public void concatMapXRangeGx(Blackhole bh) {
        concatMapXRangeGx.forEach(bh::consume);
    }

}
