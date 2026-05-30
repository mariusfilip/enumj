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
import java.util.function.Supplier;

/**
 * {@code Function} that a {@code PipeEnumerable} applies in order
 * to constructs its {@code Enumerator} instances.
 *
 * @param <T> type of input enumerated elements.
 * @param <E> type of output enumerated elements.
 * @see Function
 * @see PipeEnumerable
 * @see Enumerator
 */
final class PipeOperator<T,E> implements Function<Enumerator<T>,
                                                  Enumerator<E>> {
    /**
     * {@code Lazy} value indicating whether the operator can be applied
     * only once or not.
     */
    public  final Lazy<Boolean>                          onceOnly;
    private final Function<Enumerator<T>, Enumerator<E>> operator;

    /**
     * Creates a {@code PipeOperator} instance with the given {@code operator}
     * and {@code onceOnly} flag.
     *
     * @param operator value for the internal {@code operator}.
     * @param onceOnly value for {@link #onceOnly}.
     */
    public PipeOperator(Function<Enumerator<T>,
                                 Enumerator<E>> operator,
                        Supplier<Boolean> onceOnly) {
        this.onceOnly = new Lazy<Boolean>(onceOnly);
        this.operator = operator;
    }

    @Override
    public Enumerator<E> apply(Enumerator<T> enumerator) {
        return operator.apply(enumerator);
    }
}
