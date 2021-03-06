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

import java.util.NoSuchElementException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LongRangeEnumeratorTest {

    public LongRangeEnumeratorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testEnumerating() {
        System.out.println("enumerating");
        final LongRangeEnumerator en = new LongRangeEnumerator(1, 1, true);
        assertFalse(en.enumerating());
        assertTrue(en.hasNext());
        assertTrue(en.enumerating());
    }

    @Test
    public void testHasNext() {
        System.out.println("hasNext");
        final LongRangeEnumerator en = new LongRangeEnumerator(1, 1, true);
        assertTrue(en.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNextLong() {
        System.out.println("nextLong");
        final LongRangeEnumerator en = new LongRangeEnumerator(1, 1, false);
        assertNull(en.next());
    }

    @Test
    public void testNext() {
        System.out.println("next");
        final LongRangeEnumerator en = new LongRangeEnumerator(
                Long.MAX_VALUE,
                Long.MAX_VALUE,
                true);
        assertEquals(Long.MAX_VALUE, (long)en.next());
        assertFalse(en.hasNext());
    }
}
