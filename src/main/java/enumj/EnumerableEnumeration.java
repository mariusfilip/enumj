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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * {@code Enumeration} encapsulating an {@code Iterator}.
 *
 * @param <E> Type of enumerated elements.
 * @see Enumeration
 * @see Iterator
 * @see Enumerator
 * @see EnumerationEnumerator
 */
final class EnumerableEnumeration<E> implements Enumeration<E> {

    private Iterator<E> source;

    /**
     * Constructs a new {@code EnumerableEnumeration} instance.
     * <p>
     * The new {@link EnumerableEnumeration} stores its {@code source}
     * internally.
     * </p>
     *
     * @param source {@link Iterator} instance to get elements from.
     */
    public EnumerableEnumeration(Iterator<E> source) {
        Checks.ensureNotNull(source, Messages.NULL_ENUMERATOR_SOURCE);
        this.source = source;
    }

    @Override
    public boolean hasMoreElements() {
        return source.hasNext();
    }
    @Override
    public E nextElement() {
        return source.next();
    }
}
