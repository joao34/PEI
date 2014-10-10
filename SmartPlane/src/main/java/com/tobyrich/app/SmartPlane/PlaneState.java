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

import android.app.Application;

import com.tobyrich.app.SmartPlane.util.Util;

/**
 * @author Radu Hambasan
 * @date Jun 13 2014
 *
 * Singleton handling data shared at global level
 */
public class PlaneState extends Application{
    private float motorSpeed;
    private double scaler = 0;
    private int flAssistMode;
    private boolean screenLocked = false;

    public int rudderTrim = 0;

    public void setScaler(double scaler) {
        this.scaler = scaler;
    }
    public boolean isScreenLocked() {
        return this.screenLocked;
    }
    public void setScreenLock(boolean screenLocked) {
        this.screenLocked = screenLocked;
    }
    /**
     * If flight assist is enabled, it returns the scaled motorSpeed.
     */
    public float getAdjustedMotorSpeed() {
        if (isFlAssistEnabled()) {
            float adjustedMotorSpeed = (float) (motorSpeed * (1 + scaler));
            if (adjustedMotorSpeed > 1)
                adjustedMotorSpeed = 1;
            return adjustedMotorSpeed;
        } else {
            return motorSpeed;
        }
    }

    public void setMotorSpeed(float motorSpeed) {
        this.motorSpeed = motorSpeed;
    }
    public float getMotorSpeed() { return this.motorSpeed; }

    public void setFlAssistMode(int flMode) {
        if (!(flMode == Util.FA_OFF ||
                flMode == Util.FA_ADVANCED ||
                flMode == Util.FA_BEGINNER)) {
            return;
        }
        this.flAssistMode = flMode;
    }

    public int getFlAssistMode() {
        return this.flAssistMode;
    }

    public boolean isFlAssistEnabled() {
        return flAssistMode != Util.FA_OFF;
    }
}
