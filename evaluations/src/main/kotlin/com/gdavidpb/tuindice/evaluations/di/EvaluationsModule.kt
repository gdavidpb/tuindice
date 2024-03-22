package com.gdavidpb.tuindice.evaluations.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.EvaluationDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.EvaluationsApi
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.SettingsDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.EvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.PreferencesDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.RoomDataSource
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store.EvaluationConverter
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store.EvaluationFetcher
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store.EvaluationSourceOfTruth
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store.EvaluationStore
import com.gdavidpb.tuindice.evaluations.data.repository.evaluation.source.store.EvaluationUpdater
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.AddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.CloseAddDialogActionProcessor
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
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.CloseListDialogActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenAddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.PickEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RemoveEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.SetEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.UncheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit

val evaluationsModule = module {
	/* View Models */

	viewModelOf(::EvaluationsViewModel)
	viewModelOf(::EvaluationViewModel)

	/* Action processor */

	factoryOf(::LoadAvailableSubjectsActionProcessor)
	factoryOf(::LoadEvaluationActionProcessor)
	factoryOf(::AddEvaluationActionProcessor)
	factoryOf(::EditEvaluationActionProcessor)
	factoryOf(::PickGradeActionProcessor)
	factoryOf(::PickMaxGradeActionProcessor)
	factoryOf(::CloseAddDialogActionProcessor)
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
	factoryOf(::CloseListDialogActionProcessor)

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

	/* Store */

	factoryOf(::EvaluationStore)
	factoryOf(::EvaluationFetcher)
	factoryOf(::EvaluationSourceOfTruth)
	factoryOf(::EvaluationConverter)
	factoryOf(::EvaluationUpdater)

	/* Data sources */

	factoryOf(::EvaluationsApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsDataSource>() }

	/* SignIn Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(get())
			.client(get())
			.build()
			.create<EvaluationsApi>()
	}

	/* Exception handlers */

	factoryOf(::AddEvaluationExceptionHandler)
}