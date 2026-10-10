package com.gdavidpb.tuindice.auth.di

import com.gdavidpb.tuindice.auth.data.repository.AuthApiDataRepository
import com.gdavidpb.tuindice.auth.data.source.KtorAuthApiDataSource
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.auth.domain.repository.AuthRetryWindowRepository
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.auth.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.FakeAuthApiDataSource
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.PendingChangesRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthModuleKoinSmokeTest {
	@Test
	fun resolvesAuthViewModels() = withKoinSmokeTest(
			authModule,
			module {
				single<AuthApiDataRepository> { FakeAuthApiDataSource() }
				single<AuthRepository> { RecordingAuthRepository() }
				single<MessagingRepository> { RecordingMessagingRepository() }
				single<AttestationRepository> { FakeAttestationRepository() }
				single<SessionRepository> { FakeSessionRepository() }
				single<SessionInvalidationRepository> { FakeSessionInvalidationRepository() }
				single<SyncRepository> { FakeSyncRepository() }
				single<CredentialsRepository> { FakeCredentialsRepository() }
				single<SyncStatusRepository> { FakeSyncStatusRepository() }
				single<ApplicationRepository> { RecordingApplicationRepository() }
				single<SettingsRepository> { FakeSettingsRepository() }
			single<NetworkRepository> { FakeNetworkRepository(isAvailable = true) }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<ConfigRepository> { FakeConfigRepository() }
			single<AppEnvironmentRepository> { FakeAppEnvironmentRepository() }
				single<PendingChangesRepository> { FakePendingChangesRepository() }
				single<UsageDataConsentRepository> { InMemoryUsageDataConsentRepository() }
				single<EventPublisher> { NoOpEventPublisher }
				single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
				single { testSessionCoroutineScope() }
			}
		) {
		assertResolves(
			SignInViewModel::class,
			SignOutViewModel::class,
			UpdatePasswordViewModel::class
		)
	}

	// The platform modules build the API data source with `retryWindow = get()`, as below. This
	// module has to answer that with the same memory sign-in reads: a wait recorded in one
	// instance and looked up in another would never hold a call back.
	@Test
	fun sharesOneRetryWindow_betweenTheApiDataSourceAndSignIn() = runTest {
		withKoinSmokeTest(
			authModule,
			module {
				factory<AuthApiDataRepository> {
					KtorAuthApiDataSource(
						ktorClient = HttpClient(
							MockEngine {
								respond(
									content = "",
									status = HttpStatusCode.ServiceUnavailable,
									headers = headersOf(HttpHeaders.RetryAfter, "30")
								)
							}
						) {
							expectSuccess = true
						},
						retryWindow = get()
					)
				}
			}
		) {
			val retryWindow = get<AuthRetryWindowRepository>()

			assertEquals(0L, retryWindow.signInRemainingMillis("20-26123"))

			runCatching {
				get<AuthApiDataRepository>().bootstrapSignIn(usbId = "20-26123", password = "secret123")
			}

			assertTrue(retryWindow.signInRemainingMillis("20-26123") > 0L)
		}
	}
}
