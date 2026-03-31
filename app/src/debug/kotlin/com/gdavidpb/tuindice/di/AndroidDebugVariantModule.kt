package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.contract.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.data.MockAttestationProviderDataSource
import com.gdavidpb.tuindice.data.contract.attestation.AttestationProviderDataSource
import com.gdavidpb.tuindice.data.source.messaging.DebugPushTokenDataSource
import com.gdavidpb.tuindice.data.contract.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.summary.data.contract.user.DebugProfilePictureStorageDataSource
import com.gdavidpb.tuindice.summary.data.source.user.DebugSummaryRemoteDataSource
import com.gdavidpb.tuindice.summary.data.source.user.FileKitDebugProfilePictureStorageDataSource
import com.gdavidpb.tuindice.summary.data.contract.user.RemoteDataSource
import com.gdavidpb.tuindice.summary.data.source.user.SummaryApiDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val androidDebugVariantModule = module {
	factoryOf(::MockAttestationProviderDataSource) { bind<AttestationProviderDataSource>() }

	single<DebugProfilePictureStorageDataSource> {
		FileKitDebugProfilePictureStorageDataSource(
			sourceName = "android-debug-summary"
		)
	}

	factory<RemoteDataSource> {
		DebugSummaryRemoteDataSource(
			apiRemoteDataSource = get<SummaryApiDataSource>(),
			debugProfilePictureStorageDataSource = get()
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
			token = "android-debug-push-token",
			sourceName = "android-debug"
		)
	}

	single<ReportingRepository> {
		DebugReportingDataSource(sourceName = "android-debug")
	}
}
