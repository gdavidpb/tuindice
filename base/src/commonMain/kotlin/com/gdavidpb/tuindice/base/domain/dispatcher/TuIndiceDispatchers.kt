package com.gdavidpb.tuindice.base.domain.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

interface TuIndiceDispatchers {
	val main: CoroutineDispatcher
	val default: CoroutineDispatcher
	val io: CoroutineDispatcher
	val unconfined: CoroutineDispatcher
}
