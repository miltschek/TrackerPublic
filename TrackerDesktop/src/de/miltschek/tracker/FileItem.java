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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Fragment of the respective class of the wearable app.
 */
public class FileItem {
    /**
     * The header of the file.
     */
    public static final byte[] HEADER = "//MILTSCHEK/TRACKER/".getBytes(StandardCharsets.UTF_8);
    /**
     * The implemented version of the file.
     */
    public static final short VERSION = 2;

    /**
     * Writes a field as a structure containing:
     * a start indicator,
     * the total length of the data
     * the data.
     * Please note, the individual byte arrays are not separated in any way. The implementor
     * needs to take care of their interpretation afterwards.
     *
     * @param os     output stream to write to
     * @param values byte arrays to be stored within of this field structure after each other
     * @throws IOException in case of an IO issue
     */
    public static void writeField(OutputStream os, byte[] ... values) throws IOException {
        int totalLength = 0;
        for (byte[] value : values) {
            totalLength += value.length;
        }

        os.write((byte)'#');
        os.write(BitUtility.getBytes(totalLength));

        for (byte[] value : values) {
            os.write(value);
        }
    }

    /**
     * Read a field structure as written by the {@link #writeField(OutputStream, byte[]...)} function.
     *
     * @param is the input stream to read from
     * @return the contained byte array (if multiple arrays have been stored, they will be returned as one concatenated array)
     * @throws IOException in case of an IO issue
     */
    public static byte[] readField(InputStream is) throws IOException {
        if (is.read() != (byte)'#') {
            throw new IOException("Beginning of a field not found.");
        }

        byte[] buffer = new byte[4];
        if (is.read(buffer) != buffer.length) {
            throw new IOException("Premature end of file.");
        }

        int totalLength = BitUtility.getInt(buffer, 0);
        buffer = new byte[totalLength];
        if (is.read(buffer) != buffer.length) {
            throw new IOException("Premature end of file.");
        }

        return buffer;
    }
}
