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

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * {@code Enumerable} whose source of elements can be defined after
 * construction.
 *
 * @param <E> type of enumerated elements.
 * @see Enumerable
 * @see AbstractEnumerable
 */
public final class LateBindingEnumerable<E> extends AbstractEnumerable<E> {

    private volatile Iterable<E> source;

    @Override
    protected boolean internalOnceOnly() {
        final Iterable<E> source = this.source;
        return Enumerable.onceOnly(source);
    }
    @Override
    protected Enumerator<E> internalEnumerator() {
        final Iterable<E> source = this.source;
        if (source == null) {
            throw new NoSuchElementException();
        }
        return Enumerator.of(source.iterator());
    }

    // ---------------------------------------------------------------------- //

    /**
     * Binds the current {@code LateBindingEnumerable} to the given
     * {@code source}.
     *
     * @param source {@link Iterable} to get the elements from.
     * @see LateBindingEnumerable
     */
    public void bind(Iterable<? extends E> source) {
        Checks.ensureNonEnumerating(this);
        Checks.ensureNotNull(source, Messages.NULL_ENUMERATOR_SOURCE);
        @SuppressWarnings("unchecked")
        final Iterable<E> checkedSource = (Iterable<E>)source;
        this.source = checkedSource;
    }

    /**
     * Gets the bound {@code Iterable} source, if any.
     *
     * @return optional source {@link Iterable}.
     */
    public Optional<Iterable<E>> binding() {
        return Optional.ofNullable(source);
    }
}
