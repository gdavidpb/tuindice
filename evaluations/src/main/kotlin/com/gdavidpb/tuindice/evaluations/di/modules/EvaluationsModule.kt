package com.gdavidpb.tuindice.evaluations.di.modules

import org.koin.core.annotation.KoinReflectAPI
import org.koin.dsl.module
import com.gdavidpb.tuindice.evaluations.domain.usecase.*
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationPlanViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.factoryOf

@KoinReflectAPI
val evaluationsModule = module {
	/* View Models */

	viewModel<EvaluationPlanViewModel>()
	viewModel<EvaluationViewModel>()

	/* Use cases */

	factoryOf(::GetSubjectUseCase)
	factoryOf(::GetEvaluationUseCase)
	factoryOf(::GetSubjectEvaluationsUseCase)
	factoryOf(::UpdateEvaluationUseCase)
	factoryOf(::RemoveEvaluationUseCase)
	factoryOf(::AddEvaluationUseCase)
}