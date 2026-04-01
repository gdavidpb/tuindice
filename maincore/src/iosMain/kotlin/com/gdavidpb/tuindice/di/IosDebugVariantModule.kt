package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.data.source.attestation.IosDebugAttestationDataSource
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataRepository
import com.gdavidpb.tuindice.data.source.messaging.DebugPushTokenDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.DebugProfilePictureStorageDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.RemoteDataRepository
import com.gdavidpb.tuindice.summary.data.source.DebugSummaryRemoteDataSource
import com.gdavidpb.tuindice.summary.data.source.FileKitDebugProfilePictureStorageDataSource
import com.gdavidpb.tuindice.summary.data.source.SummaryApiDataSource
import org.koin.dsl.module

private const val IOS_DEBUG_SUMMARY_SOURCE = "ios-debug-summary"

val iosDebugVariantModule = module {
	factory<AttestationRepository> {
		IosDebugAttestationDataSource()
	}

	single<DebugProfilePictureStorageDataRepository> {
		FileKitDebugProfilePictureStorageDataSource(
			sourceName = IOS_DEBUG_SUMMARY_SOURCE
		)
	}
	factory<RemoteDataRepository> {
		DebugSummaryRemoteDataSource(
			apiRemoteDataSource = get<SummaryApiDataSource>(),
			debugProfilePictureStorageDataSource = get()
		)
	}

	single<RemoteConfigDataRepository> {
		DebugRemoteConfigDataSource(
			defaults = get(),
			sourceName = "ios-debug"
		)
	}

	factory<PushTokenDataRepository> {
		DebugPushTokenDataSource(
			token = "ios-debug-push-token",
			sourceName = "ios-debug"
		)
	}

	single<ReportingRepository> {
		DebugReportingDataSource(sourceName = "ios-debug")
	}
}
