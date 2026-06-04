package com.gdavidpb.tuindice.evaluations.di

import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataRepository
import com.gdavidpb.tuindice.evaluations.data.source.EvaluationDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataRepository
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_STORE_ID
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.data.source.KtorEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.source.LocalSettingsDataSource
import com.gdavidpb.tuindice.evaluations.data.source.RoomDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.data.resolver.VisibleEvaluationsStateResolver
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationsExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.AddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.EditEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadAvailableAttemptsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetDateActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetAttemptActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetTypeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.CheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.ClearEvaluationFiltersActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenAddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.PickEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RefreshEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RemoveEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.SelectEvaluationsTabActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.SelectEvaluationsWeekActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.SetEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.UncheckEvaluationFilterActionProcessor
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

	/* Action processor */

	factoryOf(::LoadAvailableAttemptsActionProcessor)
	factoryOf(::LoadEvaluationActionProcessor)
	factoryOf(::AddEvaluationActionProcessor)
	factoryOf(::EditEvaluationActionProcessor)
	factoryOf(::PickGradeActionProcessor)
	factoryOf(::PickMaxGradeActionProcessor)
	factoryOf(::SetAttemptActionProcessor)
	factoryOf(::SetTypeActionProcessor)
	factoryOf(::SetDateActionProcessor)
	factoryOf(::SetGradeActionProcessor)
	factoryOf(::SetMaxGradeActionProcessor)

	factoryOf(::LoadEvaluationsActionProcessor)
	factoryOf(::RefreshEvaluationsActionProcessor)
	factoryOf(::CheckEvaluationFilterActionProcessor)
	factoryOf(::UncheckEvaluationFilterActionProcessor)
	factoryOf(::ClearEvaluationFiltersActionProcessor)
	factoryOf(::SelectEvaluationsTabActionProcessor)
	factoryOf(::SelectEvaluationsWeekActionProcessor)
	factoryOf(::OpenAddEvaluationActionProcessor)
	factoryOf(::PickEvaluationGradeActionProcessor)
	factoryOf(::SetEvaluationGradeActionProcessor)
	factoryOf(::OpenEvaluationActionProcessor)
	factoryOf(::RemoveEvaluationActionProcessor)

	/* Use cases */

	factoryOf(::GetEvaluationAndAvailableAttemptsUseCase)
	factoryOf(::GetEvaluationsUseCase)
	factoryOf(::UpdateEvaluationsUseCase)
	factoryOf(::GetEvaluationUseCase)
	factoryOf(::UpdateEvaluationUseCase)
	factoryOf(::RemoveEvaluationUseCase)
	factoryOf(::AddEvaluationUseCase)
	factoryOf(::GetAvailableAttemptsUseCase)

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
	single<StoreBackedMutationEngine<String, EvaluationMutation, LocalEvaluationsSnapshot, List<LocalEvaluation>, EvaluationMutationAck>>(named(EVALUATIONS_MUTATION_ENGINE_QUALIFIER)) {
		StoreBackedMutationEngine(
			storeId = EVALUATIONS_MUTATION_STORE_ID,
			outboxStore = get(named(EVALUATIONS_MUTATION_STORE_QUALIFIER))
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
	singleOf(::LocalSettingsDataSource) { bind<SettingsDataRepository>() }

	/* Exception handlers */

	factoryOf(::UpdateEvaluationsExceptionHandler)
	factoryOf(::AddEvaluationExceptionHandler)
	factoryOf(::UpdateEvaluationExceptionHandler)
	factoryOf(::RemoveEvaluationExceptionHandler)
}
