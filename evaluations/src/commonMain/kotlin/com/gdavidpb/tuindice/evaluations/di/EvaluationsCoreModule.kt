package com.gdavidpb.tuindice.evaluations.di

import com.gdavidpb.tuindice.evaluations.data.repository.DatabaseDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataSource
import com.gdavidpb.tuindice.evaluations.data.source.DefaultEvaluationFilterLabelsDataSource
import com.gdavidpb.tuindice.evaluations.data.source.KtorEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.source.PreferencesDataSource
import com.gdavidpb.tuindice.evaluations.data.source.RoomDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationFilterLabelsRepository
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.GetEvaluationsExceptionHandler
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
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RemoveEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.SetEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.UncheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.mapper.DefaultEvaluationItemMappingProvider
import com.gdavidpb.tuindice.evaluations.presentation.mapper.EvaluationItemMappingProvider
import com.gdavidpb.tuindice.evaluations.presentation.resource.DefaultEvaluationTextProvider
import com.gdavidpb.tuindice.evaluations.presentation.resource.EvaluationTextProvider
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val evaluationsCoreModule = module {
	/* View models */

	factoryOf(::EvaluationsViewModel)
	factoryOf(::EvaluationViewModel)

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
	factoryOf(::GetEvaluationUseCase)
	factoryOf(::UpdateEvaluationUseCase)
	factoryOf(::RemoveEvaluationUseCase)
	factoryOf(::AddEvaluationUseCase)
	factoryOf(::GetAvailableSubjectsUseCase)

	/* Validators */

	factoryOf(::AddEvaluationParamsValidator)

	/* Repositories */

	factoryOf(::EvaluationDataRepository) { bind<EvaluationRepository>() }

	/* Data sources */

	factoryOf(::KtorEvaluationsApiDataSource) { bind<EvaluationsApiDataSource>() }
	factoryOf(::RoomDatabaseDataSource) { bind<DatabaseDataSource>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsDataSource>() }

	/* Exception handlers */

	factoryOf(::GetEvaluationsExceptionHandler)
	factoryOf(::AddEvaluationExceptionHandler)

	/* Shared text resources */

	factoryOf(::DefaultEvaluationItemMappingProvider) { bind<EvaluationItemMappingProvider>() }
	factoryOf(::DefaultEvaluationTextProvider) { bind<EvaluationTextProvider>() }
	factoryOf(::DefaultEvaluationFilterLabelsDataSource) { bind<EvaluationFilterLabelsRepository>() }
}
