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

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Pipe processor that receives an element, processes it and returns an output.
 *
 * @param <T> Type of element being processed.
 * @param <R> Type of processed result.
 * @see PipeEnumerator
 * @see AbstractPipeMultiProcessor
 */
abstract class AbstractPipeProcessor<T,R> {

    /**
     * Represents whether the pipeline should proceed to the next element
     * of the same source when {@code hasOutputValue()} returns false.
     *
     * @see #hasOutputValue()
     * @see #hasNextNeedsValue
     */
    public final boolean nextOnSameSourceOnNoValue;
    /**
     * Represents whether the processor needs an input value to participate
     * to the outcome of {@code PipeEnumerator.hasNext()}.
     *
     * @see PipeEnumerator#hasNext()
     * @see #nextOnSameSourceOnNoValue
     */
    public final boolean hasNextNeedsValue;

    /**
     * Constructs a new instance of {@code AbstractPipeProcessor}.
     *
     * @param nextOnSameSourceOnNoValue value for
     * {@link #nextOnSameSourceOnNoValue}.
     * @param hasNextNeedsValue value for {@link #hasNextNeedsValue}.
     */
    protected AbstractPipeProcessor(boolean nextOnSameSourceOnNoValue,
                                    boolean hasNextNeedsValue) {
        this.nextOnSameSourceOnNoValue =
                nextOnSameSourceOnNoValue;
        this.hasNextNeedsValue = hasNextNeedsValue;
    }

    // ---------------------------------------------------------------------- //

    /**
     * Link to the next {@code AbstarctPipeProcessor} in the pipe.
     *
     * @see AbstractPipeProcessor
     */
    private AbstractPipeProcessor<? extends R,?> next;
    /**
     * The {@code PipeSource} instance whose elements this processor processes.
     *
     * @see AbstractPipeProcessor
     * @see PipeSource
     */
    @SuppressWarnings("rawtypes")
    private PipeSource source;

    /**
     * Returns the value of {@code AbstractPipeProcessor.next}.
     *
     * @return Value of {@link #next}.
     * @see #setNext(enumj.AbstractPipeProcessor)
     */
    public AbstractPipeProcessor<? extends R,?> getNext() {
        return next;
    }
    /**
     * Sets the value of {@code AbstractPipeProcessor.next}.
     * <p>
     * Once {@link next} is set to a non-null value, it may not change.
     * </p>
     *
     * @param next Value for {@link #next}.
     * @throws UnsupportedOperationException {@code next} is being set twice.
     * @see #getNext()
     */
    public void setNext(AbstractPipeProcessor<? extends R,?> next) {
        if (this.next == null) {
            this.next = next;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Gets the value of {@code AbstractPipeProcessor.source}.
     *
     * @return Value of {@link #source}.
     * @see #setSource(enumj.PipeSource)
     */
    @SuppressWarnings("rawtypes")
    public PipeSource getSource() {
        return source;
    }
    /**
     * Sets the value of {@code AbstractPipeProcessor.source}.
     * <p>
     * Once {@link #source} is set to a non-null value, it may not change.
     * </p>
     *
     * @param reference Value for {@link #source}.
     * @see #getSource()
     */
    public void setSource(@SuppressWarnings("rawtypes") PipeSource reference) {
        if (this.source == null) {
            this.source = reference;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    // ---------------------------------------------------------------------- //

    /**
     * Returns true and aggregates the given {@code mapper} to the
     * top of the existing mappers, otherwise it returns false.
     * <p>
     * This method returns false by default. The only
     * {@link AbstractPipeProcessor} capable of mapping aggregation is
     * {@link MapPipeProcessor}.
     * </p>
     *
     * @param <U> type of mapped enumerated elements.
     * @param mapper mapping {@link Function}.
     * @return true on successful aggregation of mappers, false
     * otherwise.
     */
    public <U> boolean pushFrontMap(Function<R,U> mapper) {
        return false;
    }
    /**
     * Returns true and aggregates the given {@code mapper} to the
     * end of existing mappers, otherwise it returns false.
     * <p>
     * This method returns false by default. The only
     * {@link AbstractPipeProcessor} capable of mapping aggregation is
     * {@link MapPipeProcessor}.
     * </p>
     * @param <U> type of mapped enumerated elements.
     * @param mapper mapping {@link Function}.
     * @return true on successful aggregation of mappers, false
     * otherwise.
     */
    public <U> boolean enqueueMap(Function<R,U> mapper) {
        return false;
    }
    /**
     * Returns true and aggregates the given {@code predicate} to the
     * top of the existing predicates, otherwise it returns false.
     * <p>
     * This method returns false by default. The only
     * {@link AbstractPipeProcessor} capable of predicate aggregation is
     * {@link FilterPipeProcessor}.
     * </p>
     *
     * @param predicate filtering {@link Predicate}.
     * @return true on successful aggregation of predicates, false
     * otherwise.
     */
    public boolean pushFrontFilter(Predicate<R> predicate) {
        return false;
    }
    /**
     * Returns true and aggregates the given {@code predicate} to the
     * end of existing predicates, otherwise it returns false.
     * <p>
     * This method returns false by default. The only
     * {@link AbstractPipeProcessor} capable of mapping aggregation is
     * {@link FilterPipeProcessor}.
     * </p>
     * @param predicate filtering {@link Predicate}.
     * @return true on successful aggregation of predicates, false
     * otherwise.
     */
    public boolean enqueueFilter(Predicate<R> predicate) {
        return false;
    }

    // ---------------------------------------------------------------------- //

    /**
     * Processes an input value in order to return a result, if any.
     * <p>
     * The result of processing must be stored internally.
     * </p>
     *
     * @param value Value to process.
     * @see #hasOutputValue()
     * @see #getOutputValue()
     */
    public abstract void    processInputValue(T value);
    /**
     * Gets whether processing was successful and the processor has a value
     * to yield.
     * <p>
     * If {@link #hasOutputValue()} returns false than no internal
     * storage of input or output may take place.
     * </p>
     *
     * @return true if there is an output value to yield, false
     * otherwise.
     * @see #processInputValue(java.lang.Object)
     * @see #getOutputValue()
     */
    public abstract boolean hasOutputValue();
    /**
     * Gets the processed result produced by
     * {@code AbstractPipeProcessor.processInputValue(T)} and
     * clears the internal storage of it.
     *
     * @return Processed result.
     * @see #processInputValue(Object)
     * @see #hasOutputValue()
     * @see #retrieveOutputValue()
     * @see #clearOutputValue()
     */
    public final R getOutputValue() {
        final R result = retrieveOutputValue();
        clearOutputValue();
        return result;
    }
    /**
     * Retrieves the processed result produced by
     * {@code AbstractPipeProcessor.processInputValue(Object)}.
     *
     * @return Processed result.
     * @see #getOutputValue()
     * @see #clearOutputValue()
     */
    protected abstract R retrieveOutputValue();
    /**
     * Clears the processed result after being returned by
     * {@code AbstractPipeProcessor.retrieveOutputValue()}.
     *
     * @see #getOutputValue()
     * @see #retrieveOutputValue()
     */
    protected abstract void clearOutputValue();
    /**
     * Gets whether the processor does not process any more and the pipeline
     * up to it should be discarded.
     * <p>
     * {@link #isInactive()} returning {@code true} is equivalent to the
     * situation when no matter with what value
     * {@link #processInputValue(Object)} will be called,
     * {@link #hasOutputValue()} will always return {@code false}.
     * </p>
     * <p>
     * Most processors have this function return {@code false} with the
     * exception of {@code while} and {@code limit} compositions. These will
     * turn completely inactive once a certain condition is met.
     * </p>
     * @return {@code true} if processing must stop, {@code false} otherwise.
     */
    public abstract boolean isInactive();
}
