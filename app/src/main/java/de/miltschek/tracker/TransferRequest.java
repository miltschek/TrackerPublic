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

/**
 * Parameters of a file transfer request.
 */
public class TransferRequest {
    private String address;
    private int port;
    private String filePath;

    /**
     * Creates a file transfer request.
     * @param address target address (FQDN, IPv4 or IPv6).
     * @param port port number (1..65535).
     * @param filePath full path to the file to be transferred.
     */
    public TransferRequest(String address, int port, String filePath) {
        this.address = address;
        this.port = port;
        this.filePath = filePath;
    }

    /**
     * Gets the target address (FQDN, IPv4 or IPv6).
     * @return target address (FQDN, IPv4 or IPv6).
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the port number (1..65535).
     * @return port number (1..65535).
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the full path to the file to be transferred.
     * @return full path to the file to be transferred.
     */
    public String getFilePath() {
        return filePath;
    }
}
