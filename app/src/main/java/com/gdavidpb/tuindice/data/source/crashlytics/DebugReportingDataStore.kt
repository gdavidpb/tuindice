package com.gdavidpb.tuindice.data.source.crashlytics

import android.util.Log
import com.gdavidpb.tuindice.domain.repository.ReportingRepository

open class DebugReportingDataStore : ReportingRepository {
    override fun setIdentifier(identifier: String) {
        Log.d("setIdentifier", identifier)
    }

    override fun logException(throwable: Throwable) {
        throwable.printStackTrace()
    }

    override fun setInt(key: String, value: Int) {
        Log.d("setInt", "$key = $value")
    }

    override fun setLong(key: String, value: Long) {
        Log.d("setLong", "$key = $value")
    }

    override fun setString(key: String, value: String) {
        Log.d("v", "$key = $value")
    }
}