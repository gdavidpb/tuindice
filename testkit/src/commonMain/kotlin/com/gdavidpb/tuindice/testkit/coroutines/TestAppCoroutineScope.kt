package com.gdavidpb.tuindice.testkit.coroutines

import com.gdavidpb.tuindice.base.domain.coroutine.AppCoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

fun testAppCoroutineScope(
	dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
): AppCoroutineScope = AppCoroutineScope(TestTuIndiceDispatchers(dispatcher))
