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

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * {@code Enumerator} that retrieves its source of elements lazily.
 *
 * @param <E> type of enumerated elements.
 * @see Enumerator
 * @see AbstractEnumerator
 */
final class LazyEnumerator<E> extends AbstractEnumerator<E> {

    private Supplier<? extends Iterator<E>> source;
    private Enumerator<E> iterator;

    /**
     * Constructs a {@code LazyEnumerator} instance.
     * <p>
     * The new {@link LazyEnumerator} stores its {@code source} internally.
     * </p>
     * 
     * @param source {@link Supplier} for the internal {@link Iterator}.
     */
    private LazyEnumerator(Supplier<? extends Iterator<E>> source) {
        this.source = source;
    }

    @Override
    protected boolean internalHasNext() {
        if (iterator == null) {
            iterator = Enumerator.of(source.get());
        }
        return iterator.hasNext();
    }
    @Override
    protected E internalNext() {
        return iterator.next();
    }
    @Override
    protected void cleanup() {
        source = null;
        iterator = null;
    }
    
    /**
     * Creates a {@code LazyEnumerator} instance that uses {@code source} to
     * get its source of elements.
     *
     * @param <E> type of enumerated elements.
     * @param source {@link Supplier} instance providing the source of elements
     * lazily.
     * @return {@link LazyEnumerator} instance.
     */
    public static <E> LazyEnumerator<E> of(
            Supplier<? extends Iterator<E>> source) {
        Checks.ensureNotNull(source, Messages.NULL_ENUMERATOR_SOURCE);
        return new LazyEnumerator<E>(source);
    }
}
