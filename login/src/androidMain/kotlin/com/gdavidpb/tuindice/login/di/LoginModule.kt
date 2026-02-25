package com.gdavidpb.tuindice.login.di

import com.gdavidpb.tuindice.login.data.repository.CrashlyticsReportingDataRepository
import com.gdavidpb.tuindice.login.data.repository.FirebaseMessagingDataRepository
import com.gdavidpb.tuindice.login.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.login.domain.repository.ReportingRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val loginModule = module {
	/* Android repositories */

	factoryOf(::CrashlyticsReportingDataRepository) { bind<ReportingRepository>() }
	factoryOf(::FirebaseMessagingDataRepository) { bind<MessagingRepository>() }
}
