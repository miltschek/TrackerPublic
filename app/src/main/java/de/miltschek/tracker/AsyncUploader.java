package de.miltschek.tracker;

import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
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

                // send the size of the file
                os.write(BitUtility.getBytes(fileSize), 0, 4);

                while ((read = is.read(buffer)) > 0) {
                    os.write(buffer, 0, read);
                    totalData += read;
                    publishProgress(currentRequest * oneRequestValue + ((fileSize > 0) ? oneRequestValue * totalData / fileSize : 0));
                }

                os.flush();

                // wait max 3 minutes for a confirmation
                socket.setSoTimeout(3 * 60 * 1000);
                try {
                    read = socket.getInputStream().read();
                    if (read == 5) {
                        Log.i(TAG, "Successfully sent the file.");
                    } else {
                        Log.e(TAG, "Failed to send the file [" + read + "].");
                    }
                } catch (SocketTimeoutException ex) {
                    Log.e(TAG, "Timeout while waiting for a reception confirmation.");
                }

                is.close();
                os.close();
                socket.close();

                publishProgress((currentRequest + 1) * oneRequestValue);

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
