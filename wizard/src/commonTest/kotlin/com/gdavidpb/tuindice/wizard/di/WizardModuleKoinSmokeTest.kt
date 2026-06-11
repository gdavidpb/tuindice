package com.gdavidpb.tuindice.wizard.di

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import com.gdavidpb.tuindice.wizard.presentation.model.WizardTopBarActionBus
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.WizardViewModel
import kotlin.test.Test
import org.koin.dsl.module

class WizardModuleKoinSmokeTest {
	@Test
	fun resolvesWizardRuntime() = withKoinSmokeTest(
		wizardModule,
		module {
			single<SettingsRepository> { FakeSettingsRepository() }
			single<SessionRepository> { FakeSessionRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<EventPublisher> { NoOpEventPublisher }
			single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
		}
	) {
		assertResolves(
			WizardViewModel::class,
			WizardTopBarActionBus::class
		)
	}
}
