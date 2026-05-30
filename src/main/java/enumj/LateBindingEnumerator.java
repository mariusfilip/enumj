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
import java.util.Optional;

/**
 * {@code Enumerator} whose source of elements can be defined after
 * construction.
 *
 * @param <E> type of enumerated elements.
 * @see Enumerator
 * @see AbstractEnumerator
 */
public final class LateBindingEnumerator<E> extends AbstractEnumerator<E> {

    private Iterator<E> source;

    @Override
    protected boolean internalHasNext() {
        Checks.ensureNotNull(source, Messages.NULL_ENUMERATOR_SOURCE);
        return source.hasNext();
    }
    @Override
    protected E internalNext() {
        Checks.ensureNotNull(source, Messages.NULL_ENUMERATOR_SOURCE);
        return source.next();
    }
    @Override
    protected void cleanup() {
        source = null;
    }

    // ---------------------------------------------------------------------- //

    /**
     * Binds the current {@code LateBindingEnumerator} to the given
     * {@code source}.
     *
     * @param source {@link Iterator} to get the elements from.
     * @see LateBindingEnumerator
     */
    public void bind(Iterator<? extends E> source) {
        Checks.ensureNonEnumerating(this);
        Checks.ensureNotNull(source, Messages.NULL_ENUMERATOR_SOURCE);
        @SuppressWarnings("unchecked")
        final Iterator<E> checkedSource = (Iterator<E>)source;
        this.source = checkedSource;
    }

    /**
     * Gets the bound {@link Iterable} source, if any.
     *
     * @return optional source {@link Iterable}.
     */
    public Optional<Iterator<E>> binding() {
        return Optional.ofNullable(source);
    }
}
