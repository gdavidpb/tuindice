package com.gdavidpb.tuindice.evaluations.di

import com.gdavidpb.tuindice.evaluations.data.source.DatabaseDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationDataRepository
import com.gdavidpb.tuindice.evaluations.data.source.EvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.source.SettingsDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.mutation.EVALUATIONS_MUTATION_STORE_ID
import com.gdavidpb.tuindice.evaluations.data.repository.mutation.EvaluationMutation
import com.gdavidpb.tuindice.evaluations.data.repository.mutation.EvaluationMutationAck
import com.gdavidpb.tuindice.evaluations.data.source.KtorEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.source.LocalSettingsDataSource
import com.gdavidpb.tuindice.evaluations.data.source.RoomDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.VisibleEvaluationsStateResolver
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.GetEvaluationsExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationsExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.AddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.EditEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadAvailableSubjectsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetDateActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetSubjectActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetTypeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.CheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.ClearEvaluationFiltersActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenAddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.PickEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RefreshEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RemoveEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.SetEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.UncheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.persistence.data.room.RoomMutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlinx.serialization.builtins.serializer

private const val EVALUATIONS_MUTATION_STORE_QUALIFIER = "evaluationsMutationStore"
private const val EVALUATIONS_MUTATION_ENGINE_QUALIFIER = "evaluationsMutationEngine"

val evaluationsModule = module {
	/* View models */

	viewModelOf(::EvaluationsViewModel)
	viewModelOf(::EvaluationViewModel)

	/* Action processor */

	factoryOf(::LoadAvailableSubjectsActionProcessor)
	factoryOf(::LoadEvaluationActionProcessor)
	factoryOf(::AddEvaluationActionProcessor)
	factoryOf(::EditEvaluationActionProcessor)
	factoryOf(::PickGradeActionProcessor)
	factoryOf(::PickMaxGradeActionProcessor)
	factoryOf(::SetSubjectActionProcessor)
	factoryOf(::SetTypeActionProcessor)
	factoryOf(::SetDateActionProcessor)
	factoryOf(::SetGradeActionProcessor)
	factoryOf(::SetMaxGradeActionProcessor)

	factoryOf(::LoadEvaluationsActionProcessor)
	factoryOf(::RefreshEvaluationsActionProcessor)
	factoryOf(::CheckEvaluationFilterActionProcessor)
	factoryOf(::UncheckEvaluationFilterActionProcessor)
	factoryOf(::ClearEvaluationFiltersActionProcessor)
	factoryOf(::OpenAddEvaluationActionProcessor)
	factoryOf(::PickEvaluationGradeActionProcessor)
	factoryOf(::SetEvaluationGradeActionProcessor)
	factoryOf(::OpenEvaluationActionProcessor)
	factoryOf(::RemoveEvaluationActionProcessor)

	/* Use cases */

	factoryOf(::GetEvaluationAndAvailableSubjectsUseCase)
	factoryOf(::GetEvaluationsUseCase)
	factoryOf(::UpdateEvaluationsUseCase)
	factoryOf(::GetEvaluationUseCase)
	factoryOf(::UpdateEvaluationUseCase)
	factoryOf(::RemoveEvaluationUseCase)
	factoryOf(::AddEvaluationUseCase)
	factoryOf(::GetAvailableSubjectsUseCase)

	/* Validators */

	factoryOf(::AddEvaluationParamsValidator)

	/* Repositories */

	singleOf(::VisibleEvaluationsStateResolver)
	single<MutationEnvelopeStore<String, EvaluationMutation>>(named(EVALUATIONS_MUTATION_STORE_QUALIFIER)) {
		RoomMutationEnvelopeStore(
			room = get(),
			storeId = EVALUATIONS_MUTATION_STORE_ID,
			scopeKeySerializer = String.serializer(),
			commandSerializer = EvaluationMutation.serializer()
		)
	}
	single<StoreBackedMutationEngine<String, EvaluationMutation, com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot, List<com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation>, EvaluationMutationAck>>(named(EVALUATIONS_MUTATION_ENGINE_QUALIFIER)) {
		StoreBackedMutationEngine(
			storeId = EVALUATIONS_MUTATION_STORE_ID,
			outboxStore = get(named(EVALUATIONS_MUTATION_STORE_QUALIFIER))
		)
	}
	single<EvaluationRepository> {
		EvaluationDataRepository(
			databaseDataSource = get(),
			evaluationsApiDataSource = get(),
			settingsDataSource = get(),
			mutationEngine = get(named(EVALUATIONS_MUTATION_ENGINE_QUALIFIER)),
			identifierRepository = get()
		)
	}

	/* Data sources */

	factoryOf(::KtorEvaluationsApiDataSource) { bind<EvaluationsApiDataSource>() }
	single<DatabaseDataSource> {
		RoomDatabaseDataSource(
			room = get(),
			mutationEngine = get(named(EVALUATIONS_MUTATION_ENGINE_QUALIFIER)),
			visibleEvaluationsStateResolver = get()
		)
	}
	single<SettingsDataSource> {
		LocalSettingsDataSource(get<Settings>())
	}

	/* Exception handlers */

	factoryOf(::GetEvaluationsExceptionHandler)
	factoryOf(::UpdateEvaluationsExceptionHandler)
	factoryOf(::AddEvaluationExceptionHandler)
	factoryOf(::UpdateEvaluationExceptionHandler)
	factoryOf(::RemoveEvaluationExceptionHandler)
}
