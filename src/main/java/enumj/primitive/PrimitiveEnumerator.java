/*
 * The MIT License
 *
 * Copyright 2016 Marius Filip.
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
package enumj.primitive;

import enumj.Enumerator;
import java.util.PrimitiveIterator;

public interface PrimitiveEnumerator {
    public static interface OfInt extends PrimitiveIterator.OfInt,
                                          Enumerator<Integer> {
        @Override
        public default Integer next() { return nextInt(); }
    }
    public static interface OfLong extends PrimitiveIterator.OfLong,
                                           Enumerator<Long> {
        @Override
        public default Long next() { return nextLong(); }
    }
    public static interface OfDouble extends PrimitiveIterator.OfDouble,
                                             Enumerator<Double> {
        @Override
        public default Double next() { return nextDouble(); }
    }
}