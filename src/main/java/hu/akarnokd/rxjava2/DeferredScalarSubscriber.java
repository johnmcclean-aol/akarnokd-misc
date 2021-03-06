package hu.akarnokd.rxjava2;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.reactivestreams.*;

import reactor.core.*;
import reactor.core.publisher.Operators;

/**
 * A Subscriber/Subscription barrier that holds a single value at most and properly gates asynchronous behaviors
 * resulting from concurrent request or cancel and onXXX signals.
 *
 * @param <I> The upstream sequence type
 * @param <O> The downstream sequence type
 */
class DeferredScalarSubscriber<I, O> implements Subscriber<I>, Loopback,
Trackable,
Receiver, Producer,
Fuseable.QueueSubscription<O> {

    static final int SDS_NO_REQUEST_NO_VALUE   = 0;
    static final int SDS_NO_REQUEST_HAS_VALUE  = 1;
    static final int SDS_HAS_REQUEST_NO_VALUE  = 2;
    static final int SDS_HAS_REQUEST_HAS_VALUE = 3;

    protected final Subscriber<? super O> subscriber;

    protected O value;

    volatile int state;
    @SuppressWarnings("rawtypes")
    static final AtomicIntegerFieldUpdater<DeferredScalarSubscriber> STATE =
    AtomicIntegerFieldUpdater.newUpdater(DeferredScalarSubscriber.class, "state");

    protected byte outputFused;

    static final byte OUTPUT_NO_VALUE = 1;
    static final byte OUTPUT_HAS_VALUE = 2;
    static final byte OUTPUT_COMPLETE = 3;

    DeferredScalarSubscriber(Subscriber<? super O> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void request(long n) {
        if (Operators.validate(n)) {
            for (; ; ) {
                int s = state;
                if (s == SDS_HAS_REQUEST_NO_VALUE || s == SDS_HAS_REQUEST_HAS_VALUE) {
                    return;
                }
                if (s == SDS_NO_REQUEST_HAS_VALUE) {
                    if (STATE.compareAndSet(this, SDS_NO_REQUEST_HAS_VALUE, SDS_HAS_REQUEST_HAS_VALUE)) {
                        Subscriber<? super O> a = downstream();
                        a.onNext(value);
                        a.onComplete();
                    }
                    return;
                }
                if (STATE.compareAndSet(this, SDS_NO_REQUEST_NO_VALUE, SDS_HAS_REQUEST_NO_VALUE)) {
                    return;
                }
            }
        }
    }

    @Override
    public void cancel() {
        state = SDS_HAS_REQUEST_HAS_VALUE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onNext(I t) {
        value = (O) t;
    }

    @Override
    public void onError(Throwable t) {
        subscriber.onError(t);
    }

    @Override
    public void onSubscribe(Subscription s) {
        //if upstream
    }

    @Override
    public void onComplete() {
        subscriber.onComplete();
    }

    @Override
    public final boolean isCancelled() {
        return state == SDS_HAS_REQUEST_HAS_VALUE;
    }

    @Override
    public final Subscriber<? super O> downstream() {
        return subscriber;
    }

    public void setValue(O value) {
        this.value = value;
    }

    /**
     * Tries to emit the value and complete the underlying subscriber or
     * stores the value away until there is a request for it.
     * <p>
     * Make sure this method is called at most once
     * @param value the value to emit
     */
    public final void complete(O value) {
        Objects.requireNonNull(value);
        for (; ; ) {
            int s = state;
            if (s == SDS_NO_REQUEST_HAS_VALUE || s == SDS_HAS_REQUEST_HAS_VALUE) {
                return;
            }
            if (s == SDS_HAS_REQUEST_NO_VALUE) {
                if (outputFused == OUTPUT_NO_VALUE) {
                    setValue(value); // make sure poll sees it
                    outputFused = OUTPUT_HAS_VALUE;
                }
                Subscriber<? super O> a = downstream();
                a.onNext(value);
                if (state != SDS_HAS_REQUEST_HAS_VALUE) {
                    a.onComplete();
                }
                return;
            }
            setValue(value);
            if (STATE.compareAndSet(this, SDS_NO_REQUEST_NO_VALUE, SDS_NO_REQUEST_HAS_VALUE)) {
                return;
            }
        }
    }

    @Override
    public boolean isStarted() {
        return state != SDS_NO_REQUEST_NO_VALUE;
    }

    @Override
    public Object connectedOutput() {
        return value;
    }

    @Override
    public boolean isTerminated() {
        return isCancelled();
    }

    @Override
    public Object upstream() {
        return value;
    }

    @Override
    public int requestFusion(int requestedMode) {
        if ((requestedMode & Fuseable.ASYNC) != 0) {
            outputFused = OUTPUT_NO_VALUE;
            return Fuseable.ASYNC;
        }
        return Fuseable.NONE;
    }

    @Override
    public O poll() {
        if (outputFused == OUTPUT_HAS_VALUE) {
            outputFused = OUTPUT_COMPLETE;
            return value;
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return outputFused != OUTPUT_HAS_VALUE;
    }

    @Override
    public void clear() {
        outputFused = OUTPUT_COMPLETE;
        value = null;
    }

    @Override
    public int size() {
        return isEmpty() ? 0 : 1;
    }
}