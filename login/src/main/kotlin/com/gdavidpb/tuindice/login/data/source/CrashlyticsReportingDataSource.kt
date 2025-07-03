package com.gdavidpb.tuindice.login.data.source

import com.gdavidpb.tuindice.login.data.repository.ReportingDataSource
import com.google.firebase.crashlytics.FirebaseCrashlytics

class CrashlyticsReportingDataSource(
	private val crashlytics: FirebaseCrashlytics
) : ReportingDataSource {
	override suspend fun setIdentifier(id: String) {
		crashlytics.setUserId(id)
	}
}