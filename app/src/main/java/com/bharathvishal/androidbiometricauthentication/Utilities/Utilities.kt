package com.bharathvishal.androidbiometricauthentication.Utilities

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.bharathvishal.androidbiometricauthentication.R
import com.google.android.material.snackbar.Snackbar

object Utilities {
    fun showSnackBar(snackTitle: String?, act: Activity) {
        try {
            val view1 = act.findViewById<ConstraintLayout>(R.id.constraintlayoutMain)!!
            val snackbar: Snackbar = Snackbar.make(view1, snackTitle!!,  Snackbar.LENGTH_SHORT)
            val view: View = snackbar.view

            if (!act.isFinishing)
                snackbar.show()

            val txtv = view.findViewById(R.id.snackbar_text) as TextView
            txtv.gravity = Gravity.CENTER_HORIZONTAL
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    fun isBiometricHardWareAvailable(con: Context):Boolean {
        var result=false
        val biometricManager = BiometricManager.from(con)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->result=true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->result=false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> result=false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->result=false
        }
        return result
    }


    fun deviceHasPasswordPinLock(con: Context):Boolean {
        val keymgr=con.getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
        if(keymgr.isKeyguardSecure)
            return true
        return false
    }
}