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
 * Generic result of an asynchronous task.
 */
public class AsyncResult {
    private boolean success;
    private String message;

    /**
     * Creates a result.
     * @param success flag denoting whether the task has been successfully finished.
     * @param message an optional message, especially in case of a failure.
     */
    public AsyncResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Gets the flag denoting whether the task has been successfully finished.
     * @return true if succeeded, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets an optional message, especially in case of a failure.
     * @return message describing the result in details.
     */
    public String getMessage() {
        return message;
    }
}
