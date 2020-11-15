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

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.function.Consumer;

/**
 * Dialog with selectable actions on a file.
 */
public class FileActionDialog extends Dialog implements View.OnClickListener {
    private Consumer<String> sendCallback;
    private Consumer<String> deleteCallback;
    private TextView mFileNameTextView;
    private Button mSendFileButton, mDeleteFileButton, mCancelButton;
    private String filePath;

    /**
     * Creates a dialog.
     * @param activity the parent activity.
     */
    public FileActionDialog(Activity activity) {
        super(activity);
    }

    /**
     * Registers a handler for a delete request.
     * @param deleteCallback handler for a delete request receiving a full file path as an argument.
     */
    public void setDeleteAction(Consumer<String> deleteCallback) {
        this.deleteCallback = deleteCallback;
    }

    /**
     * Registers a handler for a send request.
     * @param sendCallback hanlder for a send request receiving a full file path as an argument.
     */
    public void setSendAction(Consumer<String> sendCallback) {
        this.sendCallback = sendCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.file_action_selection);
        mFileNameTextView = (TextView)findViewById(R.id.textFileName);
        mSendFileButton = (Button)findViewById(R.id.buttonSendFile);
        mDeleteFileButton = (Button)findViewById(R.id.buttonDeleteFile);
        mCancelButton = (Button)findViewById(R.id.buttonCancel);

        mSendFileButton.setOnClickListener(this);
        mDeleteFileButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonSendFile) {
            if (sendCallback != null) {
                sendCallback.accept(filePath);
            }
        } else if (v.getId() == R.id.buttonDeleteFile) {
            if (deleteCallback != null) {
                deleteCallback.accept(filePath);
            }
        }
        dismiss();
    }

    /**
     * The correct way of showing the dialog as it enables setting the file path
     * for display purposes and for the callback mechanisms.
     * @param filePath full path to the file.
     */
    public void show(String filePath) {
        this.filePath = filePath;
        show();
    }

    @Override
    public void show() {
        super.show();
        String title;
        if (this.filePath != null) {
            int index = this.filePath.lastIndexOf('/');
            title = this.filePath.substring(index < 0 ? 0 : index);
        } else {
            title = "unspecified file";
        }

        mFileNameTextView.setText(title);
    }
}
