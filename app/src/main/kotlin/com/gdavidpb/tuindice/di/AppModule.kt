package com.gdavidpb.tuindice.di

import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.data.repository.SessionDataRepository
import com.gdavidpb.tuindice.base.data.source.InMemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.MemorySessionDataSource
import com.gdavidpb.tuindice.base.data.source.PreferencesSessionDataSource
import com.gdavidpb.tuindice.base.data.source.SharedPreferencesSessionDataSource
import com.gdavidpb.tuindice.base.data.source.UUIDIdentifierDataSource
import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.data.repository.attestation.AttestationDataRepository
import com.gdavidpb.tuindice.data.repository.attestation.DigestDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.ChallengeApiDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.PlayIntegrityDataSource
import com.gdavidpb.tuindice.data.repository.attestation.source.SHA256DigestDataSource
import com.gdavidpb.tuindice.data.repository.messaging.MessagingDataRepository
import com.gdavidpb.tuindice.data.repository.messaging.source.FirebaseMessagingDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.MessagingApiDataSource
import com.gdavidpb.tuindice.data.repository.messaging.source.MessagingPreferencesDataSource
import com.gdavidpb.tuindice.data.source.application.AndroidApplicationDataSource
import com.gdavidpb.tuindice.data.source.config.RemoteConfigDataSource
import com.gdavidpb.tuindice.data.source.di.ReleaseKoinDataSource
import com.gdavidpb.tuindice.data.source.network.AndroidNetworkDataSource
import com.gdavidpb.tuindice.data.source.reporting.CrashlyticsReportingDataSource
import com.gdavidpb.tuindice.data.source.settings.PreferencesDataSource
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastDestinationUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.login.domain.model.SignInAttestationPayload
import com.gdavidpb.tuindice.login.domain.repository.AuthApiRepository
import com.gdavidpb.tuindice.presentation.action.browser.NavigateToActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.OpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.SetLoadingActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestReviewActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestUpdateActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.SetLastDestinationActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.StartUpActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.UpdateStateActionProcessor
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.utils.UserAgent
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
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

	factoryOf(::NavigateToActionProcessor)
	factoryOf(::SetLoadingActionProcessor)
	factoryOf(::OpenExternalResourceActionProcessor)

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
		val masterKey = MasterKey.Builder(androidContext())
			.setRequestStrongBoxBacked(true)
			.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
			.build()

		EncryptedSharedPreferences.create(
			androidContext(),
			androidContext().packageName,
			masterKey,
			EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
			EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
		)
	}

	single {
		ResourceResolver(androidContext())
	}

	single {
		AppUpdateManagerFactory.create(androidContext())
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

			val userAgent = runCatching { UserAgent(androidContext()) }.getOrNull()

			install(DefaultRequest) {
				url(BuildConfig.URL_API)

				contentType(ContentType.Application.Json)

				if (userAgent != null)
					userAgent("$userAgent")
			}

			install(HttpTimeout) {
				val configRepository = get<ConfigRepository>()
				val timeout = configRepository.getTimeout()

				requestTimeoutMillis = timeout
				connectTimeoutMillis = timeout
				socketTimeoutMillis = timeout
			}

			install(ContentNegotiation) {
				json(get())
			}

			install(Logging) {
				logger = get<Logger>()
				level = LogLevel.ALL

				sanitizeHeader { header ->
					header == HttpHeaders.Authorization
				}
			}

			install(Auth) {
				bearer {
					loadTokens {
						val sessionRepository = get<SessionRepository>()

						val hasActiveTokens = sessionRepository.hasActiveSession()

						if (hasActiveTokens)
							BearerTokens(
								accessToken = sessionRepository.getAccessToken(),
								refreshToken = sessionRepository.getRefreshToken()
							)
						else
							null
					}

					refreshTokens {
						val sessionRepository = get<SessionRepository>()
						val authApiRepository = get<AuthApiRepository>()

						val oldAccessToken = oldTokens
							?.accessToken
							?: sessionRepository.getAccessToken()

						val oldRefreshToken = oldTokens
							?.refreshToken
							?: sessionRepository.getRefreshToken()

						val response = authApiRepository.refreshTokens(
							accessToken = oldAccessToken,
							refreshToken = oldRefreshToken
						)

						sessionRepository.setAccessToken(response.accessToken)
						sessionRepository.setRefreshToken(response.refreshToken)

						BearerTokens(
							accessToken = response.accessToken,
							refreshToken = response.refreshToken
						)
					}
				}
			}
		}
	}

	single<Logger> {
		object : Logger {
			override fun log(message: String) {
				get<ReportingRepository>().logMessage(message)
			}
		}
	}

	single {
		Json {
			serializersModule = SerializersModule {
				polymorphic(AttestationPayload::class) {
					subclass(SignInAttestationPayload::class)
				}
			}
		}
	}

	/* Repositories */

	factoryOf(::SessionDataRepository) { bind<SessionRepository>() }
	factoryOf(::MessagingDataRepository) { bind<MessagingRepository>() }
	factoryOf(::AttestationDataRepository) { bind<AttestationRepository>() }

	/* Data sources */

	singleOf(::InMemorySessionDataSource) { bind<MemorySessionDataSource>() }
	singleOf(::SharedPreferencesSessionDataSource) { bind<PreferencesSessionDataSource>() }
	singleOf(::PlayIntegrityDataSource) { bind<AttestationProvider>() }
	singleOf(::UUIDIdentifierDataSource) { bind<IdentifierRepository>() }
	singleOf(::MessagingApiDataSource) { bind<MessagingRemote>() }
	singleOf(::ChallengeApiDataSource) { bind<AttestationRemote>() }
	singleOf(::SHA256DigestDataSource) { bind<DigestDataSource>() }
	singleOf(::FirebaseMessagingDataSource) { bind<MessagingProvider>() }
	singleOf(::MessagingPreferencesDataSource) { bind<MessagingLocal>() }
	singleOf(::AndroidApplicationDataSource) { bind<ApplicationRepository>() }
	singleOf(::PreferencesDataSource) { bind<SettingsRepository>() }
	singleOf(::RemoteConfigDataSource) { bind<ConfigRepository>() }
	singleOf(::CrashlyticsReportingDataSource) { bind<ReportingRepository>() }
	singleOf(::ReleaseKoinDataSource) { bind<DependenciesRepository>() }
	singleOf(::AndroidNetworkDataSource) { bind<NetworkRepository>() }
}