package com.gdavidpb.tuindice.data.source.network

import android.net.ConnectivityManager
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.utils.extension.isNetworkAvailable

class AndroidNetworkDataSource(
        private val connectivityManager: ConnectivityManager
) : NetworkRepository {
    override fun isAvailable(): Boolean {
        return connectivityManager.isNetworkAvailable()
    }
}