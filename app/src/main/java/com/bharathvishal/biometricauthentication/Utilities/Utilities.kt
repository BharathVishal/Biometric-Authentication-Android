/**
 *
 * Copyright 2018-2023 Bharath Vishal G.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/

package com.bharathvishal.biometricauthentication.utilities

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.bharathvishal.biometricauthentication.R
import com.google.android.material.snackbar.Snackbar

object Utilities {
    fun showSnackBar(snackTitle: String?, act: Activity) {
        try {
            val view1 = act.findViewById<ConstraintLayout>(R.id.constraintlayoutMain)!!
            val snackbar: Snackbar = Snackbar.make(view1, snackTitle!!, Snackbar.LENGTH_SHORT)
            val view: View = snackbar.view

            if (!act.isFinishing)
                snackbar.show()

            val txtv = view.findViewById(R.id.snackbar_text) as TextView
            txtv.gravity = Gravity.CENTER_HORIZONTAL
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    fun isBiometricHardWareAvailable(con: Context): Boolean {
        var result = false
        val biometricManager = BiometricManager.from(con)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> result = true
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> result = false
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> result = false
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> result = false
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                    result = true
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                    result = true
                BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                    result = false
            }
        } else {
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> result = true
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> result = false
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> result = false
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> result = false
            }
        }
        return result
    }


    fun deviceHasPasswordPinLock(con: Context): Boolean {
        val keymgr = con.getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
        if (keymgr.isKeyguardSecure)
            return true
        return false
    }
}