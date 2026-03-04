package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.data.MockAttestationProviderDataSource
import com.gdavidpb.tuindice.data.repository.attestation.ProviderDataSource as AttestationProvider
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.DebugPushTokenDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

private const val ANDROID_DEBUG_PUSH_TOKEN = "android-debug-push-token"

val androidDebugVariantModule = module {
	factoryOf(::MockAttestationProviderDataSource) { bind<AttestationProvider>() }

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
