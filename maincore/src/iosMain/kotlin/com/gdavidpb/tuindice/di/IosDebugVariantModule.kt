package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.event.DebugEventSubscriber
import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.data.source.attestation.IosDebugAttestationDataSource
import com.gdavidpb.tuindice.base.data.repository.messaging.PushTokenDataRepository
import com.gdavidpb.tuindice.base.data.source.messaging.DebugPushTokenDataSource
import com.gdavidpb.tuindice.summary.data.repository.user.DebugProfilePictureStorageDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.RemoteDataRepository
import com.gdavidpb.tuindice.summary.data.source.DebugSummaryRemoteDataSource
import com.gdavidpb.tuindice.summary.data.source.FileKitDebugProfilePictureStorageDataSource
import com.gdavidpb.tuindice.summary.data.source.SummaryApiDataSource
import com.gdavidpb.tuindice.subjects.data.repository.SubjectCatalogRemoteDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsApiDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsLocalDataRepository
import com.gdavidpb.tuindice.subjects.data.source.DebugSubjectStatsLocalDataSource
import com.gdavidpb.tuindice.subjects.data.source.DebugSubjectsApiDataSource
import com.gdavidpb.tuindice.subjects.data.source.SubjectStatsRoomDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val IOS_DEBUG_SUMMARY_SOURCE = "ios-debug-summary"

val iosDebugVariantModule = module {
	single<EventSubscriber>(named("iosDebugEventSubscriber")) {
		DebugEventSubscriber(
			sourceName = "ios-debug"
		)
	}

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

	factoryOf(::DebugSubjectsApiDataSource) {
		bind<SubjectStatsApiDataRepository>()
		bind<SubjectCatalogRemoteDataRepository>()
	}

	factory<SubjectStatsLocalDataRepository> {
		DebugSubjectStatsLocalDataSource(
			localDataSource = get<SubjectStatsRoomDataSource>()
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
