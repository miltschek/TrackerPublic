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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileItemAdapter extends RecyclerView.Adapter<FileItemAdapter.ViewHolder> {
    private static final String TAG = FileItemAdapter.class.getSimpleName();

    private static final int TAG_FILE_ID = 0x12345678;

    private List<FileItem> mFileItems = new ArrayList<>();
    private SimpleDateFormat sdfDate = new SimpleDateFormat("dd. MMM yyyy", Locale.getDefault()),
        sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private RequestListener requestListener;

    public static interface RequestListener {
        void onRequest(String fileName);
    }

    public void setRequestListener(RequestListener requestListener) {
        this.requestListener = requestListener;
    }

    public void setFiles(File ... files) {
        mFileItems.clear();

        for (File file : files) {
            try {
                mFileItems.add(new FileItem(file));
            } catch (Exception ex) {
                Log.d(TAG, "Failed to load the file " + file + " due to " + ex.getClass().getSimpleName() + " " + ex.getMessage());

                for (StackTraceElement stack :  ex.getStackTrace()) {
                    Log.d(TAG, stack.getFileName() + " " + " " + stack.getClassName() + " " + stack.getMethodName() + " " + stack.getLineNumber());
                }
            }
        }

        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View fileItemView = inflater.inflate(R.layout.file_item_entry, parent, false);
        ViewHolder viewHolder = new ViewHolder(fileItemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem fileItem = mFileItems.get(position);
        holder.mRootLayout.setTag(TAG_FILE_ID, fileItem.getFileName());

        float totalDurationSeconds = (fileItem.getStopNanoseconds() - fileItem.getStartNanoseconds()) / 1000f / 1000f / 1000f;
        int durationHours = (int)(totalDurationSeconds / 3600);
        int durationMinutes = (int)(totalDurationSeconds / 60 - durationHours * 60);
        int durationSeconds = (int)(totalDurationSeconds - durationMinutes * 60 - durationHours * 3600);

        Date startDateTime = new Date(fileItem.getStartTimestampRtc());
        holder.mTextFirstLine.setText(sdfDate.format(startDateTime) + " " + sdfTime.format(startDateTime));
        holder.mTextSecondLine.setText(String.format("%d:%02d:%02d", durationHours, durationMinutes, durationSeconds) + " // " + String.format("%.1f kB", fileItem.getFileSize() / 1024f));

        holder.mTextThirdLine.setText("❤ Φ " + String.format("%.0f bpm ↑ %d bpm", fileItem.getAvgHeartRate(), fileItem.getMaxHeartRate()));
        holder.mTextFourthLine.setText("\uD83D\uDC63 Σ " + String.format("%d Φ %.0f/min", fileItem.getTotalSteps(), fileItem.getAvgStepRate()) + "\n"
            + "\uD83D\uDE80 Φ " + String.format("%.1f", fileItem.getAvgSpeed() * 3.6f) + "km/h ↑ " + String.format("%.0f", fileItem.getTotalAscent()) + " m ↓ " + String.format("%.0f", fileItem.getTotalDescent()) + " m");
    }

    @Override
    public int getItemCount() {
        return mFileItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mRootLayout;
        private TextView mTextFirstLine, mTextSecondLine, mTextThirdLine, mTextFourthLine;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mRootLayout = itemView.findViewById(R.id.rootLayout);
            mTextFirstLine = itemView.findViewById(R.id.textFirstLine);
            mTextSecondLine = itemView.findViewById(R.id.textSecondLine);
            mTextThirdLine = itemView.findViewById(R.id.textThirdLine);
            mTextFourthLine = itemView.findViewById(R.id.textFourthLine);

            mRootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("milt", "On click " + mRootLayout.getTag(TAG_FILE_ID));
                }
            });

            mRootLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Object tag = mRootLayout.getTag(TAG_FILE_ID);

                    Log.i("milt", "Long click " + tag);

                    if (tag != null) {
                        if (requestListener != null) {
                            requestListener.onRequest(tag.toString());
                        }
                    }

                    return true;
                }
            });
        }
    }
}
