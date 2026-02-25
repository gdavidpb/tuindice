package com.gdavidpb.tuindice.evaluations.di

import com.gdavidpb.tuindice.evaluations.presentation.mapper.EvaluationItemMappingProvider
import com.gdavidpb.tuindice.evaluations.presentation.mapper.IosEvaluationItemMappingProvider
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val evaluationsIosModule = module {
	factoryOf(::IosEvaluationItemMappingProvider) {
		bind<EvaluationItemMappingProvider>()
	}
}
