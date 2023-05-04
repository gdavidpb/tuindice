package com.gdavidpb.tuindice.base.di

import com.gdavidpb.tuindice.base.domain.usecase.SignOutUseCase
import org.koin.core.annotation.KoinReflectAPI
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

@KoinReflectAPI
val baseModule = module {

	/* Use cases */

	factoryOf(::SignOutUseCase)
}