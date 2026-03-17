package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.RiskAttestationRepository
import com.gdavidpb.tuindice.data.ios.IosDebugRiskAttestationDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.DebugPushTokenDataSource
import org.koin.dsl.module

val iosDebugVariantModule = module {
	factory<RiskAttestationRepository> {
		IosDebugRiskAttestationDataRepository()
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
