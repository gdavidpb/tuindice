package com.gdavidpb.tuindice.base.utils.extension

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

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