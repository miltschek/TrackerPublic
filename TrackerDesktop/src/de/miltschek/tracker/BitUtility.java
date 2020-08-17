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

import java.nio.charset.StandardCharsets;

/**
 * Utility class providing data conversions from and to their binary representation.
 */
public final class BitUtility {
	private BitUtility() {}
	
	/**
	 * Gets a binary representation of a string in US-ASCII encoding.
	 * @param value string to be converted
	 * @return binary representation as a byte array
	 */
    public static byte[] getBytes(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Gets a binary representation of a short integer (16 bits) according to the big-endian format.
     * @param value value to be converted
     * @return binary representation as a byte array
     */
    public static byte[] getBytes(short value) {
        return new byte[] {
                (byte)(value >>> 8),
                (byte)value
        };
    }

    /**
     * Gets a binary representation of an integer (32 bits) according to the big-endian format.
     * @param value value to be converted
     * @return binary representation as a byte array
     */
    public static byte[] getBytes(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value
        };
    }

    /**
     * Gets a binary representation of a long integer (64 bits) according to the big-endian format.
     * @param value value to be converted
     * @return binary representation as a byte array
     */
    public static byte[] getBytes(long value) {
        return new byte[] {
                (byte)(value >>> 56),
                (byte)(value >>> 48),
                (byte)(value >>> 40),
                (byte)(value >>> 32),
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value
        };
    }

    /**
     * Gets a binary representation of a float value (32 bits) according to the IEEE 754 standard.
     * @param value value to be converted
     * @return binary representation as a byte array
     */
    public static byte[] getBytes(float value) {
        return getBytes(Float.floatToRawIntBits(value));
    }

    /**
     * Gets a binary representation of a double value (64 bits) according to the IEEE 754 standard.
     * @param value value to be converted
     * @return binary representation as a byte array
     */
    public static byte[] getBytes(double value) {
        return getBytes(Double.doubleToRawLongBits(value));
    }

    /**
     * Converts a big-endian binary representation of an integer (32 bits) to a value.
     * @param buffer buffer containing the binary representation
     * @param offset starting offset of the valid data
     * @return converted value
     */
    public static int getInt(byte[] buffer, int offset) {
        return (0xff & buffer[offset]) << 24
                | (0xff & buffer[offset + 1]) << 16
                | (0xff & buffer[offset + 2]) << 8
                | (0xff & buffer[offset + 3]);
    }

    /**
     * Converts a big-endian binary representation of a short integer (16 bits) to a value.
     * @param buffer buffer buffer containing the binary representation
     * @param offset starting offset of the valid data
     * @return converted value
     */
    public static short getShort(byte[] buffer, int offset) {
        return (short)((0xff & buffer[offset]) << 8
                | (0xff & buffer[offset + 1]));
    }

    /**
     * Converts a big-endian binary representation of a long integer (64 bits) to a value.
     * @param buffer buffer buffer containing the binary representation
     * @param offset starting offset of the valid data
     * @return converted value
     */
    public static long getLong(byte[] buffer, int offset) {
        return (long)(0xff & buffer[offset]) << 56
                | (long)(0xff & buffer[offset + 1]) << 48
                | (long)(0xff & buffer[offset + 2]) << 40
                | (long)(0xff & buffer[offset + 3]) << 32
                | (long)(0xff & buffer[offset + 4]) << 24
                | (long)(0xff & buffer[offset + 5]) << 16
                | (long)(0xff & buffer[offset + 6]) << 8
                | (long)(0xff & buffer[offset + 7]);
    }

    /**
     * Converts an IEEE 754 binary representation of a float (32 bits) to a value.
     * @param buffer buffer buffer containing the binary representation
     * @param offset starting offset of the valid data
     * @return converted value
     */
    public static float getFloat(byte[] buffer, int offset) {
        return Float.intBitsToFloat(getInt(buffer, offset));
    }
    
    /**
     * Converts an IEEE 754 binary representation of a double (64 bits) to a value.
     * @param buffer buffer buffer containing the binary representation
     * @param offset starting offset of the valid data
     * @return converted value
     */
    public static double getDouble(byte[] buffer, int offset) {
        return Double.longBitsToDouble(getLong(buffer, offset));
    }
}
