package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.data.MockAttestationProviderDataSource
import com.gdavidpb.tuindice.data.repository.attestation.AttestationProviderDataSource
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.DebugPushTokenDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.source.DebugSummaryUserRepository
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

private const val ANDROID_DEBUG_PUSH_TOKEN = "android-debug-push-token"
private const val ANDROID_DEBUG_SUMMARY_SOURCE = "android-debug-summary"

val androidDebugVariantModule = module {
	factoryOf(::MockAttestationProviderDataSource) { bind<AttestationProviderDataSource>() }

	factory<UserRepository> {
		DebugSummaryUserRepository(
			localDataSource = get<LocalDataSource>(),
			sourceName = ANDROID_DEBUG_SUMMARY_SOURCE
		)
	}

	single<RemoteConfigDataSource> {
		DebugRemoteConfigDataSource(
			defaults = get(),
			sourceName = "android-debug"
		)
	}

	factory<PushTokenDataSource> {
		DebugPushTokenDataSource(
			token = ANDROID_DEBUG_PUSH_TOKEN,
			sourceName = "android-debug"
		)
	}

	single<ReportingRepository> {
		DebugReportingDataSource(sourceName = "android-debug")
	}
}
