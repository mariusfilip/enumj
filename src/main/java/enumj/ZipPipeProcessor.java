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
import java.util.List;
import java.util.Optional;

/**
 * Pipe processor that zips enumerated values together.
 *
 * @param <E> type of enumerated elements to zip together.
 * @see MapPipeProcessor
 */
final class ZipPipeProcessor<E>
            extends AbstractPipeProcessor<Optional<E>,Optional<E>[]> {

    private final Iterator<? extends E>[] iterators;
    private       Optional<E>[] value;
    private       boolean       hasAny;

    /**
     * Constructs a {@code ZipPipeProcessor} that zips together the processed
     * element with elements from other {@link Iterator} instances.
     * <p>
     * The new {@link ZipPipeProcessor} stores internally the participating
     * iterators.
     * </p>
     *
     * @param first first {@link Iterator} to use in zipping.
     * @param rest rest of {@link Iterator} instances to use in zipping.
     */
    public ZipPipeProcessor(Iterator<? extends E> first,
                            List<? extends Iterator<? extends E>> rest) {
        super(false, true);
        @SuppressWarnings("unchecked")
        final Iterator<? extends E>[] its =
                (Iterator<? extends E>[])new Iterator<?>[1+rest.size()];
        iterators = its;
        iterators[0] = first;
        for(int i=1; i<iterators.length; ++i) {
            iterators[i] = rest.get(i-1);
        }
    }

    @Override
    public void processInputValue(Optional<E> value) {
        @SuppressWarnings("unchecked")
        final Optional<E>[] tuple = (Optional<E>[])
                new Optional<?>[1+iterators.length];
        tuple[0] = value;
        hasAny = value.isPresent();
        for(int i=0; i<iterators.length; ++i) {
            if (iterators[i].hasNext()) {
                hasAny = true;
                tuple[i+1] = Optional.of(iterators[i].next());
            }
            else {
                tuple[i+1] = Optional.empty();
            }
        }
        if (hasAny) {
            this.value = tuple;
        }
    }
    @Override
    public boolean hasOutputValue() {
        return value != null;
    }
    @Override
    protected Optional<E>[] retrieveOutputValue() {
        return value;
    }
    @Override
    protected void clearOutputValue() {
        value = null;
    }
    @Override
    public boolean isInactive() {
        return false;
    }
}
