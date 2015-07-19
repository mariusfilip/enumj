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
import java.util.function.Function;

/**
 * Type of {@link AbstractPipeMultiProcessor} that flattens sub-iterators
 * generated by mapping {@link Enumerator} elements.
 * @param <In> input value mapped to an {@link Iterator}.
 * @param <Out> output value from the generated sub-iterator.
 */
final class FlatMapPipeProcessor<In,Out>
            extends AbstractPipeMultiProcessor<In,Out> {

    protected final Function<In,Iterator<Out>> mapper;
    protected final Nullable<Out>              value;    
    protected       Iterator<Out>              iterator;

    /**
     * Constructs {@link FlatMapPipeProcessor} instances.
     * @param mapper {@link Function} instance mapping input elements to
     * sub-iterators.
     */
    public FlatMapPipeProcessor(Function<In,Iterator<Out>> mapper) {
        super(true, true);
        this.mapper = mapper;
        this.iterator = null;
        this.value = Nullable.empty();
    }

    @Override
    protected boolean needsValue() {
        return !value.isPresent();
    }
    @Override
    protected void processInputValue(In value) {
        this.iterator = this.mapper.apply(value);
        if (this.iterator != null) {
            if (this.iterator.hasNext()) {
                this.value.set(this.iterator.next());
            } else {
                this.value.clear();
            }
        } else {
            this.value.clear();
        }
    }
    @Override
    protected boolean hasOutputValue() {
        return value.isPresent();
    }
    @Override
    protected Out retrieveOutputValue() {
        return value.get();
    }
    @Override
    protected void clearOutputValue() {
        value.clear();
        if (iterator.hasNext()) {
            value.set(this.iterator.next());
        } else {
            iterator = null;
            value.clear();
        }
    }
    @Override
    protected boolean isInactive() {
        return false;
    }
}
