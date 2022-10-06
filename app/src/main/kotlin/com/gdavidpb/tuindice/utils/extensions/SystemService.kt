package com.gdavidpb.tuindice.utils.extensions

import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

fun ConnectivityManager.isNetworkAvailable(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getNetworkCapabilities(activeNetwork)?.run {
            when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
    } else {
        @Suppress("DEPRECATION")
        activeNetworkInfo?.run {
            when (type) {
                ConnectivityManager.TYPE_WIFI -> true
                ConnectivityManager.TYPE_MOBILE -> true
                else -> false
            }
        }
    } ?: false
}

fun PackageManager.hasCamera() = hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)