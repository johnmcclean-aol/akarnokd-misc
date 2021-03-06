/*
 * Copyright 2015 David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package hu.akarnokd.queue;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Control;

import rx.internal.util.atomic.SpscLinkedArrayQueue;
import rx.internal.util.unsafe.SpscArrayQueue;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@Threads(2)
@State(Scope.Group)
public class MpscLinkedArrayQueueXPerf {

    @Param({ "1", "16", "128", "1024" })
    public int capacity;

    MpscLinkedArrayQueue<Integer> queue;

    SpscLinkedArrayQueue<Integer> q2;

    SpscArrayQueue<Integer> q3;

    @Setup(Level.Iteration)
    public void setup() {
        queue = new MpscLinkedArrayQueue<>(capacity);
        q2 = new SpscLinkedArrayQueue<>(capacity);
        q3 = new SpscArrayQueue<>(capacity);
    }

    @Group("mpsc")
    @GroupThreads(1)
    @Benchmark
    public void send1(Control control) {
        final MpscLinkedArrayQueue<Integer> q = queue;
        while (!q.offer(1) && !control.stopMeasurement) {
        }
    }

    @Group("mpsc")
    @GroupThreads(1)
    @Benchmark
    public void recv1(Control control) {
        final MpscLinkedArrayQueue<Integer> q = queue;
        while (!control.stopMeasurement && q.poll() == null) {
            ;
        }
    }

    @Group("spscLinked")
    @GroupThreads(1)
    @Benchmark
    public void send2(Control control) {
        final SpscLinkedArrayQueue<Integer> q = q2;
        while (!q.offer(1) && !control.stopMeasurement) {
        }
    }

    @Group("spscLinked")
    @GroupThreads(1)
    @Benchmark
    public void recv2(Control control) {
        final SpscLinkedArrayQueue<Integer> q = q2;
        while (!control.stopMeasurement && q.poll() == null) {
            ;
        }
    }

    @Group("spsc")
    @GroupThreads(1)
    @Benchmark
    public void send3(Control control) {
        final SpscArrayQueue<Integer> q = q3;
        while (!q.offer(1) && !control.stopMeasurement) {
        }
    }

    @Group("spsc")
    @GroupThreads(1)
    @Benchmark
    public void recv3(Control control) {
        final SpscArrayQueue<Integer> q = q3;
        while (!control.stopMeasurement && q.poll() == null) {
            ;
        }
    }

}