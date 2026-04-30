package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.data.MockAttestationProviderDataSource
import com.gdavidpb.tuindice.data.repository.attestation.AttestationProviderDataRepository
import com.gdavidpb.tuindice.data.source.messaging.DebugPushTokenDataSource
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.DebugProfilePictureStorageDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.RemoteDataRepository
import com.gdavidpb.tuindice.summary.data.source.DebugSummaryRemoteDataSource
import com.gdavidpb.tuindice.summary.data.source.FileKitDebugProfilePictureStorageDataSource
import com.gdavidpb.tuindice.summary.data.source.SummaryApiDataSource
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsApiDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsLocalDataRepository
import com.gdavidpb.tuindice.subjects.data.source.DebugSubjectStatsLocalDataSource
import com.gdavidpb.tuindice.subjects.data.source.DebugSubjectsApiDataSource
import com.gdavidpb.tuindice.subjects.data.source.KtorSubjectsApiDataSource
import com.gdavidpb.tuindice.subjects.data.source.SubjectStatsRoomDataSource
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val androidDebugVariantModule = module {
	factoryOf(::MockAttestationProviderDataSource) { bind<AttestationProviderDataRepository>() }

	single<DebugProfilePictureStorageDataRepository> {
		FileKitDebugProfilePictureStorageDataSource(
			sourceName = "android-debug-summary"
		)
	}

	factory<RemoteDataRepository> {
		DebugSummaryRemoteDataSource(
			apiRemoteDataSource = get<SummaryApiDataSource>(),
			debugProfilePictureStorageDataSource = get()
		)
	}

	factory<SubjectStatsApiDataRepository> {
		DebugSubjectsApiDataSource(
			apiDataSource = get<KtorSubjectsApiDataSource>(),
			json = get()
		)
	}

	factory<SubjectStatsLocalDataRepository> {
		DebugSubjectStatsLocalDataSource(
			localDataSource = get<SubjectStatsRoomDataSource>()
		)
	}

	single<RemoteConfigDataRepository> {
		DebugRemoteConfigDataSource(
			defaults = get(),
			sourceName = "android-debug"
		)
	}

	factory<PushTokenDataRepository> {
		DebugPushTokenDataSource(
			token = "android-debug-push-token",
			sourceName = "android-debug"
		)
	}

	single<ReportingRepository> {
		DebugReportingDataSource(sourceName = "android-debug")
	}
}
