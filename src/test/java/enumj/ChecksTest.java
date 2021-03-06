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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChecksTest {

    public ChecksTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        checks = new Checks();
    }
    Checks checks;

    @After
    public void tearDown() {
        checks = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureNotNull() {
        System.out.println("ensureNotNull");
        Checks.ensureNotNull(null, "null!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureNonNegative() {
        System.out.println("ensureNonNegative");
        Checks.ensureNonNegative(-1, "negative!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureLessThan() {
        System.out.println("ensureLessThan");
        Checks.ensureLessThan(4, 3, "greater!");
    }

    @Test(expected = IllegalStateException.class)
    public void testEnsureNonEnumerating_Enumerator() {
        System.out.println("ensureNonEnumerating");
        final Enumerator<String> en = Enumerator.on("titi", "fifi");
        assertEquals("titi", en.next());
        Checks.ensureNonEnumerating(en);
    }

    @Test(expected = IllegalStateException.class)
    public void testEnsureNonEnumerating_Enumerable() {
        System.out.println("ensureNonEnumerating");
        final Enumerable<String> en = Enumerable.on("titi", "fifi");
        assertEquals("titi", en.enumerator().next());
        Checks.ensureNonEnumerating(en);
    }    
}
