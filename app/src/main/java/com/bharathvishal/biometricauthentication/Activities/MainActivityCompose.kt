/**
 *
 * Copyright 2018-2025 Bharath Vishal G.
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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.bharathvishal.biometricauthentication.R
import com.bharathvishal.biometricauthentication.constants.Constants
import com.bharathvishal.biometricauthentication.theme.Material3AppTheme
import com.bharathvishal.biometricauthentication.utilities.Utilities
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.*
import java.util.concurrent.Executor

class MainActivityCompose : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var activityContext: Context

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var biometricHardwareVal = mutableStateOf("Unknown")
    private var deviceHasUserFingerPrintVal = mutableStateOf("Unknown")
    private var deviceHasPinPasswordVal = mutableStateOf("Unknown")
    private var authFingerprintButtonEnabledVal = mutableStateOf(true)
    private var authFingerprintButtonTextVal = mutableStateOf("Authenticate using Fingerprint")
    private var biometricAuthResultVisibilityVal = mutableStateOf(false)
    private var showSnackBarVal = mutableStateOf(false)
    private var snackBarMessageVal = mutableStateOf("-")

    override fun onCreate(savedInstanceState: Bundle?) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                enableEdgeToEdge()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        super.onCreate(savedInstanceState)

        //Applies Material dynamic theming
        try {
            DynamicColors.applyToActivityIfAvailable(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        activityContext = this

        setContent {
            Material3AppTheme(darkTheme = isSystemInDarkTheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainViewImplementation()
                }
            }
        }


        if (Utilities.deviceHasPasswordPinLock(activityContext))
            deviceHasPinPasswordVal.value = Constants.TRUE
        else
            deviceHasPinPasswordVal.value = Constants.FALSE

        executor = ContextCompat.getMainExecutor(activityContext)

        setPrompt()

        if (Utilities.isBiometricHardWareAvailable(activityContext)) {
            biometricHardwareVal.value = Constants.AVAILABLE
            deviceHasUserFingerPrintVal.value = Constants.TRUE
            authFingerprintButtonTextVal.value = Constants.AUTHENTICATE_FINGERPRINT

            //Enable the button if the device has biometric hardware available
            authFingerprintButtonEnabledVal.value = true

            initBiometricPrompt(
                Constants.BIOMETRIC_AUTHENTICATION,
                Constants.BIOMETRIC_AUTHENTICATION_SUBTITLE,
                Constants.BIOMETRIC_AUTHENTICATION_DESCRIPTION,
                false
            )
        } else {
            biometricHardwareVal.value = Constants.UNAVAILABLE
            deviceHasUserFingerPrintVal.value = Constants.FALSE
            authFingerprintButtonEnabledVal.value = false

            //Fallback, use device password/pin
            if (Utilities.deviceHasPasswordPinLock(activityContext)) {
                authFingerprintButtonEnabledVal.value = true
                authFingerprintButtonTextVal.value = Constants.AUTHENTICATE_OTHER

                initBiometricPrompt(
                    Constants.PASSWORD_PIN_AUTHENTICATION,
                    Constants.PASSWORD_PIN_AUTHENTICATION_SUBTITLE,
                    Constants.PASSWORD_PIN_AUTHENTICATION_DESCRIPTION,
                    true
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun MainViewImplementation() {
        Column {
            TopAppBarMain()
            Box(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                CardViewMain()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomStart),
                ) {
                    SnackBarViewComposable(showSnackBarVal.value, snackBarMessageVal.value)
                }
            }
        }
    }

    //Top App bar composable function
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBarMain() {
        TopAppBar(
            title = { Text("Biometric Authentication") },
            colors = TopAppBarDefaults.topAppBarColors(
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }

    //CardView composable function
    @Suppress("UNNECESSARY_SAFE_CALL")
    @Composable
    fun CardViewMain() {
        Column {
            Spacer(modifier = Modifier.padding(top = 6.dp))
            Card(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                {
                    ImageLogo()
                    TextHeader()

                    RowComponentInCard("Biometric Hardware", biometricHardwareVal.value)
                    Divider(thickness = 0.5.dp)
                    RowComponentInCard(
                        "Device Has User Fingerprint",
                        deviceHasUserFingerPrintVal.value
                    )
                    Divider(thickness = 0.5.dp)
                    RowComponentInCard("Device Has PIN/Password", deviceHasPinPasswordVal.value)
                    Spacer(modifier = Modifier.padding(top = 6.dp))
                    ButtonAuthenticate(
                        authFingerprintButtonTextVal.value,
                        authFingerprintButtonEnabledVal.value
                    )
                    Spacer(modifier = Modifier.padding(top = 6.dp))
                    TextAuthenticationResult(biometricAuthResultVisibilityVal.value)

                }//end of column
            }//end of card
        }//end of outer column
    }//end of card view main


    //Biometric Image Logo composable function
    @Composable
    fun ImageLogo() {
        Image(
            painter = painterResource(R.drawable.ic_baseline_fingerprint_24),
            contentDescription = "Image Logo",
            modifier = Modifier
                .requiredHeight(90.dp)
                .requiredWidth(90.dp)
                .padding(5.dp)
        )
    }

    //Biometric Authentication app name Text
    @Composable
    fun TextHeader() {
        Text(
            text = "BIOMETRIC AUTHENTICATION",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge
        )
    }


    //Row component composable function for Biometric related info
    @Composable
    fun RowComponentInCard(strDesc: String, mutableVal: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier.weight(0.5f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = strDesc,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .padding(5.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier.weight(0.5f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = mutableVal,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .padding(5.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    @Composable
    fun ButtonAuthenticate(btnText: String, enabledState: Boolean) {
        OutlinedButton(
            onClick = { biometricPrompt.authenticate(promptInfo) },
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            ),
            enabled = enabledState
        ) {
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = btnText)
        }
    }


    @Composable
    fun TextAuthenticationResult(visibilityState: Boolean) {
        AnimatedVisibility(visible = visibilityState) {
            Text(
                text = "Authentication success",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(5.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color("#9ccc65".toColorInt())
            )
            Spacer(modifier = Modifier.padding(bottom = 10.dp))
        }
    }

    @Composable
    fun SnackBarViewComposable(visibilityState: Boolean, message: String) {
        AnimatedVisibility(visible = visibilityState) {
            Snackbar(action = {}) {
                Text(text = message)
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
                    biometricAuthResultVisibilityVal.value = false

                    launch(Dispatchers.Default) {
                        showSnackBarVal.value = true
                        snackBarMessageVal.value = Constants.AUTHENTICATION_ERROR
                        delay(1500)
                        withContext(Dispatchers.Main) {
                            showSnackBarVal.value = false
                        }
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    biometricAuthResultVisibilityVal.value = true

                    launch(Dispatchers.Default) {
                        showSnackBarVal.value = true
                        snackBarMessageVal.value = Constants.AUTHENTICATION_SUCCEEDED
                        delay(1500)
                        withContext(Dispatchers.Main) {
                            showSnackBarVal.value = false
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Utilities.showSnackBar(
                        Constants.AUTHENTICATION_FAILED,
                        activityContext as MainActivityCompose
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
                val authFlag =
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG
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
    }


    override fun onDestroy() {
        super.onDestroy()

        //Cancels this coroutine score
        cancel()
    }

    //Preview for jetpack composable view
    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        Material3AppTheme {
            MainViewImplementation()
        }
    }
}