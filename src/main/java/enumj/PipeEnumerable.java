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
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * {@code Enumerable} that support high compositionality.
 *
 * @param <T> type of source enumerated elements.
 * @param <E> type of enumerated elements.
 * @see Enumerable
 * @see AbstractEnumerable
 */
final class PipeEnumerable<T,E> extends AbstractEnumerable<E> {

    private final Iterable<T> source;
    private final Optional<PipeEnumerable<?,? extends T>> pipeSource;
    private final PipeOperator<E,T> reversedOperator;
    private final Lazy<Boolean> onceOnly;

    /**
     * Constructs a {@code PipeEnumerable} using a regular {@code Iterable}
     * as a link.
     *
     * @param source {@link Iterable} source.
     * @param operator {@link Enumerator} transformation being added to the
     * pipe.
     * @param onceOnly true if {@code operator} is a once-only
     * operation, false otherwise.
     * @see PipeEnumerable
     */
    protected PipeEnumerable(Iterable<T> source,
                             Function<Enumerator<E>, Enumerator<T>> operator,
                             Supplier<Boolean> onceOnly) {
        Checks.ensureNotNull(source, Messages.NULL_ENUMERATOR_SOURCE);
        Checks.ensureNotNull(operator, Messages.NULL_PIPE_PROCESSOR_OPERATOR);
        this.source = source;
        this.pipeSource = Optional.empty();
        this.reversedOperator = new PipeOperator<E,T>(operator, onceOnly);
        this.onceOnly = new Lazy<Boolean>(this::getOnceOnly);
    }
    /**
     * Constructs a {@code PipeEnumerable} using a {@code Enumerable} as a link.
     *
     * @param source {@link Iterable} source.
     * @param operator {@link Enumerator} transformation being added to the
     * pipe.
     * @param onceOnly true if {@code operator} is a once-only
     * operation, false otherwise.
     * @see PipeEnumerable
     */
    protected PipeEnumerable(PipeEnumerable<?,T> source,
                             Function<Enumerator<E>, Enumerator<T>> operator,
                             Supplier<Boolean> onceOnly) {
        Checks.ensureNotNull(source, Messages.NULL_ENUMERATOR_SOURCE);
        Checks.ensureNonEnumerating(source);
        Checks.ensureNotNull(operator, Messages.NULL_PIPE_PROCESSOR_OPERATOR);
        this.source = source;
        this.pipeSource = Optional.of(source);
        this.reversedOperator = new PipeOperator<E,T>(operator, onceOnly);
        this.onceOnly = new Lazy<Boolean>(this::getOnceOnly);
    }

    private Boolean getOnceOnly() {
        PipeEnumerable<?,?> ptr = this;
        while(true) {
            if (ptr.reversedOperator.onceOnly.get()) {
                return true;
            }
            if (ptr.onceOnly.isInitialized() && ptr.onceOnly.get()) {
                return true;
            }
            if (ptr.pipeSource.isPresent()) {
                ptr = ptr.pipeSource.get();
            } else {
                break;
            }
        }
        return Enumerable.onceOnly(ptr.source);
    }

    @Override
    protected boolean internalOnceOnly() {
        return onceOnly.get();
    }
    @Override
    @SuppressWarnings("unchecked")
    protected Enumerator<E> internalEnumerator() {
        PipeEnumerator en = new PipeEnumerator();
        PipeEnumerable<?,?> ptr = this;

        while(true) {
            en = (PipeEnumerator)ptr.reversedOperator.apply(en);
            if (ptr.pipeSource.isPresent()) {
                ptr = ptr.pipeSource.get();
            } else {
                break;
            }
        }
        return en.setSource(ptr.source.iterator());
    }

    // ---------------------------------------------------------------------- //

    /**
     * Creates a {@code PipeEnumerable} instance with the given {@code source},
     * {@code operator} and <em>once only</em> flag.
     *
     * @param <T> type of source enumerated elements.
     * @param <E> type of enumerated elements.
     * @param source source of elements to transform.
     * @param operator transformation to apply on elements.
     * @param onceOnly true of the operation can be applied only
     * once, false otherwise.
     * @return new {@link PipeEnumerable} instance.
     */
    public static <T,E> PipeEnumerable<T,E> of(
            Iterable<? extends T>                  source,
            Function<Enumerator<E>, Enumerator<T>> operator,
            Supplier<Boolean>                      onceOnly) {
        return (source instanceof PipeEnumerable<?,?>)
                ? new PipeEnumerable((PipeEnumerable<T,E>)source,
                                     operator,
                                     onceOnly)
                : new PipeEnumerable(source, operator, onceOnly);
    }

    // ---------------------------------------------------------------------- //

    /**
     * Concatenates the given {@code elements} to the given {@code enumerable}.
     *
     * @param <E> type of enumerated elements.
     * @param enumerable {@link Enumerable} to concatenate to.
     * @param elements {@link Iterable} to concatenate.
     * @return concatenated {@code Enumerable}.
     */
    public static <E> Enumerable<E> concat(
            Enumerable<E> enumerable,
            Iterable<? extends E> elements) {
        return of(enumerable,
                  in -> ((PipeEnumerator)in).reversedConcat(
                          elements.iterator()),
                  () -> Enumerable.onceOnly(elements));
    }

    /**
     * Returns an {@code Enumerable} that has the same elements as the given
     * {@code enumerable} but with duplicates removed.
     *
     * @param <E> type of enumerated elements.
     * @param enumerable {@link Enumerable} to get the elements from.
     * @return {@code Enumerable} with duplicates removed.
     */
    public static <E> Enumerable<E> distinct(Enumerable<E> enumerable) {
        return of(enumerable,
                  in -> Reversible.distinct(in, true),
                  () -> false);                          
    }

    /**
     * Returns an {@code Enumerable} that has the elements of the given
     * {@code enumerable} that pass the given {@code predicate}.
     *
     * @param <E> type of enumerated elements.
     * @param enumerable {@link Enumerable} to filter.
     * @param predicate condition filtering the elements.
     * @return filtered {@code Enumerable}.
     */
    public static <E> Enumerable<E> filter(
            Enumerable<E> enumerable,
            Predicate<? super E> predicate) {
        return of(enumerable,
                  in -> ((PipeEnumerator)in).reversedFilter(predicate),
                  () -> false);
    }

    /**
     * Returns an {@code Enumerable} that replaces the elements of the given
     * {@code enumerable} with the elements of the {@code Iterable} obtained
     * by applying the given {@code mapper} on the each element.
     *
     * @param <E> type of enumerated elements to map.
     * @param <R> type of mapped enumerated elements.
     * @param enumerable {@link Enumerable} to flat-map.
     * @param mapper transformer to apply on each source enumerated element.
     * @return flat-mapped {@code Enumerable}.
     * @see Iterable
     */
    public static <E,R> Enumerable<R> flatMap(
            Enumerable<E> enumerable,
            Function<? super E, ? extends Iterable<? extends R>> mapper) {
        return of(enumerable,
                  in -> ((PipeEnumerator)in).reversedFlatMap(
                          e -> mapper.apply((E)e).iterator()),
                  () -> false);
    }

    /**
     * Returns an {@code Enumerable} that limits the elements of the given
     * {@code enumerable} to the given limit.
     *
     * @param <E> type of enumerated elements.
     * @param enumerable source {@link Enumerable}.
     * @param maxSize enumeration limit.
     * @return limited {@code Enumerable}.
     */
    public static <E> Enumerable<E> limit(
            Enumerable<E> enumerable,
            long maxSize) {
        return of(enumerable,
                  in -> ((PipeEnumerator)in).reversedLimit(maxSize),
                  () -> false);
    }

    /**
     * Returns an {@code Enumerable} that limits the elements of the given
     * {@code enumerable} while the given {@code predicate} holds true when
     * applied on the elements of the given {@code enumerable}.
     *
     * @param <E> type of enumerated elements.
     * @param enumerable source {@link Enumerable}.
     * @param predicate limiting {@code Predicate}.
     * @return limited {@code Enumerable}.
     */
    public static <E> Enumerable<E> limitWhile(
            Enumerable<E> enumerable,
            Predicate<? super E> predicate) {
        return takeWhile(enumerable, predicate);
    }

    /**
     * Returns an {@code Enumerable} that maps the elements of the given
     * {@code enumerable} to the values mapped by {@code mapper} applied on
     * each element and its index.
     * <p>
     * The index of the first enumerated element is {@code 0}.
     * </p>
     *
     * @param <E> type of source enumerated elements.
     * @param <R> type of mapped enumerated elements.
     * @param enumerable source {@link Enumerable}.
     * @param mapper element transformer.
     * @return mapped {@code Enumerable}.
     */
    public static <E,R> Enumerable<R> map(
            Enumerable<E> enumerable,
            Function<? super E, ? extends R> mapper) {
        return of(enumerable,
                  in -> ((PipeEnumerator)in).reversedMap(mapper),
                  () -> false);
    }

    /**
     * Returns an {@code Enumerable} that maps the elements of the given
     * {@code enumerable} to the values mapped by {@code mapper} applied on
     * each element and its index.
     * <p>
     * The index of the first enumerated element is {@code 0}.
     * </p>
     *
     * @param <E> type of source enumerated elements.
     * @param <R> type of mapped enumerated elements.
     * @param enumerable source {@link Enumerable}.
     * @param mapper element transformer.
     * @return mapped {@code Enumerable}.
     */
    public static <E,R> Enumerable<R> map(
            Enumerable<E> enumerable,
            BiFunction<? super E, ? super Long, ? extends R> mapper) {
        return of(enumerable,
                  in -> Reversible.map((Enumerator)in, mapper, true),
                  () -> false);
    }

    /**
     * Returns an {@code Enumerable} that peeks the elements of the given
     * {@code enumerable}, applies the given {@code action} on each of them
     * and then returns the elements unchanged.
     *
     * @param <E> type of enumerated elements.
     * @param enumerable source {@link Enumerable}.
     * @param action {@link Consumer} to apply on each element.
     * @return peeked {@code Enumerable}.
     */
    public static <E> Enumerable<E> peek(
            Enumerable<E> enumerable,
            Consumer<? super E> action) {
        return of(enumerable,
                  in -> Reversible.peek(in, action, true),
                  () -> false);
    }

    /**
     * Returns an {@code Enumerable} that skips {@code n} times over the
     * elements of the given {@code enumerable}.
     *
     * @param <E> type of enumerated elements.
     * @param enumerable source {@code enumerable}.
     * @param n number of elements to skip.
     * @return skipped {@link Enumerable}.
     */
    public static <E> Enumerable<E> skip(Enumerable<E> enumerable, long n) {
        return of(enumerable,
                  in -> ((PipeEnumerator)in).reversedSkip(n),
                  () -> false);
    }

    /**
     * Returns an {@code Enumerable} that skips the elements of the given
     * {@code enumerable} while the given {@code predicate} holds true.
     *
     * @param <E> type of enumerated elements.
     * @param enumerable source {@link Enumerable}.
     * @param predicate skipping {@link Predicate}.
     * @return skipped {@code Enumerable}.
     */
    public static <E> Enumerable<E> skipWhile(
            Enumerable<E> enumerable,
            Predicate<? super E> predicate) {
        return of(enumerable,
                  in -> ((PipeEnumerator)in).reversedSkipWhile(predicate),
                  () -> false);
    }

    /**
     * Returns an {@code Enumerable} that limits the elements of the given
     * {@code enumerable} while the given {@code predicate} holds true when
     * applied on the elements of the given {@code enumerable}.
     *
     * @param <E> type of enumerated elements.
     * @param enumerable source {@link Enumerable}.
     * @param predicate limiting {@link Predicate}.
     * @return limited {@code Enumerable}.
     */
    public static <E> Enumerable<E> takeWhile(
            Enumerable<E> enumerable,
            Predicate<? super E> predicate) {
        return of(enumerable,
                  in -> ((PipeEnumerator)in).reversedTakeWhile(predicate),
                  () -> false);
    }

    /**
     * Returns an {@code Enumerable} that zips the elements of the given
     * {@code enumerable} with the elements of the {@code first}
     * {@code Iterable} and the elements of the {@code rest} of the given
     * {@code Iterable} instances.
     *
     * @param <E> type of enumerated elements to zip.
     * @param enumerable {@link Enumerable} to zip.
     * @param first first {@link Iterable} to zip with.
     * @param rest rest of the {@code Iterable} to zip with.
     * @return zipped {@code Enumerable}.
     */
    public static <E> Enumerable<Optional<E>[]> zipAll(
            Enumerable<E> enumerable,
            Iterable<? extends E> first,
            Iterable<? extends E>... rest) {
        Checks.ensureNonEnumerating(enumerable);
        Checks.ensureNotNull(first, Messages.NULL_ITERATOR);
        for(Iterable<?> it: rest) {
            Checks.ensureNotNull(it, Messages.NULL_ITERATOR);
        }
        return of(enumerable,
                in -> {
                    final PipeEnumerator pipe = (PipeEnumerator)in;
                    final Iterator firstIt = first.iterator();
                    final List<Iterator> restList =
                            Enumerator.of(Arrays.asList(rest))
                                      .map(Iterable::iterator)
                                      .map(it -> (Iterator)it)
                                      .toList();
                    return pipe.reversedZipAll(firstIt, restList);
                },
                () -> Enumerable.onceOnly(first)
                      || Enumerator.of(Arrays.asList(rest))
                                   .anyMatch(it -> Enumerable.onceOnly(it)));
    }
}
