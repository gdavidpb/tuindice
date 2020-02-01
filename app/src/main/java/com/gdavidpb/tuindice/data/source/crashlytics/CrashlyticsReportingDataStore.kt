package com.gdavidpb.tuindice.data.source.crashlytics

import com.crashlytics.android.Crashlytics
import com.gdavidpb.tuindice.domain.repository.ReportingRepository

open class CrashlyticsReportingDataStore : ReportingRepository {
    override fun setIdentifier(identifier: String) {
        Crashlytics.setUserIdentifier(identifier)
    }

    override fun logException(throwable: Throwable) {
        Crashlytics.logException(throwable)
    }

    override fun setInt(key: String, value: Int) {
        Crashlytics.setInt(key, value)
    }

    override fun setLong(key: String, value: Long) {
        Crashlytics.setLong(key, value)
    }
}