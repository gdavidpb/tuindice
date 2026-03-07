package com.gdavidpb.tuindice.login.di

import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.RiskAttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.login.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.login.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.login.testing.FakeSessionRepository
import com.gdavidpb.tuindice.login.testing.RecordingApplicationRepository
import com.gdavidpb.tuindice.login.testing.RecordingLoginRepository
import com.gdavidpb.tuindice.login.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.login.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import org.koin.dsl.module

class LoginModuleKoinSmokeTest {
	@Test
	fun resolvesLoginViewModels() = withKoinSmokeTest(
		loginModule,
		module {
			single<LoginRepository> { RecordingLoginRepository() }
			single<MessagingRepository> { RecordingMessagingRepository() }
			single<RiskAttestationRepository> { FakeAttestationRepository() }
			single<SessionRepository> { FakeSessionRepository() }
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
