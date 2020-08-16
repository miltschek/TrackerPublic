/*
 *  MIT License
 *
 *  Copyright (c) 2020 miltschek
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package de.miltschek.tracker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BitUtilityTest {

    @Test
    void getLongByteOrder() {
        long testValue = 0x0123456789abcdefL;
        byte[] bytes = BitUtility.getBytes(testValue);
        assertArrayEquals(new byte[] { 0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef }, bytes);
        long longValue = BitUtility.getLong(bytes, 0);
        assertEquals(testValue, longValue);
    }

    @Test
    void getLongNegativeValue() {
        long testValue = -13;
        byte[] bytes = BitUtility.getBytes(testValue);
        long longValue = BitUtility.getLong(bytes, 0);
        assertEquals(testValue, longValue);
    }

    @Test
    void getFloatValue() {
        float testValue = 3.1415f;
        byte[] bytes = BitUtility.getBytes(testValue);
        float floatValue = BitUtility.getFloat(bytes, 0);
        assertEquals(testValue, floatValue);
    }

    @Test
    void getFloatMaxValue() {
        float testValue = Float.MAX_VALUE;
        byte[] bytes = BitUtility.getBytes(testValue);
        float floatValue = BitUtility.getFloat(bytes, 0);
        assertEquals(testValue, floatValue);
    }

    @Test
    void getFloatMinValue() {
        float testValue = Float.MIN_VALUE;
        byte[] bytes = BitUtility.getBytes(testValue);
        float floatValue = BitUtility.getFloat(bytes, 0);
        assertEquals(testValue, floatValue);
    }

    @Test
    void getFloatNaN() {
        float testValue = Float.NaN;
        byte[] bytes = BitUtility.getBytes(testValue);
        float floatValue = BitUtility.getFloat(bytes, 0);
        assertEquals(testValue, floatValue);
    }

    @Test
    void getFloatPositiveInfinity() {
        float testValue = Float.POSITIVE_INFINITY;
        byte[] bytes = BitUtility.getBytes(testValue);
        float floatValue = BitUtility.getFloat(bytes, 0);
        assertEquals(testValue, floatValue);
    }

    @Test
    void getFloatNegativeInfinity() {
        float testValue = Float.NEGATIVE_INFINITY;
        byte[] bytes = BitUtility.getBytes(testValue);
        float floatValue = BitUtility.getFloat(bytes, 0);
        assertEquals(testValue, floatValue);
    }

}