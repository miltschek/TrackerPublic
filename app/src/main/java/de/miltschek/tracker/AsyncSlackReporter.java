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

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Asynchronous task sending messages to a slack channel.
 */
public class AsyncSlackReporter extends AsyncTask<SlackReport, Float, AsyncResult> {
    private static final String TAG = AsyncSlackReporter.class.getSimpleName();
    private final Consumer<AsyncResult> onFinished;

    /**
     * Create a new asynchronous message reporter.
     * @param onFinished an optional listener for a finished task (or null)
     */
    public AsyncSlackReporter(Consumer<AsyncResult> onFinished) {
        this.onFinished = onFinished;
    }

    @Override
    protected AsyncResult doInBackground(SlackReport... slackReports) {
        if (slackReports.length != 1) {
            return new AsyncResult(false, "Exactly one request expected.");
        }

        SlackReport report = slackReports[0];
        try {
            Log.i(TAG, "About to send a report...");
            String token = report.getToken();
            String channelEncoded = URLEncoder.encode(report.getChannel(), "UTF-8");
            String messageEncoded = URLEncoder.encode(report.getMessage(), "UTF-8");

            String message = "channel=" + channelEncoded
                    + "&text=" + messageEncoded;
            byte[] dataOut = message.getBytes(StandardCharsets.UTF_8);

            URL url = new URL("https://slack.com/api/chat.postMessage");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setConnectTimeout(15000);
            http.setReadTimeout(15000);
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.setRequestProperty("Authorization", "Bearer " + token);
            http.setRequestProperty("Content-Length", String.valueOf(dataOut.length));
            http.setDoOutput(true);
            //http.connect();
            Log.i(TAG, "About to send data...");
            OutputStream os = http.getOutputStream();
            os.write(dataOut, 0, dataOut.length);
            os.close();

            Log.i(TAG, "About to receive data...");
            boolean success = false;
            String errorMessage = null;
            try (InputStreamReader isr = new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8)) {
                JsonReader reader = new JsonReader(isr);
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("ok")) {
                        success = reader.nextBoolean();
                    } else if (name.equals("error")) {
                        errorMessage = reader.nextString();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
            }

            http.disconnect();
            Log.i(TAG, "Done.");

            return new AsyncResult(success, errorMessage);
        } catch (IOException ex) {
            Log.e(TAG, "Failed to send the report " + ex.getClass().getSimpleName() + " " + ex.getMessage());
            return new AsyncResult(false, ex.getClass().getSimpleName() + " " + ex.getMessage());
        } catch (Exception ex) {
            Log.e(TAG, "Failed to send the report " + ex.getClass().getSimpleName() + " " + ex.getMessage());
            return new AsyncResult(false, "Unexpected error.");
        }
    }

    @Override
    protected void onPostExecute(AsyncResult asyncResult) {
        super.onPostExecute(asyncResult);

        if (onFinished != null) {
            onFinished.accept(asyncResult);
        }
    }
}
