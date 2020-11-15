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
 * A single slack message to a particular slack channel.
 */
public class SlackReport {
    private String token;
    private String channel;
    private String message;

    /**
     * Creates a slack message.
     * @param token slack authentication token.
     * @param channel name of the channel to write to, including the hash symbol.
     * @param message message to be sent to the channel.
     */
    public SlackReport(String token, String channel, String message) {
        this.token = token;
        this.channel = channel;
        this.message = message;
    }

    /**
     * Gets the name of the channel the message should be written to.
     * @return name of the channel, including the hash symbol
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Gets the message to be sent.
     * @return message to be sent.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the slack authentication token.
     * @return slack authentication token.
     */
    public String getToken() {
        return token;
    }
}
