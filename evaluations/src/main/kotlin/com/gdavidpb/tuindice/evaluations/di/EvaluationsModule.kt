package com.gdavidpb.tuindice.evaluations.di

import com.gdavidpb.tuindice.base.BuildConfig
import com.gdavidpb.tuindice.base.utils.extension.create
import com.gdavidpb.tuindice.evaluations.data.api.ApiDataSource
import com.gdavidpb.tuindice.evaluations.data.api.EvaluationsApi
import com.gdavidpb.tuindice.evaluations.data.api.parser.EvaluationAddParser
import com.gdavidpb.tuindice.evaluations.data.api.parser.EvaluationUpdateParser
import com.gdavidpb.tuindice.evaluations.data.evaluation.EvaluationDataRepository
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.LocalDataSource
import com.gdavidpb.tuindice.evaluations.data.evaluation.source.RemoteDataSource
import com.gdavidpb.tuindice.evaluations.data.room.RoomDataSource
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.*
import com.gdavidpb.tuindice.evaluations.domain.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.domain.validator.UpdateEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationPlanViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@KoinReflectAPI
val evaluationsModule = module {
	/* View Models */

	viewModel<EvaluationPlanViewModel>()
	viewModel<EvaluationViewModel>()

	/* Use cases */

	factoryOf(::GetEvaluationUseCase)
	factoryOf(::GetEvaluationsUseCase)
	factoryOf(::UpdateEvaluationUseCase)
	factoryOf(::RemoveEvaluationUseCase)
	factoryOf(::AddEvaluationUseCase)

	/* Validators */

	factoryOf(::AddEvaluationParamsValidator)
	factoryOf(::UpdateEvaluationParamsValidator)

	/* Repositories */

	factoryOf(::EvaluationDataRepository) { bind<EvaluationRepository>() }

	/* Data sources */

	factoryOf(::ApiDataSource) { bind<RemoteDataSource>() }
	factoryOf(::RoomDataSource) { bind<LocalDataSource>() }

	/* SignIn Api */

	single {
		Retrofit.Builder()
			.baseUrl(BuildConfig.ENDPOINT_TU_INDICE_API)
			.addConverterFactory(GsonConverterFactory.create())
			.client(get())
			.build()
			.create<EvaluationsApi>()
	}

	/* Parsers */

	factoryOf(::EvaluationAddParser)
	factoryOf(::EvaluationUpdateParser)
}