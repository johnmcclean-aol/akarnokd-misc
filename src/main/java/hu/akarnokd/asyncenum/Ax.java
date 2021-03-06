/*
 * Copyright 2016 David Karnok
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

package hu.akarnokd.asyncenum;

import java.util.concurrent.Executor;

/**
 * Asynchronous Enumerable Extensions.
 *
 * @param <T> the value type
 */
public abstract class Ax<T> implements IAsyncEnumerable<T> {

    public static Ax<Integer> range(int start, int count) {
        return new AxRange(start, count);
    }

    public final Ax<T> observeOn(Executor executor) {
        return new AxObserveOn<>(this, executor);
    }

    public final Ax<T> subscribeOn(Executor executor) {
        return new AxSubscribeOn<>(this, executor);
    }
}
