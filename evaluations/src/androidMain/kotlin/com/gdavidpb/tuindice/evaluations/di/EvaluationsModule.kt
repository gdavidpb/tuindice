package com.gdavidpb.tuindice.evaluations.di

import com.gdavidpb.tuindice.evaluations.presentation.mapper.AndroidEvaluationItemMappingProvider
import com.gdavidpb.tuindice.evaluations.presentation.mapper.EvaluationItemMappingProvider
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val evaluationsModule = module {
	factoryOf(::AndroidEvaluationItemMappingProvider) {
		bind<EvaluationItemMappingProvider>()
	}
}
