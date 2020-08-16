package de.miltschek.tracker;

import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class AsyncUploader extends AsyncTask<TransferRequest, Float, Integer> {
    private static final String TAG = AsyncUploader.class.getSimpleName();
    private Consumer<Integer> finishedCallback;

    public AsyncUploader(Consumer<Integer> finishedCallback) {
        this.finishedCallback = finishedCallback;
    }

    @Override
    protected Integer doInBackground(TransferRequest ... params) {
        if (params == null || params.length == 0) {
            return 0;
        }

        int succeeded = 0;
        int totalRequests = params.length;
        int currentRequest = 0;
        float oneRequestValue = 1f / totalRequests;
        for (TransferRequest transferRequest : params) {
            try {
                Socket socket = new Socket(transferRequest.getAddress(), transferRequest.getPort());
                OutputStream os = socket.getOutputStream();
                InputStream is = new FileInputStream(transferRequest.getFilePath());
                int fileSize = is.available();

                int read;
                long totalData = 0;
                byte[] buffer = new byte[1024 * 1024];

                while ((read = is.read(buffer)) > 0) {
                    os.write(buffer, 0, read);
                    totalData += read;
                    publishProgress(currentRequest * oneRequestValue + ((fileSize > 0) ? oneRequestValue * totalData / fileSize : 0));
                }

                is.close();
                os.close();
                socket.close();

                publishProgress((currentRequest + 1) * oneRequestValue);

            /*HttpURLConnection http = (HttpURLConnection)new URL("http://192.168.1.3:8080/post").openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setFixedLengthStreamingMode(buffer.length);
            OutputStream os = http.getOutputStream();
            os.write(buffer);
            os.close();

            int responseCode = http.getResponseCode();
            http.disconnect();*/

                succeeded++;
            } catch (Exception ex) {
                Log.d(TAG, "Failed to upload file " + transferRequest.getFilePath() + " to " + transferRequest.getAddress() + ":" + transferRequest.getPort() + " due to " + ex.getClass().getSimpleName() + " " + ex.getMessage());
            }

            currentRequest++;
        }

        return succeeded;
    }

    @Override
    protected void onProgressUpdate(Float... values) {
        for (Float value : values) {
            Log.d(TAG, "Progress(" + values.length + ") " + value);
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (this.finishedCallback != null) {
            this.finishedCallback.accept(integer);
        }
    }
}
