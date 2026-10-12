@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.di

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataRepository
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsRevalidationRepository
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.FakeSettingsDataSource
import com.gdavidpb.tuindice.evaluations.testing.InMemoryEvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingSyncStatusRepository
import com.gdavidpb.tuindice.evaluations.testing.createEvaluationsMutationEngine
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleEvaluationsRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class EvaluationsModuleKoinSmokeTest {
	@Test
	fun resolvesEvaluationsViewModels() = withKoinSmokeTest(
		evaluationsModule,
		module {
			single<EvaluationRepository> { RecordingEvaluationRepository() }
			single<EvaluationsSelectionRepository> { InMemoryEvaluationsSelectionRepository() }
			single<IdentifierRepository> { FakeIdentifierRepository() }
			single<RecordDataPrerequisiteRepository> { ReadyRecordDataPrerequisiteRepository() }
			single<SyncStatusRepository> { RecordingSyncStatusRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<EventPublisher> { NoOpEventPublisher }
			single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
			single<Clock> { Clock.System }
		}
	) {
		assertResolves(
			EvaluationsViewModel::class,
			EvaluationViewModel::class,
			VisibleEvaluationsRepository::class
		)
	}

	// One instance behind both contracts: the tab and the revalidation after a sync must share its
	// download lock, or they download twice at once and the lock protects nothing.
	@Test
	fun bindsRevalidationToTheSameDataSourceAsTheRepository() = withKoinSmokeTest(
		evaluationsModule,
		module {
			single<DatabaseDataRepository> { FakeDatabaseDataSource() }
			single<EvaluationsApiDataRepository> { FakeEvaluationsApiDataSource() }
			single<SettingsDataRepository> { FakeSettingsDataSource(onCooldown = false) }
			single<StoreBackedMutationEngine<String, EvaluationMutation, EvaluationMutationAck>>(
				named("evaluationsMutationEngine")
			) { createEvaluationsMutationEngine() }
			single<IdentifierRepository> { FakeIdentifierRepository() }
			single<Clock> { Clock.System }
		}
	) {
		assertSame<Any>(get<EvaluationRepository>(), get<EvaluationsRevalidationRepository>())
	}
}
