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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation of highly composable enumerators.
 * <p>
 * <strong>Important:</strong> this class is not {@code final} for testing
 * purposes.
 * </p>
 *
 * @param <E> type of enumerated elements.
 * @see Enumerator
 * @see AbstractEnumerator
 */
class PipeEnumerator<E> extends AbstractEnumerator<E> {

    private LinkedList<AbstractPipeProcessor>      pipeline;
    private LinkedList<AbstractPipeMultiProcessor> multiPipeline;
    private LinkedList<PipeSource<?>>              sources;
    private Nullable<E>                            value;
    private long                                   needValueForHasNext;

    /**
     * Creates a new {@code PipeEnumerator} instance based on the given
     * {@code source} {@code Enumerator}.
     *
     * @param source {@link Enumerator} providing the elements that the
     * pipeline works upon.
     */
    public PipeEnumerator(Enumerator<E> source) {
        this();
        Checks.ensureNotNull(source, Messages.NULL_ENUMERATOR_SOURCE);
        Checks.ensureNonEnumerating(source);
        PipeSource<?> src = PipeSource.of(source);
        this.sources.add(src);
    }
    /**
     * Creates a new {@code PipeEnumerator} instance with an empty internal
     * pipeline.
     */
    public PipeEnumerator() {
        this.pipeline = new LinkedList<>();
        this.multiPipeline = new LinkedList<>();
        this.sources = new LinkedList<>();
        this.value = Nullable.empty();
    }

    /**
     * Creates a {@code PipeEnumerator} based on the given {@code source}
     * {@code Iterator}.
     * <p>
     * If {@code source} is already a {@link PipeEnumerator} then this function
     * returns it unchanged.
     * </p>
     *
     * @param <T> type of enumerated elements.
     * @param source {@link Iterator} to get elements from.
     * @return created {@link PipeEnumerator}.
     */
    public static <T> PipeEnumerator<T> of(Iterator<T> source) {
        Checks.ensureNotNull(source, Messages.NULL_ENUMERATOR_SOURCE);
        return (source instanceof PipeEnumerator)
               ? (PipeEnumerator<T>)source
               : new PipeEnumerator(Enumerator.of(source));
    }

    // ---------------------------------------------------------------------- //

    /**
     * Adds the given {@code processor} to the back of the pipeline.
     * <p>
     * This method also maintains the consistency of
     * {@link #needValueForHasNext}, {@link AbstractPipeProcessor#next} of
     * the last element in {@link #pipeline} as well as
     * {@link PipeSource#firstProcessor} of the last element in
     * {@link #sources}.
     * </p>
     * @param <X> type of processed enumerated elements.
     * @param processor {@link AbstractPipeProcessor} to add.
     * @return the current {@link PipeEnumerator}.
     */
    protected <X> Enumerator<X> enqueueProcessor(
            AbstractPipeProcessor<? super E, ? extends X> processor) {
        final AbstractPipeProcessor<?,? extends E> last = pipeline.peekLast();
        safePipelineAddLast(processor);
        if (processor.hasNextNeedsValue) {
            ++needValueForHasNext;
        }
        if (last != null) {
            last.setNext((AbstractPipeProcessor)processor);
        }
        if (!sources.isEmpty()) {
            final PipeSource<?> source = sources.getLast();
            source.setFirstProcessorIfNone(processor);
            processor.setSource(source);
        }
        return (Enumerator<X>)this;
    }

    /**
     * Adds the given {@code processor} to the front of the pipeline.
     * <p>
     * This method also maintains the consistency of
     * {@link #needValueForHasNext} as well as
     * {@link AbstractPipeProcessor#next} of {@code processor}.
     * </p>
     *
     * @param <X> type of elements produced by {@code processor}.
     * @param processor {@link AbstractPipeProcessor} to add.
     * @return the current {@link PipeEnumerator}.
     */
    protected <X> PipeEnumerator<E> pushFrontProcessor(
            AbstractPipeProcessor<? super X, ?> processor) {
        final AbstractPipeProcessor<?,?> first = pipeline.peekFirst();
        safePipelineAddFirst(processor);
        if (processor.hasNextNeedsValue) {
            ++needValueForHasNext;
        }
        if (first != null) {
            processor.setNext((AbstractPipeProcessor)first);
        }
        return this;
    }

    /**
     * Adds the given {@code processor} to the back of the multi-pipeline.
     * <p>
     * This method also maintains the consistency of
     * {@link #needValueForHasNext}, {@link AbstractPipeProcessor#next} of
     * the last element in {@link pipeline} as well as
     * {@link PipeSource#firstProcessor} of the last element in
     * {@link #sources}.
     * </p>
     *
     * @param <X> type of processed enumerated elements.
     * @param processor {@link AbstractPipeMultiProcessor} to add.
     * @return the current {@link PipeEnumerator}.
     */
    protected <X> Enumerator<X> enqueueProcessor(
            AbstractPipeMultiProcessor<? super E, ? extends X> processor) {
        final AbstractPipeProcessor<?,? extends E> last = pipeline.peekLast();
        safeMultiPipelineAddLast(processor);
        if (processor.hasNextNeedsValue) {
            ++needValueForHasNext;
        }
        if (last != null) {
            last.setNext((AbstractPipeProcessor)processor);
        }
        if (!sources.isEmpty()) {
            final PipeSource<?> source = sources.getLast();
            source.setFirstProcessorIfNone(processor);
            processor.setSource(source);
        }
        return (Enumerator<X>)this;
    }

    /**
     * Adds the given {@code processor} to the front of the multi-pipeline.
     * <p>
     * This method also maintains the consistency of
     * {@link #needValueForHasNext} as well as
     * {@link AbstractPipeProcessor#next} of {@code processor}.
     * </p>
     *
     * @param <X> type of elements produced by {@code processor}.
     * @param processor {@link AbstractPipeMultiProcessor} to add.
     * @return the current {@link PipeEnumerator}.
     */
    protected <X> PipeEnumerator<E> pushFrontProcessor(
            AbstractPipeMultiProcessor<? super X, ?> processor) {
        final AbstractPipeProcessor<?,?> first = pipeline.peekFirst();
        safeMultiPipelineAddFirst(processor);
        if (processor.hasNextNeedsValue) {
            ++needValueForHasNext;
        }
        if (first != null) {
            processor.setNext((AbstractPipeProcessor)first);
        }
        return this;
    }

    /**
     * Enqueues the given mapper by aggregating it with the last mappers,
     * if possible.
     *
     * @param <X> type of mapped enumerated elements.
     * @param mapper mapping {@link Function}.
     * @return mapped {@link Enumerator}.
     */
    protected <X> Enumerator<X> enqueueMapProcessor(
            Function<? super E, ? extends X> mapper) {
        if (pipeline.isEmpty() || !pipeline.getLast().enqueueMap(mapper)) {
            return enqueueProcessor(new MapPipeProcessor(mapper));
        }
        return (Enumerator<X>)this;
    }

    /**
     * Prepends the given mapper to the existing pipeline by aggregating it with
     * the first mappers, if possible.
     *
     * @param <X> type of enumerated elements to map.
     * @param mapper mapping {@link Function}.
     * @return mapped {@link Enumerator}.
     */
    protected <X> PipeEnumerator<E> pushFrontMapProcessor(
            Function<? super X, ?> mapper) {
        if (pipeline.isEmpty()
            || !pipeline.getFirst().pushFrontMap(mapper)) {
            return pushFrontProcessor(new MapPipeProcessor(mapper));
        }
        return this;
    }

    /**
     * Enqueues the given filter by aggregating it with the last predicates,
     * if possible.
     *
     * @param filter filtering {@link Predicate}.
     * @return filtered {@link Enumerator}.
     */
    protected Enumerator<E> enqueueFilterProcessor(
            Predicate<? super E> filter) {
        if (pipeline.isEmpty()
            || !pipeline.getLast().enqueueFilter(filter)) {
            return enqueueProcessor(new FilterPipeProcessor(filter));
        }
        return this;
    }

    /**
     * Prepends the given filter to the existing pipeline by aggregating it with
     * the first filters, if possible.
     *
     * @param filter filtering {@link Predicate}.
     * @return filtered {@link Enumerator}.
     */
    protected PipeEnumerator<E> pushFrontFilterProcessor(Predicate<?> filter) {
        if (pipeline.isEmpty()
            || !pipeline.getFirst().pushFrontFilter(filter)) {
            return pushFrontProcessor(new FilterPipeProcessor(filter));
        }
        return this;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  //

    /**
     * Adds the given {@code processor} to the end of {@code pipeline}.
     * <p>
     * This method allows fault-injection for testing purposes.
     * </p>
     *
     * @param <X> type of processed elements.
     * @param processor {@link AbstractPipeProcessor} to add.
     */
    protected <X> void safePipelineAddLast(
            AbstractPipeProcessor<? super E, ? extends X> processor) {
        pipeline.addLast(processor);
    }

    /**
     * Adds the given {@code processor} to the end of {@code multiPipeline}.
     * <p>
     * This method calls
     * {@link #safePipelineAddLast(enumj.AbstractPipeProcessor)}
     * first.
     * </p>
     *
     * @param <X> type of processed elements.
     * @param processor {@link AbstractPipeMultiProcessor} to add.
     */
    private <X> void safeMultiPipelineAddLast(
            AbstractPipeMultiProcessor<? super E, ? extends X> processor) {
        safePipelineAddLast(processor);
        try {
            multiPipelineAddLast(processor);
        } catch(Throwable err) {
            pipeline.removeLast();
            throw err;
        }
    }

    /**
     * Adds the given {@code processor} to the end of {@code multiPipeLine}.
     * <p>
     * This method exists for testing purposes, to support fault injection.
     * </p>
     *
     * @param <X> type of processed elements.
     * @param processor {@link AbstractPipeProcessor} to add.
     */
    protected <X> void multiPipelineAddLast(
            AbstractPipeMultiProcessor<? super E, ? extends X> processor) {
        multiPipeline.addLast(processor);
    }

    /**
     * Adds the given {@code processor} at the front of {@code pipeline}.
     *
     * @param <X> type of processed elements.
     * @param processor {@link AbstractPipeProcessor} to add.
     */
    protected <X> void safePipelineAddFirst(
            AbstractPipeProcessor<? super X, ?> processor) {
        pipeline.addFirst(processor);
    }

    /**
     * Adds the given {@code processor} at the front of {@code multiPipeline}.
     * <p>
     * This method calls
     * {@link #safePipelineAddFirst(enumj.AbstractPipeProcessor)}
     * first.
     * </p>
     *
     * @param <X> type of processed elements.
     * @param processor {@link AbstractPipeMultiProcessor} to add.
     */
    private <X> void safeMultiPipelineAddFirst(
            AbstractPipeMultiProcessor<? super X, ?> processor) {
        safePipelineAddFirst(processor);
        try {
            multiPipelineAddFirst(processor);
        } catch(Throwable err) {
            pipeline.removeFirst();
            throw err;
        }
    }

    /**
     * Adds the given {@code processor} to the beginning of
     * {@code multiPipeline}.
     * <p>
     * This method exists for testing purposes, to support fault injection.
     * </p>
     *
     * @param <X> type of processed elements.
     * @param processor {@link AbstractPipeMultiProcessor} to add.
     */
    protected <X> void multiPipelineAddFirst(
            AbstractPipeMultiProcessor<? super X, ?> processor) {
        multiPipeline.addFirst(processor);
    }

    // ---------------------------------------------------------------------- //

    /**
     * Removes the first source in {@code sources}.
     * <p>
     * This happens because the first source in {@link #sources} has no more
     * elements. Removing the first source also involves removing the
     * {@link AbstractPipeProcessor} instances that were processing specifically
     * the elements of that source.
     * <p>
     * This method clears {@link #value}.
     * </p>
     */
    protected void dequeueSourceWithProcessors() {
        dequeueSourceProcessors(sources.remove());
        value.clear();
    }

    /**
     * Removes the front elements of {@link #sources} until the given
     * {@code AbstractPipeProcessor} becomes first in {@code pipeline}.
     * <p>
     * This method keeps calling {@link #dequeueSourceWithProcessors()}
     * until the condition is met.
     * </p>
     *
     * @param processor {@link AbstractPipeProcessor} to bring to the front
     * of {@link #sources}.
     */
    protected void dequeueSourcesUpToProcessor(
            AbstractPipeProcessor processor) {
        final PipeSource<?> processorSource = processor.getSource();
        while(processorSource != sources.getFirst()) {
            dequeueSourceWithProcessors();
        }
    }

    /**
     * Removes the processors of the given {@code PipeSource} from
     * the {@code pipeline}.
     *
     * @param removed {@link PipeSource} that has just been removed and need
     * its processors to be removed as well.
     */
    protected void dequeueSourceProcessors(PipeSource<?> removed) {
        if (sources.isEmpty()) {
            pipeline.clear();
            multiPipeline.clear();
            return;
        }

        AbstractPipeProcessor firstInPipeline = pipeline.peekFirst();
        final PipeSource<?> firstSource = sources.getFirst();

        if (firstSource.getFirstProcessor() == null) {
            if (removed.getFirstProcessor() != null) {
                while(firstInPipeline != null) {
                    firstInPipeline = dequeueProcessor();
                }
            }
            return;
        }

        while(firstInPipeline != null
              && firstInPipeline != firstSource.getFirstProcessor()) {
            firstInPipeline = dequeueProcessor();
        }
    }

    /**
     * Removes the element in front of {@code pipeline} and returns the new
     * head of {@code pipeline}.
     * <p>
     * This method removes the same element from the front of
     * {@link #multiPipeline} if the removed element matches the head of
     * {@link #multiPipeline}.
     * <p>
     * The method also maintains the consistency of
     * {@link #needValueForHasNext}.
     * </p>
     *
     * @return the new head of {@link #pipeline}, if any.
     */
    protected AbstractPipeProcessor dequeueProcessor() {
        final AbstractPipeProcessor head = pipeline.remove();
        if (!multiPipeline.isEmpty()
            && head == multiPipeline.getFirst()) {
            multiPipeline.remove();
        }
        if (head.hasNextNeedsValue) {
            --needValueForHasNext;
        }
        return pipeline.peekFirst();
    }

    /**
     * Tries to obtain obtain an input value for the pipeline.
     *
     * @param in {@link Out} value serving as input for the pipeline.
     * @param processor head of {@link #pipeline} if {@code in} gets
     * extracted successfully.
     * @return true if the input value for {@link #pipeline} has been
     * extracted successfully, false otherwise.
     */
    protected boolean tryPipelineIn(
            Out<Object>                in,
            Out<AbstractPipeProcessor> processor) {
        in.clear();
        processor.clear();

        if (!multiPipeline.isEmpty()) {
            final Iterator<AbstractPipeMultiProcessor> multiProcessorIterator =
                    multiPipeline.descendingIterator();
            AbstractPipeMultiProcessor multiProcessor;
            while(multiProcessorIterator.hasNext()) {
                multiProcessor = multiProcessorIterator.next();
                if (multiProcessor.isInactive()) {
                    this.dequeueSourcesUpToProcessor(multiProcessor);
                    break;
                }
                if (!multiProcessor.needsValue()) {
                    in.set(multiProcessor.getOutputValue());
                    processor.set(multiProcessor.getNext());
                    return true;
                }
            }
        }
        if (!straightHasNext()) {
            return false;
        }

        in.set(sources.peekFirst().next());
        processor.set(pipeline.peekFirst());
        return true;
    }

    /**
     * Tries to process the given {@code in} value through the {@code pipeline}
     * and produce an output in {@code value}.
     *
     * @param in input for the {@link #pipeline}.
     * @param processor {@link AbstractPipeProcessor} instance in
     * {@link #pipeline} to start processing from.
     * @param nextOnSameSourceOnNoValue {@link Out} boolean telling whether
     * to keep the same source on a failure to process the input or not.
     * @return true if the {@code in} value has been processed to the
     * end, false otherwise.
     */
    protected boolean tryPipelineOut(
            Object                in,
            AbstractPipeProcessor processor,
            Out<Boolean>          nextOnSameSourceOnNoValue) {
        value.clear();
        nextOnSameSourceOnNoValue.clear();

        Object val = in;
        while(processor != null) {
            if (processor.isInactive()) {
                this.dequeueSourcesUpToProcessor(processor);
                nextOnSameSourceOnNoValue.set(false);
                return false;
            }
            processor.processInputValue(val);
            if (processor.hasOutputValue()) {
                val = processor.getOutputValue();
            }
            else {
                nextOnSameSourceOnNoValue.set(
                        processor.nextOnSameSourceOnNoValue);
                return false;
            }
            processor = processor.getNext();
        }

        value.set((E)val);
        return true;
    }

    // ---------------------------------------------------------------------- //

    @Override
    protected boolean internalHasNext() {
        if (value.isPresent()) {
            return true;
        }
        return needValueForHasNext > 0
                ? tryGetNext()
                : straightHasNext();
    }
    @Override
    protected E internalNext() {
        if (!value.isPresent()) {
            final boolean gotten = tryGetNext();
            assert gotten; // should always be true;
        }
        return retrieveValue();
    }    
    private E retrieveValue() {
        final E result = value.get();
        value.clear();
        return result;
    }
    @Override
    protected void cleanup() {
        pipeline = null;
        multiPipeline = null;
        sources = null;
        value = null;
    }

    /**
     * Gets whether there are elements in one of the {@code sources}.
     * <p>
     * This method also discards the items in {@link #sources} with no elements.
     * </p>
     * @return true if there are more elements, false otherwise.
     */
    protected final boolean straightHasNext() {
        while(!sources.isEmpty() && !sources.peekFirst().hasNext()) {
            dequeueSourceWithProcessors();
        }
        return !sources.isEmpty();
    }

    /**
     * Tries to get the next value.
     *
     * @return the next value.
     */
    protected final boolean tryGetNext() {
        final Out<Object> in = Out.empty();
        final Out<AbstractPipeProcessor> processor = Out.empty();
        final Out<Boolean> nextOnSameSourceOnNoValue = Out.empty();
        while(true) {
            if (!tryPipelineIn(in, processor)) {
                return false;
            }
            if (tryPipelineOut(in.get(),
                               processor.get(),
                               nextOnSameSourceOnNoValue)) {
                return true;
            }

            if (nextOnSameSourceOnNoValue.get()) {
                // continue to next element of same source
            } else {
                // continue to next source
                dequeueSourceWithProcessors();
            }
        }
    }

    // ---------------------------------------------------------------------- //

    @Override
    public Enumerator<E> concat(Iterator<? extends E> elements) {
        sources.addLast(PipeSource.of(elements));
        return this;
    }
    @Override
    public Enumerator<E> filter(Predicate<? super E> predicate) {
        return enqueueFilterProcessor(predicate);
    }
    @Override
    public <R> Enumerator<R> flatMap(
        Function<? super E, ? extends Iterator<? extends R>> mapper) {
        return enqueueProcessor(new FlatMapPipeProcessor(mapper));
    }
    @Override
    public <X> Enumerator<X> map(
            Function<? super E, ? extends X> mapper) {
        return enqueueMapProcessor(mapper);
    }
    @Override
    public Enumerator<E> limit(long maxSize) {
        return enqueueProcessor(new LimitPipeProcessor(maxSize));
    }
    @Override
    public Enumerator<E> prepend(Iterator<? extends E> elements) {
        return reversedConcat(elements);
    }
    @Override
    public Enumerator<E> skip(long n) {
        if (n == 0) {
            return this;
        }
        return enqueueProcessor(new SkipPipeProcessor(n));
    }
    @Override
    public Enumerator<E> takeWhile(Predicate<? super E> predicate) {
        return enqueueProcessor(new WhilePipeProcessor(predicate));
    }
    @Override
    public Enumerator<E> skipWhile(Predicate<? super E> predicate) {
        return enqueueProcessor(new SkipWhilePipeProcessor(predicate));
    }
    @Override
    public Enumerator<Optional<E>[]> zipAll(Iterator<? extends E> first,
                                            Iterator<? extends E>... rest) {
        return zipAll((Iterator<E>)first,
                      (List<Iterator<E>>)(List<?>)Arrays.asList(rest));
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -//

    /**
     * Zips the elements of the {@code first} {@code Iterator} with the
     * elements of the {@code rest} of iterators.
     *
     * @param first first {@link Iterator} to zip.
     * @param rest rest of the {@code Iterator} instances to zip.
     * @return {@link Enumerator} of zipped elements.
     */
    public Enumerator<Optional<E>[]> zipAll(Iterator<E> first,
                                            List<Iterator<E>> rest) {
        PipeEnumerator<Optional<E>> optionalPipe =
                (PipeEnumerator<Optional<E>>)map(e -> Optional.of(e))
                .concat(Enumerator.of(() -> Optional.of(Optional.empty())));
        return optionalPipe.enqueueProcessor(
                new ZipPipeProcessor<E>(first, rest));
    }

    // ---------------------------------------------------------------------- //

    /**
     * Sets the source of enumeration.
     * <p>
     * This method gets called when the {@link PipeEnumerator} gets constructed
     * in reverse order, during enumerator extraction from a
     * {@link PipeEnumerable}.
     * </p>
     *
     * @param elements {@link Iterator} to set as source.
     * @return the current {@code PipeEnumerator}.
     */
    public PipeEnumerator<E> setSource(Iterator<?> elements) {
        final PipeSource<?> source = PipeSource.of(elements);
        sources.addFirst(source);
        final Iterator<AbstractPipeProcessor> pipelineIterator =
                pipeline.iterator();
        while(pipelineIterator.hasNext()) {
            final AbstractPipeProcessor processor = pipelineIterator.next();
            if (processor.getSource() != null) {
                break;
            }
            source.setFirstProcessorIfNone(processor);
            processor.setSource(source);
        }
        return this;
    }
    
    /**
     * Prepends the given {@code elements} at the front of {@code sources}.
     *
     * @param elements {@link Iterator} to prepend.
     * @return this {@link PipeEnumerator}.
     */
    public PipeEnumerator<E> reversedConcat(Iterator<?> elements) {
        return setSource(elements);
    }

    /**
     * Adds a filter processor at the front of the {@code pipeline}.
     *
     * @param predicate condition to filter elements upon.
     * @return this {@link PipeEnumerator}.
     */
    public PipeEnumerator<E> reversedFilter(Predicate<?> predicate) {
        return pushFrontFilterProcessor(predicate);
    }

    /**
     * Adds a flat-map processor at the front of {@code pipeline}.
     *
     * @param mapper {@link Function} mapping enumerated elements.
     * @return this {@link PipeEnumerator}.
     */
    public PipeEnumerator<E> reversedFlatMap(
        Function<?, ? extends Iterator<?>> mapper) {
        return pushFrontProcessor(new FlatMapPipeProcessor(mapper));
    }

    /**
     * Adds a map processor at the front of {@code pipeline}.
     *
     * @param <X> type of mapped elements. 
     * @param mapper {@link Function} mapping enumerated elements.
     * @return this {@link PipeEnumerator}.
     */
    public <X> PipeEnumerator<E> reversedMap(
            Function<? super X, ?> mapper) {
        return pushFrontMapProcessor(mapper);
    }

    /**
     * Adds a limit processor at the front of {@code pipeline}.
     *
     * @param maxSize enumeration limit.
     * @return this {@link PipeEnumerator}.
     */
    public Enumerator<E> reversedLimit(long maxSize) {
        return pushFrontProcessor(new LimitPipeProcessor(maxSize));
    }

    /**
     * Adds a skip processor at the front of {@code pipeline}.
     *
     * @param n number of elements to skip.
     * @return this {@link PipeEnumerator}.
     */
    public Enumerator<E> reversedSkip(long n) {
        if (n == 0) {
            return this;
        }
        return pushFrontProcessor(new SkipPipeProcessor(n));
    }

    /**
     * Adds a take-while processor at the front of {@code pipeline}.
     *
     * @param predicate condition to continue taking elements.
     * @return this {@link PipeEnumerator}.
     */
    public PipeEnumerator<E> reversedTakeWhile(Predicate<?> predicate) {
        return pushFrontProcessor(new WhilePipeProcessor(predicate));
    }

    /**
     * Adds a skip-while processor at the front of {@code pipeline}.
     *
     * @param predicate condition to continue skipping elements.
     * @return this {@link PipeEnumerator}.
     */
    public PipeEnumerator<E> reversedSkipWhile(Predicate<?> predicate) {
        return pushFrontProcessor(new SkipWhilePipeProcessor(predicate));
    }

    /**
     * Adds a zip-all processor at the front of {@code pipeline}.
     *
     * @param first {@link Iterator} to zip.
     * @param rest rest of {@code Iterator} instances to zip with.
     * @return this {@link PipeEnumerator}.
     */
    public PipeEnumerator<E> reversedZipAll(
            Iterator<? extends E> first,
            List<? extends Iterator<? extends E>> rest) {
        pushFrontProcessor(new ZipPipeProcessor<E>(first, rest));
        reversedConcat(Enumerator.of(() -> Optional.of(Optional.empty())));
        return reversedMap(e -> Optional.of(e));
    }
}
