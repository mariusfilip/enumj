/*
 * The MIT License
 *
 * Copyright 2015 Marius Filip.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package enumj;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract implementation of the {@code Enumerable} interface.
 * <p>
 * This class forms the basis for all the concrete implementations of
 * {@link Enumerable} within the {@code enumj} package.
 * </p>
 *
 * @param <E> Type of elements being enumerated.
 * @see AbstractEnumerator
 * @see Enumerable
 * @see Enumerator
 */
abstract class AbstractEnumerable<E> implements Enumerable<E> {

    private final AtomicBoolean enumerating;

    /**
     * Constructs a new {@code AbstractEnumerable} instance.
     * <p>
     * The new {@link AbstractEnumerable} instance gets initialised as
     * non-enumerating.
     * </p>
     *
     * @see #enumerating
     */
    protected AbstractEnumerable() {
        enumerating = new AtomicBoolean(false);
    }

    @Override
    public final boolean enumerating() {
        return enumerating.get();
    }
    @Override
    public final boolean onceOnly() {
        return internalOnceOnly();
    }
    @Override
    public final Enumerator<E> enumerator() {
        return onceOnly() ? onceEnumerator() : multipleEnumerator();
    }

    private final Enumerator<E> onceEnumerator() {
        final boolean alreadyEnumerating =
                !enumerating.compareAndSet(false, true);
        if (alreadyEnumerating) {
            throw new IllegalStateException(
                    Messages.ILLEGAL_MULTIPLE_ENUMERATIONS);
        }
        return internalEnumerator();
    }
    private final Enumerator<E> multipleEnumerator() {
        enumerating.set(true);
        return internalEnumerator();
    }

    /**
     * Gets whether the current instance may yield a {@code Enumerator}
     * only once.
     * <p>
     * This method is the internal counterpart of {@link #onceOnly()}.
     * </p>
     *
     * @return true if {@link #enumerator()} may be called
     * only once, false otherwise.
     */
    protected abstract boolean internalOnceOnly();
    /**
     * Yields a new {@code Enumerator} for the current {@code Enumerable}.
     * <p>
     * This method is the internal counterpart of {@link #enumerator()}.
     * </p>
     * @return {@link Enumerator} instance.
     * @see Enumerable
     */
    protected abstract Enumerator<E> internalEnumerator();
}
