package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.data.repository.attestation.IosDebugAttestationDataRepository
import com.gdavidpb.tuindice.data.source.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.source.messaging.DebugPushTokenDataSource
import com.gdavidpb.tuindice.summary.data.source.user.DebugProfilePictureStorageDataSource
import com.gdavidpb.tuindice.summary.data.source.user.DebugSummaryRemoteDataSource
import com.gdavidpb.tuindice.summary.data.source.user.FileKitDebugProfilePictureStorageDataSource
import com.gdavidpb.tuindice.summary.data.source.user.RemoteDataSource
import com.gdavidpb.tuindice.summary.di.summaryApiRemoteDataSourceQualifier
import org.koin.dsl.module

private const val IOS_DEBUG_SUMMARY_SOURCE = "ios-debug-summary"

val iosDebugVariantModule = module {
	factory<AttestationRepository> {
		IosDebugAttestationDataRepository()
	}

	single<DebugProfilePictureStorageDataSource> {
		FileKitDebugProfilePictureStorageDataSource(
			sourceName = IOS_DEBUG_SUMMARY_SOURCE
		)
	}
	factory<RemoteDataSource> {
		DebugSummaryRemoteDataSource(
			apiRemoteDataSource = get(qualifier = summaryApiRemoteDataSourceQualifier),
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
