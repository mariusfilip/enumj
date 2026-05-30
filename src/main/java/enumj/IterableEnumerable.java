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

/**
 * {@code Enumerable} encapsulating an {@code Iterable}.
 *
 * @param <E> type of enumerated elements.
 * @see Enumerable
 * @see AbstractEnumerable
 * @see Iterable
 */
final class IterableEnumerable<E> extends AbstractEnumerable<E> {

    private Iterable<E> source;

    private IterableEnumerable(Iterable<E> source) {
        this.source = source;
    }

    @Override
    protected boolean internalOnceOnly() {
        return false;
    }
    @Override
    protected Enumerator<E> internalEnumerator() {
        return Enumerator.of(source.iterator());
    }

    // ---------------------------------------------------------------------- //

    /**
     * Constructs an {@code Enumerable} from an {@code Iterable}. If the
     * iterable is already an enumerable, it returns it unchanged, otherwise
     * it returns an {@link IterableEnumerable} encapsulating it.
     *
     * @param <E> type of enumerated elements.
     * @param source {@link Iterable} to get elements from.
     * @return {@link Enumerable} instance.
     */
    public static <E> Enumerable<E> of(Iterable<E> source) {
        Checks.ensureNotNull(source, Messages.NULL_ENUMERATOR_SOURCE);
        if (source instanceof Enumerable) {
            return (Enumerable<E>)source;
        }
        return new IterableEnumerable<E>(source);
    }
}
