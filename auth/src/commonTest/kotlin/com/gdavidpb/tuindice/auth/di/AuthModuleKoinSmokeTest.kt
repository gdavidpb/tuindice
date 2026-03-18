package com.gdavidpb.tuindice.auth.di

import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.auth.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.auth.testing.FakeSessionRepository
import com.gdavidpb.tuindice.auth.testing.RecordingApplicationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.auth.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import org.koin.dsl.module

class AuthModuleKoinSmokeTest {
	@Test
	fun resolvesAuthViewModels() = withKoinSmokeTest(
		authModule,
		module {
			single<AuthRepository> { RecordingAuthRepository() }
			single<MessagingRepository> { RecordingMessagingRepository() }
			single<AttestationRepository> { FakeAttestationRepository() }
			single<SessionRepository> { FakeSessionRepository() }
			single<SyncRepository> { FakeSyncRepository() }
			single<CredentialsRepository> { FakeCredentialsRepository() }
			single<SyncStatusRepository> { FakeSyncStatusRepository() }
			single<ApplicationRepository> { RecordingApplicationRepository() }
			single<NetworkRepository> { FakeNetworkRepository(isAvailable = true) }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<ConfigRepository> { FakeConfigRepository() }
			single<AppEnvironmentRepository> { FakeAppEnvironmentRepository() }
		}
	) {
		assertResolves(
			SignInViewModel::class,
			SignOutViewModel::class,
			UpdatePasswordViewModel::class
		)
	}
}
