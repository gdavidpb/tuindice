package com.gdavidpb.tuindice.di

import android.util.Log
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.data.DebugKoinDataSource
import com.gdavidpb.tuindice.data.DebugReportingDataSource
import com.gdavidpb.tuindice.data.MockAttestationProviderDataSource
import com.gdavidpb.tuindice.data.MockRemoteConfigDataSource
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.ktor.client.plugins.logging.Logger
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import com.gdavidpb.tuindice.data.repository.attestation.ProviderDataSource as AttestationProvider

val appMockModule = module {
	/* Firebase */

	single {
		FirebaseCrashlytics.getInstance().apply {
			isCrashlyticsCollectionEnabled = false
		}
	}

	single<Logger> {
		object : Logger {
			override fun log(message: String) {
				Log.i("Http Client", message)
			}
		}
	}

	/* Data sources */

	factoryOf(::MockAttestationProviderDataSource) { bind<AttestationProvider>() }
	factoryOf(::MockRemoteConfigDataSource) { bind<ConfigRepository>() }
	factoryOf(::DebugReportingDataSource) { bind<ReportingRepository>() }
	factoryOf(::DebugKoinDataSource) { bind<DependenciesRepository>() }
}