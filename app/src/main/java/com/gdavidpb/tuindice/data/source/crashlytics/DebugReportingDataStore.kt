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

    override fun <T : Any> setCustomKey(key: String, value: T) {
        Log.d("setCustomKey", "$key = $value (${value::class.java.name})")
    }
}