package com.example.util

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object BiometricHelper {

    fun isDeviceSecure(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return keyguardManager?.isDeviceSecure ?: false
    }

    fun hasBiometricHardware(context: Context): Boolean {
        val pm = context.packageManager
        return pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }
}
