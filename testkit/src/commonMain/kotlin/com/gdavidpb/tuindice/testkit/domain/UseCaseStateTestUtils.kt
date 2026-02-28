package com.gdavidpb.tuindice.testkit.domain

import app.cash.turbine.ReceiveTurbine
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseError
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import kotlin.test.assertIs

suspend fun <T, E : UseCaseError> awaitLoadingThenData(
	turbine: ReceiveTurbine<UseCaseState<T, E>>
): T {
	assertIs<UseCaseState.Loading<*, *>>(turbine.awaitItem())

	val data = assertIs<UseCaseState.Data<T, E>>(turbine.awaitItem())
	return data.value
}

suspend fun <T, E : UseCaseError> awaitLoadingThenError(
	turbine: ReceiveTurbine<UseCaseState<T, E>>
): UseCaseState.Error<T, E> {
	assertIs<UseCaseState.Loading<*, *>>(turbine.awaitItem())
	return assertIs<UseCaseState.Error<T, E>>(turbine.awaitItem())
}
