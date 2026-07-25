package com.gdavidpb.tuindice.evaluations.di

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_STORE_ID
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataRepository
import com.gdavidpb.tuindice.evaluations.data.resolver.VisibleEvaluationsStateResolver
import com.gdavidpb.tuindice.evaluations.data.source.EvaluationDataSource
import com.gdavidpb.tuindice.evaluations.data.source.KtorEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.source.LocalSettingsDataSource
import com.gdavidpb.tuindice.evaluations.data.source.RoomDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.EnsureEvaluationsLoadedUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.SetSelectedWeekUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationMachine
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsMachine
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.persistence.data.room.RoomMutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.data.room.daos.PendingMutationDao
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val EVALUATIONS_MUTATION_STORE_QUALIFIER = "evaluationsMutationStore"
private const val EVALUATIONS_MUTATION_ENGINE_QUALIFIER = "evaluationsMutationEngine"

val evaluationsModule = module {
	/* View models */

	viewModelOf(::EvaluationsViewModel)
	viewModelOf(::EvaluationViewModel)

	factoryOf(::EvaluationsMachine)
	factoryOf(::EvaluationMachine)

	/* Use cases */

	factoryOf(::GetEvaluationAndAvailableAttemptsUseCase)
	factoryOf(::GetEvaluationsUseCase)
	factoryOf(::EnsureEvaluationsLoadedUseCase)
	factoryOf(::UpdateEvaluationsUseCase)
	factoryOf(::GetEvaluationUseCase)
	factoryOf(::UpdateEvaluationUseCase)
	factoryOf(::RemoveEvaluationUseCase)
	factoryOf(::AddEvaluationUseCase)
	factoryOf(::GetAvailableAttemptsUseCase)
	factoryOf(::SetSelectedWeekUseCase)

	/* Validators */

	factoryOf(::AddEvaluationParamsValidator)

	/* Repositories */

	singleOf(::VisibleEvaluationsStateResolver)
	single<MutationEnvelopeStore<String, EvaluationMutation>>(named(EVALUATIONS_MUTATION_STORE_QUALIFIER)) {
		RoomMutationEnvelopeStore(
			pendingMutationDao = get<PendingMutationDao>(),
			transactionRunner = get<PersistenceTransactionRunner>(),
			storeId = EVALUATIONS_MUTATION_STORE_ID,
			commandSerializer = EvaluationMutation.serializer()
		)
	}
	single<StoreBackedMutationEngine<String, EvaluationMutation, EvaluationMutationAck>>(named(EVALUATIONS_MUTATION_ENGINE_QUALIFIER)) {
		StoreBackedMutationEngine(
			storeId = EVALUATIONS_MUTATION_STORE_ID,
			outboxStore = get(named(EVALUATIONS_MUTATION_STORE_QUALIFIER)),
			coroutineScope = get<SessionCoroutineScope>()
		)
	}
	single<EvaluationRepository> {
		EvaluationDataSource(
			databaseDataSource = get(),
			evaluationsApiDataSource = get(),
			settingsDataSource = get(),
			mutationEngine = get(named(EVALUATIONS_MUTATION_ENGINE_QUALIFIER)),
			identifierRepository = get()
		)
	}

	/* Data sources */

	factoryOf(::KtorEvaluationsApiDataSource) { bind<EvaluationsApiDataRepository>() }
	single<DatabaseDataRepository> {
		RoomDatabaseDataSource(
			evaluationDao = get(),
			evaluationSyncStateDao = get(),
			academicTermDao = get(),
			academicAttemptDao = get(),
			transactionRunner = get(),
			mutationEngine = get(named(EVALUATIONS_MUTATION_ENGINE_QUALIFIER)),
			visibleEvaluationsStateResolver = get()
		)
	}
	singleOf(::LocalSettingsDataSource) {
		bind<SettingsDataRepository>()
		bind<EvaluationsSelectionRepository>()
	}

	/* Exception handlers */

	factoryOf(::AddEvaluationExceptionHandler)
	factoryOf(::UpdateEvaluationExceptionHandler)
	factoryOf(::RemoveEvaluationExceptionHandler)
}
