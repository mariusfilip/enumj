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

import java.util.function.Supplier;
import org.apache.commons.lang3.concurrent.ConcurrentException;

class LazySupplier<T> implements Supplier<T> {

    volatile Lazy<T> lazy;

    LazySupplier(Supplier<T> supplier) {
        lazy = new Lazy(supplier);
    }

    boolean isInitialized() {
        Lazy<T> val = lazy;
        return val.isInitialized();
    }
    
    void refresh(Supplier<T> supplier) {
        lazy = new Lazy(supplier);
    }

    void clear() {
        lazy = null;
    }

    @Override
    public T get() {
        try {
            Lazy<T> o = lazy;
            return o.get();
        } catch(ConcurrentException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }
}