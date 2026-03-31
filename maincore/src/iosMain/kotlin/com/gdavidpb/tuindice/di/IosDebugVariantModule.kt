package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.contract.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.data.source.attestation.IosDebugAttestationDataSource
import com.gdavidpb.tuindice.data.contract.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.source.messaging.DebugPushTokenDataSource
import com.gdavidpb.tuindice.summary.data.contract.user.DebugProfilePictureStorageDataSource
import com.gdavidpb.tuindice.summary.data.source.user.DebugSummaryRemoteDataSource
import com.gdavidpb.tuindice.summary.data.source.user.FileKitDebugProfilePictureStorageDataSource
import com.gdavidpb.tuindice.summary.data.contract.user.RemoteDataSource
import com.gdavidpb.tuindice.summary.data.source.user.SummaryApiDataSource
import org.koin.dsl.module

private const val IOS_DEBUG_SUMMARY_SOURCE = "ios-debug-summary"

val iosDebugVariantModule = module {
	factory<AttestationRepository> {
		IosDebugAttestationDataSource()
	}

	single<DebugProfilePictureStorageDataSource> {
		FileKitDebugProfilePictureStorageDataSource(
			sourceName = IOS_DEBUG_SUMMARY_SOURCE
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
