package com.gdavidpb.tuindice.di

import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.data.repository.source.api.retrofit.AuthorizationInterceptor
import com.gdavidpb.tuindice.base.data.repository.source.uuid.UUIDIdentifierDataSource
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.MobileServicesRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.base.utils.extension.sharedPreferences
import com.gdavidpb.tuindice.data.repository.attestation.AttestationApi
import com.gdavidpb.tuindice.data.repository.attestation.AttestationDataRepository
import com.gdavidpb.tuindice.data.repository.attestation.source.AttestationApiDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.DigestDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.PlayIntegrityDataSource
import com.gdavidpb.tuindice.data.repository.messaging.MessagingApi
import com.gdavidpb.tuindice.data.repository.messaging.MessagingDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.source.FirebaseMessagingDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.MessagingApiDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.MessagingPreferencesDataSource
import com.gdavidpb.tuindice.data.source.application.AndroidApplicationDataSource
import com.gdavidpb.tuindice.data.source.auth.FirebaseAuthDataSource
import com.gdavidpb.tuindice.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.data.source.di.ReleaseKoinDataSource
import com.gdavidpb.tuindice.data.source.mobile.GooglePlayServicesDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.reporting.CrashlyticsReportingDataSource
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataSource
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastDestinationUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.presentation.action.browser.CloseBrowserDialogActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.ConfirmOpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.NavigateToActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.OpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.SetLoadingActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.CloseMainDialogActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestReviewActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestUpdateActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.SetLastDestinationActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.StartUpActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.UpdateStateActionProcessor
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import com.gdavidpb.tuindice.data.repository.attestation.LocalDataSource as AttestationLocal
import com.gdavidpb.tuindice.data.repository.attestation.ProviderDataSource as AttestationProvider
import com.gdavidpb.tuindice.data.repository.attestation.RemoteDataSource as AttestationRemote
import com.gdavidpb.tuindice.data.repository.messaging.LocalDataSource as MessagingLocal
import com.gdavidpb.tuindice.data.repository.messaging.ProviderDataSource as MessagingProvider
import com.gdavidpb.tuindice.data.repository.messaging.RemoteDataSource as MessagingRemote

val appModule = module {
	/* View Models */

	viewModelOf(::MainViewModel)
	viewModelOf(::BrowserViewModel)

	/* Action processor */

	factoryOf(::UpdateStateActionProcessor)
	factoryOf(::StartUpActionProcessor)
	factoryOf(::RequestReviewActionProcessor)
	factoryOf(::RequestUpdateActionProcessor)
	factoryOf(::SetLastDestinationActionProcessor)
	factoryOf(::CloseMainDialogActionProcessor)

	factoryOf(::NavigateToActionProcessor)
	factoryOf(::SetLoadingActionProcessor)
	factoryOf(::OpenExternalResourceActionProcessor)
	factoryOf(::ConfirmOpenExternalResourceActionProcessor)
	factoryOf(::CloseBrowserDialogActionProcessor)

	/* Use cases */

	factoryOf(::StartUpUseCase)
	factoryOf(::RequestReviewUseCase)
	factoryOf(::SetLastDestinationUseCase)
	factoryOf(::GetUpdateInfoUseCase)

	/* Exception handlers */

	factoryOf(::StartUpExceptionHandler)

	/* Android Services */

	single {
		androidContext().getSystemService<ConnectivityManager>()
	}

	single {
		androidContext().contentResolver
	}

	single {
		androidContext().resources
	}

	single {
		androidContext().sharedPreferences()
	}

	single {
		ResourceResolver(androidContext())
	}

	single {
		AppUpdateManagerFactory.create(androidContext())
	}

	single {
		GoogleApiAvailability.getInstance()
	}

	single {
		ReviewManagerFactory.create(androidContext())
	}

	/* Firebase */

	single {
		FirebaseRemoteConfig.getInstance().apply {
			setDefaultsAsync(R.xml.default_remote_config)
		}
	}

	single {
		FirebaseAuth.getInstance()
	}

	single {
		FirebaseMessaging.getInstance()
	}

	single {
		FirebaseCrashlytics.getInstance()
	}

	single {
		IntegrityManagerFactory.create(androidContext())
	}

	/* KtorHttpClient */

	single {
		HttpClient(CIO) {
			expectSuccess = true

			install(DefaultRequest) {
				url(BuildConfig.ENDPOINT_TU_INDICE_API)
			}

			install(HttpTimeout) {
				val configRepository = get<ConfigRepository>()
				val timeout = configRepository.getConnectionTimeout()

				requestTimeoutMillis = timeout
				connectTimeoutMillis = timeout
				socketTimeoutMillis = timeout
			}

			install(ContentNegotiation) {
				json()
			}

			install(Logging) {
				logger = Logger.ANDROID
				level = LogLevel.ALL

				sanitizeHeader { header ->
					header == HttpHeaders.Authorization
				}
			}

			install(get<ClientPlugin<Unit>>(named("Authorization")))
		}
	}

	single(named("Authorization")) {
		createClientPlugin("Authorization") {
			onRequest { request, _ ->
				val authRepository = get<AuthRepository>()
				val isActiveAuth = authRepository.isActiveAuth()

				if (isActiveAuth) {
					val bearerToken = authRepository.getActiveToken()

					request.bearerAuth(token = bearerToken)
				}
			}
		}
	}

	/* OkHttpClient */

	singleOf(::AuthorizationInterceptor)

	single {
		val logger = HttpLoggingInterceptor.Logger { message ->
			get<ReportingRepository>().logMessage(message)
		}

		HttpLoggingInterceptor(logger).apply {
			level = HttpLoggingInterceptor.Level.BODY

			redactHeader("Cookie")
			redactHeader("Authorization")
		}
	}

	single {
		val connectionTimeout = get<ConfigRepository>().getConnectionTimeout()

		OkHttpClient.Builder()
			.callTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.readTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.writeTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
			.addInterceptor(get<HttpLoggingInterceptor>())
			.addInterceptor(get<AuthorizationInterceptor>())
			.build()
	}

	/* Apis */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(get())
			.client(get())
			.build()
			.create<MessagingApi>()
	}

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(get())
			.client(get())
			.build()
			.create<AttestationApi>()
	}

	/* Utils */

	factory {
		Json.asConverterFactory("application/json".toMediaType())
	}

	/* Repositories */

	factoryOf(::MessagingDataRepository) { bind<MessagingRepository>() }
	factoryOf(::AttestationDataRepository) { bind<AttestationRepository>() }

	/* Data sources */

	factoryOf(::UUIDIdentifierDataSource) { bind<IdentifierRepository>() }
	factoryOf(::MessagingApiDataSource) { bind<MessagingRemote>() }
	factoryOf(::DigestDataSource) { bind<AttestationLocal>() }
	factoryOf(::AttestationApiDataSource) { bind<AttestationRemote>() }
	factoryOf(::PlayIntegrityDataSource) { bind<AttestationProvider>() }
	factoryOf(::FirebaseMessagingDataSource) { bind<MessagingProvider>() }
	factoryOf(::MessagingPreferencesDataSource) { bind<MessagingLocal>() }
	factoryOf(::AndroidApplicationDataSource) { bind<ApplicationRepository>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsRepository>() }
	factoryOf(::RemoteConfigDataSource) { bind<ConfigRepository>() }
	factoryOf(::FirebaseAuthDataSource) { bind<AuthRepository>() }
	factoryOf(::CrashlyticsReportingDataSource) { bind<ReportingRepository>() }
	factoryOf(::ReleaseKoinDataSource) { bind<DependenciesRepository>() }
	factoryOf(::AndroidNetworkDataSource) { bind<NetworkRepository>() }
	factoryOf(::GooglePlayServicesDataSource) { bind<MobileServicesRepository>() }
}