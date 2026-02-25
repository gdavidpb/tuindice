package com.gdavidpb.tuindice.login.data.repository

import com.gdavidpb.tuindice.login.domain.repository.ReportingRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics

class CrashlyticsReportingDataRepository(
	private val crashlytics: FirebaseCrashlytics
) : ReportingRepository {
	override suspend fun setIdentifier(id: String) {
		crashlytics.setUserId(id)
	}
}