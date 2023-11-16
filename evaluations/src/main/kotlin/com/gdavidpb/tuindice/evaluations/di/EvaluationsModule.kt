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
import com.gdavidpb.tuindice.evaluations.presentation.action.add.AddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.CloseAddDialogActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.LoadAvailableSubjectsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.PickGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.PickMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.SetDateActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.SetGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.SetMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.SetSubjectActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.add.SetTypeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.CheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.CloseListDialogActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.OpenAddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.OpenEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.PickEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.RemoveEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.SetEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.list.UncheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.AddEvaluationViewModel
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
	viewModelOf(::AddEvaluationViewModel)

	/* Action processor */

	factoryOf(::LoadAvailableSubjectsActionProcessor)
	factoryOf(::AddEvaluationActionProcessor)
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