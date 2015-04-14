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
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

/**
 * A type of {@link Iterable} with high composability that returns an
 * {@link Enumerator} instance for an {@link Iterator}.
 *
 * @param <E> the type of elements returned by this enumerable.
 * @see Enumerator
 * @see Iterable
 * @see Iterator
 * @see #iterator()
 * @see #enumerator()
 */
public interface Enumerable<E> extends Iterable<E> {

    /**
     * Returns a new {@link Iterator} instance that can traverse the current
     * {@link Enumerable}.
     * <p>
     * The default implementation of this method returns the result of
     * {@link #enumerator()}.
     * </p>
     * @return {@link Iterator} instance iterating over the current
     * {@link Enumerable} instance.
     * @see #enumerator()
     * @see Iterable
     */
    @Override
    public default Iterator<E> iterator() {
        return enumerator();
    }

    /**
     * Returns a new {@link Enumerator} instance that enumerates over the
     * current {@link Enumerable}.
     * <p>
     * The default implementation of {@link #iterator()} calls this method.
     * </p>
     * @return {@link Enumerator} instance enumerating over the current
     * {@link Enumerable} instance.
     * @see #iterator()
     * @see Enumerable
     */
    public Enumerator<E> enumerator();
    /**
     * Returns whether {@link #enumerating()} has been called at least once.
     * <p>
     * Most non-static methods of {@link Enumerable} require that this method
     * returns <c>false</c>.
     * </p>
     * @return <c>True</c> if {@link #enumerating()} has been called,
     * <c>false</c> otherwise.
     */
    public boolean enumerating();

    /**
     * Returns a copy of the current {@link Enumerable} instance.
     * <p>
     * Most non-static methods of {@link Enumerable} do not guarantee that a
     * new instance is being returned. In order to make sure a
     * {@link Enumerable} instance does not change, one needs to call
     * {@link #clone()}.
     * </p>
     * @return Copy of the current instance.
     * @throws IllegalStateException {@link #cloneable()} returns <c>false</c>.
     */
    public Enumerable<E> clone();
    /**
     * Returns whether the current {@link Enumerable} instance is cloneable.
     * <p>
     * A cloneable enumerable does not throw {@link IllegalStateException}
     * on {@link #clone()}.
     * </p>
     * @return <c>True</c> if the current instance if cloneable, <c>false</c>
     * otherwise.
     */
    public boolean cloneable();

    // ---------------------------------------------------------------------- //

    @SafeVarargs
    public static <E> Enumerable<E> on(E... elements) {
        return of(Arrays.asList(elements));
    }

    public static <E> Enumerable<E> of(Iterable<E> source) {
        return IterableEnumerable.of(source);
    }

    public static <E> OnceEnumerable<E> of(Iterator<E> source) {
        return Enumerator.of(source).asEnumerable();
    }

    public static <E> OnceEnumerable<E> of(Enumeration<E> source) {
        return Enumerator.of(source).asEnumerable();
    }

    public static <E> OnceEnumerable<E> of(Stream<E> source) {
        return Enumerator.of(source).asEnumerable();
    }

    public static <E> OnceEnumerable<E> of(Spliterator<E> source) {
        return Enumerator.of(source).asEnumerable();
    }

    public static <E> OnceEnumerable<E> of(Supplier<Optional<E>> source) {
        return Enumerator.of(source).asEnumerable();
    }

    public static <E> Enumerable<E> ofLazyIterable(
            Supplier<? extends Iterable<E>> source) {
        return LazyEnumerable.of(source);
    }

    public static <E> OnceEnumerable<E> ofLazyIterator(
            Supplier<? extends Iterator<E>> source) {
        return of(Enumerator.ofLazyIterator(source));
    }

    public static <E> OnceEnumerable<E> ofLazyEnumeration(
            Supplier<? extends Enumeration<E>> source) {
        return of(Enumerator.ofLazyEnumeration(source));
    }

    public static <E> OnceEnumerable<E> ofLazyStream(
            Supplier<? extends Stream<E>> source) {
        return of(Enumerator.ofLazyStream(source));
    }

    public static <E> OnceEnumerable<E> ofLazySpliterator(
            Supplier<? extends Spliterator<E>> source) {
        return of(Enumerator.ofLazySpliterator(source));
    }

    public static <E> LateBindingEnumerable<E> ofLateBinding(Class<E> clazz) {
        return new LateBindingEnumerable<E>();
    }

    public default <T> Enumerable<T> as(Class<T> clazz) {
        return (Enumerable<T>)this;
    }

    public default <T> Enumerable<T> asFiltered(Class<T> clazz) {
        return filter(clazz::isInstance).as(clazz);
    }

    public default Enumeration<E> asEnumeration() {
        return enumerator().asEnumeration();
    }

    public default Spliterator<E> asSpliterator() {
        return enumerator().asSpliterator();
    }

    public default Stream<E> asStream() {
        return enumerator().asStream();
    }

    public default Enumerable<E> asTolerant(
            Consumer<? super Exception> handler) {
        return asTolerant(handler, 0);
    }

    public default Enumerable<E> asTolerant(
            Consumer<? super Exception> handler,
            int retries) {
        return new PipeEnumerable(this).asTolerant(handler, retries);
    }

    // ---------------------------------------------------------------------- //

    public default Enumerable<E> append(E... elements) {
        return concat(on(elements));
    }

    public default <T> Enumerable<Pair<E,T>> cartesianProduct(
            Iterable<T> other) {
        Utils.ensureNonEnumerating(this);
        Utils.ensureNotNull(other, Messages.NULL_ENUMERATOR_SOURCE);
        return new PipeEnumerable(this).cartesianProduct(other);
    }

    public static <E> Enumerable<E> choiceOf(
            IntSupplier indexSupplier,
            Iterable<E> first,
            Iterable<? extends E> second,
            Iterable<? extends E>... rest) {
        final int size = 1 + 1 + rest.length;
        final IntUnaryOperator altIndexSupplier = i -> (i+1)%size;
        return choiceOf(indexSupplier,
                        altIndexSupplier,
                        first,
                        second,
                        rest);
    }

    public static <E> Enumerable<E> choiceOf(
            IntSupplier indexSupplier,
            IntUnaryOperator altIndexSupplier,
            Iterable<E> first,
            Iterable<? extends E> second,
            Iterable<? extends E>... rest) {
        return new ChoiceEnumerable(indexSupplier,
                                    altIndexSupplier,
                                    first,
                                    second,
                                    Arrays.asList(rest));
    }

    public default Enumerable<E> concat(Iterable<? extends E> elements) {
        return new PipeEnumerable(this).concat(elements);
    }

    public default Enumerable<E> concatOn(E... elements) {
        return concat(on(elements));
    }

    public default Enumerable<E> distinct() {
        Utils.ensureNonEnumerating(this);
        return new PipeEnumerable(this).distinct();
    }

    public default <T> boolean elementsEqual(Iterable<T> elements) {
        return enumerator().elementsEqual(elements.iterator());
    }

    public static <E> Enumerable<E> empty() {
        return of(Collections.emptyList());
    }

    public default Enumerable<E> filter(Predicate<? super E> predicate) {
        return new PipeEnumerable(this).filter(predicate);
    }

    public default <R> Enumerable<R> flatMap(
            Function<? super E, ? extends Iterator<? extends R>> mapper) {
        return new PipeEnumerable(this).flatMap(mapper);
    }

    public default <R> Enumerable<R> indexedMap(
            BiFunction<? super E, ? super Long, ? extends R> mapper) {
        return new PipeEnumerable(this).indexedMap(mapper);
    }

    public static <E> Enumerable<E> iterate(E seed, UnaryOperator<E> f) {
        return new PipeEnumerable(seed, f);
    }

    public default Enumerable<E> limit(long maxSize) {
        return new PipeEnumerable(this).limit(maxSize);
    }

    public default Enumerable<E> limitWhile(Predicate<? super E> predicate) {
        return new PipeEnumerable(this).limitWhile(predicate);
    }

    public default <R> Enumerable<R> map(
            Function<? super E, ? extends R> mapper) {
        return new PipeEnumerable(this).map(mapper);
    }

    public default Enumerable<E> prepend(Iterable<? extends E> elements) {
        Utils.ensureNonEnumerating(this);
        return of((Iterable<E>)elements).concat(this);
    }

    public default Enumerable<E> prependOn(E... elements) {
        return prepend(on(elements));
    }

    public static <E> Enumerable<E> range(E startInclusive,
                                          E endExclusive,
                                          UnaryOperator<E> succ,
                                          Comparator<? super E> cmp) {
        Utils.ensureNotNull(succ, Messages.NULL_ENUMERATOR_GENERATOR);
        Utils.ensureNotNull(cmp, Messages.NULL_ENUMERATOR_COMPARATOR);
        return cmp.compare(startInclusive, endExclusive) >= 0
               ? Enumerable.empty()
               : iterate(startInclusive, succ)
                        .takeWhile(e -> cmp.compare(e, endExclusive) < 0);
    }

    public static <E> Enumerable<E> rangeClosed(E startInclusive,
                                                E endInclusive,
                                                UnaryOperator<E> succ,
                                                Comparator<? super E> cmp) {
        Utils.ensureNotNull(succ, Messages.NULL_ENUMERATOR_GENERATOR);
        Utils.ensureNotNull(cmp, Messages.NULL_ENUMERATOR_COMPARATOR);
        return cmp.compare(startInclusive, endInclusive) > 0
               ? Enumerable.empty()
               : iterate(startInclusive, succ)
                        .takeWhile(e -> cmp.compare(e, endInclusive) <= 0);
    }

    public static Enumerable<Integer> rangeInt(int startInclusive,
                                               int endExclusive) {
        return range(startInclusive, endExclusive,
                     i -> i+1, Comparator.comparingInt(n -> n));
    }

    public static Enumerable<Integer> rangeIntClosed(int startInclusive,
                                                     int endInclusive) {
        return rangeClosed(startInclusive, endInclusive,
                           i -> i+1, Comparator.comparingInt(i -> i));
    }

    public static Enumerable<Long> rangeLong(long startInclusive,
                                             long endExclusive) {
        return range(startInclusive, endExclusive,
                     i -> i+1, Comparator.comparingLong(i -> i));
    }

    public static Enumerable<Long> rangeLongClosed(long startInclusive,
                                                   long endInclusive) {
        return rangeClosed(startInclusive, endInclusive,
                           i -> i+1, Comparator.comparingLong(i -> i));
    }

    public static <E> Enumerable<E> repeat(E element, long count) {
        return new PipeEnumerable(element, count);
    }

    public default Enumerable<E> repeatAll(long count) {
        Utils.ensureNonEnumerating(this);
        Utils.ensureNonNegative(count, Messages.NEGATIVE_ENUMERATOR_SIZE);
        final Enumerable<E> clone = clone();
        return rangeLong(0L, count).flatMap(l -> clone.enumerator());
    }

    public default Enumerable<E> repeatEach(long count) {
        Utils.ensureNonNegative(count, Messages.NEGATIVE_ENUMERATOR_SIZE);
        return flatMap(e -> Enumerator.repeat(e, count));
    }

    public default Enumerable<E> reverse() {
        return new PipeEnumerable(this).reverse();
    }

    public default Enumerable<E> skip(long n) {
        return new PipeEnumerable(this).skip(n);
    }

    public default Enumerable<E> skipWhile(Predicate<? super E> predicate) {
        return new PipeEnumerable(this).skipWhile(predicate);
    }

    public default Enumerable<E> sorted() {
        return new PipeEnumerable(this).sorted();
    }

    public default Enumerable<E> sorted(Comparator<? super E> comparator) {
        return new PipeEnumerable(this).sorted(comparator);
    }

    public default Enumerable<E> take(long n) {
        return limit(n);
    }

    public default Enumerable<E> takeWhile(Predicate<? super E> predicate) {
        return new PipeEnumerable(this).takeWhile(predicate);
    }

    public default Enumerable<E> union(Iterable<E> others) {
        return concat(others).distinct();
    }

    public default Enumerable<E> unionOn(E... others) {
        return union(on(others));
    }

    public default <T>
                   Enumerable<Pair<Optional<E>, Optional<T>>>
                   zipAny(Iterable<T> elements) {
        return zipAll((Iterable<E>)elements)
               .map(arr -> Pair.of(arr[0], (Optional<T>)arr[1]));
    }

    public default <T>
                   Enumerable<Pair<E, T>>
                   zipBoth(Enumerable<E> elements) {
        return zipAll((Iterable<E>)elements)
               .takeWhile(arr -> arr[0].isPresent() && arr[1].isPresent())
               .map(arr -> Pair.of(arr[0].get(), ((Optional<T>)arr[1]).get()));
    }

    public default <T>
                   Enumerable<Pair<E, Optional<T>>>
                   zipLeft(Iterable<T> elements) {
        return zipAll((Iterable<E>)elements)
               .takeWhile(arr -> arr[0].isPresent())
               .map(arr -> Pair.of(arr[0].get(), (Optional<T>)arr[1]));
    }

    public default <T>
                   Enumerable<Pair<Optional<E>, T>>
                   zipRight(Iterable<T> elements) {
        Utils.ensureNonEnumerating(this);
        return zipAll((Iterable<E>)elements)
               .takeWhile(arr -> arr[1].isPresent())
               .map(arr -> Pair.of(arr[0], ((Optional<T>)arr[1]).get()));
    }

    public default Enumerable<Optional<E>[]>
                   zipAll(Iterable<? extends E> first,
                          Iterable<? extends E>... rest) {
        return new PipeEnumerable(this).zipAll(first, rest);
    }
}
