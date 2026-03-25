package com.gdavidpb.tuindice.base.utils.extension

import android.Manifest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun ConnectivityManager.isNetworkAvailable(): Boolean {
	return with(getNetworkCapabilities(activeNetwork)) {
		when {
			this == null -> false
			hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
			hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
			hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
			else -> false
		}
	}
}
