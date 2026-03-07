package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.RiskAttestationRepository
import com.gdavidpb.tuindice.data.ios.IosDebugRiskAttestationDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.PushTokenDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.DebugPushTokenDataSource
import com.gdavidpb.tuindice.platform.ios.IOS_IDENTITY_HTTP_CLIENT_QUALIFIER
import io.ktor.client.*
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val IOS_DEBUG_PUSH_TOKEN = "ios-debug-push-token"

val iosDebugVariantModule = module {
	factory<RiskAttestationRepository> {
		IosDebugRiskAttestationDataRepository(
			identityHttpClient = get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER)),
			attestationCapability = get()
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
			token = IOS_DEBUG_PUSH_TOKEN,
			sourceName = "ios-debug"
		)
	}

	single<ReportingRepository> {
		DebugReportingDataSource(sourceName = "ios-debug")
	}
}
