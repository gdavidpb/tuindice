package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.data.repository.attestation.IosDebugAttestationDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.DebugPushTokenDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.source.DebugSummaryUserRepository
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import org.koin.dsl.module

private const val IOS_DEBUG_SUMMARY_SOURCE = "ios-debug-summary"

val iosDebugVariantModule = module {
	factory<AttestationRepository> {
		IosDebugAttestationDataRepository()
	}

	factory<UserRepository> {
		DebugSummaryUserRepository(
			localDataSource = get<LocalDataSource>(),
			sourceName = IOS_DEBUG_SUMMARY_SOURCE
		)
	}

	single<RemoteConfigDataSource> {
		DebugRemoteConfigDataSource(
			defaults = get(),
			sourceName = "ios-debug"
		)
	}

	factory<PushTokenDataSource> {
		DebugPushTokenDataSource(
			token = "ios-debug-push-token",
			sourceName = "ios-debug"
		)
	}

	single<ReportingRepository> {
		DebugReportingDataSource(sourceName = "ios-debug")
	}
}
