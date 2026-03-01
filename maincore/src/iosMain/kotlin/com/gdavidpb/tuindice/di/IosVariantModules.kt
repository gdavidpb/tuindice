package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.data.ios.IosDebugAttestationDataRepository
import com.gdavidpb.tuindice.data.ios.IosDebugLoginMessagingDataSource
import com.gdavidpb.tuindice.login.data.repository.LoginMessagingDataSource
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val iosDebugVariantModule: Module = module {
	factory<AttestationRepository> {
		IosDebugAttestationDataRepository(
			identityHttpClient = get<HttpClient>(qualifier = named(IOS_IDENTITY_HTTP_CLIENT_QUALIFIER))
		)
	}

	factory<LoginMessagingDataSource> {
		IosDebugLoginMessagingDataSource()
	}
}

internal fun iosVariantModules(buildVariant: IosBuildVariant): List<Module> {
	return when (buildVariant) {
		IosBuildVariant.DEBUG -> listOf(iosDebugVariantModule)
		IosBuildVariant.PRODUCTION -> emptyList()
	}
}
