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

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main activity of the wearable app.
 */
public class StopWatchActivity extends FragmentActivity implements IDataListener<HeartRateSensorData> {
    private static final String TAG = StopWatchActivity.class.getSimpleName();

    private static final long REFRESH_INTERVAL_MS = 200;
    private static final String PREFERENCES_HEART_SENSOR = "heart_sensor";
    private static final String PREFERENCES_GEO_SENSOR = "geo_sensor";
    private static final String PREFERENCES_STEPS_SENSOR = "steps_sensor";
    private static final String PREFERENCES_AIR_PRESSURE_SENSOR = "air_pressure_sensor";
    private static final String PREFERENCES_GEO_ALWAYS_ON = "geo_always_on";
    private static final String PREFERENCES_DISPLAY_ALWAYS_ON = "display_always_on";

    private static final String PREFERENCES_ADDRESS = "address";
    private static final String PREFERENCES_PORT = "port";

    private Intent sensorCollectorIntent;
    private ViewPager mPager;
    private PagerAdapter pagerAdapter;

    private View mStopwatchView;
    private ToggleButton mStartStopButton;
    private TextView mBigDisplayText;
    private MeterView mMeterView;
    private XYGraphView xyGraphView;
    private XYGraphView.XYData mHeartRateGraph;

    private View mSettingsView;
    private Switch mSwitchHeartRate, mSwitchStepsCounter, mSwitchGeoLocation, mSwitchGeoAlwaysOn, mSwitchAirPressure, mSwitchDisplayOn;
    private EditText mAddress, mPort;

    private View mFilesView;
    private RecyclerView mFileList;
    private FileItemAdapter mFileItemAdapter;

    private ImageView mGeoAvailabilityImageView;

    private ISensorReadout mSensorReadout;
    private Handler mDataForwarderHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate " + savedInstanceState);
        setContentView(R.layout.activity_screen_slide);

        sensorCollectorIntent = new Intent(this, SensorCollector.class);

        mStopwatchView = getLayoutInflater().inflate(R.layout.stopwatch_main, null);
        mStartStopButton = mStopwatchView.findViewById(R.id.startStopButton);
        mBigDisplayText = mStopwatchView.findViewById(R.id.bigDisplayText);
        mMeterView = mStopwatchView.findViewById(R.id.meterView);

        mSettingsView = getLayoutInflater().inflate(R.layout.settings_page, null);
        mSwitchHeartRate = mSettingsView.findViewById(R.id.switchHeartRate);
        mSwitchStepsCounter = mSettingsView.findViewById(R.id.switchStepsCounter);
        mSwitchGeoLocation = mSettingsView.findViewById(R.id.switchGeoLocation);
        mSwitchGeoAlwaysOn = mSettingsView.findViewById(R.id.switchGeoAlwaysOn);
        mSwitchAirPressure = mSettingsView.findViewById(R.id.switchAirPressure);
        mSwitchDisplayOn = mSettingsView.findViewById(R.id.switchDisplayOn);
        mAddress = mSettingsView.findViewById(R.id.editTextAddress);
        mPort = mSettingsView.findViewById(R.id.editTextPort);

        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        mSwitchHeartRate.setChecked(preferences.getBoolean(PREFERENCES_HEART_SENSOR, true));
        mSwitchStepsCounter.setChecked(preferences.getBoolean(PREFERENCES_STEPS_SENSOR, true));
        mSwitchGeoLocation.setChecked(preferences.getBoolean(PREFERENCES_GEO_SENSOR, true));
        // todo: still buggy, not to be presented
        // mSwitchGeoAlwaysOn.setChecked(preferences.getBoolean(PREFERENCES_GEO_ALWAYS_ON, false));
        mSwitchAirPressure.setChecked(preferences.getBoolean(PREFERENCES_AIR_PRESSURE_SENSOR, false));

        // todo: rethink this feature
        // mSwitchDisplayOn.setChecked(preferences.getBoolean(PREFERENCES_DISPLAY_ALWAYS_ON, false));

        mSwitchHeartRate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREFERENCES_HEART_SENSOR, mSwitchHeartRate.isChecked());
                editor.apply();
            }
        });

        mSwitchStepsCounter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREFERENCES_STEPS_SENSOR, mSwitchStepsCounter.isChecked());
                editor.apply();
            }
        });

        mSwitchGeoLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREFERENCES_GEO_SENSOR, mSwitchGeoLocation.isChecked());
                editor.apply();
            }
        });

        mSwitchAirPressure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREFERENCES_AIR_PRESSURE_SENSOR, mSwitchAirPressure.isChecked());
                editor.apply();
            }
        });

        mAddress.setText(preferences.getString(PREFERENCES_ADDRESS, "foo.bar.com"));
        mPort.setText(preferences.getString(PREFERENCES_PORT, "8080"));
        mAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREFERENCES_ADDRESS, mAddress.getText().toString());
                editor.apply();
            }
        });
        mPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREFERENCES_PORT, mPort.getText().toString());
                editor.apply();
            }
        });

        mFilesView = getLayoutInflater().inflate(R.layout.files_list, null);
        mFileList = mFilesView.findViewById(R.id.items);
        mFileItemAdapter = new FileItemAdapter();
        updateFileList();
        mFileList.setAdapter(mFileItemAdapter);
        mFileList.setLayoutManager(new LinearLayoutManager(this));

        mSwitchDisplayOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSwitchDisplayOn.isChecked()) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREFERENCES_DISPLAY_ALWAYS_ON, mSwitchDisplayOn.isChecked());
                editor.apply();
            }
        });

        mSwitchGeoAlwaysOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSensorReadout != null) {
                    mSensorReadout.setGeoLocationAlwaysActive(mSwitchGeoAlwaysOn.isChecked());
                }

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREFERENCES_GEO_ALWAYS_ON, mSwitchGeoAlwaysOn.isChecked());
                editor.apply();
            }
        });

        mFileItemAdapter.setRequestListener(new FileItemAdapter.RequestListener() {
            @Override
            public void onRequest(String fileName) {
                // todo send the file to a configured address/port
                try {
                    new AsyncUploader(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) {
                            Toast.makeText(StopWatchActivity.this, integer + " file(s) uploaded.", Toast.LENGTH_SHORT).show();
                        }
                    }).execute(new TransferRequest(mAddress.getText().toString(), Integer.parseInt(mPort.getText().toString()), fileName));
                } catch (Exception ex) {
                    Toast.makeText(StopWatchActivity.this, "Not possible.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Failed to upload a file " + fileName + " due to " + ex.getClass().getSimpleName() + " " + ex.getMessage());
                }
            }
        });

        mGeoAvailabilityImageView = mStopwatchView.findViewById(R.id.geoAvailabilityImageView);
        //mGeoAccuracyTextView = mStopwatchView.findViewById(R.id.geoAccuracyTextView);
        //mSpeedTextView = mStopwatchView.findViewById(R.id.speedTextView);

        xyGraphView = mStopwatchView.findViewById(R.id.XYGraphView2);
        mHeartRateGraph = this.xyGraphView.addDataSet(Color.WHITE, 200f);
        mHeartRateGraph.addMarker(60, Color.GREEN);
        mHeartRateGraph.addMarker(150, Color.RED);

        mGeoAvailabilityImageView.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);

        mPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), mStopwatchView, mFilesView, mSettingsView);
        mPager.setAdapter(pagerAdapter);

        mDataForwarderHandler = new Handler();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult(requestCode = " + requestCode + ", resultCode = " + resultCode + ", ...)");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart, binding service");
        bindService(sensorCollectorIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mDataForwarderHandler.post(mDataForwarderTask);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mDataForwarderHandler.removeCallbacks(mDataForwarderTask);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop, unbind service");
        unbindService(connection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (/*isFinishing() ||*/ mSensorReadout == null || !mSensorReadout.isSportActivityRunning()) {
            Log.d(TAG, "stopping service");
            stopService(sensorCollectorIntent);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private Collection<HeartRateSensorData> heartRateData = new ArrayList<>();

    private final Runnable mDataForwarderTask = new Runnable() {
        @Override
        public void run() {
            if (StopWatchActivity.this.mSensorReadout != null) {
                synchronized (heartRateData) {
                    for (HeartRateSensorData sensorData : heartRateData) {
                        mHeartRateGraph.put(sensorData.getHeartRate());
                    }

                    heartRateData.clear();
                }

                float positionAccuracy = mSensorReadout.getGeoAccuracy();
                int numSatellites = mSensorReadout.getBestSatellitesCount();

                int blendColor;
                if (Float.isNaN(positionAccuracy)) {
                    if (numSatellites == 0) {
                        blendColor = Color.DKGRAY;
                    } else if (numSatellites == 1) {
                        blendColor = Color.RED;
                    } else if (numSatellites == 2) {
                        blendColor = Color.MAGENTA;
                    } else {
                        blendColor = Color.YELLOW;
                    }
                } else {
                    blendColor = Color.WHITE;
                }

                mGeoAvailabilityImageView.setColorFilter(blendColor, PorterDuff.Mode.MULTIPLY);

                float minutes = (mSensorReadout.getSportActivityDurationNs() / 1000L / 1000L / 1000L) / 60f;
                int minutesInt = (int)minutes;
                int secondsInt = (int)((minutes - minutesInt) * 60);
                mBigDisplayText.setText(String.format("%02d:%02d", minutesInt, secondsInt));

                mMeterView.setValue(mHeartRateGraph.getLastValue() / 200f);
            }
            StopWatchActivity.this.mDataForwarderHandler.postDelayed(mDataForwarderTask, REFRESH_INTERVAL_MS);
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected " + name.getShortClassName());
            SensorCollector.LocalBinder binder = (SensorCollector.LocalBinder) service;
            mSensorReadout = (ISensorReadout)binder.getService();
            mSensorReadout.registerDataListener(new HeartRateSensorData[0], StopWatchActivity.this);
            StopWatchActivity.this.mStartStopButton.setChecked(mSensorReadout.isSportActivityRunning());
            StopWatchActivity.this.mStartStopButton.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected " + name.getShortClassName());
            mSensorReadout = null;
            StopWatchActivity.this.mStartStopButton.setEnabled(false);
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.e(TAG, "Binding died to the service  " + name.getShortClassName());
            mSensorReadout = null;
            StopWatchActivity.this.mStartStopButton.setEnabled(false);
        }

        @Override
        public void onNullBinding(ComponentName name) {
            Log.e(TAG, "Null binding from the service " + name.getShortClassName());
            StopWatchActivity.this.mStartStopButton.setEnabled(false);
        }
    };

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private final ScreenSlidePageFragment[] fragments;

        /**
         * Instantiates a new Screen slide pager adapter.
         *
         * @param fm       the fm
         * @param allViews the all views
         */
        public ScreenSlidePagerAdapter(FragmentManager fm, View ... allViews) {
            super(fm);
            this.fragments = new ScreenSlidePageFragment[allViews.length];
            for (int n = 0; n < allViews.length; n++) {
                this.fragments[n] = new ScreenSlidePageFragment(allViews[n]);
            }
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments[position];
        }

        @Override
        public int getCount()
        {
            return this.fragments.length;
        }
    }

    /**
     * On start stop sport.
     *
     * @param view the view
     */
    public void onStartStopSport(View view) {
        boolean isRunning = mSensorReadout != null && mSensorReadout.isSportActivityRunning();

        if (!mStartStopButton.isChecked() && isRunning) {
            onStopSport(view);
        } else if (mStartStopButton.isChecked() && !isRunning) {
            onStartSport(view);
        } else {
            // desync between the button and the sport activity ("it should never happen")
            Log.e(TAG, "Button/SportActivity desync.");
        }
    }

    /**
     * On delete all.
     *
     * @param view the view
     */
    public void onDeleteAll(View view) {
        // 'compat' due to a target API below 29
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_AppCompat_Dialog))
                .setTitle(R.string.delete_all)
                .setMessage(R.string.delete_all_confirmation)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File[] files = getFilesDir().listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.endsWith(".trk");
                            }
                        });

                        for (File file : files) {
                            file.delete();
                        }

                        updateFileList();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void onStartSport(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.BODY_SENSORS, Manifest.permission.ACCESS_FINE_LOCATION }, 0);
            mStartStopButton.setChecked(false);
            return;
        }

        if (mSensorReadout == null) {
            Log.e(TAG, "No mSensorReadout in Start Sport handler.");
        } else {
            Log.i(TAG, "Starting Sport...");
            Log.d(TAG, "starting service");
            startService(sensorCollectorIntent);
            mSensorReadout.resetSportActivity();
            mHeartRateGraph.clear();
            mSensorReadout.startSportActivity(mSwitchHeartRate.isChecked(), mSwitchStepsCounter.isChecked(), mSwitchAirPressure.isChecked(), mSwitchGeoLocation.isChecked());
        }
    }

    private void onStopSport(View view) {
        if (mSensorReadout == null) {
            Log.e(TAG, "No mSensorReadout in Stop Sport handler.");
        } else {
            mStartStopButton.setEnabled(false);
            mSensorReadout.stopSportActivity();
            Log.d(TAG, "stopping service (user action)");
            stopService(sensorCollectorIntent);

            // todo
            new AsyncSaver(new Consumer<Integer>() {
                        @Override
                        public void accept(Integer integer) {
                            updateFileList();
                            // todo: potential desync with service bind status
                            mStartStopButton.setEnabled(true);
                            if (integer != 1) {
                                Toast.makeText(StopWatchActivity.this, "Save Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    getFilesDir()).execute(mSensorReadout);
        }
    }

    private void updateFileList() {

        File[] files = getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".trk");
            }
        });

        mFileItemAdapter.setFiles(files);
        Log.d(TAG, "files refreshed " + files.length);
    }


    /**
     * The type Screen slide page fragment.
     */
    public static class ScreenSlidePageFragment extends Fragment {
        private static final String TAG = ScreenSlidePageFragment.class.getSimpleName();

        private View view;

        /**
         * Instantiates a new Screen slide page fragment.
         *
         * @param view the view
         */
        public ScreenSlidePageFragment(View view) {
            this.view = view;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return view;
        }
    }

    @Override
    public void onDataReceived(HeartRateSensorData data) {
        synchronized (heartRateData) {
            heartRateData.add(data);
        }
    }
}
