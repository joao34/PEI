/*

Copyright (c) 2014, TobyRich GmbH
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package com.tobyrich.app.SmartPlane;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.tailortoys.app.PowerUp.R;
import com.tobyrich.app.SmartPlane.util.Const;
import com.tobyrich.app.SmartPlane.util.InfoBox;
import com.tobyrich.app.SmartPlane.util.Util;

import org.w3c.dom.Text;

import lib.smartlink.BluetoothDisabledException;

/**
 * @author Samit Vaidya
 * @date 04 March 2014
 * @edit Radu Hambasan
 * @date 19 Jun 2014
 */

public class FullscreenActivity extends Activity {
    private static final String TAG = "SmartPlane";

    private BluetoothDelegate bluetoothDelegate;  // bluetooth events
    private SensorHandler sensorHandler;  // accelerometer & magnetometer
    private GestureDetector gestureDetector;  // touch events
    private PlaneState planeState;  // singleton with variables used app-wide

    @Override
    public void onResume() {
        super.onResume();

        // The resolution might have changed while the app was paused
        ViewTreeObserver viewTree = findViewById(R.id.controlPanel).getViewTreeObserver();
        viewTree.addOnGlobalLayoutListener(new GlobalLayoutListener(this));

        sensorHandler.registerListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorHandler.unregisterListener();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        String appVersion = "";
        try {
            appVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Could not locate package; needed to set appVersion.");
            e.printStackTrace();
        }

        ImageView infoButton = (ImageView) findViewById(R.id.imgInfo);
        InfoBox infoBox = new InfoBox(Const.UNKNOWN, infoButton, appVersion);
        infoButton.setOnClickListener(infoBox);

        bluetoothDelegate = new BluetoothDelegate(this, infoBox);
        try {
            bluetoothDelegate.connect();
        } catch (BluetoothDisabledException ex) {
            Intent enableBtIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Util.BT_REQUEST_CODE);
        }
        sensorHandler = new SensorHandler(this, bluetoothDelegate);
        sensorHandler.registerListener();
        gestureDetector = new GestureDetector(this,
                new GestureListener(this, bluetoothDelegate));
        planeState = (PlaneState) getApplicationContext();

         /* setting the trivial listeners */
        ImageView socialShare = (ImageView) findViewById(R.id.socialShare);
        socialShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showSocialShareDialog(FullscreenActivity.this);
            }
        });

        ImageView horizonImage = (ImageView) findViewById(R.id.imageHorizon);
        horizonImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        // sound played when the user presses the "Control Tower" button
        final MediaPlayer atcSound = MediaPlayer.create(this, R.raw.atc_sounds1);

        final ImageView atcOffButton = (ImageView) findViewById(R.id.atcOff);
        final ImageView atcOnButton = (ImageView) findViewById(R.id.atcOn);

        atcOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atcOnButton.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
                atcSound.start();
            }
        });

        atcOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atcOffButton.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
                atcSound.pause();
            }
        });

        final ImageView controlPanel = (ImageView) findViewById(R.id.imgPanel);
        controlPanel.setOnTouchListener(new PanelTouchListener(this,
                bluetoothDelegate));

        final ImageView settings = (ImageView) findViewById(R.id.settings);

        final Switch rudderReverse = (Switch) findViewById(R.id.rudderSwitch);
        final TextView revRudderText = (TextView) findViewById(R.id.revRudderText);

        final TextView trimLabel = (TextView) findViewById(R.id.trimRudderText);
        final Button decreaseTrimBtn = (Button) findViewById(R.id.decreaseTrimBtn);
        final TextView trimValueTxtVw = (TextView) findViewById(R.id.trimValueTxtVw);
        final Button increaseTrimBtn = (Button) findViewById(R.id.increaseTrimBtn);
        final LinearLayout trimBtnContainer = (LinearLayout) findViewById(R.id.trimBtnContainer);

        final Button flAssistOffBtn = (Button) findViewById(R.id.flAssistOffBtn);
        final Button flAssistBegBtn = (Button) findViewById(R.id.flAssistBeginnerBtn);
        final Button flAssistAdvBtn = (Button) findViewById(R.id.flAssistAdvancedBtn);

        final TextView flAssistText = (TextView) findViewById(R.id.flAssistText);
        settings.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                settings.setVisibility(View.INVISIBLE);
                rudderReverse.setVisibility(View.VISIBLE);
                revRudderText.setVisibility(View.VISIBLE);
                trimLabel.setVisibility(View.VISIBLE);
                trimBtnContainer.setVisibility(View.VISIBLE);
                flAssistOffBtn.setVisibility(View.VISIBLE);
                flAssistBegBtn.setVisibility(View.VISIBLE);
                flAssistAdvBtn.setVisibility(View.VISIBLE);
                flAssistText.setVisibility(View.VISIBLE);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rudderReverse.setVisibility(View.INVISIBLE);
                        revRudderText.setVisibility(View.INVISIBLE);
                        trimLabel.setVisibility(View.INVISIBLE);
                        trimBtnContainer.setVisibility(View.INVISIBLE);
                        flAssistOffBtn.setVisibility(View.INVISIBLE);
                        flAssistBegBtn.setVisibility(View.INVISIBLE);
                        flAssistAdvBtn.setVisibility(View.INVISIBLE);
                        flAssistText.setVisibility(View.INVISIBLE);
                        settings.setVisibility(View.VISIBLE);
                    }
                }, Const.HIDE_SETTINGS_DELAY);
                return true;
            }
        });  // End  settings listener
        rudderReverse.setChecked(Const.RUDDER_REVERSED_DEFAULT);

        decreaseTrimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float currTrim = planeState.rudderTrim;
                currTrim -= Const.TRIM_INCREMENT;
                if (currTrim < -Const.MAX_TRIM) return;

                planeState.rudderTrim = currTrim;
                trimValueTxtVw.setText(String.valueOf(currTrim));
            }
        });

        increaseTrimBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float currTrim = planeState.rudderTrim;
                currTrim += Const.TRIM_INCREMENT;
                if (currTrim > Const.MAX_TRIM) return;

                planeState.rudderTrim = currTrim;
                trimValueTxtVw.setText(String.valueOf(currTrim));
            }
        });

        View.OnClickListener flAssistListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int darkish_blue = getResources().getColor(R.color.darkish_blue);
                int gray = getResources().getColor(R.color.gray);
                switch(v.getId()) {
                    case R.id.flAssistOffBtn:
                        flAssistOffBtn.setBackgroundColor(darkish_blue);
                        flAssistBegBtn.setBackgroundColor(gray);
                        flAssistAdvBtn.setBackgroundColor(gray);
                        planeState.setFlAssistMode(Util.FA_OFF);
                        break;
                    case R.id.flAssistAdvancedBtn:
                        flAssistOffBtn.setBackgroundColor(gray);
                        flAssistBegBtn.setBackgroundColor(gray);
                        flAssistAdvBtn.setBackgroundColor(darkish_blue);
                        planeState.setFlAssistMode(Util.FA_ADVANCED);
                        break;
                    case R.id.flAssistBeginnerBtn:
                        flAssistOffBtn.setBackgroundColor(gray);
                        flAssistBegBtn.setBackgroundColor(darkish_blue);
                        flAssistAdvBtn.setBackgroundColor(gray);
                        planeState.setFlAssistMode(Util.FA_BEGINNER);
                        break;
                }
            }
        };
        flAssistOffBtn.setOnClickListener(flAssistListener);
        flAssistAdvBtn.setOnClickListener(flAssistListener);
        flAssistBegBtn.setOnClickListener(flAssistListener);
        // Default:
        flAssistAdvBtn.callOnClick();
    }  // End onCreate()

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Util.BT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        bluetoothDelegate.connect();
                    } catch (BluetoothDisabledException ex) {
                        Log.wtf(TAG, "user enabled BT, but we still couldn't connect");
                    }
                } else {
                    Log.e(TAG, "Bluetooth enabling was canceled by user");
                }
                return;
            case Util.PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED)
                    return;

                Uri photoUri = Util.photoUri;
                if (photoUri == null) {
                    Util.inform(FullscreenActivity.this,
                            getString(R.string.social_share_picture_problem));
                    return;
                }
                Util.socialShare(this, photoUri);
                return;
            case Util.SHARE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Util.inform(this, getString(R.string.social_share_success));
                } else {
                    Log.e(TAG, "Sharing not successful");
                }
                // noinspection UnnecessaryReturnStatement
                return;
        }  // end switch
    }

    @Override
    public void onBackPressed() { //change functionality of back button
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        //This resets all cached data from the app and breaks the connection.
                        //The app itself is only minimized, but not closed.
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                    }
                }).create().show();
    }

}
