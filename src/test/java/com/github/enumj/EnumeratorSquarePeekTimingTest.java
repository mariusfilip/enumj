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
package com.github.enumj;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EnumeratorSquarePeekTimingTest
             extends EnumeratorStringTimingTestBase {

    public final static long[] sizes = {
        1,
        10,
        100,
        1000,
        2000,
        3000,
        4000,
        5000,
        6000,
        7000,
        8000,
        9000,
        10_000
    };

    {
        buildBuffers(sizes);
    }

    private final Consumer<String> peeker = x -> {};

    @Override
    protected Enumerator<String> enumerator(StringTimingTestArgs args) {
        return Enumerator.of(buffers.get(args.widePeekCount.get()));
    }
    @Override
    protected Stream<String> stream(StringTimingTestArgs args) {
        return Arrays.stream(buffers.get(args.widePeekCount.get()));
    }
    @Override
    protected Enumerator<String> transform(StringTimingTestArgs args,
                                           Enumerator<String> enumerator) {
        final long count = args.deepPeekCount.get();
        for(long i=0; i<count; ++i) {
            enumerator = enumerator.peek(peeker);
        }
        return enumerator;
    }
    @Override
    protected Stream<String> transform(StringTimingTestArgs args,
                                       Stream<String> stream) {
        final long count = args.deepPeekCount.get();
        for(long i=0; i<count; ++i) {
            stream = stream.peek(peeker);
        }
        return stream;
    }
    @Override
    protected long sizeOf(StringTimingTestArgs args) {
        return args.widePeekCount.get() * args.deepPeekCount.get();
    }

    // ---------------------------------------------------------------------- //

    @Override
    protected double comparisonFactor(StringTimingTestArgs args,
                                      TimingTestKind kind) {
        switch(kind) {
            case CONSTRUCTION:
                return 200;
            case CONSUMPTION:
                return 200;
            case BOTH:
                return comparisonFactor(args, TimingTestKind.CONSTRUCTION)
                       +
                       comparisonFactor(args, TimingTestKind.CONSUMPTION);
            default:
                throw new IllegalArgumentException();
        }
    }
    @Override
    protected StringTimingTestArgs[] comparisonArgs(TimingTestKind kind) {
        final StringTimingTestArgs[] args =
                new StringTimingTestArgs[sizes.length];
        for(int i=0; i<args.length; ++i) {
            args[i] = StringTimingTestArgs.ofSquarePeek(sizes[i]);
        }
        return args;
    }

    // ---------------------------------------------------------------------- //

    @Override
    protected double scalabilityFactor(StringTimingTestArgs args,
                                       TimingTestKind kind) {
        return 0;
    }
    @Override
    protected StringTimingTestArgs[] scalabilityArgs(TimingTestKind kind) {
        return new StringTimingTestArgs[0];
    }
}