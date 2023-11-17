package com.gdavidpb.tuindice.evaluations.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.evaluations.data.api.ApiDataSource
import com.gdavidpb.tuindice.evaluations.data.api.EvaluationsApi
import com.gdavidpb.tuindice.evaluations.data.api.parser.EvaluationAddParser
import com.gdavidpb.tuindice.evaluations.data.api.parser.EvaluationRemoveParser
import com.gdavidpb.tuindice.evaluations.data.api.parser.EvaluationUpdateParser
import com.gdavidpb.tuindice.evaluations.data.evaluation.EvaluationDataRepository
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.SettingsDataSource
import com.gdavidpb.tuindice.evaluations.data.preferences.PreferencesDataSource
import com.gdavidpb.tuindice.evaluations.data.resolution.EvaluationResolutionHandler
import com.gdavidpb.tuindice.evaluations.data.room.RoomDataSource
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.UpdateEvaluationParamsValidator
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
import retrofit2.converter.gson.GsonConverterFactory

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
	factoryOf(::UpdateEvaluationParamsValidator)

	/* Repositories */

	factoryOf(::EvaluationDataRepository) { bind<EvaluationRepository>() }

	/* Data sources */

	factoryOf(::ApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }
	factoryOf(::PreferencesDataSource) { bind<SettingsDataSource>() }

	/* SignIn Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(GsonConverterFactory.create())
			.client(get())
			.build()
			.create<EvaluationsApi>()
	}

	/* Request parsers */

	factoryOf(::EvaluationAddParser)
	factoryOf(::EvaluationUpdateParser)
	factoryOf(::EvaluationRemoveParser)

	/* Resolution handlers */

	factoryOf(::EvaluationResolutionHandler)

	/* Exception handlers */

	factoryOf(::AddEvaluationExceptionHandler)
	factoryOf(::UpdateEvaluationExceptionHandler)
}