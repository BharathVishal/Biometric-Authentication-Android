/**
 *
 * Copyright 2018-2024 Bharath Vishal G.
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

package com.bharathvishal.biometricauthentication.activities

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.bharathvishal.biometricauthentication.databinding.ActivityMainBinding
import com.bharathvishal.biometricauthentication.constants.Constants
import com.bharathvishal.biometricauthentication.utilities.Utilities
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity() {
    private lateinit var activityContext: Context

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        activityContext = this

        if (Utilities.deviceHasPasswordPinLock(activityContext))
            binding.DeviceHasPINPasswordLock.text = Constants.TRUE
        else
            binding.DeviceHasPINPasswordLock.text = Constants.FALSE

        executor = ContextCompat.getMainExecutor(activityContext)

        setPrompt()

        if (Utilities.isBiometricHardWareAvailable(activityContext)) {
            binding.DeviceHasBiometricFeatures.text = Constants.AVAILABLE
            binding.DeviceHasFingerPrint.text = Constants.TRUE

            //Enable the button if the device has biometric hardware available
            binding.authenticatefingerprintbutton.isEnabled = true

            initBiometricPrompt(
                Constants.BIOMETRIC_AUTHENTICATION,
                Constants.BIOMETRIC_AUTHENTICATION_SUBTITLE,
                Constants.BIOMETRIC_AUTHENTICATION_DESCRIPTION,
                false
            )
        } else {
            binding.DeviceHasBiometricFeatures.text = Constants.UNAVAILABLE
            binding.DeviceHasFingerPrint.text = Constants.FALSE
            binding.authenticatefingerprintbutton.isEnabled = false

            //Fallback, use device password/pin
            if (Utilities.deviceHasPasswordPinLock(activityContext)) {
                binding.authenticatefingerprintbutton.isEnabled = true
                binding.authenticatefingerprintbutton.text = Constants.AUTHENTICATE_OTHER

                initBiometricPrompt(
                    Constants.PASSWORD_PIN_AUTHENTICATION,
                    Constants.PASSWORD_PIN_AUTHENTICATION_SUBTITLE,
                    Constants.PASSWORD_PIN_AUTHENTICATION_DESCRIPTION,
                    true
                )
            }
        }
    }

    private fun setPrompt() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Utilities.showSnackBar(
                        Constants.AUTHENTICATION_ERROR + " " + errString,
                        activityContext as MainActivity
                    )
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Utilities.showSnackBar(
                        Constants.AUTHENTICATION_SUCCEEDED,
                        activityContext as MainActivity
                    )
                    binding.textViewAuthResult.visibility = View.VISIBLE
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Utilities.showSnackBar(
                        Constants.AUTHENTICATION_FAILED,
                        activityContext as MainActivity
                    )
                }
            })
    }

    private fun initBiometricPrompt(
        title: String,
        subtitle: String,
        description: String,
        setDeviceCred: Boolean
    ) {
        if (setDeviceCred) {
            /*For API level > 30
              Newer API setAllowedAuthenticators is used*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val authFlag = DEVICE_CREDENTIAL or BIOMETRIC_STRONG
                promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setDescription(description)
                    .setAllowedAuthenticators(authFlag)
                    .build()
            } else {
                /*SetDeviceCredentials method deprecation is ignored here
                  as this block is for API level<30*/
                @Suppress("DEPRECATION")
                promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setDescription(description)
                    .setDeviceCredentialAllowed(true)
                    .build()
            }
        } else {
            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText(Constants.CANCEL)
                .build()
        }

        binding.authenticatefingerprintbutton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }
}