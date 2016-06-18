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
import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Pipe processor that flattens sub-iterators
 * generated by mapping enumerated elements.
 *
 * @param <TIn> input value mapped to an {@link Iterator}.
 * @param <TOut> output value from the generated sub-iterator.
 * @see MapPipeProcessor
 * @see ZipPipeProcessor
 */
final class FlatMapPipeProcessor<TIn,TOut>
            extends AbstractPipeMultiProcessor<TIn,TOut> {

    private final Value.Type                   type;
    private final Function<TIn,Iterator<TOut>> mapper;
    private final Nullable<TOut>               value;
    private       Iterator<TOut>               iterator;
    private       PrimitiveIterator.OfInt      intIterator;
    private       PrimitiveIterator.OfLong     longIterator;
    private       PrimitiveIterator.OfDouble   doubleIterator;

    /**
     * Constructs a {@code FlatMapPipeProcessor} instances.
     * <p>
     * The new {@link FlatMapPipeProcessor} stores its mapper internally.
     * </p>
     *
     * @param mapper {@link Function} instance mapping input elements to
     * sub-iterators.
     */
    public FlatMapPipeProcessor(Function<TIn,Iterator<TOut>> mapper,
                                Value.Type                   type) {
        super(true, true);
        this.type = type;
        this.mapper = mapper;
        this.iterator = null;
        this.value = Nullable.empty();
    }

    @Override public boolean needsValue() {
        return !value.isPresent();
    }
    @Override public void processInputValue(In<TIn> value) {
        getIt(value.get());
        if (this.iterator != null) {
            if (this.iterator.hasNext()) {
                getNextValue();
            } else {
                this.value.clear();
            }
        } else {
            this.value.clear();
        }
    }
    @Override public boolean hasOutputValue() {
        return value.isPresent();
    }
    @Override public void getOutputValue(Out<TOut> value) {
        value.setValue(this.value);
        if (iterator.hasNext()) {
            getNextValue();
        } else {
            value.clear();
            clearIt();
        }
    }
    @Override public boolean isInactive() {
        return false;
    }
    
    private void getIt(TIn value) {
        iterator = mapper.apply(value);
        switch(type) {
            case INT:
                intIterator = (PrimitiveIterator.OfInt)iterator;
                break;
            case LONG:
                longIterator = (PrimitiveIterator.OfLong)iterator;
                break;
            case DOUBLE:
                doubleIterator = (PrimitiveIterator.OfDouble)iterator;
                break;
            default:
                intIterator = null;
                longIterator = null;
                doubleIterator = null;
                break;
        }
    }
    private void clearIt() {
        iterator = null;
        intIterator = null;
        longIterator = null;
        doubleIterator = null;
    }
    private void getNextValue() {
        switch(type) {
            case GENERIC:
                value.set(iterator.next());
                break;
            case INT:
                value.setInt(intIterator.nextInt());
                break;
            case LONG:
                value.setLong(longIterator.nextLong());
                break;
            case DOUBLE:
                value.setDouble(doubleIterator.nextDouble());
                break;
            default:
                assert false;
        }
    }
}
